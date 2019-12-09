/*
 * Copyright 2017-2019 Sanofi and EPAM Systems, Inc. (https://www.epam.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.fonda;

import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.utils.CellRangerUtils;
import com.epam.fonda.utils.TemplateEngineUtils;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;


@RunWith(DataProviderRunner.class)
public class SCImmuneProfileCellRangerFastqTest extends AbstractIntegrationTest {

    private static final String OUTPUT_DIR = "output";
    private static final String SCIMMUNE_PROFILE_CELLRANGER_FASTQ_TEST_TEMPLATE_PATH =
            "scImmuneProfileCellRangerFastq_VdjQc_template";
    private static final String GLOBAL_CONFIG_NAME = "SCImmuneProfileCellRangerFastq/vdj.txt";
    private static final String STUDY_CONFIG_NAME =
            "SCImmuneProfileCellRangerFastq/SCImmuneProfileCellRangerFastq.txt";
    private static String fastqDirs;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();

    @Before
    public void setup() {
        FastqFileSample expectedSample = FastqFileSample.builder()
                .name("sampleName")
                .fastq1(Collections.singletonList("/ngs/data/demo/test/fastq_data/pbmc4k_S1_L001_R1_001.fastq.gz"))
                .fastq2(Collections.singletonList("/ngs/data/demo/test/fastq_data/pbmc4k_S1_L002_R1_001.fastq.gz"))
                .sampleOutputDir("build/resources/integrationTest/output/pbmc4k")
                .build();
        fastqDirs = String.join(",", CellRangerUtils.extractFastqDir(expectedSample).getFastqDirs());
    }

    @DataProvider
    public static Object[] getSCImmuneProfileCellRangerFastqExpectedStrings() {
        return new Object[][]{
                {"output/sh_files/scImmuneProfile_CellRanger_Fastq_qcsummary_for_cohort_analysis.sh", new String[]{
                    "echo $(date) Error QC results from pbmc4k:",
                    "echo $(date) Confirm QC results from pbmc4k",
                    "build/resources/integrationTest/output/log_files/"
                            + "scImmuneProfile_CellRanger_Fastq_alignment_for_pbmc4k_analysis.log",
                    "/ngs/data/app/R/v3.5.0/bin/Rscript",
                    "scImmuneProfile_CellRanger_Fastq"}
                }
        };
    }

    @Test
    @UseDataProvider("getSCImmuneProfileCellRangerFastqExpectedStrings")
    public void testVdj(String outputFile, String[] expectedStrings) throws IOException, URISyntaxException {
        startAppWithConfigs(GLOBAL_CONFIG_NAME, STUDY_CONFIG_NAME);

        Path path = Paths.get(getClass().getClassLoader().getResource(outputFile).toURI());
        Stream<String> lines = Files.lines(path);
        List<String> linesList = lines.collect(Collectors.toList());
        for (String expectedString : expectedStrings) {
            assertTrue(linesList.stream().anyMatch(line -> line.contains(expectedString)));
        }
        assertFalse(linesList.stream().anyMatch(line -> line.contains("null")));
        lines.close();
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @Test
    public void testVdjForPbmc4kAnalysis() throws IOException, URISyntaxException {
        startAppWithConfigs(GLOBAL_CONFIG_NAME, STUDY_CONFIG_NAME);

        Context context = new Context();
        context.setVariable("fastqDir", fastqDirs);
        final String expectedCmd = expectedTemplateEngine
                .process(SCIMMUNE_PROFILE_CELLRANGER_FASTQ_TEST_TEMPLATE_PATH, context);

        Path path = Paths.get(this.getClass().getClassLoader()
                .getResource("output/sh_files/scImmuneProfile_CellRanger_Fastq_alignment_for_pbmc4k_analysis.sh")
                .toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        final String actualCmd = new String(fileBytes);

        assertEquals(expectedCmd, actualCmd);
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }
}
