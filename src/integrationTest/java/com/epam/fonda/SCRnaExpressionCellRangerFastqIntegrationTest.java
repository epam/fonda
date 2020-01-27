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
import org.junit.Before;
import org.junit.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import static com.epam.fonda.utils.PipelineUtils.getExecutionPath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SCRnaExpressionCellRangerFastqIntegrationTest extends AbstractIntegrationTest {

    private static final String OUTPUT_DIR = "output";
    private static final String OUTPUT_DIR_ROOT = "build/resources/integrationTest/";
    private static final String SCRNA_ANALYSIS_COUNT_QC_DOUBLET_DETECTION_TEST_TEMPLATE_PATH =
            "scRnaExpressionCellRangerFastq_CountDoubletDetectionQC_template";
    private static final String SCRNA_ANALYSIS_COUNT_QC_TEST_TEMPLATE_PATH =
            "scRnaExpressionCellRangerFast_CountQC_template";
    private static final String SCRNA_ANALYSIS_COUNT_QC_SCRUBLET_TEST_TEMPLATE_PATH =
            "scRnaExpressionCellRangerFast_CountScrubletQC_template";
    private static final String SCRNA_TEST_SAMPLE_LIST = "/ngs/data/demo/test/fastq";
    private static final String SCRNA_EXPRESSION_FASTQ_STUDY_CONFIG =
            "scRnaExpressionCellRangerFastq/sscRnaExpressionCellRangerFastq.txt";
    private static final String TEST_SHELL_SCRIPT_TEMPLATE_PATH =
            "output/sh_files/scRnaExpression_CellRanger_Fastq_alignment_for_smv1_analysis.sh";
    private static final String APP_NAME = "fonda";

    private static final String FASTQ_1 = "fastq1";
    private static final String FASTQ_2 = "fastq2";

    private FastqFileSample expectedSample;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private String jarPath;
    private Context context;

    @Before
    public void setup() {
        expectedSample = new FastqFileSample();
        expectedSample.setFastq1(Arrays.asList("fastq1", "fastq2"));
        expectedSample.setName("sampleName");
        jarPath = getExecutionPath();
        context = new Context();
        context.setVariable("jarPath", jarPath);
        context.setVariable("sampleList", SCRNA_TEST_SAMPLE_LIST);
    }

    @Test
    public void testCreateRnaCellRangerFastqSpecificDir() throws IOException {
        startAppWithConfigs(
                "scRnaExpressionCellRangerFastq/gCount.txt",
                SCRNA_EXPRESSION_FASTQ_STUDY_CONFIG);
        assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + "/sh_files").exists());
        assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + "/log_files").exists());
        assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + "/err_files").exists());
        assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + "/count").exists());

        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @Test
    public void testCount() throws IOException, URISyntaxException {
        startAppWithConfigs(
                "scRnaExpressionCellRangerFastq/gCount.txt",
                SCRNA_EXPRESSION_FASTQ_STUDY_CONFIG);
        final String expectedCmd = expectedTemplateEngine
                .process(SCRNA_ANALYSIS_COUNT_QC_TEST_TEMPLATE_PATH, context);

        assertEquals(expectedCmd, getActualCmd());
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @Test
    public void testDoubletDetection() throws IOException, URISyntaxException {
        startAppWithConfigs("scRnaExpressionCellRangerFastq/gDoubletDetection.txt",
                SCRNA_EXPRESSION_FASTQ_STUDY_CONFIG);

        final String expectedCmd = expectedTemplateEngine
                .process(SCRNA_ANALYSIS_COUNT_QC_DOUBLET_DETECTION_TEST_TEMPLATE_PATH, context);

        assertEquals(expectedCmd, getActualCmd());
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @Test
    public void testScrublet() throws IOException, URISyntaxException {
        startAppWithConfigs("scRnaExpressionCellRangerFastq/gScrublet.txt",
                SCRNA_EXPRESSION_FASTQ_STUDY_CONFIG);

        final String expectedCmd = expectedTemplateEngine
                .process(SCRNA_ANALYSIS_COUNT_QC_SCRUBLET_TEST_TEMPLATE_PATH, context);

        assertEquals(expectedCmd, getActualCmd());
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @Test
    public void testExtractFastqDirFastq1() {
        expectedSample.setFastq1(Arrays.asList(FASTQ_1, FASTQ_2));
        FastqFileSample actualSample = CellRangerUtils.extractFastqDir(expectedSample);

        assertTrue(actualSample.getFastqDirs().get(0).endsWith(APP_NAME));
    }

    @Test
    public void testExtractFastqDirFastq1Length1() {
        expectedSample.setFastq1(Collections.singletonList(FASTQ_1));
        FastqFileSample actualSample = CellRangerUtils.extractFastqDir(expectedSample);

        assertTrue(actualSample.getFastqDirs().get(0).endsWith(APP_NAME));
    }

    @Test
    public void testExtractFastqDirFastq1Fastq2() {
        expectedSample.setFastq1(Collections.singletonList(FASTQ_1));
        expectedSample.setFastq2(Collections.singletonList(FASTQ_2));
        FastqFileSample actualSample = CellRangerUtils.extractFastqDir(expectedSample);

        assertTrue(actualSample.getFastqDirs().get(0).endsWith(APP_NAME));
    }

    private String getActualCmd() throws URISyntaxException, IOException {
        Path path = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource(TEST_SHELL_SCRIPT_TEMPLATE_PATH)).toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        return new String(fileBytes);
    }
}
