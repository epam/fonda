/*
 * Copyright 2017-2020 Sanofi and EPAM Systems, Inc. (https://www.epam.com/)
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
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static com.epam.fonda.utils.PipelineUtils.getExecutionPath;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SCRnaExpressionCellRangerFastqIntegrationTest extends AbstractIntegrationTest {

    private static final String OUTPUT_DIR = "output";
    private static final String OUTPUT_DIR_ROOT = "build/resources/integrationTest/";
    private static final String SCRNA_ANALYSIS_COUNT_QC_DOUBLET_DETECTION_TEST_TEMPLATE_PATH =
            "scRnaExpressionCellRangerFastq/scRnaExpressionCellRangerFastq_CountDoubletDetectionQC_template.txt";
    private static final String SCRNA_ANALYSIS_COUNT_QC_TEST_TEMPLATE_PATH =
            "scRnaExpressionCellRangerFastq/scRnaExpressionCellRangerFast_CountQC_template.txt";
    private static final String SCRNA_ANALYSIS_COUNT_QC_SCRUBLET_TEST_TEMPLATE_PATH =
            "scRnaExpressionCellRangerFastq/scRnaExpressionCellRangerFast_CountScrubletQC_template.txt";
    private static final String SCRNA_EXPRESSION_FASTQ_STUDY_CONFIG =
            "scRnaExpressionCellRangerFastq/sscRnaExpressionCellRangerFastq.txt";
    private static final String TEST_SHELL_SCRIPT_TEMPLATE_PATH =
            "output/sh_files/scRnaExpression_CellRanger_Fastq_alignment_for_smv1_analysis.sh";
    private static final String SCRNA_EXPRESSION_FASTQ_COUNT_QC_GLOBAL_CONFIG =
            "scRnaExpressionCellRangerFastq/gCount.txt";
    private static final String SCRNA_EXPRESSION_FASTQ_COUNT_QC_DOUBLET_DETECTION_GLOBAL_CONFIG =
            "scRnaExpressionCellRangerFastq/gDoubletDetection.txt";
    private static final String SCRNA_EXPRESSION_FASTQ_COUNT_QC_SCRUBLET_GLOBAL_CONFIG =
            "scRnaExpressionCellRangerFastq/gScrublet.txt";
    private static final String FASTQ_DIR = "/ngs/data/demo/test/fastq/smv1_GTGTTCTA_L004_R1_001.fastq.gz";

    private static final String FASTQ_1 = "fastq1";
    private static final String FASTQ_2 = "fastq2";
    private static final String SAMPLE_1 = "sampleName";

    private FastqFileSample expectedSample;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private Context context;
    private String appName;

    @BeforeEach
    void setup() {
        expectedSample = new FastqFileSample();
        expectedSample.setFastq1(Arrays.asList(FASTQ_1, FASTQ_2));
        expectedSample.setName(SAMPLE_1);
        context = new Context();
        context.setVariable("jarPath", getExecutionPath());
        FastqFileSample sampleForTestingWorkflow = new FastqFileSample();
        sampleForTestingWorkflow.setFastq1(Collections.singletonList(FASTQ_DIR));
        context.setVariable("sampleList", String.join(",",
                CellRangerUtils.extractFastqDir(sampleForTestingWorkflow).getFastqDirs()));
        appName = Paths.get(System.getProperty("user.dir")).getFileName().toString();
    }

    @Test
    void testCreateRnaCellRangerFastqSpecificDir() throws IOException {
        startAppWithConfigs("scRnaExpressionCellRangerFastq/gCount.txt",
                SCRNA_EXPRESSION_FASTQ_STUDY_CONFIG);

        assertAll(() -> assertTrue(new File(format("%s%s/sh_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/log_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/err_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/count", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()));

        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @ParameterizedTest
    @MethodSource("initParameters")
    void testCount(String globalConfigPath, String templatePath) throws IOException, URISyntaxException {
        startAppWithConfigs(globalConfigPath, SCRNA_EXPRESSION_FASTQ_STUDY_CONFIG);
        final String expectedCmd = expectedTemplateEngine.process(templatePath, context);
        assertEquals(expectedCmd.trim(), getCmd(TEST_SHELL_SCRIPT_TEMPLATE_PATH).trim());
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initParameters() {
        return Stream.of(
                Arguments.of(SCRNA_EXPRESSION_FASTQ_COUNT_QC_GLOBAL_CONFIG,
                        SCRNA_ANALYSIS_COUNT_QC_TEST_TEMPLATE_PATH),
                Arguments.of(SCRNA_EXPRESSION_FASTQ_COUNT_QC_DOUBLET_DETECTION_GLOBAL_CONFIG,
                        SCRNA_ANALYSIS_COUNT_QC_DOUBLET_DETECTION_TEST_TEMPLATE_PATH),
                Arguments.of(SCRNA_EXPRESSION_FASTQ_COUNT_QC_SCRUBLET_GLOBAL_CONFIG,
                        SCRNA_ANALYSIS_COUNT_QC_SCRUBLET_TEST_TEMPLATE_PATH)
        );
    }

    @Test
    void testExtractFastqDirFastq1() {
        expectedSample.setFastq1(Arrays.asList(FASTQ_1, FASTQ_2));
        FastqFileSample actualSample = CellRangerUtils.extractFastqDir(expectedSample);

        assertTrue(actualSample.getFastqDirs().get(0).endsWith(appName));
    }

    @Test
    void testExtractFastqDirFastq1Length1() {
        expectedSample.setFastq1(Collections.singletonList(FASTQ_1));
        FastqFileSample actualSample = CellRangerUtils.extractFastqDir(expectedSample);

        assertTrue(actualSample.getFastqDirs().get(0).endsWith(appName));
    }

    @Test
    void testExtractFastqDirFastq1Fastq2() {
        expectedSample.setFastq1(Collections.singletonList(FASTQ_1));
        expectedSample.setFastq2(Collections.singletonList(FASTQ_2));
        FastqFileSample actualSample = CellRangerUtils.extractFastqDir(expectedSample);

        assertTrue(actualSample.getFastqDirs().get(0).endsWith(appName));
    }
}
