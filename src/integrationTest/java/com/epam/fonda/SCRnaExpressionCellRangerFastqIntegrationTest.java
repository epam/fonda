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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.epam.fonda.utils.PipelineUtils.getExecutionPath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(DataProviderRunner.class)
public class SCRnaExpressionCellRangerFastqIntegrationTest extends AbstractIntegrationTest {

    private static final String OUTPUT_DIR = "output";
    private static final String OUTPUT_DIR_ROOT = "build/resources/integrationTest/";
    private static final String SCRNA_ANALYSIS_COUNT_QC_DOUBLET_DETECTION_TEST_TEMPLATE_PATH =
            "scRnaExpressionCellRangerFastq_CountDoubletDetectionQC_template";
    private static final String SCRNA_ANALYSIS_COUNT_QC_SCRUBLET_TEST_TEMPLATE_PATH =
            "scRnaExpressionCellRangerFast_CountScrubletQC_template";
    private static final String SCRNA_TEST_SAMPLE_LIST = "/ngs/data/demo/test/fastq";
    private static final String SCRNA_EXPRESSION_FASTQ_STUDY_CONFIG =
            "scRnaExpressionCellRangerFastq/sscRnaExpressionCellRangerFastq.txt";
    private static final String TEST_SHELL_SCRIPT_TEMPLATE_PATH =
            "output/sh_files/scRnaExpression_CellRanger_Fastq_alignment_for_smv1_analysis.sh";
    private static final String APP_NAME = "fonda";

    private static Map<String, String> argMapForCreateAndExtractTestWithFastqs1Fastqs2;
    private static Map<String, String> argMapForGetRunLibTest;
    private static Map<String, String> argMapForCreateAndExtractTestWithFastqs1;
    private static Map<String, String> argMapForCreateAndExtractTestWithFastqs1Length1;

    private static final String COUNT_AGGREGATION_TOOLSET = "count+aggregation";
    private static final String FASTQ_1 = "fastq1";
    private static final String FASTQ_2 = "fastq2";
    private static final String SC_RNA_EXPRESSION_CELLRANGER_FASTQ_WORKFLOW = "scRnaExpression_CellRanger_Fastq";
    private static final String RUN_ID = "2";

    private FastqFileSample expectedSample;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private String jarPath;

    @Before
    public void setup() {
        argMapForCreateAndExtractTestWithFastqs1Fastqs2 = new HashMap<>();
        argMapForCreateAndExtractTestWithFastqs1Fastqs2.put(OUTPUT_DIR, OUTPUT_DIR);
        argMapForCreateAndExtractTestWithFastqs1Fastqs2.put("toolset", COUNT_AGGREGATION_TOOLSET);
        argMapForCreateAndExtractTestWithFastqs1Fastqs2.put("fastqs_1", FASTQ_1);
        argMapForCreateAndExtractTestWithFastqs1Fastqs2.put("fastqs_2", FASTQ_2);

        argMapForGetRunLibTest = new HashMap<>();
        argMapForGetRunLibTest.put(OUTPUT_DIR, OUTPUT_DIR);
        argMapForGetRunLibTest.put("workflow", SC_RNA_EXPRESSION_CELLRANGER_FASTQ_WORKFLOW);
        argMapForGetRunLibTest.put("fastq_list", this.getClass()
                .getClassLoader().getResource("fastq_list.tsv").getPath());
        argMapForGetRunLibTest.put("Run", RUN_ID);

        argMapForCreateAndExtractTestWithFastqs1 = new HashMap<>();
        argMapForCreateAndExtractTestWithFastqs1.put(OUTPUT_DIR, OUTPUT_DIR);
        argMapForCreateAndExtractTestWithFastqs1.put("toolset", COUNT_AGGREGATION_TOOLSET);
        argMapForCreateAndExtractTestWithFastqs1.put("fastqs_1", "fasts1, fastq_2, fastqs_3");

        argMapForCreateAndExtractTestWithFastqs1Length1 = new HashMap<>();
        argMapForCreateAndExtractTestWithFastqs1Length1.put(OUTPUT_DIR, OUTPUT_DIR);
        argMapForCreateAndExtractTestWithFastqs1Length1.put("toolset", COUNT_AGGREGATION_TOOLSET);
        argMapForCreateAndExtractTestWithFastqs1Length1.put("fastqs_1", "fasts1");

        expectedSample = new FastqFileSample();
        expectedSample.setFastq1(Arrays.asList("fastq1", "fastq2"));
        expectedSample.setName("sampleName");
        jarPath = getExecutionPath();
    }

    @DataProvider
    public static Object[][] getScRnaExpressionCellRangerFastqExpectedStrings() {
        return new Object[][] {
                {"output/sh_files/scRnaExpression_CellRanger_Fastq_alignment_for_smv1_analysis.sh", new String[] {
                    "Begin Step: Cellranger count...",
                    "cd build/resources/integrationTest/output/count",
                    "path/to/cellranger count --id=smv1 ",
                    "--transcriptome=" +
                            "/common/reference_genome/GRCh38/Sequence/GRCh38.gencode.v26.pc_transcripts.fa ",
                    "--fastqs=" + SCRNA_TEST_SAMPLE_LIST,
                    "--sample=smv1 --expect-cells=5000",
                    "echo `date` Begin Step: Generate gene-barcode matrix...",
                    "path/to/cellranger mat2csv build/resources/integrationTest/output/count/smv1/outs/" +
                            "filtered_feature_bc_matrix smv1_genome1_umi_count_matrix.csv",
                    "Begin Step: Merge gene-barcode matrix..",
                    "/usr/bin/Rscript",
                    "/R/merge_data_matrix.R -a smv1_genome1_umi_count_matrix.tsv " +
                            "-b smv1_genome2_umi_count_matrix.tsv " +
                            "-o smv1_genome1_and_genome2_umi_count_matrix.tsv",
                    "< smv1_genome1_umi_count_matrix.csv > smv1_genome1_umi_count_matrix.tsv",
                    "rm smv1_genome1_umi_count_matrix.csv",
                    "path/to/cellranger ",
                    "smv1_genome2_umi_count_matrix.csv",
                    "rm smv1_genome2_umi_count_matrix.csv"
                }},
                {"output/sh_files/scRnaExpression_CellRanger_Fastq_qcsummary_for_cohort_analysis.sh", new String[] {
                    "echo $(date) Error QC results from smv1:",
                    "echo $(date) Confirm QC results from smv1",
                    "build/resources/integrationTest/output/log_files/" +
                            "scRnaExpression_CellRanger_Fastq_alignment_for_smv1_analysis.log"
                }},
        };
    }

    @Test
    @UseDataProvider("getScRnaExpressionCellRangerFastqExpectedStrings")
    public void testCount(String outputFile, String[] expectedStrings) throws IOException {
        startAppWithConfigs(
                "scRnaExpressionCellRangerFastq/gCount.txt",
                SCRNA_EXPRESSION_FASTQ_STUDY_CONFIG);
        File outputShFile = new File(this.getClass().getClassLoader().getResource(outputFile).getPath());
        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            for (String expectedString : expectedStrings) {
                assertTrue(lines.stream().anyMatch(line -> line.contains(expectedString)));
            }
            assertFalse(lines.stream().anyMatch(line -> line.contains("null")));
        }
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
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
    public void testDoubletDetection() throws IOException, URISyntaxException {
        startAppWithConfigs("scRnaExpressionCellRangerFastq/gDoubletDetection.txt",
                SCRNA_EXPRESSION_FASTQ_STUDY_CONFIG);

        Context context = new Context();
        context.setVariable("jarPath", jarPath);
        context.setVariable("sampleList", SCRNA_TEST_SAMPLE_LIST);
        final String expectedCmd = expectedTemplateEngine
                .process(SCRNA_ANALYSIS_COUNT_QC_DOUBLET_DETECTION_TEST_TEMPLATE_PATH, context);

        Path path = Paths.get(this.getClass().getClassLoader().getResource(TEST_SHELL_SCRIPT_TEMPLATE_PATH).toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        final String actualCmd = new String(fileBytes);

        assertEquals(expectedCmd, actualCmd);
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @Test
    public void testScrublet() throws IOException, URISyntaxException {
        startAppWithConfigs("scRnaExpressionCellRangerFastq/gScrublet.txt",
                SCRNA_EXPRESSION_FASTQ_STUDY_CONFIG);

        Context context = new Context();
        context.setVariable("jarPath", jarPath);
        context.setVariable("sampleList", SCRNA_TEST_SAMPLE_LIST);
        final String expectedCmd = expectedTemplateEngine
                .process(SCRNA_ANALYSIS_COUNT_QC_SCRUBLET_TEST_TEMPLATE_PATH, context);

        Path path = Paths.get(this.getClass().getClassLoader().getResource(TEST_SHELL_SCRIPT_TEMPLATE_PATH).toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        final String actualCmd = new String(fileBytes);

        assertEquals(expectedCmd, actualCmd);
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
}
