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

import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.stream.Stream;

import static com.epam.fonda.utils.PipelineUtils.getExecutionPath;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DnaCaptureVarFastqIntegrationTest extends AbstractIntegrationTest {
    private static final String OUTPUT_DIR = "output";
    private static final String DNA_CAPTURE_VAR_FASTQ_DIR = "DnaCaptureVarFastq";
    private static final String OUTPUT_SH_FILES_DIR = "output/sh_files";
    private static final String STUDY_CONFIG_SINGLE = "sSingle.txt";
    private static final String STUDY_CONFIG_PAIRED = "sPaired.txt";


    private TemplateEngine templateEngine = TemplateEngineUtils.init();
    private Context context = new Context();

    @BeforeEach
    public void setup() {
        context = new Context();
        context.setVariable("jarPath", getExecutionPath());
    }

    @AfterEach
    public void cleanWorkDir() throws IOException {
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initParameters() {
        return Stream.of(
                Arguments.of(
                        format("%s/gXenomeSeqpurgeBwaPaired.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/%s", DNA_CAPTURE_VAR_FASTQ_DIR, STUDY_CONFIG_PAIRED),
                        format("%s/DnaCaptureVar_Fastq_alignment_for_GA5_1_analysis.sh",
                                OUTPUT_SH_FILES_DIR),
                        format("%s/XenomeSeqpurgeBwaPaired/dnaCaptureVar_Fastq_alignment_for_" +
                                        "GA5_1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR)),
                Arguments.of(
                        format("%s/gXenomeSeqpurgeBwaPaired.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/%s", DNA_CAPTURE_VAR_FASTQ_DIR, STUDY_CONFIG_PAIRED),
                        format("%s/DnaCaptureVar_Fastq_postalignment_for_GA5_analysis.sh",
                                OUTPUT_SH_FILES_DIR),
                        format("%s/XenomeSeqpurgeBwaPaired/dnaCaptureVar_Fastq_postalignment_for_" +
                                        "GA5_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR)),
                Arguments.of(
                        format("%s/gTrimmomaticNovoalignSingle.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/%s", DNA_CAPTURE_VAR_FASTQ_DIR, STUDY_CONFIG_SINGLE),
                        format("%s/DnaCaptureVar_Fastq_alignment_for_GA5_1_analysis.sh",
                                OUTPUT_SH_FILES_DIR),
                        format("%s/TrimmomaticNovoalignSingle/dnaCaptureVar_Fastq_alignment_for_" +
                                        "GA5_1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR)),
                Arguments.of(
                        format("%s/gTrimmomaticNovoalignSingle.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/%s", DNA_CAPTURE_VAR_FASTQ_DIR, STUDY_CONFIG_SINGLE),
                        format("%s/dnaCaptureVar_Fastq_postalignment_for_GA5_analysis.sh",
                                OUTPUT_SH_FILES_DIR),
                        format("%s/TrimmomaticNovoalignSingle/dnaCaptureVar_Fastq_postalignment_for_" +
                                        "GA5_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR)),
                Arguments.of(
                        format("%s/gBwaPicardQcAbraGatkPaired.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/%s", DNA_CAPTURE_VAR_FASTQ_DIR, STUDY_CONFIG_PAIRED),
                        format("%s/DnaCaptureVar_Fastq_alignment_for_GA5_1_analysis.sh",
                                OUTPUT_SH_FILES_DIR),
                        format("%s/BwaPicardQcAbraGatkPaired/dnaCaptureVar_Fastq_alignment_for_" +
                                        "GA5_1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR)),

                //Successful Step: GATK recalibration ??? case isn't ready for this question
                Arguments.of(
                        format("%s/gBwaPicardQcAbraGatkPaired.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/%s", DNA_CAPTURE_VAR_FASTQ_DIR, STUDY_CONFIG_PAIRED),
                        format("%s/dnaCaptureVar_Fastq_postalignment_for_GA5_analysis.sh",
                                OUTPUT_SH_FILES_DIR),
                        format("%s/BwaPicardQcAbraGatkPaired/dnaCaptureVar_Fastq_postalignment_for_" +
                                        "GA5_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR)),

                //Successful Step: Remove temporary directories or Merge DNA QC ??? case isn't ready for this question
                Arguments.of(
                        format("%s/gBwaPicardQcAbraGatkPaired.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/%s", DNA_CAPTURE_VAR_FASTQ_DIR, STUDY_CONFIG_PAIRED),
                        format("%s/dnaCaptureVar_Fastq_qcsummary_for_cohort_analysis.sh",
                                OUTPUT_SH_FILES_DIR),
                        format("%s/BwaPicardQcAbraGatkPaired/dnaCaptureVar_Fastq_qcsummary_for_" +
                                        "cohort_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR)),
                Arguments.of(
                        format("%s/gBwaPicardQcAbraGatkSingle.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/%s", DNA_CAPTURE_VAR_FASTQ_DIR, STUDY_CONFIG_SINGLE),
                        format("%s/dnaCaptureVar_Fastq_alignment_for_GA5_1_analysis.sh",
                                OUTPUT_SH_FILES_DIR),
                        format("%s/BwaPicardQcAbraGatkSingle/dnaCaptureVar_Fastq_alignment_for_" +
                                        "GA5_1_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR)),

                //Successful Step: GATK recalibration or Index rmdup bam ?? case isn't ready for this question
                Arguments.of(
                        format("%s/gBwaPicardQcAbraGatkSingle.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/%s", DNA_CAPTURE_VAR_FASTQ_DIR, STUDY_CONFIG_SINGLE),
                        format("%s/dnaCaptureVar_Fastq_postalignment_for_GA5_analysis.sh",
                                OUTPUT_SH_FILES_DIR),
                        format("%s/BwaPicardQcAbraGatkSingle/dnaCaptureVar_Fastq_postalignment_for_" +
                                        "GA5_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR)),

                //Successful Step: Remove temporary directories or Merge DNA QC ?? case isn't ready for this question
                Arguments.of(
                        format("%s/gBwaPicardQcAbraGatkSingle.txt", DNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/%s", DNA_CAPTURE_VAR_FASTQ_DIR, STUDY_CONFIG_SINGLE),
                        format("%s/dnaCaptureVar_Fastq_qcsummary_for_cohort_analysis.sh",
                                OUTPUT_SH_FILES_DIR),
                        format("%s/BwaPicardQcAbraGatkSingle/dnaCaptureVar_Fastq_qcsummary_for_" +
                                        "cohort_analysis_template",
                                DNA_CAPTURE_VAR_FASTQ_DIR))
        );
    }

    @ParameterizedTest
    @MethodSource("initParameters")
    void testDnaCaptureVarFastq(String gConfigPath, String sConfigPath, String outputShFile, String templatePath)
            throws IOException, URISyntaxException {
        startAppWithConfigs(gConfigPath, sConfigPath);
        String expectedCmd = templateEngine.process(templatePath, context);
        assertEquals(expectedCmd.trim(), getCmd(outputShFile).trim());
    }
}
