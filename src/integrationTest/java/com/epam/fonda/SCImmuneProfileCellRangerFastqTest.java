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
import com.epam.fonda.utils.PipelineUtils;
import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.stream.Stream;

import static java.lang.String.format;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;

public class SCImmuneProfileCellRangerFastqTest extends AbstractIntegrationTest {

    private static final String OUTPUT_DIR_ROOT = "build/resources/integrationTest/";
    private static final String OUTPUT_DIR = "output";
    private static final String SCIMMUNE_PROFILE_CELLRANGER_FASTQ_PBMC4K_TEST_TEMPLATE =
            "scImmuneProfileCellRangerFastq_VdjQc_template";
    public static final String SCIMMUNE_PROFILE_CELL_RANGER_FASTQ_COHORT_TEST_TEMPLATE =
            "SCImmuneProfileCellRangerFastq_Cohort_template";
    private static final String GLOBAL_CONFIG_NAME = "SCImmuneProfileCellRangerFastq/vdj.txt";
    private static final String STUDY_CONFIG_NAME =
            "SCImmuneProfileCellRangerFastq/SCImmuneProfileCellRangerFastq.txt";
    private static final String OUTPUT_FILE_COHORT_ANALYSIS_SH =
            "output/sh_files/scImmuneProfile_CellRanger_Fastq_qcsummary_for_cohort_analysis.sh";
    private static final String OUTPUT_FILE_PBMC_4_K_ANALYSIS_SH =
            "output/sh_files/scImmuneProfile_CellRanger_Fastq_alignment_for_pbmc4k_analysis.sh";
    private static final String FASTQ_DATA_FOLDER = "fastq_data";
    private Context context = new Context();
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private FastqFileSample expectedSample;

    @BeforeEach
    void setup() {
        expectedSample = FastqFileSample.builder()
                .name("sampleName")
                .fastq1(Collections.singletonList("/ngs/data/demo/test/fastq_data/pbmc4k_S1_L001_R1_001.fastq.gz"))
                .fastq2(Collections.singletonList("/ngs/data/demo/test/fastq_data/pbmc4k_S1_L002_R1_001.fastq.gz"))
                .sampleOutputDir("build/resources/integrationTest/output/pbmc4k")
                .build();
        String fastqDirs = String.join(",", CellRangerUtils.extractFastqDir(expectedSample).getFastqDirs());
        String jarPath = PipelineUtils.getExecutionPath();
        context = new Context();
        context.setVariable("fastqDir", fastqDirs);
        context.setVariable("jarPath", jarPath);
        startAppWithConfigs(GLOBAL_CONFIG_NAME, STUDY_CONFIG_NAME);
    }

    @AfterEach
    void cleanUp() throws IOException {
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @Test
    void testCreateSpecificDir() {
        assertAll(
                () -> assertTrue(new File(format("%s%s/sh_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
                () -> assertTrue(new File(format("%s%s/log_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
                () -> assertTrue(new File(format("%s%s/err_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
                () -> assertTrue(new File(format("%s%s/vdj", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists())
        );
    }

    @Test
    void testExtractFastqDirFastq1Fastq2() {
        FastqFileSample actualSample = CellRangerUtils.extractFastqDir(expectedSample);
        String fastqDirs = actualSample.getFastqDirs().get(0);

        assertTrue(fastqDirs.endsWith(FASTQ_DATA_FOLDER));
    }

    @ParameterizedTest
    @MethodSource("initCmdAndOutput")
    void testVdj(String testTemplate, String outputFilePath) throws IOException, URISyntaxException {
        final String expectedCmd = expectedTemplateEngine.process(testTemplate, context).trim();
        final String actualCmd = getCmd(outputFilePath).trim();

        assertEquals(expectedCmd, actualCmd);
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initCmdAndOutput() {
        return Stream.of(
                Arguments.of(SCIMMUNE_PROFILE_CELL_RANGER_FASTQ_COHORT_TEST_TEMPLATE, OUTPUT_FILE_COHORT_ANALYSIS_SH),
                Arguments.of(SCIMMUNE_PROFILE_CELLRANGER_FASTQ_PBMC4K_TEST_TEMPLATE, OUTPUT_FILE_PBMC_4_K_ANALYSIS_SH)
        );
    }
}

