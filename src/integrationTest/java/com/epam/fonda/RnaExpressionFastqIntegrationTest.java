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
import static org.junit.Assert.assertEquals;

public class RnaExpressionFastqIntegrationTest extends AbstractIntegrationTest {

    private static final String OUTPUT_DIR = "output";
    private static final String RNA_EXPRESSION_FASTQ_TEMPLATES_SUFFIX = 
            "templates/RnaExpressionFastq/";
    private static final String OUTPUT_SH_FILE = 
            "output/sh_files/RnaExpression_Fastq_alignment_for_smv1_analysis.sh";
    private static final String RNA_EXPRESSION_FASTQ_SUFFIX =
            "RnaExpressionFastq/";
    private static final String S_CONFIG_PATH = 
            RNA_EXPRESSION_FASTQ_SUFFIX + "sRnaExpressionFastq.txt";
    private static final String RNA_EXPRESSION_FASTQ_ALIGNMENT_FOR_SMV1_ANALYSIS_TEMPLATE_PATH =
            "rnaExpression_Fastq_alignment_flag_Xenome_yes_template";
    private static final String RNA_EXPRESSION_FASTQ_G_FLAG_XENOME_YES = RNA_EXPRESSION_FASTQ_SUFFIX +
            "gFlagXenomeYes.txt";
    private static final String RNA_EXPRESSION_FASTQ_G_SEQPURGE_WITH_ADAPTERS = RNA_EXPRESSION_FASTQ_SUFFIX +
            "gSeqpurgeWithAdapters.txt";
    private static final String RNA_EXPRESSION_FASTQ_G_SEQPURGE_WITHOUT_ADAPTERS = 
            RNA_EXPRESSION_FASTQ_SUFFIX + "gSeqpurgeWithoutAdapters.txt";
    private static final String RNA_EXPRESSION_FASTQ_G_TRIMMOMATIC_WITH_ADAPTER = 
            RNA_EXPRESSION_FASTQ_SUFFIX + "gTrimmomaticWithAdapter.txt";
    private static final String RNA_EXPRESSION_FASTQ_G_TRIMMOMATIC_WITHOUT_ADAPTER = 
            RNA_EXPRESSION_FASTQ_SUFFIX + "gTrimmomaticWithoutAdapter.txt";
    private static final String RNA_EXPRESSION_FASTQ_G_STAR_WITH_RSEM = 
            RNA_EXPRESSION_FASTQ_SUFFIX + "gStarWithRsem.txt";
    private static final String RNA_EXPRESSION_FASTQ_G_STAR_WITHOUT_RSEM = 
            RNA_EXPRESSION_FASTQ_SUFFIX + "gStarWithoutRsem.txt";
    private static final String RNA_EXPRESSION_FASTQ_G_HISAT2 = 
            RNA_EXPRESSION_FASTQ_SUFFIX + "gHisat2.txt";
    private static final String RNA_EXPRESSION_FASTQ_G_SALMON = 
            RNA_EXPRESSION_FASTQ_SUFFIX + "gSalmon.txt";
    private static final String RNA_EXPRESSION_FASTQ_G_FEATURE_COUNT = 
            RNA_EXPRESSION_FASTQ_SUFFIX + "gFeatureCount.txt";
    private static final String RNA_EXPRESSION_FASTQ_G_CUFFLINKS = 
            RNA_EXPRESSION_FASTQ_SUFFIX + "gCufflinks.txt";
    private static final String RNA_EXPRESSION_FASTQ_G_STRINGTIE = 
            RNA_EXPRESSION_FASTQ_SUFFIX + "gStringtie.txt";
    private static final String RNA_EXPRESSION_FASTQ_G_RSEM_WITHOUT_HISAT2 = 
            RNA_EXPRESSION_FASTQ_SUFFIX + "gRsemWithoutHisat2.txt";
    private static final String RNA_EXPRESSION_FASTQ_G_NON_FLAG_XENOME = 
            RNA_EXPRESSION_FASTQ_SUFFIX + "gNonFlagXenome.txt";
    private static final String RNA_EXPRESSION_FASTQ_G_NON_SEQPURGE = 
            RNA_EXPRESSION_FASTQ_SUFFIX + "gNonSeqpurge.txt";
    private static final String RNA_EXPRESSION_FASTQ_G_NON_TRIMMOMATIC = 
            RNA_EXPRESSION_FASTQ_SUFFIX + "gNonTrimmomatic.txt";
    private static final String RNA_EXPRESSION_FASTQ_G_NON_STAR = 
            RNA_EXPRESSION_FASTQ_SUFFIX + "gNonStar.txt";
    private static final String RNA_EXPRESSION_FASTQ_G_NON_HISAT2 =
            RNA_EXPRESSION_FASTQ_SUFFIX + "gNonHisat2.txt";
    private static final String RNA_EXPRESSION_FASTQ_G_NON_SALMON =
            RNA_EXPRESSION_FASTQ_SUFFIX + "gNonSalmon.txt";
    private static final String OUTPUT_SH_FILES_SUFFIX = 
            "output/sh_files/";
    private static final String RNA_EXPRESSION_FASTQ_SEQPURGE_WITH_ADAPTERS_TEMPLATE = 
            "rnaExpression_Fastq_Seqpurge_with_Adapters";
    private static final String RNA_EXPRESSION_FASTQ_SEQPURGE_WITHOUT_ADAPTERS_TEMPLATE = 
            "rnaExpression_Fastq_Seqpurge_without_Adapters";
    private static final String RNA_EXPRESSION_FASTQ_TRIMMOMATIC_WITH_ADAPTER_TEMPLATE = 
            "rnaExpression_Fastq_Trimmomatic_with_adapter";
    private static final String RNA_EXPRESSION_FASTQ_TRIMMOMATIC_WITHOUT_ADAPTER_TEMPLATE = 
            "rnaExpression_Fastq_Trimmomatic_without_adapter";
    private static final String RNA_EXPRESSION_FASTQ_CUFFLINKS_SUFFIX = 
            "RnaExpressionFastqCufflinks/";
    private static final String RNA_EXPRESSION_FASTQ_FEATURE_COUNT_SUFFIX = 
            "RnaExpressionFastqFeatureCount/";
    private static final String RNA_EXPRESSION_FASTQ_STAR_WITH_RSEM_SUFFIX = 
            "RnaExpressionFastqStarWithRsem/";
    private static final String RNA_EXPRESSION_FASTQ_STRINGTIE_SUFFIX = 
            "RnaExpressionFastqStringtie/";
    private static final String RNA_EXPRESSION_FASTQ_STAR_WITHOUT_RSEM_SUFFIX = 
            "RnaExpressionFastqStarWithoutRsem/";
    private static final String RNA_EXPRESSION_FASTQ_RSEM_WITHOUT_HISAT2_SUFFIX = 
            "RnaExpressionFastqRsemWithoutHisat2/";
    private static final String RNA_EXPRESSION_FASTQ_HISAT2_TEMPLATE = 
            "rnaExpression_Fastq_Hisat2";
    private static final String RNA_EXPRESSION_FASTQ_SALMON_TEMPLATE = 
            "rnaExpression_Fastq_Salmon";
    private static final String RNA_EXPRESSION_FASTQ_NON_FLAG_XENOME_TEMPLATE = 
            "rnaExpression_Fastq_non_flag_Xenome";
    private static final String RNA_EXPRESSION_FASTQ_NON_SEQPURGE_TEMPLATE = 
            "rnaExpression_Fastq_non_Seqpurge";
    private static final String RNA_EXPRESSION_FASTQ_NON_TRIMMOMATIC_TEMPLATE = 
            "rnaExpression_Fastq_non_Trimmomatic";
    private static final String RNA_EXPRESSION_FASTQ_NON_STAR_TEMPLATE = 
            "rnaExpression_Fastq_non_Star";
    private static final String RNA_EXPRESSION_FASTQ_NON_HISAT2_TEMPLATE = 
            "rnaExpression_Fastq_non_Hisat2";
    private static final String RNA_EXPRESSION_FASTQ_NON_SALMON_TEMPLATE = 
            "rnaExpression_Fastq_non_Salmon";
    private TemplateEngine templateEngine = TemplateEngineUtils.init(RNA_EXPRESSION_FASTQ_TEMPLATES_SUFFIX);
    private Context context = new Context();

    @BeforeEach
    public void setup() {
        context = new Context();
        context.setVariable("jarPath", getExecutionPath());
        context.setVariable("javaPath", System.getProperty("java.home"));
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initParameters() {
        return Stream.of(
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_FLAG_XENOME_YES, 
                        OUTPUT_SH_FILE,
                        RNA_EXPRESSION_FASTQ_ALIGNMENT_FOR_SMV1_ANALYSIS_TEMPLATE_PATH),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_SEQPURGE_WITH_ADAPTERS, 
                        OUTPUT_SH_FILE,
                        RNA_EXPRESSION_FASTQ_SEQPURGE_WITH_ADAPTERS_TEMPLATE),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_SEQPURGE_WITHOUT_ADAPTERS, 
                        OUTPUT_SH_FILE,
                        RNA_EXPRESSION_FASTQ_SEQPURGE_WITHOUT_ADAPTERS_TEMPLATE),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_TRIMMOMATIC_WITH_ADAPTER, 
                        OUTPUT_SH_FILE,
                        RNA_EXPRESSION_FASTQ_TRIMMOMATIC_WITH_ADAPTER_TEMPLATE),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_TRIMMOMATIC_WITHOUT_ADAPTER, 
                        OUTPUT_SH_FILE,
                        RNA_EXPRESSION_FASTQ_TRIMMOMATIC_WITHOUT_ADAPTER_TEMPLATE),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_STAR_WITH_RSEM, 
                        OUTPUT_SH_FILE, 
                        RNA_EXPRESSION_FASTQ_STAR_WITH_RSEM_SUFFIX + 
                                "RnaExpression_Fastq_alignment_for_smv1_analysis"),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_STAR_WITH_RSEM, 
                        OUTPUT_SH_FILES_SUFFIX + "RnaExpression_Fastq_qcsummary_for_cohort_analysis.sh",
                        RNA_EXPRESSION_FASTQ_STAR_WITH_RSEM_SUFFIX + 
                                "RnaExpression_Fastq_qcsummary_for_cohort_analysis"),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_STAR_WITH_RSEM, 
                        OUTPUT_SH_FILES_SUFFIX + "RnaExpression_Fastq_rsem_for_smv1_analysis.sh",
                        RNA_EXPRESSION_FASTQ_STAR_WITH_RSEM_SUFFIX + 
                                "RnaExpression_Fastq_rsem_for_smv1_analysis"),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_STAR_WITHOUT_RSEM, 
                        OUTPUT_SH_FILE, 
                        RNA_EXPRESSION_FASTQ_STAR_WITHOUT_RSEM_SUFFIX + 
                                "RnaExpression_Fastq_alignment_for_smv1_analysis"),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_STAR_WITHOUT_RSEM,
                        OUTPUT_SH_FILES_SUFFIX + "RnaExpression_Fastq_qcsummary_for_cohort_analysis.sh",
                        RNA_EXPRESSION_FASTQ_STAR_WITHOUT_RSEM_SUFFIX + 
                                "RnaExpression_Fastq_qcsummary_for_cohort_analysis"),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_HISAT2, 
                        OUTPUT_SH_FILE,
                        RNA_EXPRESSION_FASTQ_HISAT2_TEMPLATE),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_SALMON, 
                        OUTPUT_SH_FILE,
                        RNA_EXPRESSION_FASTQ_SALMON_TEMPLATE),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_FEATURE_COUNT,
                        OUTPUT_SH_FILES_SUFFIX + "RnaExpression_Fastq_featureCount_for_smv1_analysis.sh", 
                        RNA_EXPRESSION_FASTQ_FEATURE_COUNT_SUFFIX + 
                                "RnaExpression_Fastq_featureCount_for_smv1_analysis"),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_CUFFLINKS,
                        OUTPUT_SH_FILES_SUFFIX + "RnaExpression_Fastq_cufflinks_for_smv1_analysis.sh", 
                        RNA_EXPRESSION_FASTQ_CUFFLINKS_SUFFIX + 
                                "RnaExpression_Fastq_cufflinks_for_smv1_analysis"),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_STRINGTIE,
                        OUTPUT_SH_FILES_SUFFIX + "RnaExpression_Fastq_stringtie_for_smv1_analysis.sh", 
                        RNA_EXPRESSION_FASTQ_STRINGTIE_SUFFIX + 
                                "RnaExpression_Fastq_stringtie_for_smv1_analysis"),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_RSEM_WITHOUT_HISAT2,
                        OUTPUT_SH_FILES_SUFFIX + "RnaExpression_Fastq_rsem_for_smv1_analysis.sh", 
                        RNA_EXPRESSION_FASTQ_RSEM_WITHOUT_HISAT2_SUFFIX + 
                                "RnaExpression_Fastq_rsem_for_smv1_analysis"),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_RSEM_WITHOUT_HISAT2, 
                        OUTPUT_SH_FILE,
                        RNA_EXPRESSION_FASTQ_RSEM_WITHOUT_HISAT2_SUFFIX + 
                                "RnaExpression_Fastq_alignment_for_smv1_analysis"),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_RSEM_WITHOUT_HISAT2,
                        OUTPUT_SH_FILES_SUFFIX + "RnaExpression_Fastq_qcsummary_for_cohort_analysis.sh",
                        RNA_EXPRESSION_FASTQ_RSEM_WITHOUT_HISAT2_SUFFIX + 
                                "RnaExpression_Fastq_qcsummary_for_cohort_analysis"),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_NON_FLAG_XENOME, 
                        OUTPUT_SH_FILE,
                        RNA_EXPRESSION_FASTQ_NON_FLAG_XENOME_TEMPLATE),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_NON_SEQPURGE, 
                        OUTPUT_SH_FILE,
                        RNA_EXPRESSION_FASTQ_NON_SEQPURGE_TEMPLATE),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_NON_TRIMMOMATIC, 
                        OUTPUT_SH_FILE,
                        RNA_EXPRESSION_FASTQ_NON_TRIMMOMATIC_TEMPLATE),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_NON_STAR, 
                        OUTPUT_SH_FILE,
                        RNA_EXPRESSION_FASTQ_NON_STAR_TEMPLATE),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_NON_HISAT2, 
                        OUTPUT_SH_FILE,
                        RNA_EXPRESSION_FASTQ_NON_HISAT2_TEMPLATE),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_NON_SALMON, 
                        OUTPUT_SH_FILE,
                        RNA_EXPRESSION_FASTQ_NON_SALMON_TEMPLATE)
        );
    }

    @ParameterizedTest(name = "{2}-test")
    @MethodSource("initParameters")
    void testRnaExpressionFastq(
            String gConfigPath, String outputShFile, String templatePath) throws IOException, URISyntaxException {
        startAppWithConfigs(gConfigPath, S_CONFIG_PATH);
        String expectedCmd = templateEngine.process(templatePath, context);
        assertEquals(expectedCmd.trim(), getCmd(outputShFile).trim());
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }
}
