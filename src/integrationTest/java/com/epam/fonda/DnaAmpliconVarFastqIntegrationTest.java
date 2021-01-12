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
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DnaAmpliconVarFastqIntegrationTest extends AbstractIntegrationTest {

    private static final String OUTPUT_DIR_ROOT = "build/resources/integrationTest/";
    private static final String ERROR_MESSAGE = "An error occurred with task %s";

    private static final String POST_ALIGNMENT_SH_FILE =
            "output/sh_files/DnaAmpliconVar_Fastq_postalignment_for_GA5_analysis.sh";
    private static final String ALIGNMENT_SH_FILE =
            "output/sh_files/DnaAmpliconVar_Fastq_alignment_for_GA5_1_analysis.sh";
    private static final String QC_SUMMARY_SH_FILE =
            "output/sh_files/DnaAmpliconVar_Fastq_qcsummary_for_cohort_analysis.sh";
    private static final String MERGE_MUTATION_SH_FILE =
            "output/sh_files/DnaAmpliconVar_Fastq_mergeMutation_for_cohort_analysis.sh";

    private static final String NULL = "null";
    private static final String SINGLE_STUDY_CONFIG = "DnaAmpliconVarFastq/sSingle.txt";
    private static final String PAIRED_STUDY_CONFIG = "DnaAmpliconVarFastq/sPaired.txt";
    private static final String ALL_TASKS_FOLDER =
            "dnaAmpliconVarFastq/testControlSampleAllTasksXenomeNo";
    private static final String ALL_TASKS_FREEBAYES = format(
            "%s/DnaAmpliconVar_Fastq_freebayes_for_GA5_analysis", ALL_TASKS_FOLDER);
    private static final String ALL_TASKS_GATK_HAPLOTYPE_CALLER = format(
            "%s/DnaAmpliconVar_Fastq_gatkHaplotypeCaller_for_GA5_analysis", ALL_TASKS_FOLDER);
    private static final String ALL_TASKS_LOFREQ = format(
            "%s/DnaAmpliconVar_Fastq_lofreq_for_GA5_analysis", ALL_TASKS_FOLDER);
    private static final String ALL_TASKS_MUTECT1 = format(
            "%s/DnaAmpliconVar_Fastq_mutect1_for_GA5_analysis", ALL_TASKS_FOLDER);
    private static final String ALL_TASKS_SCALPEL = format(
            "%s/DnaAmpliconVar_Fastq_scalpel_for_GA5_analysis", ALL_TASKS_FOLDER);
    private static final String ALL_TASKS_STRELKA2 = format(
            "%s/DnaAmpliconVar_Fastq_strelka2_for_GA5_analysis", ALL_TASKS_FOLDER);
    private static final String ALL_TASKS_VARDICT = format(
            "%s/DnaAmpliconVar_Fastq_vardict_for_GA5_analysis", ALL_TASKS_FOLDER);
    private static final String ALIGNMENT_TEMPLATE = "DnaAmpliconVar_Fastq_alignment_for_GA5_1_analysis.txt";
    private static final String POST_ALIGNMENT_TEMPLATE =
            "DnaAmpliconVar_Fastq_postalignment_for_GA5_analysis";
    private static final String MERGE_MUTATION_TEMPLATE =
            "DnaAmpliconVar_Fastq_mergeMutation_for_cohort_analysis.txt";
    private static final String QC_SUMMARY_TEMPLATE = "DnaAmpliconVar_Fastq_qcsummary_for_cohort_analysis";

    private static final String G_PAIRED_ALL_TASKS = "DnaAmpliconVarFastq/gPairedAllTasks.txt";
    private static final String S_CONTROL_SAMPLE_NOT_NA = "DnaAmpliconVarFastq/sControlSampleNotNA.txt";
    private static final String G_SINGLE_ALL_TASKS = "DnaAmpliconVarFastq/gSingleAllTasks.txt";
    private static final String S_CONTROL_SAMPLE_NOT_NA_FOR_PA =
            "DnaAmpliconVarFastq/sControlSampleNotNaForPostAlignment.txt";

    @ParameterizedTest
    @MethodSource("initControlSampleAllTasks")
    public void testControlSampleAllTasksXenomeNo(String task, String template) throws IOException, URISyntaxException {
        startAppWithConfigs(G_SINGLE_ALL_TASKS, SINGLE_STUDY_CONFIG);

        String filePath = format("output/sh_files/DnaAmpliconVar_Fastq_%s_for_GA5_analysis.sh", task);
        final String expectedCmd = TEMPLATE_ENGINE.process(template, context).trim();
        final String actualCmd = getCmd(filePath).trim();

        assertFalse(actualCmd.contains(NULL));
        assertEquals(expectedCmd, actualCmd, format(ERROR_MESSAGE, task));
    }

    @Test
    public void testControlSampleAllTasksXenomeNoOutput() {
        startAppWithConfigs(G_SINGLE_ALL_TASKS, SINGLE_STUDY_CONFIG);

        assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + "sh_files").exists());
        assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + "log_files").exists());
        assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + "err_files").exists());
    }

    @ParameterizedTest
    @MethodSource("initConfigsTestAlignmentAndMergeMutation")
    public void testAlignmentAndMergeMutation(String globalConfig, String studyConfig, String folder)
            throws IOException, URISyntaxException {
        startAppWithConfigs(globalConfig, studyConfig);

        String expectedAlignmentCmd = getTestTemplate(folder, ALIGNMENT_TEMPLATE, context).trim();
        String actualAlignmentCmd = getCmd(ALIGNMENT_SH_FILE).trim();
        assertEquals(expectedAlignmentCmd, actualAlignmentCmd);

        String expectedMergeMutationCmd = getTestTemplate(folder, MERGE_MUTATION_TEMPLATE, context);
        String actualMergeMutationCmd = getCmd(MERGE_MUTATION_SH_FILE).trim();
        assertEquals(expectedMergeMutationCmd, actualMergeMutationCmd);

        assertAll(
            () -> assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + "sh_files").exists()),
            () -> assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + "log_files").exists()),
            () -> assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + "err_files").exists())
        );
    }

    @ParameterizedTest
    @MethodSource("initTestFolderAndConfigs")
    public void testStagesAndQcSummary(String testFolder, String globalConfig, String studyConfig)
            throws IOException, URISyntaxException {
        startAppWithConfigs(globalConfig, studyConfig);

        String expectedAlignmentCmd = getTestTemplate(testFolder, ALIGNMENT_TEMPLATE, context);
        String expectedPostAlignmentCmd = getTestTemplate(testFolder, POST_ALIGNMENT_TEMPLATE, context);
        String expectedQcSummaryCmd = getTestTemplate(testFolder, QC_SUMMARY_TEMPLATE, context);

        String actualAlignmentCmd = getCmd(ALIGNMENT_SH_FILE).trim();
        String actualPostAlignmentCmd = getCmd(POST_ALIGNMENT_SH_FILE).trim();
        String actualQcSummaryCmd = getCmd(QC_SUMMARY_SH_FILE).trim();

        assertEquals(expectedAlignmentCmd, actualAlignmentCmd);
        assertEquals(expectedPostAlignmentCmd, actualPostAlignmentCmd);
        assertEquals(expectedQcSummaryCmd, actualQcSummaryCmd);
        assertFalse(actualAlignmentCmd.contains(NULL));
        assertFalse(actualPostAlignmentCmd.contains(NULL));
        assertFalse(actualQcSummaryCmd.contains(NULL));
    }

    @ParameterizedTest
    @MethodSource("initConfigsTestPostAlignment")
    public void testPostAlignment(String globalConfig, String studyConfig, String folder)
            throws IOException, URISyntaxException {
        startAppWithConfigs(globalConfig, studyConfig);

        String expectedPostAlignmentCmd = getTestTemplate(folder, POST_ALIGNMENT_TEMPLATE, context);
        String actualPostAlignmentCmd = getCmd(POST_ALIGNMENT_SH_FILE).trim();
        assertEquals(expectedPostAlignmentCmd, actualPostAlignmentCmd);
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initConfigsTestPostAlignment() {
        return Stream.of(
                Arguments.of(G_SINGLE_ALL_TASKS, SINGLE_STUDY_CONFIG,
                        "dnaAmpliconVarFastq/testControlSampleAllTasksXenomeNo"),
                Arguments.of(G_PAIRED_ALL_TASKS, S_CONTROL_SAMPLE_NOT_NA_FOR_PA,
                        "dnaAmpliconVarFastq/testControlSampleNotNAAllTasksXenomeNo")
        );
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initControlSampleAllTasks() {
        return Stream.of(
                Arguments.of("freebayes", ALL_TASKS_FREEBAYES),
                Arguments.of("gatkHaplotypeCaller", ALL_TASKS_GATK_HAPLOTYPE_CALLER),
                Arguments.of("lofreq", ALL_TASKS_LOFREQ),
                Arguments.of("mutect1", ALL_TASKS_MUTECT1),
                Arguments.of("scalpel", ALL_TASKS_SCALPEL),
                Arguments.of("strelka2", ALL_TASKS_STRELKA2),
                Arguments.of("vardict", ALL_TASKS_VARDICT)
        );
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initConfigsTestAlignmentAndMergeMutation() {
        return Stream.of(
                Arguments.of(G_SINGLE_ALL_TASKS, SINGLE_STUDY_CONFIG,
                        "dnaAmpliconVarFastq/testControlSampleAllTasksXenomeNo"),
                Arguments.of(G_PAIRED_ALL_TASKS, S_CONTROL_SAMPLE_NOT_NA,
                        "dnaAmpliconVarFastq/testControlSampleNotNAAllTasksXenomeNo")
        );
    }

    private String getTestTemplate(String folder, String task, Context context) {
        return TEMPLATE_ENGINE.process(format("%s/%s", folder, task), context).trim();
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initTestFolderAndConfigs() {
        return Stream.of(
                Arguments.of("dnaAmpliconVarFastq/testPairedPicardAbraGatk",
                        "DnaAmpliconVarFastq/gAbraGatkPicardPaired.txt", PAIRED_STUDY_CONFIG),
                Arguments.of("dnaAmpliconVarFastq/testPairedXenomeYesSeqpurgeBwa",
                        "DnaAmpliconVarFastq/gPairedSeqpurgeBwaXenomeYes.txt", PAIRED_STUDY_CONFIG),
                Arguments.of("dnaAmpliconVarFastq/testPairedXenomeYesSeqpurgeNovoalign",
                        "DnaAmpliconVarFastq/gPairedSeqpurgeNovoalignXenomeYes.txt", PAIRED_STUDY_CONFIG),
                Arguments.of("dnaAmpliconVarFastq/testPairedXenomeYesTrimmomaticBwa",
                        "DnaAmpliconVarFastq/gPairedTrimmomaticBwaXenomeYes.txt", PAIRED_STUDY_CONFIG),
                Arguments.of("dnaAmpliconVarFastq/testSinglePicardAbraGatk",
                        "DnaAmpliconVarFastq/gAbraGatkPicardSingle.txt", SINGLE_STUDY_CONFIG),
                Arguments.of("dnaAmpliconVarFastq/testSingleXenomeYesTrimmomaticBwa",
                        "DnaAmpliconVarFastq/gSingleTrimmomaticBwaYes.txt", SINGLE_STUDY_CONFIG),
                Arguments.of("dnaAmpliconVarFastq/testSingleXenomeYesTrimmomaticNovoalign",
                        "DnaAmpliconVarFastq/gSingleTrimmomaticNovoalignYes.txt", SINGLE_STUDY_CONFIG)
        );
    }
}
