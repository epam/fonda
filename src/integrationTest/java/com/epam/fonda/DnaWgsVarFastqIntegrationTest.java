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

public class DnaWgsVarFastqIntegrationTest extends AbstractIntegrationTest {

    private static final String OUTPUT_DIR_ROOT = "build/resources/integrationTest/";

    private static final String DNA_WGS_VAR_FASTQ_ALIGNMENT_FOR_GA_5_1_SH_FILE_PATH =
        "output/sh_files/DnaWgsVar_Fastq_alignment_for_GA5_1_analysis.sh";
    private static final String DNA_WGS_VAR_FASTQ_GET_HAPLOTYPE_CALLER_FOR_GA5_SH_FILE_PATH =
        "output/sh_files/DnaWgsVar_Fastq_gatkHaplotypeCaller_for_GA5_analysis.sh";
    private static final String DNA_WGS_VAR_FASTQ_MERGE_MUTATION_FOR_COHORT_SH_FILE_PATH =
        "output/sh_files/DnaWgsVar_Fastq_mergeMutation_for_cohort_analysis.sh";
    private static final String DNA_WGS_VAR_FASTQ_POSTALIGNMENT_FOR_GA5_SH_FILE_PATH =
        "output/sh_files/DnaWgsVar_Fastq_postalignment_for_GA5_analysis.sh";

    private static final String DNA_WGS_VAR_FASTQ_ALIGNMENT_FOR_GA_5_1_TEMPLATE_PATH =
        "DnaWgsVarFastq/DnaWgsVar_Fastq_alignment_for_GA5_1_analysis_template.txt";
    private static final String DNA_WGS_VAR_FASTQ_GET_HAPLOTYPE_CALLER_FOR_GA5_TEMPLATE_PATH =
        "DnaWgsVarFastq/DnaWgsVar_Fastq_gatkHaplotypeCaller_for_GA5_analysis_template.txt";
    private static final String DNA_WGS_VAR_FASTQ_MERGE_MUTATION_FOR_COHORT_TEMPLATE_PATH =
        "DnaWgsVarFastq/DnaWgsVar_Fastq_mergeMutation_for_cohort_analysis_template.txt";
    private static final String DNA_WGS_VAR_FASTQ_POSTALIGNMENT_FOR_GA5_TEMPLATE_PATH =
        "DnaWgsVarFastq/DnaWgsVar_Fastq_postalignment_for_GA5_analysis_template.txt";

    public static final String DNA_WGS_VAR_FASTQ_GLOBAL_CONFIG_PATH =
        "DnaWgsVarFastq/global_config_DnaWgsVar_Fastq_v1.1.txt";
    public static final String STUDY_CONFIG_PATH = "DnaWgsVarFastq/sscRnaExpressionCellRangerFastq.txt";

    @ParameterizedTest
    @MethodSource("initParameters")
    void testControlSample(String templatePath, String filePath) throws IOException, URISyntaxException {
        startAppWithConfigs(DNA_WGS_VAR_FASTQ_GLOBAL_CONFIG_PATH, STUDY_CONFIG_PATH);
        String expectedCmd = templateEngine.process(templatePath, context);
        assertEquals(expectedCmd.trim(), getCmd(filePath).trim());
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initParameters() {
        return Stream.of(
            Arguments.of(DNA_WGS_VAR_FASTQ_ALIGNMENT_FOR_GA_5_1_TEMPLATE_PATH,
                DNA_WGS_VAR_FASTQ_ALIGNMENT_FOR_GA_5_1_SH_FILE_PATH),
            Arguments.of(DNA_WGS_VAR_FASTQ_GET_HAPLOTYPE_CALLER_FOR_GA5_TEMPLATE_PATH,
                DNA_WGS_VAR_FASTQ_GET_HAPLOTYPE_CALLER_FOR_GA5_SH_FILE_PATH),
            Arguments.of(DNA_WGS_VAR_FASTQ_MERGE_MUTATION_FOR_COHORT_TEMPLATE_PATH,
                DNA_WGS_VAR_FASTQ_MERGE_MUTATION_FOR_COHORT_SH_FILE_PATH),
            Arguments.of(DNA_WGS_VAR_FASTQ_POSTALIGNMENT_FOR_GA5_TEMPLATE_PATH,
                DNA_WGS_VAR_FASTQ_POSTALIGNMENT_FOR_GA5_SH_FILE_PATH)
        );
    }

    @Test
    void testDir() {
        startAppWithConfigs(DNA_WGS_VAR_FASTQ_GLOBAL_CONFIG_PATH, STUDY_CONFIG_PATH);
        assertAll(
            () -> assertTrue(new File(format("%s%s/sh_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/log_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/err_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA5", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA5/bam", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA5/fastq", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA5/gatkHaplotypeCaller", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(
                new File(format("%s%s/GA5/gatkHaplotypeCaller/tmp", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA5/qc", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA5/tmp", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists())
        );
    }
}
