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
    private static final String RNA_EXPRESSION_FASTQ_TEMPLATES_SUFFIX = "RnaExpressionFastq/";
    private static final String OUTPUT_SH_FILE = 
            "output/sh_files/RnaExpression_Fastq_alignment_for_smv1_analysis.sh";
    private static final String RNA_EXPRESSION_FASTQ_SUFFIX = "RnaExpressionFastq/";
    private static final String S_CONFIG_PATH =
            String.format("%ssRnaExpressionFastq.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_ALIGNMENT_FOR_SMV1_ANALYSIS_TEMPLATE_PATH =
            String.format("%srnaExpression_Fastq_alignment_flag_Xenome_yes_template",
                    RNA_EXPRESSION_FASTQ_TEMPLATES_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_FLAG_XENOME_YES =
            String.format("%sgFlagXenomeYes.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_SEQPURGE_WITH_ADAPTERS =
            String.format("%sgSeqpurgeWithAdapters.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_SEQPURGE_WITHOUT_ADAPTERS =
            String.format("%sgSeqpurgeWithoutAdapters.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_TRIMMOMATIC_WITH_ADAPTER =
            String.format("%sgTrimmomaticWithAdapter.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_TRIMMOMATIC_WITHOUT_ADAPTER =
            String.format("%sgTrimmomaticWithoutAdapter.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_STAR_WITH_RSEM =
            String.format("%sgStarWithRsem.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_STAR_WITHOUT_RSEM =
            String.format("%sgStarWithoutRsem.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_HISAT2 =
            String.format("%sgHisat2.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_SALMON =
            String.format("%sgSalmon.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_FEATURE_COUNT =
            String.format("%sgFeatureCount.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_CUFFLINKS =
            String.format("%sgCufflinks.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_STRINGTIE =
            String.format("%sgStringtie.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_RSEM_WITHOUT_HISAT2 =
            String.format("%sgRsemWithoutHisat2.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_NON_FLAG_XENOME =
            String.format("%sgNonFlagXenome.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_NON_SEQPURGE =
            String.format("%sgNonSeqpurge.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_NON_TRIMMOMATIC =
            String.format("%sgNonTrimmomatic.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_NON_STAR =
            String.format("%sgNonStar.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_NON_HISAT2 =
            String.format("%sgNonHisat2.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_G_NON_SALMON =
            String.format("%sgNonSalmon.txt", RNA_EXPRESSION_FASTQ_SUFFIX);
    private static final String OUTPUT_SH_FILES_SUFFIX = "output/sh_files/";
    private static final String RNA_EXPRESSION_FASTQ_SEQPURGE_WITH_ADAPTERS_TEMPLATE =
            String.format("%srnaExpression_Fastq_Seqpurge_with_Adapters", RNA_EXPRESSION_FASTQ_TEMPLATES_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_SEQPURGE_WITHOUT_ADAPTERS_TEMPLATE =
            String.format("%srnaExpression_Fastq_Seqpurge_without_Adapters", RNA_EXPRESSION_FASTQ_TEMPLATES_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_TRIMMOMATIC_WITH_ADAPTER_TEMPLATE =
            String.format("%srnaExpression_Fastq_Trimmomatic_with_adapter", RNA_EXPRESSION_FASTQ_TEMPLATES_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_TRIMMOMATIC_WITHOUT_ADAPTER_TEMPLATE =
            String.format("%srnaExpression_Fastq_Trimmomatic_without_adapter", RNA_EXPRESSION_FASTQ_TEMPLATES_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_CUFFLINKS_SUFFIX =
            String.format("%sRnaExpressionFastqCufflinks/", RNA_EXPRESSION_FASTQ_TEMPLATES_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_FEATURE_COUNT_SUFFIX =
            String.format("%sRnaExpressionFastqFeatureCount/", RNA_EXPRESSION_FASTQ_TEMPLATES_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_STAR_WITH_RSEM_SUFFIX =
            String.format("%sRnaExpressionFastqStarWithRsem/", RNA_EXPRESSION_FASTQ_TEMPLATES_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_STRINGTIE_SUFFIX =
            String.format("%sRnaExpressionFastqStringtie/", RNA_EXPRESSION_FASTQ_TEMPLATES_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_STAR_WITHOUT_RSEM_SUFFIX =
            String.format("%sRnaExpressionFastqStarWithoutRsem/", RNA_EXPRESSION_FASTQ_TEMPLATES_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_RSEM_WITHOUT_HISAT2_SUFFIX =
            String.format("%sRnaExpressionFastqRsemWithoutHisat2/", RNA_EXPRESSION_FASTQ_TEMPLATES_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_HISAT2_TEMPLATE =
            String.format("%srnaExpression_Fastq_Hisat2", RNA_EXPRESSION_FASTQ_TEMPLATES_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_SALMON_TEMPLATE =
            String.format("%srnaExpression_Fastq_Salmon", RNA_EXPRESSION_FASTQ_TEMPLATES_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_NON_FLAG_XENOME_TEMPLATE =
            String.format("%srnaExpression_Fastq_non_flag_Xenome", RNA_EXPRESSION_FASTQ_TEMPLATES_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_NON_SEQPURGE_TEMPLATE =
            String.format("%srnaExpression_Fastq_non_Seqpurge", RNA_EXPRESSION_FASTQ_TEMPLATES_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_NON_TRIMMOMATIC_TEMPLATE =
            String.format("%srnaExpression_Fastq_non_Trimmomatic", RNA_EXPRESSION_FASTQ_TEMPLATES_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_NON_STAR_TEMPLATE =
            String.format("%srnaExpression_Fastq_non_Star", RNA_EXPRESSION_FASTQ_TEMPLATES_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_NON_HISAT2_TEMPLATE =
            String.format("%srnaExpression_Fastq_non_Hisat2", RNA_EXPRESSION_FASTQ_TEMPLATES_SUFFIX);
    private static final String RNA_EXPRESSION_FASTQ_NON_SALMON_TEMPLATE =
            String.format("%srnaExpression_Fastq_non_Salmon", RNA_EXPRESSION_FASTQ_TEMPLATES_SUFFIX);
    private TemplateEngine templateEngine = TemplateEngineUtils.init();
    private Context context = new Context();

    @BeforeEach
    public void setup() {
        context = new Context();
        context.setVariable("jarPath", getExecutionPath());
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
                        String.format("%sRnaExpression_Fastq_alignment_for_smv1_analysis",
                                RNA_EXPRESSION_FASTQ_STAR_WITH_RSEM_SUFFIX)),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_STAR_WITH_RSEM,
                        String.format("%sRnaExpression_Fastq_qcsummary_for_cohort_analysis.sh",
                                OUTPUT_SH_FILES_SUFFIX),
                        String.format("%sRnaExpression_Fastq_qcsummary_for_cohort_analysis",
                                RNA_EXPRESSION_FASTQ_STAR_WITH_RSEM_SUFFIX)),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_STAR_WITH_RSEM,
                        String.format("%sRnaExpression_Fastq_rsem_for_smv1_analysis.sh",
                                OUTPUT_SH_FILES_SUFFIX),
                        String.format("%sRnaExpression_Fastq_rsem_for_smv1_analysis",
                                RNA_EXPRESSION_FASTQ_STAR_WITH_RSEM_SUFFIX)),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_STAR_WITHOUT_RSEM,
                        OUTPUT_SH_FILE,
                        String.format("%sRnaExpression_Fastq_alignment_for_smv1_analysis",
                                RNA_EXPRESSION_FASTQ_STAR_WITHOUT_RSEM_SUFFIX)),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_STAR_WITHOUT_RSEM,
                        String.format("%sRnaExpression_Fastq_qcsummary_for_cohort_analysis.sh",
                                OUTPUT_SH_FILES_SUFFIX),
                        String.format("%sRnaExpression_Fastq_qcsummary_for_cohort_analysis",
                                RNA_EXPRESSION_FASTQ_STAR_WITHOUT_RSEM_SUFFIX)),
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
                        String.format("%sRnaExpression_Fastq_featureCount_for_smv1_analysis.sh",
                                OUTPUT_SH_FILES_SUFFIX),
                        String.format("%sRnaExpression_Fastq_featureCount_for_smv1_analysis",
                                RNA_EXPRESSION_FASTQ_FEATURE_COUNT_SUFFIX)),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_CUFFLINKS,
                        String.format("%sRnaExpression_Fastq_cufflinks_for_smv1_analysis.sh",
                                OUTPUT_SH_FILES_SUFFIX),
                        RNA_EXPRESSION_FASTQ_CUFFLINKS_SUFFIX +
                                "RnaExpression_Fastq_cufflinks_for_smv1_analysis"),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_STRINGTIE,
                        String.format("%sRnaExpression_Fastq_stringtie_for_smv1_analysis.sh",
                                OUTPUT_SH_FILES_SUFFIX),
                        String.format("%sRnaExpression_Fastq_stringtie_for_smv1_analysis",
                                RNA_EXPRESSION_FASTQ_STRINGTIE_SUFFIX)),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_RSEM_WITHOUT_HISAT2,
                        String.format("%sRnaExpression_Fastq_rsem_for_smv1_analysis.sh",
                                OUTPUT_SH_FILES_SUFFIX),
                        String.format("%sRnaExpression_Fastq_rsem_for_smv1_analysis",
                                RNA_EXPRESSION_FASTQ_RSEM_WITHOUT_HISAT2_SUFFIX)),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_RSEM_WITHOUT_HISAT2,
                        OUTPUT_SH_FILE,
                        String.format("%sRnaExpression_Fastq_alignment_for_smv1_analysis",
                                RNA_EXPRESSION_FASTQ_RSEM_WITHOUT_HISAT2_SUFFIX)),
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_RSEM_WITHOUT_HISAT2,
                        String.format("%sRnaExpression_Fastq_qcsummary_for_cohort_analysis.sh",
                                OUTPUT_SH_FILES_SUFFIX),
                        String.format("%sRnaExpression_Fastq_qcsummary_for_cohort_analysis",
                                RNA_EXPRESSION_FASTQ_RSEM_WITHOUT_HISAT2_SUFFIX)),
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
