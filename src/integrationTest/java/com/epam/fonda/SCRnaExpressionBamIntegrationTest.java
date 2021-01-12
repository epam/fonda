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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SCRnaExpressionBamIntegrationTest extends AbstractIntegrationTest {

    private static final String OUTPUT_DIR_ROOT = "build/resources/integrationTest/";

    private static final String SC_RNA_EXPRESSION_BAM_CUFFLINKS_FOR_GA_5_ANALYSIS_TEMPLATE_PATH =
        "SCRnaExpressionBam/forGA5/scRnaExpression_Bam_cufflinks_for_GA5_analysis_template.txt";
    private static final String SC_RNA_EXPRESSION_BAM_EXPRESSION_ESTIMATION_FOR_GA_5_ANALYSIS_TEMPLATE_PATH =
        "SCRnaExpressionBam/forGA5/scRnaExpression_Bam_ExpressionEstimation_for_GA5_analysis_template.txt";
    private static final String SC_RNA_EXPRESSION_BAM_FEATURE_COUNT_FOR_GA_5_ANALYSIS_TEMPLATE_PATH =
        "SCRnaExpressionBam/forGA5/scRnaExpression_Bam_featureCount_for_GA5_analysis_template.txt";
    private static final String SC_RNA_EXPRESSION_BAM_RSEM_FOR_GA_5_ANALYSIS_TEMPLATE_PATH =
        "SCRnaExpressionBam/forGA5/scRnaExpression_Bam_rsem_for_GA5_analysis_template.txt";
    private static final String SC_RNA_EXPRESSION_BAM_STRINGTIE_FOR_GA_5_ANALYSIS_TEMPLATE_PATH =
        "SCRnaExpressionBam/forGA5/scRnaExpression_Bam_stringtie_for_GA5_analysis_template.txt";

    private static final String SC_RNA_EXPRESSION_BAM_CUFFLINKS_FOR_GA_51_ANALYSIS_TEMPLATE_PATH =
        "SCRnaExpressionBam/forGA51/scRnaExpression_Bam_cufflinks_for_GA51_analysis_template.txt";
    private static final String SC_RNA_EXPRESSION_BAM_EXPRESSION_ESTIMATION_FOR_GA_51_ANALYSIS_TEMPLATE_PATH =
        "SCRnaExpressionBam/forGA51/scRnaExpression_Bam_ExpressionEstimation_for_GA51_analysis_template.txt";
    private static final String SC_RNA_EXPRESSION_BAM_FEATURE_COUNT_FOR_GA_51_ANALYSIS_TEMPLATE_PATH =
        "SCRnaExpressionBam/forGA51/scRnaExpression_Bam_featureCount_for_GA51_analysis_template.txt";
    private static final String SC_RNA_EXPRESSION_BAM_RSEM_FOR_GA_51_ANALYSIS_TEMPLATE_PATH =
        "SCRnaExpressionBam/forGA51/scRnaExpression_Bam_rsem_for_GA51_analysis_template.txt";
    private static final String SC_RNA_EXPRESSION_BAM_STRINGTIE_FOR_GA_51_ANALYSIS_TEMPLATE_PATH =
        "SCRnaExpressionBam/forGA51/scRnaExpression_Bam_stringtie_for_GA51_analysis_template.txt";

    private static final String SC_RNA_EXPRESSION_BAM_CUFFLINKS_FOR_GA_52_ANALYSIS_TEMPLATE_PATH =
        "SCRnaExpressionBam/forGA52/scRnaExpression_Bam_cufflinks_for_GA52_analysis_template.txt";
    private static final String SC_RNA_EXPRESSION_BAM_EXPRESSION_ESTIMATION_FOR_GA_52_ANALYSIS_TEMPLATE_PATH =
        "SCRnaExpressionBam/forGA52/scRnaExpression_Bam_ExpressionEstimation_for_GA52_analysis_template.txt";
    private static final String SC_RNA_EXPRESSION_BAM_FEATURE_COUNT_FOR_GA_52_ANALYSIS_TEMPLATE_PATH =
        "SCRnaExpressionBam/forGA52/scRnaExpression_Bam_featureCount_for_GA52_analysis_template.txt";
    private static final String SC_RNA_EXPRESSION_BAM_RSEM_FOR_GA_52_ANALYSIS_TEMPLATE_PATH =
        "SCRnaExpressionBam/forGA52/scRnaExpression_Bam_rsem_for_GA52_analysis_template.txt";
    private static final String SC_RNA_EXPRESSION_BAM_STRINGTIE_FOR_GA_52_ANALYSIS_TEMPLATE_PATH =
        "SCRnaExpressionBam/forGA52/scRnaExpression_Bam_stringtie_for_GA52_analysis_template.txt";

    private static final String SC_RNA_EXPRESSION_BAM_CUFFLINKS_FOR_COHORT_TEMPLATE_PATH =
        "SCRnaExpressionBam/scRnaExpression_Bam_cufflinks_for_cohort_analysis_template.txt";
    private static final String SC_RNA_EXPRESSION_BAM_RSEM_FOR_COHORT_TEMPLATE_PATH =
        "SCRnaExpressionBam/scRnaExpression_Bam_rsem_for_cohort_analysis_template.txt";
    private static final String SC_RNA_EXPRESSION_BAM_STRINGTIE_FOR_COHORT_TEMPLATE_PATH =
        "SCRnaExpressionBam/scRnaExpression_Bam_stringtie_for_cohort_analysis_template.txt";

    private static final String SC_RNA_EXPRESSION_BAM_CUFFLINKS_FOR_COHORT_FILE_PATH =
        "output/sh_files/scRnaExpression_Bam_cufflinks_for_cohort_analysis.sh";
    private static final String SC_RNA_EXPRESSION_BAM_RSEM_FOR_COHORT_FILE_PATH =
        "output/sh_files/scRnaExpression_Bam_rsem_for_cohort_analysis.sh";
    private static final String SC_RNA_EXPRESSION_BAM_STRINGTIE_FOR_COHORT_FILE_PATH =
        "output/sh_files/scRnaExpression_Bam_stringtie_for_cohort_analysis.sh";

    private static final String SCRNA_EXPRESSION_BAM_GLOBAL_CONFIG =
        "SCRnaExpressionBam/global_config_scRnaExpression_Bam_v1.1.txt";
    private static final String SCRNA_EXPRESSION_BAM_STUDY_CONFIG =
        "SCRnaExpressionBam/config_RnaExpression_Bam_test.txt";

    private static final String FIRST_PART_OF_THE_SH_FILE_PATH = "output/sh_files/scRnaExpression_Bam_";
    private static final String LAST_PART_OF_THE_SH_FILE_PATH_FOR_GA5 = "_for_GA5_analysis.sh";
    private static final String LAST_PART_OF_THE_SH_FILE_PATH_FOR_GA51 = "_for_GA51_analysis.sh";
    private static final String LAST_PART_OF_THE_SH_FILE_PATH_FOR_GA52 = "_for_GA52_analysis.sh";

    private static final String CUFFLINKS_TASK_NAME = "cufflinks";
    private static final String EXPRESSION_ESTIMATION_TASK_NAME = "ExpressionEstimation";
    private static final String FEATURE_COUNT_TASK_NAME = "featureCount";
    private static final String RSEM_TASK_NAME = "rsem";
    private static final String STRINGTIE_TASK_NAME = "stringtie";

    @ParameterizedTest
    @MethodSource("initParameters")
    void testWorkflow(String taskName, String suffixForFilePath, String templatePath)
        throws IOException, URISyntaxException {
        startAppWithConfigs(SCRNA_EXPRESSION_BAM_GLOBAL_CONFIG, SCRNA_EXPRESSION_BAM_STUDY_CONFIG);
        String filePath = FIRST_PART_OF_THE_SH_FILE_PATH + taskName + suffixForFilePath;
        final String expectedCmd = TEMPLATE_ENGINE.process(templatePath, context);
        assertEquals(expectedCmd.trim(), getCmd(filePath).trim());
    }

    @Test
    void testConversion() throws IOException, URISyntaxException {
        startAppWithConfigs(SCRNA_EXPRESSION_BAM_GLOBAL_CONFIG, SCRNA_EXPRESSION_BAM_STUDY_CONFIG);
        String expectedCmd =
            TEMPLATE_ENGINE.process(SC_RNA_EXPRESSION_BAM_CUFFLINKS_FOR_COHORT_TEMPLATE_PATH, context);
        assertEquals(expectedCmd.trim(), getCmd(SC_RNA_EXPRESSION_BAM_CUFFLINKS_FOR_COHORT_FILE_PATH).trim());

        expectedCmd = TEMPLATE_ENGINE.process(SC_RNA_EXPRESSION_BAM_RSEM_FOR_COHORT_TEMPLATE_PATH, context);
        assertEquals(expectedCmd.trim(), getCmd(SC_RNA_EXPRESSION_BAM_RSEM_FOR_COHORT_FILE_PATH).trim());

        expectedCmd = TEMPLATE_ENGINE.process(SC_RNA_EXPRESSION_BAM_STRINGTIE_FOR_COHORT_TEMPLATE_PATH, context);
        assertEquals(expectedCmd.trim(), getCmd(SC_RNA_EXPRESSION_BAM_STRINGTIE_FOR_COHORT_FILE_PATH).trim());
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initParameters() {
        return Stream.of(streamOfGA5arguments(), streamOfGA51arguments(), streamOfGA52arguments()).flatMap(s -> s);
    }

    private static Stream<Arguments> streamOfGA5arguments() {
        return Stream.of(
            Arguments.of(CUFFLINKS_TASK_NAME, LAST_PART_OF_THE_SH_FILE_PATH_FOR_GA5,
                SC_RNA_EXPRESSION_BAM_CUFFLINKS_FOR_GA_5_ANALYSIS_TEMPLATE_PATH),
            Arguments.of(EXPRESSION_ESTIMATION_TASK_NAME, LAST_PART_OF_THE_SH_FILE_PATH_FOR_GA5,
                SC_RNA_EXPRESSION_BAM_EXPRESSION_ESTIMATION_FOR_GA_5_ANALYSIS_TEMPLATE_PATH),
            Arguments.of(FEATURE_COUNT_TASK_NAME, LAST_PART_OF_THE_SH_FILE_PATH_FOR_GA5,
                SC_RNA_EXPRESSION_BAM_FEATURE_COUNT_FOR_GA_5_ANALYSIS_TEMPLATE_PATH),
            Arguments.of(RSEM_TASK_NAME, LAST_PART_OF_THE_SH_FILE_PATH_FOR_GA5,
                SC_RNA_EXPRESSION_BAM_RSEM_FOR_GA_5_ANALYSIS_TEMPLATE_PATH),
            Arguments.of(STRINGTIE_TASK_NAME, LAST_PART_OF_THE_SH_FILE_PATH_FOR_GA5,
                SC_RNA_EXPRESSION_BAM_STRINGTIE_FOR_GA_5_ANALYSIS_TEMPLATE_PATH)
        );
    }

    private static Stream<Arguments> streamOfGA51arguments() {
        return Stream.of(
            Arguments.of(CUFFLINKS_TASK_NAME, LAST_PART_OF_THE_SH_FILE_PATH_FOR_GA51,
                SC_RNA_EXPRESSION_BAM_CUFFLINKS_FOR_GA_51_ANALYSIS_TEMPLATE_PATH),
            Arguments.of(EXPRESSION_ESTIMATION_TASK_NAME, LAST_PART_OF_THE_SH_FILE_PATH_FOR_GA51,
                SC_RNA_EXPRESSION_BAM_EXPRESSION_ESTIMATION_FOR_GA_51_ANALYSIS_TEMPLATE_PATH),
            Arguments.of(FEATURE_COUNT_TASK_NAME, LAST_PART_OF_THE_SH_FILE_PATH_FOR_GA51,
                SC_RNA_EXPRESSION_BAM_FEATURE_COUNT_FOR_GA_51_ANALYSIS_TEMPLATE_PATH),
            Arguments.of(RSEM_TASK_NAME, LAST_PART_OF_THE_SH_FILE_PATH_FOR_GA51,
                SC_RNA_EXPRESSION_BAM_RSEM_FOR_GA_51_ANALYSIS_TEMPLATE_PATH),
            Arguments.of(STRINGTIE_TASK_NAME, LAST_PART_OF_THE_SH_FILE_PATH_FOR_GA51,
                SC_RNA_EXPRESSION_BAM_STRINGTIE_FOR_GA_51_ANALYSIS_TEMPLATE_PATH)
        );
    }

    private static Stream<Arguments> streamOfGA52arguments() {
        return Stream.of(
            Arguments.of(CUFFLINKS_TASK_NAME, LAST_PART_OF_THE_SH_FILE_PATH_FOR_GA52,
                SC_RNA_EXPRESSION_BAM_CUFFLINKS_FOR_GA_52_ANALYSIS_TEMPLATE_PATH),
            Arguments.of(EXPRESSION_ESTIMATION_TASK_NAME, LAST_PART_OF_THE_SH_FILE_PATH_FOR_GA52,
                SC_RNA_EXPRESSION_BAM_EXPRESSION_ESTIMATION_FOR_GA_52_ANALYSIS_TEMPLATE_PATH),
            Arguments.of(FEATURE_COUNT_TASK_NAME, LAST_PART_OF_THE_SH_FILE_PATH_FOR_GA52,
                SC_RNA_EXPRESSION_BAM_FEATURE_COUNT_FOR_GA_52_ANALYSIS_TEMPLATE_PATH),
            Arguments.of(RSEM_TASK_NAME, LAST_PART_OF_THE_SH_FILE_PATH_FOR_GA52,
                SC_RNA_EXPRESSION_BAM_RSEM_FOR_GA_52_ANALYSIS_TEMPLATE_PATH),
            Arguments.of(STRINGTIE_TASK_NAME, LAST_PART_OF_THE_SH_FILE_PATH_FOR_GA52,
                SC_RNA_EXPRESSION_BAM_STRINGTIE_FOR_GA_52_ANALYSIS_TEMPLATE_PATH)
        );
    }

    @Test
    void testDirTree() {
        startAppWithConfigs(SCRNA_EXPRESSION_BAM_GLOBAL_CONFIG, SCRNA_EXPRESSION_BAM_STUDY_CONFIG);
        assertAll(
            () -> assertTrue(new File(format("%s%s/sh_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/log_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/err_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA5", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA5/cufflinks", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA5/feature_count", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA5/rsem", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA5/stringtie", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA5/tmp", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA51", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA51/cufflinks", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA51/feature_count", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA51/rsem", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA51/stringtie", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA51/tmp", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA52", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA52/cufflinks", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA52/feature_count", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA52/rsem", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA52/stringtie", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA52/tmp", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists())
        );
    }
}
