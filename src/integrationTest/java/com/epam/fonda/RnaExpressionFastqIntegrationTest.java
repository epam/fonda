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

import com.epam.fonda.utils.TestTemplateUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.epam.fonda.utils.TestTemplateUtils.getSamplesScripts;
import static com.epam.fonda.utils.TestTemplateUtils.trimNotImportant;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RnaExpressionFastqIntegrationTest extends AbstractIntegrationTest {

    private static final String RNA_EXPRESSION_FASTQ_SUFFIX = "RnaExpressionFastq/";
    private static final String OUTPUT_SH_FILE =
            "output/sh_files/RnaExpression_Fastq_alignment_for_smv1_analysis.sh";
    private static final String CLEAN_TMP_TEMPLATE_PATH = "clean_up_tmpdir_test_output_data";
    private static final String MASTER_TEMPLATE_TEST_PATH = "master_template_test";
    private static final String OUTPUT_FILE_MASTER = "output/sh_files/master.sh";
    private static final String S_CONFIG_PATH =
            format("%ssRnaExpressionFastq.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_ALIGNMENT_FOR_SMV1_ANALYSIS_TEMPLATE_PATH =
            format("%srnaExpression_Fastq_alignment_flag_Xenome_yes_template",
                    RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_FLAG_XENOME_YES =
            format("%sgFlagXenomeYes.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_SEQPURGE_WITH_ADAPTERS =
            format("%sgSeqpurgeWithAdapters.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_SEQPURGE_WITHOUT_ADAPTERS =
            format("%sgSeqpurgeWithoutAdapters.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_TRIMMOMATIC_WITH_ADAPTER =
            format("%sgTrimmomaticWithAdapter.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_TRIMMOMATIC_WITHOUT_ADAPTER =
            format("%sgTrimmomaticWithoutAdapter.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_STAR_WITH_RSEM =
            format("%sgStarWithRsem.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_STAR_WITHOUT_RSEM =
            format("%sgStarWithoutRsem.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_HISAT2 =
            format("%sgHisat2.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_SALMON =
            format("%sgSalmon.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_FEATURE_COUNT =
            format("%sgFeatureCount.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_CUFFLINKS =
            format("%sgCufflinks.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_STRINGTIE =
            format("%sgStringtie.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_RSEM_WITHOUT_HISAT2 =
            format("%sgRsemWithoutHisat2.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_NON_FLAG_XENOME =
            format("%sgNonFlagXenome.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_NON_SEQPURGE =
            format("%sgNonSeqpurge.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_NON_TRIMMOMATIC =
            format("%sgNonTrimmomatic.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_NON_STAR =
            format("%sgNonStar.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_NON_HISAT2 =
            format("%sgNonHisat2.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_NON_SALMON =
            format("%sgNonSalmon.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String OUTPUT_SH_FILES_SUFFIX = "output/sh_files/";
    private static final String RNA_EXPRESSION_FASTQ_SEQPURGE_WITH_ADAPTERS_TEMPLATE =
            format("%srnaExpression_Fastq_Seqpurge_with_Adapters", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_SEQPURGE_WITHOUT_ADAPTERS_TEMPLATE =
            format("%srnaExpression_Fastq_Seqpurge_without_Adapters", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_TRIMMOMATIC_WITH_ADAPTER_TEMPLATE =
            format("%srnaExpression_Fastq_Trimmomatic_with_adapter", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_TRIMMOMATIC_WITHOUT_ADAPTER_TEMPLATE =
            format("%srnaExpression_Fastq_Trimmomatic_without_adapter", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_CUFFLINKS_SUFFIX =
            format("%sRnaExpressionFastqCufflinks/", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_FEATURE_COUNT_SUFFIX =
            format("%sRnaExpressionFastqFeatureCount/", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_STAR_WITH_RSEM_SUFFIX =
            format("%sRnaExpressionFastqStarWithRsem/", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_STRINGTIE_SUFFIX =
            format("%sRnaExpressionFastqStringtie/", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_STAR_WITHOUT_RSEM_SUFFIX =
            format("%sRnaExpressionFastqStarWithoutRsem/", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_RSEM_WITHOUT_HISAT2_SUFFIX =
            format("%sRnaExpressionFastqRsemWithoutHisat2/", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_HISAT2_TEMPLATE =
            format("%srnaExpression_Fastq_Hisat2", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_SALMON_TEMPLATE =
            format("%srnaExpression_Fastq_Salmon", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_NON_FLAG_XENOME_TEMPLATE =
            format("%srnaExpression_Fastq_non_flag_Xenome", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_NON_SEQPURGE_TEMPLATE =
            format("%srnaExpression_Fastq_non_Seqpurge", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_NON_TRIMMOMATIC_TEMPLATE =
            format("%srnaExpression_Fastq_non_Trimmomatic", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_NON_STAR_TEMPLATE =
            format("%srnaExpression_Fastq_non_Star", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_NON_HISAT2_TEMPLATE =
            format("%srnaExpression_Fastq_non_Hisat2", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_NON_SALMON_TEMPLATE =
            format("%srnaExpression_Fastq_non_Salmon", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String POST_PROCESS_SCRIPT =
            "build/resources/integrationTest/output/sh_files/RnaExpression_Fastq_qcsummary_for_cohort_analysis.sh &";
    private static final String OUTPUT_DIR_ROOT = "build/resources/integrationTest/";
    private static final String SAMPLE_NAME = "smv1/";
    private static final Integer NUMBER_OF_SCRIPTS = 6;

    private static final String EXPECTED_SCRIPT_START =
            "build/resources/integrationTest/output/sh_files/RnaExpression_Fastq";
    private static final String SEQ_TO_PURGE =
            format("%1$soutput/smv%2$s/fastq/smv%2$s.merged_R%3$s.fastq.gz", OUTPUT_DIR_ROOT, "%1$d", "%2$d");
    private static final Integer SEQ_TO_PURGE_NUMBER = 6;

    @ParameterizedTest(name = "{2}-test")
    @MethodSource("initParameters")
    void testRnaExpressionFastq(final String gConfigPath, final String outputShFile, final String templatePath)
            throws IOException, URISyntaxException {
        startAppWithConfigs(gConfigPath, S_CONFIG_PATH);
        final String expectedCmd = TEMPLATE_ENGINE.process(templatePath, context);
        assertEquals(expectedCmd.trim(), getCmd(outputShFile).trim());
    }

    @ParameterizedTest(name = "{3}-test")
    @MethodSource("initParameters")
    void testRnaExpressionFastqMaster(final String gConfigPath, final String outputShFile, final String templatePath,
                                      final String expectedBaseScript, final String expectedSecondScript,
                                      final Integer numberOfScripts, final String postProcessScript)
            throws IOException, URISyntaxException {
        startAppWithConfigs(gConfigPath, S_CONFIG_PATH, new String[] { "-master" });
        final String expectedCmd = TEMPLATE_ENGINE.process(templatePath, context);
        assertEquals(expectedCmd.trim(), getCmd(outputShFile).trim());

        final String expectedMasterScript =
                getExpectedMasterScript(expectedBaseScript, expectedSecondScript, numberOfScripts, postProcessScript);

        assertEquals(trimNotImportant(expectedMasterScript), trimNotImportant(getCmd(OUTPUT_FILE_MASTER)));
    }

    @Test
    public void testCreateRnaExpressionFastqSpecificDirExpressionFastqToolset() {
        startAppWithConfigs(RNA_EXPRESSION_FASTQ_G_FLAG_XENOME_YES, S_CONFIG_PATH);
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

    private String getExpectedMasterScript(final String expectedBaseScript, final String expectedSecondScript,
                                           final Integer numberOfScripts, final String postProcessScript) {
        final String expectedMasterScript = TEMPLATE_ENGINE.process(
                MASTER_TEMPLATE_TEST_PATH,
                TestTemplateUtils.getContextForMaster(
                        context,
                        getSamplesScripts(expectedBaseScript, expectedSecondScript, numberOfScripts),
                        postProcessScript
                )
        );
        final List<String> fieldsToPurge = getFieldsToPurge(SEQ_TO_PURGE, SEQ_TO_PURGE_NUMBER);
        context.setVariable("fields", fieldsToPurge);
        final String expectedCleanScript = TEMPLATE_ENGINE.process(CLEAN_TMP_TEMPLATE_PATH, context);
        return expectedMasterScript + expectedCleanScript;
    }

    private List<String> getFieldsToPurge(final String seqToPurge, final int seqToPurgeNumber) {
        final List<String> fieldsToPurge = new ArrayList<>();
        for (int i = 1; i <= seqToPurgeNumber; i++) {
            fieldsToPurge.add(format(seqToPurge, i, 1));
            fieldsToPurge.add(format(seqToPurge, i, 2));
        }
        return fieldsToPurge;
    }

    @SuppressWarnings("all")
    private static Stream<Arguments> initParameters() {
        return Stream.of(
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_FLAG_XENOME_YES,
                        OUTPUT_SH_FILE,
                        RNA_EXPRESSION_FASTQ_ALIGNMENT_FOR_SMV1_ANALYSIS_TEMPLATE_PATH,
                        format("%1$s_alignment_for_smv%2$s_analysis.sh &", EXPECTED_SCRIPT_START, "%d"),
                        "",
                        NUMBER_OF_SCRIPTS,
                        null),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_SEQPURGE_WITH_ADAPTERS,
                        OUTPUT_SH_FILE,
                        RNA_EXPRESSION_FASTQ_SEQPURGE_WITH_ADAPTERS_TEMPLATE,
                        format("%1$s_alignment_for_smv%2$s_analysis.sh &", EXPECTED_SCRIPT_START, "%d"),
                        "",
                        NUMBER_OF_SCRIPTS,
                        null),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_SEQPURGE_WITHOUT_ADAPTERS,
                        OUTPUT_SH_FILE,
                        RNA_EXPRESSION_FASTQ_SEQPURGE_WITHOUT_ADAPTERS_TEMPLATE,
                        format("%1$s_alignment_for_smv%2$s_analysis.sh &", EXPECTED_SCRIPT_START, "%d"),
                        "",
                        NUMBER_OF_SCRIPTS,
                        null),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_TRIMMOMATIC_WITH_ADAPTER,
                        OUTPUT_SH_FILE,
                        RNA_EXPRESSION_FASTQ_TRIMMOMATIC_WITH_ADAPTER_TEMPLATE,
                        format("%1$s_alignment_for_smv%2$s_analysis.sh &", EXPECTED_SCRIPT_START, "%d"),
                        "",
                        NUMBER_OF_SCRIPTS,
                        null),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_TRIMMOMATIC_WITHOUT_ADAPTER,
                        OUTPUT_SH_FILE,
                        RNA_EXPRESSION_FASTQ_TRIMMOMATIC_WITHOUT_ADAPTER_TEMPLATE,
                        format("%1$s_alignment_for_smv%2$s_analysis.sh &", EXPECTED_SCRIPT_START, "%d"),
                        "",
                        NUMBER_OF_SCRIPTS,
                        null),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_STAR_WITH_RSEM,
                        OUTPUT_SH_FILE,
                        format("%sRnaExpression_Fastq_alignment_for_smv1_analysis",
                                RNA_EXPRESSION_FASTQ_STAR_WITH_RSEM_SUFFIX),
                        format("%1$s_alignment_for_smv%2$s_analysis.sh && \\", EXPECTED_SCRIPT_START, "%d"),
                        format("%1$s_rsem_for_smv%2$s_analysis.sh &", EXPECTED_SCRIPT_START, "%d"),
                        NUMBER_OF_SCRIPTS,
                        POST_PROCESS_SCRIPT),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_STAR_WITH_RSEM,
                        format("%sRnaExpression_Fastq_qcsummary_for_cohort_analysis.sh",
                                OUTPUT_SH_FILES_SUFFIX),
                        format("%sRnaExpression_Fastq_qcsummary_for_cohort_analysis",
                                RNA_EXPRESSION_FASTQ_STAR_WITH_RSEM_SUFFIX),
                        format("%1$s_alignment_for_smv%2$s_analysis.sh && \\", EXPECTED_SCRIPT_START, "%d"),
                        format("%1$s_rsem_for_smv%2$s_analysis.sh &", EXPECTED_SCRIPT_START, "%d"),
                        NUMBER_OF_SCRIPTS,
                        POST_PROCESS_SCRIPT),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_STAR_WITH_RSEM,
                        format("%sRnaExpression_Fastq_rsem_for_smv1_analysis.sh",
                                OUTPUT_SH_FILES_SUFFIX),
                        format("%sRnaExpression_Fastq_rsem_for_smv1_analysis",
                                RNA_EXPRESSION_FASTQ_STAR_WITH_RSEM_SUFFIX),
                        format("%1$s_alignment_for_smv%2$s_analysis.sh && \\", EXPECTED_SCRIPT_START, "%d"),
                        format("%1$s_rsem_for_smv%2$s_analysis.sh &", EXPECTED_SCRIPT_START, "%d"),
                        NUMBER_OF_SCRIPTS,
                        POST_PROCESS_SCRIPT),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_STAR_WITHOUT_RSEM,
                        OUTPUT_SH_FILE,
                        format("%sRnaExpression_Fastq_alignment_for_smv1_analysis",
                                RNA_EXPRESSION_FASTQ_STAR_WITHOUT_RSEM_SUFFIX),
                        format("%1$s_alignment_for_smv%2$s_analysis.sh &", EXPECTED_SCRIPT_START, "%d"),
                        "",
                        NUMBER_OF_SCRIPTS,
                        POST_PROCESS_SCRIPT),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_STAR_WITHOUT_RSEM,
                        format("%sRnaExpression_Fastq_qcsummary_for_cohort_analysis.sh",
                                OUTPUT_SH_FILES_SUFFIX),
                        format("%sRnaExpression_Fastq_qcsummary_for_cohort_analysis",
                                RNA_EXPRESSION_FASTQ_STAR_WITHOUT_RSEM_SUFFIX),
                        format("%1$s_alignment_for_smv%2$s_analysis.sh &", EXPECTED_SCRIPT_START, "%d"),
                        "",
                        NUMBER_OF_SCRIPTS,
                        POST_PROCESS_SCRIPT),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_HISAT2,
                        OUTPUT_SH_FILE,
                        RNA_EXPRESSION_FASTQ_HISAT2_TEMPLATE,
                        format("%1$s_alignment_for_smv%2$s_analysis.sh &", EXPECTED_SCRIPT_START, "%d"),
                        "",
                        NUMBER_OF_SCRIPTS,
                        null),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_SALMON,
                        OUTPUT_SH_FILE,
                        RNA_EXPRESSION_FASTQ_SALMON_TEMPLATE,
                        format("%1$s_alignment_for_smv%2$s_analysis.sh &", EXPECTED_SCRIPT_START, "%d"),
                        "",
                        NUMBER_OF_SCRIPTS,
                        null),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_FEATURE_COUNT,
                        format("%sRnaExpression_Fastq_featureCount_for_smv1_analysis.sh",
                                OUTPUT_SH_FILES_SUFFIX),
                        format("%sRnaExpression_Fastq_featureCount_for_smv1_analysis",
                                RNA_EXPRESSION_FASTQ_FEATURE_COUNT_SUFFIX),
                        format("%1$s_alignment_for_smv%2$s_analysis.sh && \\", EXPECTED_SCRIPT_START, "%d"),
                        format("%1$s_featureCount_for_smv%2$s_analysis.sh &", EXPECTED_SCRIPT_START, "%d"),
                        NUMBER_OF_SCRIPTS,
                        null),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_CUFFLINKS,
                        format("%sRnaExpression_Fastq_cufflinks_for_smv1_analysis.sh",
                                OUTPUT_SH_FILES_SUFFIX),
                        RNA_EXPRESSION_FASTQ_CUFFLINKS_SUFFIX +
                                "RnaExpression_Fastq_cufflinks_for_smv1_analysis",
                        format("%1$s_alignment_for_smv%2$s_analysis.sh && \\", EXPECTED_SCRIPT_START, "%d"),
                        format("%1$s_cufflinks_for_smv%2$s_analysis.sh &", EXPECTED_SCRIPT_START, "%d"),
                        NUMBER_OF_SCRIPTS,
                        null),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_STRINGTIE,
                        format("%sRnaExpression_Fastq_stringtie_for_smv1_analysis.sh",
                                OUTPUT_SH_FILES_SUFFIX),
                        format("%sRnaExpression_Fastq_stringtie_for_smv1_analysis",
                                RNA_EXPRESSION_FASTQ_STRINGTIE_SUFFIX),
                        format("%1$s_alignment_for_smv%2$s_analysis.sh && \\", EXPECTED_SCRIPT_START, "%d"),
                        format("%1$s_stringtie_for_smv%2$s_analysis.sh &", EXPECTED_SCRIPT_START, "%d"),
                        NUMBER_OF_SCRIPTS,
                        null),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_RSEM_WITHOUT_HISAT2,
                        format("%sRnaExpression_Fastq_rsem_for_smv1_analysis.sh",
                                OUTPUT_SH_FILES_SUFFIX),
                        format("%sRnaExpression_Fastq_rsem_for_smv1_analysis",
                                RNA_EXPRESSION_FASTQ_RSEM_WITHOUT_HISAT2_SUFFIX),
                        format("%1$s_alignment_for_smv%2$s_analysis.sh && \\", EXPECTED_SCRIPT_START, "%d"),
                        format("%1$s_rsem_for_smv%2$s_analysis.sh &", EXPECTED_SCRIPT_START, "%d"),
                        NUMBER_OF_SCRIPTS,
                        POST_PROCESS_SCRIPT),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_RSEM_WITHOUT_HISAT2,
                        OUTPUT_SH_FILE,
                        format("%sRnaExpression_Fastq_alignment_for_smv1_analysis",
                                RNA_EXPRESSION_FASTQ_RSEM_WITHOUT_HISAT2_SUFFIX),
                        format("%1$s_alignment_for_smv%2$s_analysis.sh && \\", EXPECTED_SCRIPT_START, "%d"),
                        format("%1$s_rsem_for_smv%2$s_analysis.sh &", EXPECTED_SCRIPT_START, "%d"),
                        NUMBER_OF_SCRIPTS,
                        POST_PROCESS_SCRIPT),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_RSEM_WITHOUT_HISAT2,
                        format("%sRnaExpression_Fastq_qcsummary_for_cohort_analysis.sh",
                                OUTPUT_SH_FILES_SUFFIX),
                        format("%sRnaExpression_Fastq_qcsummary_for_cohort_analysis",
                                RNA_EXPRESSION_FASTQ_RSEM_WITHOUT_HISAT2_SUFFIX),
                        format("%1$s_alignment_for_smv%2$s_analysis.sh && \\", EXPECTED_SCRIPT_START, "%d"),
                        format("%1$s_rsem_for_smv%2$s_analysis.sh &", EXPECTED_SCRIPT_START, "%d"),
                        NUMBER_OF_SCRIPTS,
                        POST_PROCESS_SCRIPT),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_NON_FLAG_XENOME,
                        OUTPUT_SH_FILE,
                        RNA_EXPRESSION_FASTQ_NON_FLAG_XENOME_TEMPLATE,
                        format("%1$s_alignment_for_smv%2$s_analysis.sh &", EXPECTED_SCRIPT_START, "%d"),
                        "",
                        NUMBER_OF_SCRIPTS,
                        null),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_NON_SEQPURGE,
                        OUTPUT_SH_FILE,
                        RNA_EXPRESSION_FASTQ_NON_SEQPURGE_TEMPLATE,
                        format("%1$s_alignment_for_smv%2$s_analysis.sh &", EXPECTED_SCRIPT_START, "%d"),
                        "",
                        NUMBER_OF_SCRIPTS,
                        null),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_NON_TRIMMOMATIC,
                        OUTPUT_SH_FILE,
                        RNA_EXPRESSION_FASTQ_NON_TRIMMOMATIC_TEMPLATE,
                        format("%1$s_alignment_for_smv%2$s_analysis.sh &", EXPECTED_SCRIPT_START, "%d"),
                        "",
                        NUMBER_OF_SCRIPTS,
                        null),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_NON_STAR,
                        OUTPUT_SH_FILE,
                        RNA_EXPRESSION_FASTQ_NON_STAR_TEMPLATE,
                        format("%1$s_alignment_for_smv%2$s_analysis.sh &", EXPECTED_SCRIPT_START, "%d"),
                        "",
                        NUMBER_OF_SCRIPTS,
                        null),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_NON_HISAT2,
                        OUTPUT_SH_FILE,
                        RNA_EXPRESSION_FASTQ_NON_HISAT2_TEMPLATE,
                        format("%1$s_alignment_for_smv%2$s_analysis.sh &", EXPECTED_SCRIPT_START, "%d"),
                        "",
                        NUMBER_OF_SCRIPTS,
                        null),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_NON_SALMON,
                        OUTPUT_SH_FILE,
                        RNA_EXPRESSION_FASTQ_NON_SALMON_TEMPLATE,
                        format("%1$s_alignment_for_smv%2$s_analysis.sh &", EXPECTED_SCRIPT_START, "%d"),
                        "",
                        NUMBER_OF_SCRIPTS,
                        null)
        );
    }
}
