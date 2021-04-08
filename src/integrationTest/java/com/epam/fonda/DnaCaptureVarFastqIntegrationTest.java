/*
 * Copyright 2017-2021 Sanofi and EPAM Systems, Inc. (https://www.epam.com/)
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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DnaCaptureVarFastqIntegrationTest extends AbstractIntegrationTest {
    private static final String DNA_CAPTURE_VAR_FASTQ_DIR = "DnaCaptureVarFastq";
    private static final String OUTPUT_SH_FILES_DIR = "output/sh_files";
    private static final String STUDY_CONFIG_SINGLE = "sSingle.txt";
    private static final String STUDY_CONFIG_PAIRED = "sPaired.txt";
    private static final String ALL_TASKS_PAIRED_TEMPLATES = "AllTasksPaired";
    private static final String ALL_TASKS_SINGLE_TEMPLATES = "AllTasksSingle";
    private static final String BWA_PICARD_QC_TARGET_SINGLE = "BwaPicardQcTargetSingle";
    private static final String BWA_PICARD_QC_ABRA_GATK_PAIRED_TEMPLATES = "BwaPicardQcAbraGatkPaired";
    private static final String BWA_PICARD_QC_ABRA_GATK_SINGLE_TEMPLATES = "BwaPicardQcAbraGatkSingle";
    private static final String
            BWA_VARDICT_MUTECT1_STRELKA2_GATK_HAPOTYPECALLER_SCALPEL_LOFREQ_FREEBAYES_TEMPLATES =
            "BwaVardictMutect1Strelka2GatkHaplotypeCallerScalpelLofreqFreebayesPaired";
    private static final String TRIMMOMATIC_NOVOALIGN_SINGLE_TEMPLATES = "TrimmomaticNovoalignSingle";
    private static final String XENOME_SEQPURGE_BWA_PAIRED_TEMPLATES = "XenomeSeqpurgeBwaPaired";

    @ParameterizedTest
    @MethodSource({"initParametersSingle", "initParametersPaired", "initParameters"})
    void testDnaCaptureVarFastq(String gConfigPath, String sConfigPath, String outputShFile, String templatePath)
            throws IOException, URISyntaxException {
        startAppWithConfigs(gConfigPath, sConfigPath);
        String expectedCmd = TEMPLATE_ENGINE.process(templatePath, context);
        assertEquals(expectedCmd.trim(), getCmd(outputShFile).trim());
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initParametersSingle() {
        return Stream.of(
                Arguments.of(
                        format("%s/gTrimmomaticNovoalignSingle.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/%s", DNA_CAPTURE_VAR_FASTQ_DIR, STUDY_CONFIG_SINGLE),
                        format("%s/DnaCaptureVar_Fastq_alignment_for_GA5_1_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_alignment_for_GA5_1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, TRIMMOMATIC_NOVOALIGN_SINGLE_TEMPLATES)),
                Arguments.of(
                        format("%s/gTrimmomaticNovoalignSingle.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/%s", DNA_CAPTURE_VAR_FASTQ_DIR, STUDY_CONFIG_SINGLE),
                        format("%s/DnaCaptureVar_Fastq_postalignment_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_postalignment_for_GA5_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, TRIMMOMATIC_NOVOALIGN_SINGLE_TEMPLATES)),
                Arguments.of(
                        format("%s/gBwaPicardQcAbraGatkSingle.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/%s", DNA_CAPTURE_VAR_FASTQ_DIR, STUDY_CONFIG_SINGLE),
                        format("%s/DnaCaptureVar_Fastq_alignment_for_GA5_1_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_alignment_for_GA5_1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, BWA_PICARD_QC_ABRA_GATK_SINGLE_TEMPLATES)),
                Arguments.of(
                        format("%s/gBwaPicardQcAbraGatkSingle.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/%s", DNA_CAPTURE_VAR_FASTQ_DIR, STUDY_CONFIG_SINGLE),
                        format("%s/DnaCaptureVar_Fastq_postalignment_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_postalignment_for_GA5_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, BWA_PICARD_QC_ABRA_GATK_SINGLE_TEMPLATES)),
                Arguments.of(
                        format("%s/gBwaPicardQcAbraGatkSingle.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/%s", DNA_CAPTURE_VAR_FASTQ_DIR, STUDY_CONFIG_SINGLE),
                        format("%s/DnaCaptureVar_Fastq_qcsummary_for_cohort_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_qcsummary_for_cohort_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, BWA_PICARD_QC_ABRA_GATK_SINGLE_TEMPLATES)),
                Arguments.of(
                        format("%s/gSingleAllTasks.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sSingleNotTumorOrCase.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_alignment_for_GA5_1_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_alignment_for_GA5_1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, ALL_TASKS_SINGLE_TEMPLATES)),
                Arguments.of(
                        format("%s/gSingleAllTasks.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sSingleNotTumorOrCase.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_mergeMutation_for_cohort_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_mergeMutation_for_cohort_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, ALL_TASKS_SINGLE_TEMPLATES)),
                Arguments.of(
                        format("%s/gSingleAllTasks.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sSingleNotTumorOrCase.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_postalignment_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_postalignment_for_GA5_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, ALL_TASKS_SINGLE_TEMPLATES)),
                Arguments.of(
                        format("%s/gBwaPicardQcTargetSingle.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sSingleTarget.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_alignment_for_GA5_1_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_alignment_for_GA5_1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, BWA_PICARD_QC_TARGET_SINGLE)),
                Arguments.of(
                        format("%s/gBwaPicardQcTargetSingle.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sSingleTarget.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_postalignment_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_postalignment_for_GA5_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, BWA_PICARD_QC_TARGET_SINGLE)),
                Arguments.of(
                        format("%s/gBwaPicardQcTargetSingle.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sSingleTarget.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_qcsummary_for_cohort_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_qcsummary_for_cohort_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, BWA_PICARD_QC_TARGET_SINGLE))
        );
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initParameters() {
        return Stream.of(
                Arguments.of(
                        format("%s/gXenomeSeqpurgeBwaPaired.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/%s", DNA_CAPTURE_VAR_FASTQ_DIR, STUDY_CONFIG_PAIRED),
                        format("%s/DnaCaptureVar_Fastq_alignment_for_GA5_1_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_alignment_for_GA5_1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, XENOME_SEQPURGE_BWA_PAIRED_TEMPLATES)),
                Arguments.of(
                        format("%s/gXenomeSeqpurgeBwaPaired.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/%s", DNA_CAPTURE_VAR_FASTQ_DIR, STUDY_CONFIG_PAIRED),
                        format("%s/DnaCaptureVar_Fastq_postalignment_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_postalignment_for_GA5_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, XENOME_SEQPURGE_BWA_PAIRED_TEMPLATES)),
                Arguments.of(
                        format("%s/gBwaPicardQcAbraGatkPaired.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/%s", DNA_CAPTURE_VAR_FASTQ_DIR, STUDY_CONFIG_PAIRED),
                        format("%s/DnaCaptureVar_Fastq_alignment_for_GA5_1_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_alignment_for_GA5_1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, BWA_PICARD_QC_ABRA_GATK_PAIRED_TEMPLATES)),
                Arguments.of(
                        format("%s/gBwaPicardQcAbraGatkPaired.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/%s", DNA_CAPTURE_VAR_FASTQ_DIR, STUDY_CONFIG_PAIRED),
                        format("%s/DnaCaptureVar_Fastq_postalignment_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_postalignment_for_GA5_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, BWA_PICARD_QC_ABRA_GATK_PAIRED_TEMPLATES)),
                Arguments.of(
                        format("%s/gBwaPicardQcAbraGatkPaired.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/%s", DNA_CAPTURE_VAR_FASTQ_DIR, STUDY_CONFIG_PAIRED),
                        format("%s/DnaCaptureVar_Fastq_qcsummary_for_cohort_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_qcsummary_for_cohort_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, BWA_PICARD_QC_ABRA_GATK_PAIRED_TEMPLATES))
        );
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initParametersPaired() {
        return Stream.of(
                Arguments.of(
                        format("%s/gBwaVardictMutect1Strelka2GatkHaplotypeCallerScalpelLofreqFreebayesPaired.txt",
                                DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedCaseNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_alignment_for_smv1_1_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_alignment_for_smv1_1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR,
                                BWA_VARDICT_MUTECT1_STRELKA2_GATK_HAPOTYPECALLER_SCALPEL_LOFREQ_FREEBAYES_TEMPLATES)),
                Arguments.of(
                        format("%s/gBwaVardictMutect1Strelka2GatkHaplotypeCallerScalpelLofreqFreebayesPaired.txt",
                                DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedCaseNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_freebayes_for_smv1_analysis.sh",
                                OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_freebayes_for_smv1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR,
                                BWA_VARDICT_MUTECT1_STRELKA2_GATK_HAPOTYPECALLER_SCALPEL_LOFREQ_FREEBAYES_TEMPLATES)),
                Arguments.of(
                        format("%s/gBwaVardictMutect1Strelka2GatkHaplotypeCallerScalpelLofreqFreebayesPaired.txt",
                                DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedCaseNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_gatkHaplotypeCaller_for_smv1_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_gatkHaplotypeCaller_for_smv1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR,
                                BWA_VARDICT_MUTECT1_STRELKA2_GATK_HAPOTYPECALLER_SCALPEL_LOFREQ_FREEBAYES_TEMPLATES)),
                Arguments.of(
                        format("%s/gBwaVardictMutect1Strelka2GatkHaplotypeCallerScalpelLofreqFreebayesPaired.txt",
                                DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedCaseNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_lofreq_for_smv1_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_lofreq_for_smv1_analysis_template", DNA_CAPTURE_VAR_FASTQ_DIR,
                                BWA_VARDICT_MUTECT1_STRELKA2_GATK_HAPOTYPECALLER_SCALPEL_LOFREQ_FREEBAYES_TEMPLATES)),
                Arguments.of(
                        format("%s/gBwaVardictMutect1Strelka2GatkHaplotypeCallerScalpelLofreqFreebayesPaired.txt",
                                DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedCaseNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_mergeMutation_for_cohort_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_mergeMutation_for_cohort_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR,
                                BWA_VARDICT_MUTECT1_STRELKA2_GATK_HAPOTYPECALLER_SCALPEL_LOFREQ_FREEBAYES_TEMPLATES)),
                Arguments.of(
                        format("%s/gBwaVardictMutect1Strelka2GatkHaplotypeCallerScalpelLofreqFreebayesPaired.txt",
                                DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedCaseNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_mutect1_for_smv1_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_mutect1_for_smv1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR,
                                BWA_VARDICT_MUTECT1_STRELKA2_GATK_HAPOTYPECALLER_SCALPEL_LOFREQ_FREEBAYES_TEMPLATES)),
                Arguments.of(
                        format("%s/gBwaVardictMutect1Strelka2GatkHaplotypeCallerScalpelLofreqFreebayesPaired.txt",
                                DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedCaseNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_postalignment_for_smv1_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_postalignment_for_smv1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR,
                                BWA_VARDICT_MUTECT1_STRELKA2_GATK_HAPOTYPECALLER_SCALPEL_LOFREQ_FREEBAYES_TEMPLATES)),
                Arguments.of(
                        format("%s/gBwaVardictMutect1Strelka2GatkHaplotypeCallerScalpelLofreqFreebayesPaired.txt",
                                DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedCaseNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_scalpel_for_smv1_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_scalpel_for_smv1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR,
                                BWA_VARDICT_MUTECT1_STRELKA2_GATK_HAPOTYPECALLER_SCALPEL_LOFREQ_FREEBAYES_TEMPLATES)),
                Arguments.of(
                        format("%s/gBwaVardictMutect1Strelka2GatkHaplotypeCallerScalpelLofreqFreebayesPaired.txt",
                                DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedCaseNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_strelka2_for_smv1_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_strelka2_for_smv1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR,
                                BWA_VARDICT_MUTECT1_STRELKA2_GATK_HAPOTYPECALLER_SCALPEL_LOFREQ_FREEBAYES_TEMPLATES)),
                Arguments.of(
                        format("%s/gBwaVardictMutect1Strelka2GatkHaplotypeCallerScalpelLofreqFreebayesPaired.txt",
                                DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedCaseNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_vardict_for_smv1_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_vardict_for_smv1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR,
                                BWA_VARDICT_MUTECT1_STRELKA2_GATK_HAPOTYPECALLER_SCALPEL_LOFREQ_FREEBAYES_TEMPLATES)),
                Arguments.of(
                        format("%s/gPairedAllTasks.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedControlSampleNotNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_alignment_for_GA5_1_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_alignment_for_GA5_1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, ALL_TASKS_PAIRED_TEMPLATES)),
                Arguments.of(
                        format("%s/gPairedAllTasks.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedControlSampleNotNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_contEst_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_contEst_for_GA5_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, ALL_TASKS_PAIRED_TEMPLATES)),
                Arguments.of(
                        format("%s/gPairedAllTasks.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedControlSampleNotNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_exomecnv_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_exomecnv_for_GA5_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, ALL_TASKS_PAIRED_TEMPLATES)),
                Arguments.of(
                        format("%s/gPairedAllTasks.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedControlSampleNotNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_lofreq_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_lofreq_for_GA5_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, ALL_TASKS_PAIRED_TEMPLATES)),
                Arguments.of(
                        format("%s/gPairedAllTasks.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedControlSampleNotNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_mergeMutation_for_cohort_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_mergeMutation_for_cohort_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, ALL_TASKS_PAIRED_TEMPLATES)),
                Arguments.of(
                        format("%s/gPairedAllTasks.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedControlSampleNotNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_mutect2_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_mutect2_for_GA5_analysis_template", DNA_CAPTURE_VAR_FASTQ_DIR,
                                ALL_TASKS_PAIRED_TEMPLATES)),
                Arguments.of(
                        format("%s/gPairedAllTasks.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedControlSampleNotNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_postalignment_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_postalignment_for_GA5_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, ALL_TASKS_PAIRED_TEMPLATES)),
                Arguments.of(
                        format("%s/gPairedAllTasks.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedControlSampleNotNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_scalpel_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_scalpel_for_GA5_analysis_template", DNA_CAPTURE_VAR_FASTQ_DIR,
                                ALL_TASKS_PAIRED_TEMPLATES)),
                Arguments.of(
                        format("%s/gPairedAllTasks.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedControlSampleNotNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_sequenza_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_sequenza_for_GA5_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, ALL_TASKS_PAIRED_TEMPLATES)),
                Arguments.of(
                        format("%s/gPairedAllTasks.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedControlSampleNotNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_strelka2_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_strelka2_for_GA5_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, ALL_TASKS_PAIRED_TEMPLATES)),
                Arguments.of(
                        format("%s/gPairedAllTasks.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedControlSampleNotNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_vardict_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_vardict_for_GA5_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, ALL_TASKS_PAIRED_TEMPLATES))
        );
    }
}
