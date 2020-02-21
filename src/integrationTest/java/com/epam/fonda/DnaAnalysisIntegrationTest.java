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

public class DnaAnalysisIntegrationTest extends AbstractIntegrationTest {

    private static final String OUTPUT_DIR_ROOT = "build/resources/integrationTest/";
    private static final String AMPLICON = "Amplicon";
    private static final String CAPTURE = "Capture";

    private static final String VARDICT = "vardict_for_GA5";
    private static final String MUTECT1 = "mutect1_for_GA5";
    private static final String LOFREQ = "lofreq_for_GA5";
    private static final String STRELKA2 = "strelka2_for_GA5";
    private static final String GATK_HAPLOTYPE_CALLER = "gatkHaplotypeCaller_for_GA5";
    private static final String SCALPEL = "scalpel_for_GA5";
    private static final String POSTALIGMENT = "postalignment_for_GA5";
    private static final String ALIGNMENT = "alignment_for_GA5_1";

    private static final String DNA_ANALYSIS_S_SINGLE_STUDY_CONFIG = "DnaAnalysis/sSingle.txt";

    private static final String FIRST_PART_OF_THE_PATH_TO_DNA_SHELL_SCRIPT = "output/sh_files/Dna";
    private static final String THIRD_PART_OF_THE_PATH_TO_DNA_SHELL_SCRIPT = "Var_Fastq_";
    private static final String LAST_PART_OF_THE_PATH_TO_DNA_SHELL_SCRIPT = "_analysis.sh";

    private static final String G_SINGLE_DNA_AMPLICON_GLOBAL_CONFIG_PATH = "DnaAnalysis/gSingleDnaAmplicon.txt";
    private static final String G_SINGLE_DNA_CAPTURE_GLOBAL_CONFIG_PATH = "DnaAnalysis/gSingleDnaCapture.txt";

    private static final String DNA_AMPLICON_VAR_FASTQ_ALIGNMENT_FOR_GA5_1_ANALYSIS_TEMPLATE_PATH =
        "DnaAnalyses/gSingleDnaAmplicon/DnaAmpliconVar_Fastq_alignment_for_GA5_1_analysis_template.txt";
    private static final String DNA_AMPLICON_VAR_FASTQ_GATK_HAPLOTYPE_CALLER_FOR_GA5_ANALYSIS_TEMPLATE_PATH =
        "DnaAnalyses/gSingleDnaAmplicon/DnaAmpliconVar_Fastq_gatkHaplotypeCaller_for_GA5_analysis_template.txt";
    private static final String DNA_AMPLICON_VAR_FASTQ_LOFREQ_FOR_GA5_ANALYSIS_TEMPLATE_PATH =
        "DnaAnalyses/gSingleDnaAmplicon/DnaAmpliconVar_Fastq_lofreq_for_GA5_analysis_template.txt";
    private static final String DNA_AMPLICON_VAR_FASTQ_MUTEC_1_FOR_GA5_ANALYSIS_TEMPLATE_PATH =
        "DnaAnalyses/gSingleDnaAmplicon/DnaAmpliconVar_Fastq_mutect1_for_GA5_analysis_template.txt";
    private static final String DNA_AMPLICON_VAR_FASTQ_POSTALIGNMENT_FOR_GA5_ANALYSIS_TEMPLATE_PATH =
        "DnaAnalyses/gSingleDnaAmplicon/DnaAmpliconVar_Fastq_postalignment_for_GA5_analysis_template.txt";
    private static final String DNA_AMPLICON_VAR_FASTQ_SCALPEL_FOR_GA5_ANALYSIS_TEMPLATE_PATH =
        "DnaAnalyses/gSingleDnaAmplicon/DnaAmpliconVar_Fastq_scalpel_for_GA5_analysis_template.txt";
    private static final String DNA_AMPLICON_VAR_FASTQ_STRELKA2_FOR_GA5_ANALYSIS_TEMPLATE_PATH =
        "DnaAnalyses/gSingleDnaAmplicon/DnaAmpliconVar_Fastq_strelka2_for_GA5_analysis_template.txt";
    private static final String DNA_AMPLICON_VAR_FASTQ_VARDICT_FOR_GA5_ANALYSIS_TEMPLATE_PATH =
        "DnaAnalyses/gSingleDnaAmplicon/DnaAmpliconVar_Fastq_vardict_for_GA5_analysis_template.txt";
    private static final String DNA_AMPLICON_VAR_FASTQ_MERGE_MUTATION_FOR_COHORT_ANALYSIS_TEMPLATE_PATH =
        "DnaAnalyses/gSingleDnaAmplicon/DnaAmpliconVar_Fastq_mergeMutation_for_cohort_analysis_template.txt";

    private static final String DNA_CAPTURE_VAR_FASTQ_ALIGNMENT_FOR_GA5_1_ANALYSIS_TEMPLATE_PATH =
        "DnaAnalyses/gSingleDnaCapture/DnaCaptureVar_Fastq_alignment_for_GA5_1_analysis_template.txt";
    private static final String DNA_CAPTURE_VAR_FASTQ_GATK_HAPLOTYPE_CALLER_FOR_GA5_ANALYSIS_TEMPLATE_PATH =
        "DnaAnalyses/gSingleDnaCapture/DnaCaptureVar_Fastq_gatkHaplotypeCaller_for_GA5_analysis_template.txt";
    private static final String DNA_CAPTURE_VAR_FASTQ_LOFREQ_FOR_GA5_ANALYSIS_TEMPLATE_PATH =
        "DnaAnalyses/gSingleDnaCapture/DnaCaptureVar_Fastq_lofreq_for_GA5_analysis_template.txt";
    private static final String DNA_CAPTURE_VAR_FASTQ_MUTEC_1_FOR_GA5_ANALYSIS_TEMPLATE_PATH =
        "DnaAnalyses/gSingleDnaCapture/DnaCaptureVar_Fastq_mutect1_for_GA5_analysis_template.txt";
    private static final String DNA_CAPTURE_VAR_FASTQ_POSTALIGNMENT_FOR_GA5_ANALYSIS_TEMPLATE_PATH =
        "DnaAnalyses/gSingleDnaCapture/DnaCaptureVar_Fastq_postalignment_for_GA5_analysis_template.txt";
    private static final String DNA_CAPTURE_VAR_FASTQ_SCALPEL_FOR_GA5_ANALYSIS_TEMPLATE_PATH =
        "DnaAnalyses/gSingleDnaCapture/DnaCaptureVar_Fastq_scalpel_for_GA5_analysis_template.txt";
    private static final String DNA_CAPTURE_VAR_FASTQ_STRELKA2_FOR_GA5_ANALYSIS_TEMPLATE_PATH =
        "DnaAnalyses/gSingleDnaCapture/DnaCaptureVar_Fastq_strelka2_for_GA5_analysis_template.txt";
    private static final String DNA_CAPTURE_VAR_FASTQ_VARDICT_FOR_GA5_ANALYSIS_TEMPLATE_PATH =
        "DnaAnalyses/gSingleDnaCapture/DnaCaptureVar_Fastq_vardict_for_GA5_analysis_template.txt";
    private static final String DNA_CAPTURE_VAR_FASTQ_MERGE_MUTATION_FOR_COHORT_ANALYSIS_TEMPLATE_PATH =
        "DnaAnalyses/gSingleDnaCapture/DnaCaptureVar_Fastq_mergeMutation_for_cohort_analysis_template.txt";

    private static final String FIRST_PART_TO_THE_PATH_OF_MERGE_MUTATION_SH = "output/sh_files/Dna";
    private static final String LAST_PART_TO_THE_PATH_OF_MERGE_MUTATION_SH =
        "Var_Fastq_mergeMutation_for_cohort_analysis.sh";

    @ParameterizedTest
    @MethodSource("initParameters")
    public void testPeriodicDnaMutationStatusCheck(String globalConfigPath, String templatePath, String taskName,
        String workflow, String mergeMutationTemplatePath) throws IOException, URISyntaxException {
        startAppWithConfigs(globalConfigPath, DNA_ANALYSIS_S_SINGLE_STUDY_CONFIG);

        String expectedCmd = templateEngine.process(templatePath, context);
        String filePath = FIRST_PART_OF_THE_PATH_TO_DNA_SHELL_SCRIPT + workflow +
            THIRD_PART_OF_THE_PATH_TO_DNA_SHELL_SCRIPT + taskName + LAST_PART_OF_THE_PATH_TO_DNA_SHELL_SCRIPT;
        assertEquals(expectedCmd.trim(), getCmd(filePath).trim());

        expectedCmd = templateEngine.process(mergeMutationTemplatePath, context);
        filePath = FIRST_PART_TO_THE_PATH_OF_MERGE_MUTATION_SH + workflow + LAST_PART_TO_THE_PATH_OF_MERGE_MUTATION_SH;
        assertEquals(expectedCmd.trim(), getCmd(filePath).trim());
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initParameters() {
        return Stream.concat(gSingleDnaAmpliconArguments(), gSingleDnaCaptureArguments());
    }

    private static Stream<Arguments> gSingleDnaAmpliconArguments() {
        return Stream.of(
            Arguments.of(G_SINGLE_DNA_AMPLICON_GLOBAL_CONFIG_PATH,
                DNA_AMPLICON_VAR_FASTQ_ALIGNMENT_FOR_GA5_1_ANALYSIS_TEMPLATE_PATH, ALIGNMENT, AMPLICON,
                DNA_AMPLICON_VAR_FASTQ_MERGE_MUTATION_FOR_COHORT_ANALYSIS_TEMPLATE_PATH),
            Arguments.of(G_SINGLE_DNA_AMPLICON_GLOBAL_CONFIG_PATH,
                DNA_AMPLICON_VAR_FASTQ_GATK_HAPLOTYPE_CALLER_FOR_GA5_ANALYSIS_TEMPLATE_PATH, GATK_HAPLOTYPE_CALLER,
                AMPLICON, DNA_AMPLICON_VAR_FASTQ_MERGE_MUTATION_FOR_COHORT_ANALYSIS_TEMPLATE_PATH),
            Arguments.of(G_SINGLE_DNA_AMPLICON_GLOBAL_CONFIG_PATH,
                DNA_AMPLICON_VAR_FASTQ_LOFREQ_FOR_GA5_ANALYSIS_TEMPLATE_PATH, LOFREQ, AMPLICON,
                DNA_AMPLICON_VAR_FASTQ_MERGE_MUTATION_FOR_COHORT_ANALYSIS_TEMPLATE_PATH),
            Arguments.of(G_SINGLE_DNA_AMPLICON_GLOBAL_CONFIG_PATH,
                DNA_AMPLICON_VAR_FASTQ_MUTEC_1_FOR_GA5_ANALYSIS_TEMPLATE_PATH, MUTECT1, AMPLICON,
                DNA_AMPLICON_VAR_FASTQ_MERGE_MUTATION_FOR_COHORT_ANALYSIS_TEMPLATE_PATH),
            Arguments.of(G_SINGLE_DNA_AMPLICON_GLOBAL_CONFIG_PATH,
                DNA_AMPLICON_VAR_FASTQ_POSTALIGNMENT_FOR_GA5_ANALYSIS_TEMPLATE_PATH, POSTALIGMENT, AMPLICON,
                DNA_AMPLICON_VAR_FASTQ_MERGE_MUTATION_FOR_COHORT_ANALYSIS_TEMPLATE_PATH),
            Arguments.of(G_SINGLE_DNA_AMPLICON_GLOBAL_CONFIG_PATH,
                DNA_AMPLICON_VAR_FASTQ_SCALPEL_FOR_GA5_ANALYSIS_TEMPLATE_PATH, SCALPEL, AMPLICON,
                DNA_AMPLICON_VAR_FASTQ_MERGE_MUTATION_FOR_COHORT_ANALYSIS_TEMPLATE_PATH),
            Arguments.of(G_SINGLE_DNA_AMPLICON_GLOBAL_CONFIG_PATH,
                DNA_AMPLICON_VAR_FASTQ_STRELKA2_FOR_GA5_ANALYSIS_TEMPLATE_PATH, STRELKA2, AMPLICON,
                DNA_AMPLICON_VAR_FASTQ_MERGE_MUTATION_FOR_COHORT_ANALYSIS_TEMPLATE_PATH),
            Arguments.of(G_SINGLE_DNA_AMPLICON_GLOBAL_CONFIG_PATH,
                DNA_AMPLICON_VAR_FASTQ_VARDICT_FOR_GA5_ANALYSIS_TEMPLATE_PATH, VARDICT, AMPLICON,
                DNA_AMPLICON_VAR_FASTQ_MERGE_MUTATION_FOR_COHORT_ANALYSIS_TEMPLATE_PATH)
        );
    }

    private static Stream<Arguments> gSingleDnaCaptureArguments() {
        return Stream.of(
            Arguments.of(G_SINGLE_DNA_CAPTURE_GLOBAL_CONFIG_PATH,
                DNA_CAPTURE_VAR_FASTQ_ALIGNMENT_FOR_GA5_1_ANALYSIS_TEMPLATE_PATH, ALIGNMENT, CAPTURE,
                DNA_CAPTURE_VAR_FASTQ_MERGE_MUTATION_FOR_COHORT_ANALYSIS_TEMPLATE_PATH),
            Arguments.of(G_SINGLE_DNA_CAPTURE_GLOBAL_CONFIG_PATH,
                DNA_CAPTURE_VAR_FASTQ_GATK_HAPLOTYPE_CALLER_FOR_GA5_ANALYSIS_TEMPLATE_PATH, GATK_HAPLOTYPE_CALLER,
                CAPTURE, DNA_CAPTURE_VAR_FASTQ_MERGE_MUTATION_FOR_COHORT_ANALYSIS_TEMPLATE_PATH),
            Arguments.of(G_SINGLE_DNA_CAPTURE_GLOBAL_CONFIG_PATH,
                DNA_CAPTURE_VAR_FASTQ_LOFREQ_FOR_GA5_ANALYSIS_TEMPLATE_PATH, LOFREQ, CAPTURE,
                DNA_CAPTURE_VAR_FASTQ_MERGE_MUTATION_FOR_COHORT_ANALYSIS_TEMPLATE_PATH),
            Arguments.of(G_SINGLE_DNA_CAPTURE_GLOBAL_CONFIG_PATH,
                DNA_CAPTURE_VAR_FASTQ_MUTEC_1_FOR_GA5_ANALYSIS_TEMPLATE_PATH, MUTECT1, CAPTURE,
                DNA_CAPTURE_VAR_FASTQ_MERGE_MUTATION_FOR_COHORT_ANALYSIS_TEMPLATE_PATH),
            Arguments.of(G_SINGLE_DNA_CAPTURE_GLOBAL_CONFIG_PATH,
                DNA_CAPTURE_VAR_FASTQ_POSTALIGNMENT_FOR_GA5_ANALYSIS_TEMPLATE_PATH, POSTALIGMENT, CAPTURE,
                DNA_CAPTURE_VAR_FASTQ_MERGE_MUTATION_FOR_COHORT_ANALYSIS_TEMPLATE_PATH),
            Arguments.of(G_SINGLE_DNA_CAPTURE_GLOBAL_CONFIG_PATH,
                DNA_CAPTURE_VAR_FASTQ_SCALPEL_FOR_GA5_ANALYSIS_TEMPLATE_PATH, SCALPEL, CAPTURE,
                DNA_CAPTURE_VAR_FASTQ_MERGE_MUTATION_FOR_COHORT_ANALYSIS_TEMPLATE_PATH),
            Arguments.of(G_SINGLE_DNA_CAPTURE_GLOBAL_CONFIG_PATH,
                DNA_CAPTURE_VAR_FASTQ_STRELKA2_FOR_GA5_ANALYSIS_TEMPLATE_PATH, STRELKA2, CAPTURE,
                DNA_CAPTURE_VAR_FASTQ_MERGE_MUTATION_FOR_COHORT_ANALYSIS_TEMPLATE_PATH),
            Arguments.of(G_SINGLE_DNA_CAPTURE_GLOBAL_CONFIG_PATH,
                DNA_CAPTURE_VAR_FASTQ_VARDICT_FOR_GA5_ANALYSIS_TEMPLATE_PATH, VARDICT, CAPTURE,
                DNA_CAPTURE_VAR_FASTQ_MERGE_MUTATION_FOR_COHORT_ANALYSIS_TEMPLATE_PATH)
        );
    }

    @ParameterizedTest
    @MethodSource("initParametersForDirTesting")
    void testDirTreeDnaAmpliconVar(String globalConfigPath) {
        startAppWithConfigs(globalConfigPath, DNA_ANALYSIS_S_SINGLE_STUDY_CONFIG);
        assertAll(
            () -> assertTrue(new File(format("%s%s/sh_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/log_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/err_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA5", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA5/bam", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA5/gatkHaplotypeCaller", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(
                new File(format("%s%s/GA5/gatkHaplotypeCaller/tmp", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA5/lofreq", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA5/mutect1", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA5/mutect1/tmp", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA5/qc", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA5/scalpel", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA5/strelka2", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA5/tmp", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA5/vardict", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists())
        );
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initParametersForDirTesting() {
        return Stream.of(
            Arguments.of(G_SINGLE_DNA_AMPLICON_GLOBAL_CONFIG_PATH),
            Arguments.of(G_SINGLE_DNA_CAPTURE_GLOBAL_CONFIG_PATH)
        );
    }
}
