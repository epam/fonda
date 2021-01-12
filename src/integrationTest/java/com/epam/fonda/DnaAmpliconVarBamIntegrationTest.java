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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.of;

public class DnaAmpliconVarBamIntegrationTest extends AbstractIntegrationTest {

    private static final String OUTPUT_SH_SUFFIX = "output/sh_files/";
    private static final String BAM_AMPLICON_VAR_BAM_SUFFIX = "DnaAmpliconVarBam/";
    private static final String CONTROL_SAMPLE_NAALL_TASKS_SUFFIX =
            format("%sControlSampleNAAllTasks/", BAM_AMPLICON_VAR_BAM_SUFFIX);
    private static final String CONTROL_SAMPLE_NOT_NAALL_TASKS_SUFFIX =
            format("%sControlSampleNotNAAllTasks/", BAM_AMPLICON_VAR_BAM_SUFFIX);
    private static final String CONTROL_SAMPLE_NOT_NAALL_TASKS_FOR_GA51_SUFFIX =
            format("%sControlSampleNotNAAllTasks/ForGA51/", BAM_AMPLICON_VAR_BAM_SUFFIX);
    private static final String CONTROL_SAMPLE_NOT_NAALL_TASKS_FOR_GA52_SUFFIX =
            format("%sControlSampleNotNAAllTasks/ForGA52/", BAM_AMPLICON_VAR_BAM_SUFFIX);
    private static final String FOR_GA_5_SH_SUFFIX = "for_GA5_analysis.sh";
    private static final String FOR_GA_51_SH_SUFFIX = "for_GA51_analysis.sh";
    private static final String FOR_GA_52_SH_SUFFIX = "for_GA52_analysis.sh";
    private static final String FOR_COHORT_ANALYSIS_SH_SUFFIX = "for_cohort_analysis.sh";
    private static final String FOR_GA_5_TEMPLATE_SUFFIX = "for_GA5_analysis.txt";
    private static final String FOR_GA_51_TEMPLATE_SUFFIX = "for_GA51_analysis.txt";
    private static final String FOR_GA_52_TEMPLATE_SUFFIX = "for_GA52_analysis.txt";
    private static final String FOR_COHORT_ANALYSIS_TEMPLATE_SUFFIX = "for_cohort_analysis.txt";
    private static final String DNA_AMPLICON_VAR_BAM_FREEBAYES =
            "DnaAmpliconVar_Bam_freebayes_";
    private static final String DNA_AMPLICON_VAR_BAM_GATK_HAPLOTYPE_CALLER =
            "DnaAmpliconVar_Bam_gatkHaplotypeCaller_";
    private static final String DNA_AMPLICON_VAR_BAM_LOFREQ =
            "DnaAmpliconVar_Bam_lofreq_";
    private static final String DNA_AMPLICON_VAR_BAM_MUTECT_1 =
            "DnaAmpliconVar_Bam_mutect1_";
    private static final String DNA_AMPLICON_VAR_BAM_SCALPEL =
            "DnaAmpliconVar_Bam_scalpel_";
    private static final String DNA_AMPLICON_VAR_BAM_STRELKA_2 =
            "DnaAmpliconVar_Bam_strelka2_";
    private static final String DNA_AMPLICON_VAR_BAM_VARDICT =
            "DnaAmpliconVar_Bam_vardict_";
    private static final String DNA_AMPLICON_VAR_BAM_VARIANT_DETECTION =
            "DnaAmpliconVar_Bam_variantDetection_";
    private static final String DNA_AMPLICON_VAR_BAM_CONTEST =
            "DnaAmpliconVar_Bam_contEst_";
    private static final String DNA_AMPLICON_VAR_BAM_EXOMECNV =
            "DnaAmpliconVar_Bam_exomecnv_";
    private static final String DNA_AMPLICON_VAR_BAM_MUTECT_2 =
            "DnaAmpliconVar_Bam_mutect2_";
    private static final String DNA_AMPLICON_VAR_BAM_SEQUENZA =
            "DnaAmpliconVar_Bam_sequenza_";
    private static final String DNA_AMPLICON_VAR_BAM_MERGE_MUTATION =
            "DnaAmpliconVar_Bam_mergeMutation_";
    private static final String DNA_AMPLICON_VAR_BAM_G_SINGLE_ALL_TASKS =
            "DnaAmpliconVarBam/gSingleAllTasks.txt";
    private static final String DNA_AMPLICON_VAR_BAM_S_SINGLE =
            "DnaAmpliconVarBam/sSingle.txt";
    private static final String DNA_AMPLICON_VAR_BAM_G_ALL_TASKS_NOT_NA =
            "DnaAmpliconVarBam/gAllTasksSampleNotNA.txt";
    private static final String DNA_AMPLICON_VAR_BAM_S_CONTROL_NOT_NA =
            "DnaAmpliconVarBam/sControlSampleNotNA.txt";

    @ParameterizedTest(name = "{3}-test")
    @MethodSource("initParametersNAAll")
    void testDnaAmpliconVarBamNAAll(final String gConfigPath, final String sConfigPath, final String outputShFile,
                                    final String templatePath) throws IOException, URISyntaxException {
        startAppWithConfigs(gConfigPath, sConfigPath);
        final String expectedCmd = TEMPLATE_ENGINE.process(templatePath, context);
        assertEquals(expectedCmd.trim(), getCmd(outputShFile).trim());
    }

    @ParameterizedTest(name = "{3}-test")
    @MethodSource("initParametersNotNAAll")
    void testDnaAmpliconVarBamNotNAAll(final String gConfigPath, final String sConfigPath, final String outputShFile,
                                       final String templatePath) throws IOException, URISyntaxException {
        startAppWithConfigs(gConfigPath, sConfigPath);
        final String expectedCmd = TEMPLATE_ENGINE.process(templatePath, context);
        assertEquals(expectedCmd.trim(), getCmd(outputShFile).trim());
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initParametersNAAll() {
        return Stream.of(
                of(DNA_AMPLICON_VAR_BAM_G_SINGLE_ALL_TASKS, DNA_AMPLICON_VAR_BAM_S_SINGLE,
                        format("%s%s%s",
                                OUTPUT_SH_SUFFIX, DNA_AMPLICON_VAR_BAM_FREEBAYES, FOR_GA_5_SH_SUFFIX),
                        format("%s%s%s",
                                CONTROL_SAMPLE_NAALL_TASKS_SUFFIX, DNA_AMPLICON_VAR_BAM_FREEBAYES,
                                FOR_GA_5_TEMPLATE_SUFFIX)),
                of(DNA_AMPLICON_VAR_BAM_G_SINGLE_ALL_TASKS, DNA_AMPLICON_VAR_BAM_S_SINGLE,
                        format("%s%s%s",
                                OUTPUT_SH_SUFFIX, DNA_AMPLICON_VAR_BAM_GATK_HAPLOTYPE_CALLER, FOR_GA_5_SH_SUFFIX),
                        format("%s%s%s",
                                CONTROL_SAMPLE_NAALL_TASKS_SUFFIX, DNA_AMPLICON_VAR_BAM_GATK_HAPLOTYPE_CALLER,
                                FOR_GA_5_TEMPLATE_SUFFIX)),
                of(DNA_AMPLICON_VAR_BAM_G_SINGLE_ALL_TASKS, DNA_AMPLICON_VAR_BAM_S_SINGLE,
                        format("%s%s%s",
                                OUTPUT_SH_SUFFIX, DNA_AMPLICON_VAR_BAM_LOFREQ, FOR_GA_5_SH_SUFFIX),
                        format("%s%s%s",
                                CONTROL_SAMPLE_NAALL_TASKS_SUFFIX, DNA_AMPLICON_VAR_BAM_LOFREQ,
                                FOR_GA_5_TEMPLATE_SUFFIX)),
                of(DNA_AMPLICON_VAR_BAM_G_SINGLE_ALL_TASKS, DNA_AMPLICON_VAR_BAM_S_SINGLE,
                        format("%s%s%s",
                                OUTPUT_SH_SUFFIX, DNA_AMPLICON_VAR_BAM_MUTECT_1, FOR_GA_5_SH_SUFFIX),
                        format("%s%s%s",
                                CONTROL_SAMPLE_NAALL_TASKS_SUFFIX, DNA_AMPLICON_VAR_BAM_MUTECT_1,
                                FOR_GA_5_TEMPLATE_SUFFIX)),
                of(DNA_AMPLICON_VAR_BAM_G_SINGLE_ALL_TASKS, DNA_AMPLICON_VAR_BAM_S_SINGLE,
                        format("%s%s%s",
                                OUTPUT_SH_SUFFIX, DNA_AMPLICON_VAR_BAM_SCALPEL, FOR_GA_5_SH_SUFFIX),
                        format("%s%s%s",
                                CONTROL_SAMPLE_NAALL_TASKS_SUFFIX, DNA_AMPLICON_VAR_BAM_SCALPEL,
                                FOR_GA_5_TEMPLATE_SUFFIX)),
                of(DNA_AMPLICON_VAR_BAM_G_SINGLE_ALL_TASKS, DNA_AMPLICON_VAR_BAM_S_SINGLE,
                        format("%s%s%s",
                                OUTPUT_SH_SUFFIX, DNA_AMPLICON_VAR_BAM_STRELKA_2, FOR_GA_5_SH_SUFFIX),
                        format("%s%s%s",
                                CONTROL_SAMPLE_NAALL_TASKS_SUFFIX, DNA_AMPLICON_VAR_BAM_STRELKA_2,
                                FOR_GA_5_TEMPLATE_SUFFIX)),
                of(DNA_AMPLICON_VAR_BAM_G_SINGLE_ALL_TASKS, DNA_AMPLICON_VAR_BAM_S_SINGLE,
                        format("%s%s%s",
                                OUTPUT_SH_SUFFIX, DNA_AMPLICON_VAR_BAM_VARDICT, FOR_GA_5_SH_SUFFIX),
                        format("%s%s%s",
                                CONTROL_SAMPLE_NAALL_TASKS_SUFFIX, DNA_AMPLICON_VAR_BAM_VARDICT,
                                FOR_GA_5_TEMPLATE_SUFFIX)),
                of(DNA_AMPLICON_VAR_BAM_G_SINGLE_ALL_TASKS, DNA_AMPLICON_VAR_BAM_S_SINGLE,
                        format("%s%s%s",
                                OUTPUT_SH_SUFFIX, DNA_AMPLICON_VAR_BAM_VARIANT_DETECTION, FOR_GA_5_SH_SUFFIX),
                        format("%s%s%s",
                                CONTROL_SAMPLE_NAALL_TASKS_SUFFIX, DNA_AMPLICON_VAR_BAM_VARIANT_DETECTION,
                                FOR_GA_5_TEMPLATE_SUFFIX)),
                of(DNA_AMPLICON_VAR_BAM_G_SINGLE_ALL_TASKS, DNA_AMPLICON_VAR_BAM_S_SINGLE,
                        format("%s%s%s",
                                OUTPUT_SH_SUFFIX, DNA_AMPLICON_VAR_BAM_MERGE_MUTATION, FOR_COHORT_ANALYSIS_SH_SUFFIX),
                        format("%s%s%s",
                                CONTROL_SAMPLE_NAALL_TASKS_SUFFIX, DNA_AMPLICON_VAR_BAM_MERGE_MUTATION,
                                FOR_COHORT_ANALYSIS_TEMPLATE_SUFFIX))
        );
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initParametersNotNAAll() {
        return Stream.of(
                of(DNA_AMPLICON_VAR_BAM_G_ALL_TASKS_NOT_NA, DNA_AMPLICON_VAR_BAM_S_CONTROL_NOT_NA,
                        format("%s%s%s",
                                OUTPUT_SH_SUFFIX, DNA_AMPLICON_VAR_BAM_CONTEST, FOR_GA_51_SH_SUFFIX),
                        format("%s%s%s",
                                CONTROL_SAMPLE_NOT_NAALL_TASKS_FOR_GA51_SUFFIX, DNA_AMPLICON_VAR_BAM_CONTEST,
                                FOR_GA_51_TEMPLATE_SUFFIX)),
                of(DNA_AMPLICON_VAR_BAM_G_ALL_TASKS_NOT_NA, DNA_AMPLICON_VAR_BAM_S_CONTROL_NOT_NA,
                        format("%s%s%s",
                                OUTPUT_SH_SUFFIX, DNA_AMPLICON_VAR_BAM_EXOMECNV, FOR_GA_51_SH_SUFFIX),
                        format("%s%s%s",
                                CONTROL_SAMPLE_NOT_NAALL_TASKS_FOR_GA51_SUFFIX, DNA_AMPLICON_VAR_BAM_EXOMECNV,
                                FOR_GA_51_TEMPLATE_SUFFIX)),
                of(DNA_AMPLICON_VAR_BAM_G_ALL_TASKS_NOT_NA, DNA_AMPLICON_VAR_BAM_S_CONTROL_NOT_NA,
                        format("%s%s%s",
                                OUTPUT_SH_SUFFIX, DNA_AMPLICON_VAR_BAM_LOFREQ, FOR_GA_51_SH_SUFFIX),
                        format("%s%s%s",
                                CONTROL_SAMPLE_NOT_NAALL_TASKS_FOR_GA51_SUFFIX, DNA_AMPLICON_VAR_BAM_LOFREQ,
                                FOR_GA_51_TEMPLATE_SUFFIX)),
                of(DNA_AMPLICON_VAR_BAM_G_ALL_TASKS_NOT_NA, DNA_AMPLICON_VAR_BAM_S_CONTROL_NOT_NA,
                        format("%s%s%s",
                                OUTPUT_SH_SUFFIX, DNA_AMPLICON_VAR_BAM_MUTECT_2, FOR_GA_51_SH_SUFFIX),
                        format("%s%s%s",
                                CONTROL_SAMPLE_NOT_NAALL_TASKS_FOR_GA51_SUFFIX, DNA_AMPLICON_VAR_BAM_MUTECT_2,
                                FOR_GA_51_TEMPLATE_SUFFIX)),
                of(DNA_AMPLICON_VAR_BAM_G_ALL_TASKS_NOT_NA, DNA_AMPLICON_VAR_BAM_S_CONTROL_NOT_NA,
                        format("%s%s%s",
                                OUTPUT_SH_SUFFIX, DNA_AMPLICON_VAR_BAM_SCALPEL, FOR_GA_51_SH_SUFFIX),
                        format("%s%s%s",
                                CONTROL_SAMPLE_NOT_NAALL_TASKS_FOR_GA51_SUFFIX, DNA_AMPLICON_VAR_BAM_SCALPEL,
                                FOR_GA_51_TEMPLATE_SUFFIX)),
                of(DNA_AMPLICON_VAR_BAM_G_ALL_TASKS_NOT_NA, DNA_AMPLICON_VAR_BAM_S_CONTROL_NOT_NA,
                        format("%s%s%s",
                                OUTPUT_SH_SUFFIX, DNA_AMPLICON_VAR_BAM_SEQUENZA, FOR_GA_51_SH_SUFFIX),
                        format("%s%s%s",
                                CONTROL_SAMPLE_NOT_NAALL_TASKS_FOR_GA51_SUFFIX, DNA_AMPLICON_VAR_BAM_SEQUENZA,
                                FOR_GA_51_TEMPLATE_SUFFIX)),
                of(DNA_AMPLICON_VAR_BAM_G_ALL_TASKS_NOT_NA, DNA_AMPLICON_VAR_BAM_S_CONTROL_NOT_NA,
                        format("%s%s%s",
                                OUTPUT_SH_SUFFIX, DNA_AMPLICON_VAR_BAM_STRELKA_2, FOR_GA_51_SH_SUFFIX),
                        format("%s%s%s",
                                CONTROL_SAMPLE_NOT_NAALL_TASKS_FOR_GA51_SUFFIX, DNA_AMPLICON_VAR_BAM_STRELKA_2,
                                FOR_GA_51_TEMPLATE_SUFFIX)),
                of(DNA_AMPLICON_VAR_BAM_G_ALL_TASKS_NOT_NA, DNA_AMPLICON_VAR_BAM_S_CONTROL_NOT_NA,
                        format("%s%s%s",
                                OUTPUT_SH_SUFFIX, DNA_AMPLICON_VAR_BAM_VARDICT, FOR_GA_51_SH_SUFFIX),
                        format("%s%s%s",
                                CONTROL_SAMPLE_NOT_NAALL_TASKS_FOR_GA51_SUFFIX, DNA_AMPLICON_VAR_BAM_VARDICT,
                                FOR_GA_51_TEMPLATE_SUFFIX)),
                of(DNA_AMPLICON_VAR_BAM_G_ALL_TASKS_NOT_NA, DNA_AMPLICON_VAR_BAM_S_CONTROL_NOT_NA,
                        format("%s%s%s",
                                OUTPUT_SH_SUFFIX, DNA_AMPLICON_VAR_BAM_VARIANT_DETECTION, FOR_GA_51_SH_SUFFIX),
                        format("%s%s%s",
                                CONTROL_SAMPLE_NOT_NAALL_TASKS_FOR_GA51_SUFFIX, DNA_AMPLICON_VAR_BAM_VARIANT_DETECTION,
                                FOR_GA_51_TEMPLATE_SUFFIX)),
                of(DNA_AMPLICON_VAR_BAM_G_ALL_TASKS_NOT_NA, DNA_AMPLICON_VAR_BAM_S_CONTROL_NOT_NA,
                        format("%s%s%s",
                                OUTPUT_SH_SUFFIX, DNA_AMPLICON_VAR_BAM_CONTEST, FOR_GA_52_SH_SUFFIX),
                        format("%s%s%s",
                                CONTROL_SAMPLE_NOT_NAALL_TASKS_FOR_GA52_SUFFIX, DNA_AMPLICON_VAR_BAM_CONTEST,
                                FOR_GA_52_TEMPLATE_SUFFIX)),
                of(DNA_AMPLICON_VAR_BAM_G_ALL_TASKS_NOT_NA, DNA_AMPLICON_VAR_BAM_S_CONTROL_NOT_NA,
                        format("%s%s%s",
                                OUTPUT_SH_SUFFIX, DNA_AMPLICON_VAR_BAM_EXOMECNV, FOR_GA_52_SH_SUFFIX),
                        format("%s%s%s",
                                CONTROL_SAMPLE_NOT_NAALL_TASKS_FOR_GA52_SUFFIX, DNA_AMPLICON_VAR_BAM_EXOMECNV,
                                FOR_GA_52_TEMPLATE_SUFFIX)),
                of(DNA_AMPLICON_VAR_BAM_G_ALL_TASKS_NOT_NA, DNA_AMPLICON_VAR_BAM_S_CONTROL_NOT_NA,
                        format("%s%s%s",
                                OUTPUT_SH_SUFFIX, DNA_AMPLICON_VAR_BAM_LOFREQ, FOR_GA_52_SH_SUFFIX),
                        format("%s%s%s",
                                CONTROL_SAMPLE_NOT_NAALL_TASKS_FOR_GA52_SUFFIX, DNA_AMPLICON_VAR_BAM_LOFREQ,
                                FOR_GA_52_TEMPLATE_SUFFIX)),
                of(DNA_AMPLICON_VAR_BAM_G_ALL_TASKS_NOT_NA, DNA_AMPLICON_VAR_BAM_S_CONTROL_NOT_NA,
                        format("%s%s%s",
                                OUTPUT_SH_SUFFIX, DNA_AMPLICON_VAR_BAM_MUTECT_2, FOR_GA_52_SH_SUFFIX),
                        format("%s%s%s",
                                CONTROL_SAMPLE_NOT_NAALL_TASKS_FOR_GA52_SUFFIX, DNA_AMPLICON_VAR_BAM_MUTECT_2,
                                FOR_GA_52_TEMPLATE_SUFFIX)),
                of(DNA_AMPLICON_VAR_BAM_G_ALL_TASKS_NOT_NA, DNA_AMPLICON_VAR_BAM_S_CONTROL_NOT_NA,
                        format("%s%s%s",
                                OUTPUT_SH_SUFFIX, DNA_AMPLICON_VAR_BAM_SCALPEL, FOR_GA_52_SH_SUFFIX),
                        format("%s%s%s",
                                CONTROL_SAMPLE_NOT_NAALL_TASKS_FOR_GA52_SUFFIX, DNA_AMPLICON_VAR_BAM_SCALPEL,
                                FOR_GA_52_TEMPLATE_SUFFIX)),
                of(DNA_AMPLICON_VAR_BAM_G_ALL_TASKS_NOT_NA, DNA_AMPLICON_VAR_BAM_S_CONTROL_NOT_NA,
                        format("%s%s%s",
                                OUTPUT_SH_SUFFIX, DNA_AMPLICON_VAR_BAM_SEQUENZA, FOR_GA_52_SH_SUFFIX),
                        format("%s%s%s",
                                CONTROL_SAMPLE_NOT_NAALL_TASKS_FOR_GA52_SUFFIX, DNA_AMPLICON_VAR_BAM_SEQUENZA,
                                FOR_GA_52_TEMPLATE_SUFFIX)),
                of(DNA_AMPLICON_VAR_BAM_G_ALL_TASKS_NOT_NA, DNA_AMPLICON_VAR_BAM_S_CONTROL_NOT_NA,
                        format("%s%s%s",
                                OUTPUT_SH_SUFFIX, DNA_AMPLICON_VAR_BAM_STRELKA_2, FOR_GA_52_SH_SUFFIX),
                        format("%s%s%s",
                                CONTROL_SAMPLE_NOT_NAALL_TASKS_FOR_GA52_SUFFIX, DNA_AMPLICON_VAR_BAM_STRELKA_2,
                                FOR_GA_52_TEMPLATE_SUFFIX)),
                of(DNA_AMPLICON_VAR_BAM_G_ALL_TASKS_NOT_NA, DNA_AMPLICON_VAR_BAM_S_CONTROL_NOT_NA,
                        format("%s%s%s",
                                OUTPUT_SH_SUFFIX, DNA_AMPLICON_VAR_BAM_VARDICT, FOR_GA_52_SH_SUFFIX),
                        format("%s%s%s",
                                CONTROL_SAMPLE_NOT_NAALL_TASKS_FOR_GA52_SUFFIX, DNA_AMPLICON_VAR_BAM_VARDICT,
                                FOR_GA_52_TEMPLATE_SUFFIX)),
                of(DNA_AMPLICON_VAR_BAM_G_ALL_TASKS_NOT_NA, DNA_AMPLICON_VAR_BAM_S_CONTROL_NOT_NA,
                        format("%s%s%s",
                                OUTPUT_SH_SUFFIX, DNA_AMPLICON_VAR_BAM_VARIANT_DETECTION, FOR_GA_52_SH_SUFFIX),
                        format("%s%s%s",
                                CONTROL_SAMPLE_NOT_NAALL_TASKS_FOR_GA52_SUFFIX, DNA_AMPLICON_VAR_BAM_VARIANT_DETECTION,
                                FOR_GA_52_TEMPLATE_SUFFIX)),
                of(DNA_AMPLICON_VAR_BAM_G_ALL_TASKS_NOT_NA, DNA_AMPLICON_VAR_BAM_S_CONTROL_NOT_NA,
                        format("%s%s%s",
                                OUTPUT_SH_SUFFIX, DNA_AMPLICON_VAR_BAM_MERGE_MUTATION, FOR_COHORT_ANALYSIS_SH_SUFFIX),
                        format("%s%s%s",
                                CONTROL_SAMPLE_NOT_NAALL_TASKS_SUFFIX, DNA_AMPLICON_VAR_BAM_MERGE_MUTATION,
                                FOR_COHORT_ANALYSIS_TEMPLATE_SUFFIX))
        );
    }
}
