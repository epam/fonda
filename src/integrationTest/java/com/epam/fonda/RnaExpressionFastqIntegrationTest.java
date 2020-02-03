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

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class RnaExpressionFastqIntegrationTest extends AbstractIntegrationTest {

    private static final String OUTPUT_DIR = "output";
    private static final String RNA_EXPRESSION_FASTQ_TEMPLATES_SUFFIX = "templates/RnaExpressionFastq/";
    private static final String OUTPUT_SH_FILE = "output/sh_files/RnaExpression_Fastq_alignment_for_smv1_analysis.sh";
    private static final String S_CONFIG_PATH = "RnaExpressionFastq/sRnaExpressionFastq.txt";
    private static final String RNA_EXPRESSION_FASTQ_ALIGNMENT_FOR_SMV1_ANALYSIS_TEMPLATE_PATH =
            "rnaExpression_Fastq_alignment_flag_Xenome_yes_template";
    public static final String RNA_EXPRESSION_FASTQ_G_FLAG_XENOME_YES_TXT = "RnaExpressionFastq/gFlagXenomeYes.txt";
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
                Arguments.of(RNA_EXPRESSION_FASTQ_G_FLAG_XENOME_YES_TXT, OUTPUT_SH_FILE, RNA_EXPRESSION_FASTQ_ALIGNMENT_FOR_SMV1_ANALYSIS_TEMPLATE_PATH),
                Arguments.of("RnaExpressionFastq/gSeqpurgeWithAdapters.txt", OUTPUT_SH_FILE, "rnaExpression_Fastq_Seqpurge_with_Adapters"),
                Arguments.of("RnaExpressionFastq/gSeqpurgeWithoutAdapters.txt", OUTPUT_SH_FILE, "rnaExpression_Fastq_Seqpurge_without_Adapters"),
                Arguments.of("RnaExpressionFastq/gTrimmomaticWithAdapter.txt", OUTPUT_SH_FILE, "rnaExpression_Fastq_Trimmomatic_with_adapter"),
                Arguments.of("RnaExpressionFastq/gTrimmomaticWithoutAdapter.txt", OUTPUT_SH_FILE, "rnaExpression_Fastq_Trimmomatic_without_adapter"),
                Arguments.of("RnaExpressionFastq/gStarWithRsem.txt", OUTPUT_SH_FILE, "RnaExpressionFastqStarWithRsem/RnaExpression_Fastq_alignment_for_smv1_analysis"),
                Arguments.of("RnaExpressionFastq/gStarWithRsem.txt", "output/sh_files/RnaExpression_Fastq_qcsummary_for_cohort_analysis.sh", "RnaExpressionFastqStarWithRsem/RnaExpression_Fastq_qcsummary_for_cohort_analysis"),
                Arguments.of("RnaExpressionFastq/gStarWithRsem.txt", "output/sh_files/RnaExpression_Fastq_rsem_for_smv1_analysis.sh", "RnaExpressionFastqStarWithRsem/RnaExpression_Fastq_rsem_for_smv1_analysis"),
                Arguments.of("RnaExpressionFastq/gStarWithoutRsem.txt", OUTPUT_SH_FILE, "RnaExpressionFastqStarWithoutRsem/RnaExpression_Fastq_alignment_for_smv1_analysis"),
                Arguments.of("RnaExpressionFastq/gStarWithoutRsem.txt", "output/sh_files/RnaExpression_Fastq_qcsummary_for_cohort_analysis.sh", "RnaExpressionFastqStarWithoutRsem/RnaExpression_Fastq_qcsummary_for_cohort_analysis"),
                Arguments.of("RnaExpressionFastq/gHisat2.txt", OUTPUT_SH_FILE, "rnaExpression_Fastq_Hisat2"),
                Arguments.of("RnaExpressionFastq/gSalmon.txt", OUTPUT_SH_FILE, "rnaExpression_Fastq_Salmon"),
                Arguments.of("RnaExpressionFastq/gFeatureCount.txt", "output/sh_files/RnaExpression_Fastq_featureCount_for_smv1_analysis.sh", "RnaExpressionFastqFeatureCount/RnaExpression_Fastq_featureCount_for_smv1_analysis"),
                Arguments.of("RnaExpressionFastq/gCufflinks.txt", "output/sh_files/RnaExpression_Fastq_cufflinks_for_smv1_analysis.sh", "RnaExpressionFastqCufflinks/RnaExpression_Fastq_cufflinks_for_smv1_analysis"),
                Arguments.of("RnaExpressionFastq/gStringtie.txt", "output/sh_files/RnaExpression_Fastq_stringtie_for_smv1_analysis.sh", "RnaExpressionFastqStringtie/RnaExpression_Fastq_stringtie_for_smv1_analysis"),
                Arguments.of("RnaExpressionFastq/gRsemWithoutHisat2.txt", "output/sh_files/RnaExpression_Fastq_rsem_for_smv1_analysis.sh", "RnaExpressionFastqRsemWithoutHisat2/RnaExpression_Fastq_rsem_for_smv1_analysis"),
                Arguments.of("RnaExpressionFastq/gRsemWithoutHisat2.txt", OUTPUT_SH_FILE, "RnaExpressionFastqRsemWithoutHisat2/RnaExpression_Fastq_alignment_for_smv1_analysis"),
                Arguments.of("RnaExpressionFastq/gRsemWithoutHisat2.txt", "output/sh_files/RnaExpression_Fastq_qcsummary_for_cohort_analysis.sh", "RnaExpressionFastqRsemWithoutHisat2/RnaExpression_Fastq_qcsummary_for_cohort_analysis"),
                Arguments.of("RnaExpressionFastq/gNonFlagXenome.txt", OUTPUT_SH_FILE, "rnaExpression_Fastq_non_flag_Xenome"),
                Arguments.of("RnaExpressionFastq/gNonSeqpurge.txt", OUTPUT_SH_FILE, "rnaExpression_Fastq_non_Seqpurge"),
                Arguments.of("RnaExpressionFastq/gNonTrimmomatic.txt", OUTPUT_SH_FILE, "rnaExpression_Fastq_non_Trimmomatic"),
                Arguments.of("RnaExpressionFastq/gNonStar.txt", OUTPUT_SH_FILE, "rnaExpression_Fastq_non_Star"),
                Arguments.of("RnaExpressionFastq/gNonHisat2.txt", OUTPUT_SH_FILE, "rnaExpression_Fastq_non_Hisat2"),
                Arguments.of("RnaExpressionFastq/gNonSalmon.txt", OUTPUT_SH_FILE, "rnaExpression_Fastq_non_Salmon")
        );
    }

    @ParameterizedTest(name = "{2}-test")
    @MethodSource("initParameters")
    void testRnaExpressionFastq(String gConfigPath, String outputShFile, String templatePath) throws IOException, URISyntaxException {
        startAppWithConfigs(gConfigPath, S_CONFIG_PATH);
        String expectedCmd = templateEngine.process(templatePath, context);
        assertEquals(expectedCmd.trim(), getCmd(outputShFile).trim());
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }
}
