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
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;

import static junit.framework.TestCase.assertEquals;

@RunWith(DataProviderRunner.class)
public class SCImmuneProfileCellRangerFastqTest extends AbstractIntegrationTest {

    private static final String OUTPUT_DIR = "output";
    private static final String SCIMMUNE_PROFILE_CELLRANGER_FASTQ_PBMC4K_TEST_TEMPLATE_PATH =
            "scImmuneProfileCellRangerFastq_VdjQc_template";
    public static final String SCIMMUNE_PROFILE_CELL_RANGER_FASTQ_COHORT_TEST_TEMPLATE_PATH =
            "SCImmuneProfileCellRangerFastq_Cohort_template";
    private static final String GLOBAL_CONFIG_NAME = "SCImmuneProfileCellRangerFastq/vdj.txt";
    private static final String STUDY_CONFIG_NAME =
            "SCImmuneProfileCellRangerFastq/SCImmuneProfileCellRangerFastq.txt";
    private static final String OUTPUT_FILE_FOR_COHORT_ANALYSIS_SH =
            "output/sh_files/scImmuneProfile_CellRanger_Fastq_qcsummary_for_cohort_analysis.sh";
    public static final String OUTPUT_FILE_FOR_PBMC_4_K_ANALYSIS_SH =
            "output/sh_files/scImmuneProfile_CellRanger_Fastq_alignment_for_pbmc4k_analysis.sh";
    private static String fastqDirs;
    private Context context = new Context();
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();

    @BeforeEach
    public void setup() {
        FastqFileSample expectedSample = FastqFileSample.builder()
                .name("sampleName")
                .fastq1(Collections.singletonList("/ngs/data/demo/test/fastq_data/pbmc4k_S1_L001_R1_001.fastq.gz"))
                .fastq2(Collections.singletonList("/ngs/data/demo/test/fastq_data/pbmc4k_S1_L002_R1_001.fastq.gz"))
                .sampleOutputDir("build/resources/integrationTest/output/pbmc4k")
                .build();
        fastqDirs = String.join(",", CellRangerUtils.extractFastqDir(expectedSample).getFastqDirs());
        context.setVariable("fastqDir", fastqDirs);
        startAppWithConfigs(GLOBAL_CONFIG_NAME, STUDY_CONFIG_NAME);
    }

//    @DataProvider
//    public static Object[] getSCImmuneProfileCellRangerFastqExpectedStrings() {
//        return new Object[][]{
//                {"output/sh_files/scImmuneProfile_CellRanger_Fastq_qcsummary_for_cohort_analysis.sh", new String[]{
//                    "echo $(date) Error QC results from pbmc4k:",
//                    "echo $(date) Confirm QC results from pbmc4k",
//                    "build/resources/integrationTest/output/log_files/"
//                            + "scImmuneProfile_CellRanger_Fastq_alignment_for_pbmc4k_analysis.log",
//                    "/ngs/data/app/R/v3.5.0/bin/Rscript",
//                    "scImmuneProfile_CellRanger_Fastq"}
//                }
//        };
//    }

    @Test
    public void testCohortAnalyses() throws IOException, URISyntaxException {
        startAppWithConfigs(GLOBAL_CONFIG_NAME, STUDY_CONFIG_NAME);

        String expectedCmd = expectedTemplateEngine.process(SCIMMUNE_PROFILE_CELL_RANGER_FASTQ_COHORT_TEST_TEMPLATE_PATH, context);
        assertEquals(expectedCmd, getCmd(OUTPUT_FILE_FOR_COHORT_ANALYSIS_SH));
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @Test
    public void testVdjForPbmc4kAnalysis() throws IOException, URISyntaxException {
        startAppWithConfigs(GLOBAL_CONFIG_NAME, STUDY_CONFIG_NAME);

        final String expectedCmd = expectedTemplateEngine
                .process(SCIMMUNE_PROFILE_CELLRANGER_FASTQ_PBMC4K_TEST_TEMPLATE_PATH, context);
        final String actualCmd = getCmd(OUTPUT_FILE_FOR_PBMC_4_K_ANALYSIS_SH).trim();

        assertEquals(expectedCmd, actualCmd);
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }
}
