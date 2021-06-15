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

import com.epam.fonda.entity.configuration.orchestrator.MasterScript;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;


import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DnaCaptureVarFastqIntegrationTest extends AbstractIntegrationTest {

    private static final String DNA_CAPTURE_VAR_FASTQ_DIR = "DnaCaptureVarFastq";
    private static final String MASTER_TEMPLATE_PATH = "master_template_test";
    private static final String OUTPUT_SH_FILES_DIR = "output/sh_files";
    private static final String OUTPUT_FILE_MASTER = format("%s/master.sh", OUTPUT_SH_FILES_DIR);
    private static final String STUDY_CONFIG_SINGLE = "sSingle.txt";
    private static final String STUDY_CONFIG_PAIRED = "sPaired.txt";
    private static final String ALL_TASKS_PAIRED_TEMPLATES = "AllTasksPaired";
    private static final String ALL_TASKS_SINGLE_TEMPLATES = "AllTasksSingle";
    private static final String BWA_PICARD_QC_TARGET_SINGLE = "BwaPicardQcTargetSingle";
    private static final String BWA_PICARD_QC_ABRA_GATK_PAIRED_TEMPLATES = "BwaPicardQcAbraGatkPaired";
    private static final String BWA_PICARD_QC_ABRA_GATK_SINGLE_TEMPLATES = "BwaPicardQcAbraGatkSingle";
    private static final String BWA_VARDICT_MUTECT1_STRELKA2_GATK_HAPOTYPECALLER_SCALPEL_LOFREQ_FREEBAYES_TEMPLATES =
            "BwaVardictMutect1Strelka2GatkHaplotypeCallerScalpelLofreqFreebayesPaired";
    private static final String TRIMMOMATIC_NOVOALIGN_SINGLE_TEMPLATES = "TrimmomaticNovoalignSingle";
    private static final String XENOME_SEQPURGE_BWA_PAIRED_TEMPLATES = "XenomeSeqpurgeBwaPaired";
    private static final String OUTPUT_DIR_ROOT = "build/resources/integrationTest/";

    private static final String EXPECTED_SCRIPT_START =
            format("%s%s/DnaCaptureVar_Fastq", OUTPUT_DIR_ROOT, OUTPUT_SH_FILES_DIR);
    private static final String SAMPLE_NAME = "GA5/";
    
    private static final String FOR_GA5 = "_for_GA5_analysis.sh";
    private static final String FOR_GA5_1 = "_for_GA5_1_analysis.sh";    
    private static final String FOR_SMV1 = "_for_smv1_analysis.sh";
    private static final String FOR_SMV1_1 = "_for_smv1_1_analysis.sh";
    private static final String FOR_COHORT_ANALYSIS = "_for_cohort_analysis.sh";
    private static final String ALIGNMENT = "_alignment";
    private static final String POSTALIGNMENT = "_postalignment";
    private static final String VARDICT = "_vardict";
    private static final String GATK_HAPLOTYPE_CALLER = "_gatkHaplotypeCaller";
    private static final String CONT_EST = "_contEst";
    private static final String STRELKA2 = "_strelka2";
    private static final String MUTECT1 = "_mutect1";
    private static final String MUTECT2 = "_mutect2";
    private static final String SCALPEL = "_scalpel";
    private static final String LOFREQ = "_lofreq";
    private static final String FREEBAYES = "_freebayes";
    private static final String SEQUENZA = "_sequenza";
    private static final String EXOMECNV = "_exomecnv";
    private static final String QCSUMMARY = "_qcsummary";
    private static final String MERGE_MUTATION = "_mergeMutation";
    private static final String INDENT = "[ ]{4,}";

    @ParameterizedTest(name = "{2}-test")
    @MethodSource({"initParametersSingle", "initParametersPaired", "initParameters"})
    void testDnaCaptureVarFastq(final String gConfigPath, final String sConfigPath, final String outputShFile,
                                final String templatePath)
            throws IOException, URISyntaxException {
        startAppWithConfigs(gConfigPath, sConfigPath);
        final String expectedCmd = TEMPLATE_ENGINE.process(templatePath, context);
        assertEquals(expectedCmd.trim(), getCmd(outputShFile).trim());
    }

    @ParameterizedTest(name = "{3}-test")
    @MethodSource({"initParametersSingle", "initParametersPaired", "initParameters"})
    void testDnaCaptureVarFastqMaster(final String gConfigPath, final String sConfigPath, final String outputShFile,
                                      final String templatePath,
                                      final String[] expectedBaseScript,
                                      final String[] expectedSecondScript,
                                      final String postProcessScript)
            throws IOException, URISyntaxException {
        startAppWithConfigs(gConfigPath, sConfigPath, new String[] { "-master" });
        final String expectedCmd = TEMPLATE_ENGINE.process(templatePath, context);
        assertEquals(expectedCmd.trim(), getCmd(outputShFile).trim());

        final String expectedMasterScript = TEMPLATE_ENGINE.process(
                MASTER_TEMPLATE_PATH,
                getContextForMaster(context, expectedBaseScript, expectedSecondScript, postProcessScript)
        );
        assertEquals(trimNotImportant(expectedMasterScript), trimNotImportant(getCmd(OUTPUT_FILE_MASTER)));
    }

    private String trimNotImportant(final String str){
        return str.trim()
                .replaceAll(INDENT, "")
                .replaceAll(" \\r", "")
                .replaceAll("\\r", "");
    }

    private Context getContextForMaster(final Context context,
                                        final String[] expectedBaseScript, final String[] expectedSecondScript,
                                        final String postProcessScript) {
        context.setVariable(
                "samplesProcessScripts", getScripts(expectedBaseScript, expectedSecondScript)
        );
        context.setVariable("postProcessScript", postProcessScript);
        if (StringUtils.isBlank(postProcessScript)) {
            context.setVariable("hasPostProcess", false);
        }
        return context;
    }

    private List<MasterScript.SampleScripts> getScripts(final String[] expectedBaseScript,
                                                        final String[] expectedSecondScript) {
        List<MasterScript.SampleScripts> alignmentScripts = new LinkedList<>();
        alignmentScripts.add(new MasterScript.SampleScripts(
                Arrays.asList(expectedBaseScript),
                Arrays.asList(expectedSecondScript)
        ));
        return alignmentScripts;
    }

    @Test
    public void testCreateDnaCaptureVarFastqSpecificDirExpressionFastqToolset() {
        startAppWithConfigs(
                format("%s/gTrimmomaticNovoalignSingle.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                format("%s%s%s", DNA_CAPTURE_VAR_FASTQ_DIR, "/", STUDY_CONFIG_SINGLE)
        );
        assertAll(
            () -> assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + "sh_files").exists()),
            () -> assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + "log_files").exists()),
            () -> assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + "err_files").exists()),
            () -> assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + SAMPLE_NAME).exists()),
            () -> assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + SAMPLE_NAME + "bam").exists()),
            () -> assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + SAMPLE_NAME + "fastq").exists()),
            () -> assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + SAMPLE_NAME + "qc").exists()),
            () -> assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + SAMPLE_NAME + "tmp").exists())
        );
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initParametersSingle() {
        return Stream.of(
                Arguments.of(
                        format("%s/gTrimmomaticNovoalignSingle.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/%s", DNA_CAPTURE_VAR_FASTQ_DIR, STUDY_CONFIG_SINGLE),
                        format("%s/DnaCaptureVar_Fastq_alignment_for_GA5_1_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_alignment_for_GA5_1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, TRIMMOMATIC_NOVOALIGN_SINGLE_TEMPLATES),
                        new String[] { format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_GA5_1) },
                        new String[] { format("%s%s%s &\n     ", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_GA5) },
                        null),
                Arguments.of(
                        format("%s/gTrimmomaticNovoalignSingle.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/%s", DNA_CAPTURE_VAR_FASTQ_DIR, STUDY_CONFIG_SINGLE),
                        format("%s/DnaCaptureVar_Fastq_postalignment_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_postalignment_for_GA5_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, TRIMMOMATIC_NOVOALIGN_SINGLE_TEMPLATES),
                        new String[] { format("%s%s%s && \\ ", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_GA5_1) },
                        new String[] { format("%s%s%s &\n     ", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_GA5) },
                        null),
                Arguments.of(
                        format("%s/gBwaPicardQcAbraGatkSingle.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/%s", DNA_CAPTURE_VAR_FASTQ_DIR, STUDY_CONFIG_SINGLE),
                        format("%s/DnaCaptureVar_Fastq_alignment_for_GA5_1_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_alignment_for_GA5_1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, BWA_PICARD_QC_ABRA_GATK_SINGLE_TEMPLATES),
                        new String[] { format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_GA5_1) },
                        new String[] { format("%s%s%s &\n     ", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_GA5) },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, QCSUMMARY, FOR_COHORT_ANALYSIS)),
                Arguments.of(
                        format("%s/gBwaPicardQcAbraGatkSingle.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/%s", DNA_CAPTURE_VAR_FASTQ_DIR, STUDY_CONFIG_SINGLE),
                        format("%s/DnaCaptureVar_Fastq_postalignment_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_postalignment_for_GA5_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, BWA_PICARD_QC_ABRA_GATK_SINGLE_TEMPLATES),
                        new String[] { format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_GA5_1) },
                        new String[] { format("%s%s%s &\n     ", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_GA5) },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, QCSUMMARY, FOR_COHORT_ANALYSIS)),
                Arguments.of(
                        format("%s/gBwaPicardQcAbraGatkSingle.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/%s", DNA_CAPTURE_VAR_FASTQ_DIR, STUDY_CONFIG_SINGLE),
                        format("%s/DnaCaptureVar_Fastq_qcsummary_for_cohort_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_qcsummary_for_cohort_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, BWA_PICARD_QC_ABRA_GATK_SINGLE_TEMPLATES),
                        new String[] { format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_GA5_1) },
                        new String[] { format("%s%s%s &\n     ", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_GA5) },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, QCSUMMARY, FOR_COHORT_ANALYSIS)),
                Arguments.of(
                        format("%s/gSingleAllTasks.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sSingleNotTumorOrCase.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_alignment_for_GA5_1_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_alignment_for_GA5_1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, ALL_TASKS_SINGLE_TEMPLATES),
                        new String[] { format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_GA5_1) },
                        new String[] { format("%s%s%s &\n     ", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_GA5) },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, MERGE_MUTATION, FOR_COHORT_ANALYSIS)),
                Arguments.of(
                        format("%s/gSingleAllTasks.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sSingleNotTumorOrCase.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_mergeMutation_for_cohort_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_mergeMutation_for_cohort_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, ALL_TASKS_SINGLE_TEMPLATES),
                        new String[] { format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_GA5_1) },
                        new String[] { format("%s%s%s &\n     ", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_GA5) },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, MERGE_MUTATION, FOR_COHORT_ANALYSIS)),
                Arguments.of(
                        format("%s/gSingleAllTasks.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sSingleNotTumorOrCase.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_postalignment_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_postalignment_for_GA5_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, ALL_TASKS_SINGLE_TEMPLATES),
                        new String[] { format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_GA5_1) },
                        new String[] { format("%s%s%s &\n     ", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_GA5) },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, MERGE_MUTATION, FOR_COHORT_ANALYSIS)),
                Arguments.of(
                        format("%s/gBwaPicardQcTargetSingle.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sSingleTarget.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_alignment_for_GA5_1_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_alignment_for_GA5_1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, BWA_PICARD_QC_TARGET_SINGLE),
                        new String[] { format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_GA5_1) },
                        new String[] { format("%s%s%s &\n     ", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_GA5) },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, QCSUMMARY, FOR_COHORT_ANALYSIS)),
                Arguments.of(
                        format("%s/gBwaPicardQcTargetSingle.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sSingleTarget.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_postalignment_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_postalignment_for_GA5_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, BWA_PICARD_QC_TARGET_SINGLE),
                        new String[] { format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_GA5_1) },
                        new String[] { format("%s%s%s &\n     ", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_GA5) },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, QCSUMMARY, FOR_COHORT_ANALYSIS)),
                Arguments.of(
                        format("%s/gBwaPicardQcTargetSingle.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sSingleTarget.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_qcsummary_for_cohort_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_qcsummary_for_cohort_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, BWA_PICARD_QC_TARGET_SINGLE),
                        new String[] { format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_GA5_1) },
                        new String[] { format("%s%s%s &\n     ", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_GA5) },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, QCSUMMARY, FOR_COHORT_ANALYSIS))
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
                                DNA_CAPTURE_VAR_FASTQ_DIR, XENOME_SEQPURGE_BWA_PAIRED_TEMPLATES),
                        new String[] { format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_GA5_1) },
                        new String[] { format("%s%s%s &\n     ", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_GA5) },
                        null),
                Arguments.of(
                        format("%s/gXenomeSeqpurgeBwaPaired.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/%s", DNA_CAPTURE_VAR_FASTQ_DIR, STUDY_CONFIG_PAIRED),
                        format("%s/DnaCaptureVar_Fastq_postalignment_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_postalignment_for_GA5_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, XENOME_SEQPURGE_BWA_PAIRED_TEMPLATES),
                        new String[] { format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_GA5_1) },
                        new String[] { format("%s%s%s &\n     ", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_GA5) },
                        null),
                Arguments.of(
                        format("%s/gBwaPicardQcAbraGatkPaired.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/%s", DNA_CAPTURE_VAR_FASTQ_DIR, STUDY_CONFIG_PAIRED),
                        format("%s/DnaCaptureVar_Fastq_alignment_for_GA5_1_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_alignment_for_GA5_1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, BWA_PICARD_QC_ABRA_GATK_PAIRED_TEMPLATES),
                        new String[] { format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_GA5_1) },
                        new String[] { format("%s%s%s &\n     ", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_GA5) },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, QCSUMMARY, FOR_COHORT_ANALYSIS)),
                Arguments.of(
                        format("%s/gBwaPicardQcAbraGatkPaired.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/%s", DNA_CAPTURE_VAR_FASTQ_DIR, STUDY_CONFIG_PAIRED),
                        format("%s/DnaCaptureVar_Fastq_postalignment_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_postalignment_for_GA5_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, BWA_PICARD_QC_ABRA_GATK_PAIRED_TEMPLATES),
                        new String[] { format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_GA5_1) },
                        new String[] { format("%s%s%s &\n     ", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_GA5) },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, QCSUMMARY, FOR_COHORT_ANALYSIS)),
                Arguments.of(
                        format("%s/gBwaPicardQcAbraGatkPaired.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/%s", DNA_CAPTURE_VAR_FASTQ_DIR, STUDY_CONFIG_PAIRED),
                        format("%s/DnaCaptureVar_Fastq_qcsummary_for_cohort_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_qcsummary_for_cohort_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, BWA_PICARD_QC_ABRA_GATK_PAIRED_TEMPLATES),
                        new String[] { format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_GA5_1) },
                        new String[] { format("%s%s%s &\n     ", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_GA5) },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, QCSUMMARY, FOR_COHORT_ANALYSIS))
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
                                BWA_VARDICT_MUTECT1_STRELKA2_GATK_HAPOTYPECALLER_SCALPEL_LOFREQ_FREEBAYES_TEMPLATES),
                        new String[]{
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_SMV1_1),
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_SMV1)
                        },
                        new String[]{
                                format("%s%s%s &", EXPECTED_SCRIPT_START, VARDICT, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, GATK_HAPLOTYPE_CALLER, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, STRELKA2, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, MUTECT1, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SCALPEL, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, LOFREQ, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, FREEBAYES, FOR_SMV1)
                        },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, MERGE_MUTATION, FOR_COHORT_ANALYSIS)),
                Arguments.of(
                        format("%s/gBwaVardictMutect1Strelka2GatkHaplotypeCallerScalpelLofreqFreebayesPaired.txt",
                                DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedCaseNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_freebayes_for_smv1_analysis.sh",
                                OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_freebayes_for_smv1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR,
                                BWA_VARDICT_MUTECT1_STRELKA2_GATK_HAPOTYPECALLER_SCALPEL_LOFREQ_FREEBAYES_TEMPLATES),
                        new String[]{
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_SMV1_1),
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_SMV1) },
                        new String[]{
                                format("%s%s%s &", EXPECTED_SCRIPT_START, VARDICT, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, GATK_HAPLOTYPE_CALLER, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, STRELKA2, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, MUTECT1, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SCALPEL, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, LOFREQ, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, FREEBAYES, FOR_SMV1) },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, MERGE_MUTATION, FOR_COHORT_ANALYSIS)),
                Arguments.of(
                        format("%s/gBwaVardictMutect1Strelka2GatkHaplotypeCallerScalpelLofreqFreebayesPaired.txt",
                                DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedCaseNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_gatkHaplotypeCaller_for_smv1_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_gatkHaplotypeCaller_for_smv1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR,
                                BWA_VARDICT_MUTECT1_STRELKA2_GATK_HAPOTYPECALLER_SCALPEL_LOFREQ_FREEBAYES_TEMPLATES),
                        new String[]{
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_SMV1_1),
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_SMV1) },
                        new String[]{
                                format("%s%s%s &", EXPECTED_SCRIPT_START, VARDICT, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, GATK_HAPLOTYPE_CALLER, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, STRELKA2, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, MUTECT1, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SCALPEL, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, LOFREQ, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, FREEBAYES, FOR_SMV1) },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, MERGE_MUTATION, FOR_COHORT_ANALYSIS)),
                Arguments.of(
                        format("%s/gBwaVardictMutect1Strelka2GatkHaplotypeCallerScalpelLofreqFreebayesPaired.txt",
                                DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedCaseNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_lofreq_for_smv1_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_lofreq_for_smv1_analysis_template", DNA_CAPTURE_VAR_FASTQ_DIR,
                                BWA_VARDICT_MUTECT1_STRELKA2_GATK_HAPOTYPECALLER_SCALPEL_LOFREQ_FREEBAYES_TEMPLATES),
                        new String[]{
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_SMV1_1),
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_SMV1) },
                        new String[]{
                                format("%s%s%s &", EXPECTED_SCRIPT_START, VARDICT, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, GATK_HAPLOTYPE_CALLER, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, STRELKA2, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, MUTECT1, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SCALPEL, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, LOFREQ, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, FREEBAYES, FOR_SMV1) },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, MERGE_MUTATION, FOR_COHORT_ANALYSIS)),
                Arguments.of(
                        format("%s/gBwaVardictMutect1Strelka2GatkHaplotypeCallerScalpelLofreqFreebayesPaired.txt",
                                DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedCaseNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_mergeMutation_for_cohort_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_mergeMutation_for_cohort_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR,
                                BWA_VARDICT_MUTECT1_STRELKA2_GATK_HAPOTYPECALLER_SCALPEL_LOFREQ_FREEBAYES_TEMPLATES),
                        new String[]{
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_SMV1_1),
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_SMV1) },
                        new String[]{
                                format("%s%s%s &", EXPECTED_SCRIPT_START, VARDICT, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, GATK_HAPLOTYPE_CALLER, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, STRELKA2, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, MUTECT1, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SCALPEL, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, LOFREQ, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, FREEBAYES, FOR_SMV1) },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, MERGE_MUTATION, FOR_COHORT_ANALYSIS)),
                Arguments.of(
                        format("%s/gBwaVardictMutect1Strelka2GatkHaplotypeCallerScalpelLofreqFreebayesPaired.txt",
                                DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedCaseNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_mutect1_for_smv1_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_mutect1_for_smv1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR,
                                BWA_VARDICT_MUTECT1_STRELKA2_GATK_HAPOTYPECALLER_SCALPEL_LOFREQ_FREEBAYES_TEMPLATES),
                        new String[]{
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_SMV1_1),
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_SMV1) },
                        new String[]{
                                format("%s%s%s &", EXPECTED_SCRIPT_START, VARDICT, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, GATK_HAPLOTYPE_CALLER, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, STRELKA2, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, MUTECT1, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SCALPEL, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, LOFREQ, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, FREEBAYES, FOR_SMV1) },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, MERGE_MUTATION, FOR_COHORT_ANALYSIS)),
                Arguments.of(
                        format("%s/gBwaVardictMutect1Strelka2GatkHaplotypeCallerScalpelLofreqFreebayesPaired.txt",
                                DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedCaseNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_postalignment_for_smv1_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_postalignment_for_smv1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR,
                                BWA_VARDICT_MUTECT1_STRELKA2_GATK_HAPOTYPECALLER_SCALPEL_LOFREQ_FREEBAYES_TEMPLATES),
                        new String[]{
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_SMV1_1),
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_SMV1) },
                        new String[]{
                                format("%s%s%s &", EXPECTED_SCRIPT_START, VARDICT, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, GATK_HAPLOTYPE_CALLER, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, STRELKA2, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, MUTECT1, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SCALPEL, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, LOFREQ, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, FREEBAYES, FOR_SMV1) },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, MERGE_MUTATION, FOR_COHORT_ANALYSIS)),
                Arguments.of(
                        format("%s/gBwaVardictMutect1Strelka2GatkHaplotypeCallerScalpelLofreqFreebayesPaired.txt",
                                DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedCaseNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_scalpel_for_smv1_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_scalpel_for_smv1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR,
                                BWA_VARDICT_MUTECT1_STRELKA2_GATK_HAPOTYPECALLER_SCALPEL_LOFREQ_FREEBAYES_TEMPLATES),
                        new String[]{
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_SMV1_1),
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_SMV1) },
                        new String[]{
                                format("%s%s%s &", EXPECTED_SCRIPT_START, VARDICT, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, GATK_HAPLOTYPE_CALLER, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, STRELKA2, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, MUTECT1, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SCALPEL, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, LOFREQ, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, FREEBAYES, FOR_SMV1) },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, MERGE_MUTATION, FOR_COHORT_ANALYSIS)),
                Arguments.of(
                        format("%s/gBwaVardictMutect1Strelka2GatkHaplotypeCallerScalpelLofreqFreebayesPaired.txt",
                                DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedCaseNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_strelka2_for_smv1_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_strelka2_for_smv1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR,
                                BWA_VARDICT_MUTECT1_STRELKA2_GATK_HAPOTYPECALLER_SCALPEL_LOFREQ_FREEBAYES_TEMPLATES),
                        new String[]{
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_SMV1_1),
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_SMV1) },
                        new String[]{
                                format("%s%s%s &", EXPECTED_SCRIPT_START, VARDICT, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, GATK_HAPLOTYPE_CALLER, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, STRELKA2, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, MUTECT1, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SCALPEL, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, LOFREQ, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, FREEBAYES, FOR_SMV1) },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, MERGE_MUTATION, FOR_COHORT_ANALYSIS)),
                Arguments.of(
                        format("%s/gBwaVardictMutect1Strelka2GatkHaplotypeCallerScalpelLofreqFreebayesPaired.txt",
                                DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedCaseNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_vardict_for_smv1_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_vardict_for_smv1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR,
                                BWA_VARDICT_MUTECT1_STRELKA2_GATK_HAPOTYPECALLER_SCALPEL_LOFREQ_FREEBAYES_TEMPLATES),
                        new String[]{
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_SMV1_1),
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_SMV1) },
                        new String[]{
                                format("%s%s%s &", EXPECTED_SCRIPT_START, VARDICT, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, GATK_HAPLOTYPE_CALLER, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, STRELKA2, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, MUTECT1, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SCALPEL, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, LOFREQ, FOR_SMV1),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, FREEBAYES, FOR_SMV1) },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, MERGE_MUTATION, FOR_COHORT_ANALYSIS)),
                Arguments.of(
                        format("%s/gPairedAllTasks.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedControlSampleNotNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_alignment_for_GA5_1_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_alignment_for_GA5_1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, ALL_TASKS_PAIRED_TEMPLATES),
                        new String[]{
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_GA5_1),
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_GA5) },
                        new String[]{
                                format("%s%s%s &", EXPECTED_SCRIPT_START, VARDICT, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, CONT_EST, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, STRELKA2, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, MUTECT2, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SCALPEL, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, LOFREQ, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SEQUENZA, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, EXOMECNV, FOR_GA5) },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, MERGE_MUTATION, FOR_COHORT_ANALYSIS)),
                Arguments.of(
                        format("%s/gPairedAllTasks.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedControlSampleNotNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_contEst_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_contEst_for_GA5_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, ALL_TASKS_PAIRED_TEMPLATES),
                        new String[]{
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_GA5_1),
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_GA5) },
                        new String[]{
                                format("%s%s%s &", EXPECTED_SCRIPT_START, VARDICT, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, CONT_EST, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, STRELKA2, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, MUTECT2, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SCALPEL, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, LOFREQ, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SEQUENZA, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, EXOMECNV, FOR_GA5) },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, MERGE_MUTATION, FOR_COHORT_ANALYSIS)),
                Arguments.of(
                        format("%s/gPairedAllTasks.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedControlSampleNotNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_exomecnv_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_exomecnv_for_GA5_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, ALL_TASKS_PAIRED_TEMPLATES),
                        new String[]{
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_GA5_1),
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_GA5) },
                        new String[]{
                                format("%s%s%s &", EXPECTED_SCRIPT_START, VARDICT, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, CONT_EST, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, STRELKA2, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, MUTECT2, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SCALPEL, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, LOFREQ, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SEQUENZA, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, EXOMECNV, FOR_GA5) },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, MERGE_MUTATION, FOR_COHORT_ANALYSIS)),
                Arguments.of(
                        format("%s/gPairedAllTasks.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedControlSampleNotNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_lofreq_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_lofreq_for_GA5_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, ALL_TASKS_PAIRED_TEMPLATES),
                        new String[]{
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_GA5_1),
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_GA5) },
                        new String[]{
                                format("%s%s%s &", EXPECTED_SCRIPT_START, VARDICT, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, CONT_EST, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, STRELKA2, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, MUTECT2, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SCALPEL, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, LOFREQ, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SEQUENZA, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, EXOMECNV, FOR_GA5) },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, MERGE_MUTATION, FOR_COHORT_ANALYSIS)),
                Arguments.of(
                        format("%s/gPairedAllTasks.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedControlSampleNotNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_mergeMutation_for_cohort_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_mergeMutation_for_cohort_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, ALL_TASKS_PAIRED_TEMPLATES),
                        new String[]{
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_GA5_1),
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_GA5) },
                        new String[]{
                                format("%s%s%s &", EXPECTED_SCRIPT_START, VARDICT, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, CONT_EST, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, STRELKA2, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, MUTECT2, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SCALPEL, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, LOFREQ, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SEQUENZA, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, EXOMECNV, FOR_GA5) },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, MERGE_MUTATION, FOR_COHORT_ANALYSIS)),
                Arguments.of(
                        format("%s/gPairedAllTasks.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedControlSampleNotNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_mutect2_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_mutect2_for_GA5_analysis_template", DNA_CAPTURE_VAR_FASTQ_DIR,
                                ALL_TASKS_PAIRED_TEMPLATES),
                        new String[]{
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_GA5_1),
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_GA5) },
                        new String[]{
                                format("%s%s%s &", EXPECTED_SCRIPT_START, VARDICT, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, CONT_EST, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, STRELKA2, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, MUTECT2, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SCALPEL, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, LOFREQ, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SEQUENZA, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, EXOMECNV, FOR_GA5) },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, MERGE_MUTATION, FOR_COHORT_ANALYSIS)),
                Arguments.of(
                        format("%s/gPairedAllTasks.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedControlSampleNotNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_postalignment_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_postalignment_for_GA5_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, ALL_TASKS_PAIRED_TEMPLATES),
                        new String[]{
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_GA5_1),
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_GA5) },
                        new String[]{
                                format("%s%s%s &", EXPECTED_SCRIPT_START, VARDICT, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, CONT_EST, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, STRELKA2, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, MUTECT2, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SCALPEL, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, LOFREQ, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SEQUENZA, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, EXOMECNV, FOR_GA5) },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, MERGE_MUTATION, FOR_COHORT_ANALYSIS)),
                Arguments.of(
                        format("%s/gPairedAllTasks.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedControlSampleNotNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_scalpel_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_scalpel_for_GA5_analysis_template", DNA_CAPTURE_VAR_FASTQ_DIR,
                                ALL_TASKS_PAIRED_TEMPLATES),
                        new String[]{
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_GA5_1),
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_GA5) },
                        new String[]{
                                format("%s%s%s &", EXPECTED_SCRIPT_START, VARDICT, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, CONT_EST, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, STRELKA2, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, MUTECT2, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SCALPEL, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, LOFREQ, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SEQUENZA, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, EXOMECNV, FOR_GA5) },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, MERGE_MUTATION, FOR_COHORT_ANALYSIS)),
                Arguments.of(
                        format("%s/gPairedAllTasks.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedControlSampleNotNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_sequenza_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_sequenza_for_GA5_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, ALL_TASKS_PAIRED_TEMPLATES),
                        new String[]{
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_GA5_1),
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_GA5) },
                        new String[]{
                                format("%s%s%s &", EXPECTED_SCRIPT_START, VARDICT, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, CONT_EST, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, STRELKA2, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, MUTECT2, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SCALPEL, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, LOFREQ, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SEQUENZA, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, EXOMECNV, FOR_GA5) },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, MERGE_MUTATION, FOR_COHORT_ANALYSIS)),
                Arguments.of(
                        format("%s/gPairedAllTasks.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedControlSampleNotNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_strelka2_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_strelka2_for_GA5_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, ALL_TASKS_PAIRED_TEMPLATES),
                        new String[]{
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_GA5_1),
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_GA5) },
                        new String[]{
                                format("%s%s%s &", EXPECTED_SCRIPT_START, VARDICT, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, CONT_EST, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, STRELKA2, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, MUTECT2, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SCALPEL, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, LOFREQ, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SEQUENZA, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, EXOMECNV, FOR_GA5) },
                        format("%s%s%s &", EXPECTED_SCRIPT_START, MERGE_MUTATION, FOR_COHORT_ANALYSIS)),
                Arguments.of(
                        format("%s/gPairedAllTasks.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPairedControlSampleNotNA.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/DnaCaptureVar_Fastq_vardict_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/dnaCaptureVar_Fastq_vardict_for_GA5_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR, ALL_TASKS_PAIRED_TEMPLATES),
                        new String[]{
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, ALIGNMENT, FOR_GA5_1),
                                format("%s%s%s && \\", EXPECTED_SCRIPT_START, POSTALIGNMENT, FOR_GA5) },
                        new String[]{
                                format("%s%s%s &", EXPECTED_SCRIPT_START, VARDICT, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, CONT_EST, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, STRELKA2, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, MUTECT2, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SCALPEL, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, LOFREQ, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, SEQUENZA, FOR_GA5),
                                format("%s%s%s &", EXPECTED_SCRIPT_START, EXOMECNV, FOR_GA5)},
                        format("%s%s%s &", EXPECTED_SCRIPT_START, MERGE_MUTATION, FOR_COHORT_ANALYSIS))
        );
    }
}
