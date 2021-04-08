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

public class RnaExpressionBamIntegrationTest extends AbstractIntegrationTest {

    private static final String RNA_EXPRESSION_BAM_G_FEATURE_COUNT_RSEM_CUFFLINKS_STRINGTIE =
            "RnaExpressionBam/gFeatureCountRsemCufflinksStringtie.txt";
    private static final String RNA_EXPRESSION_BAM_S_RNA_EXPRESSION_BAM =
            "RnaExpressionBam/sRnaExpressionBam.txt";
    private static final String OUTPUT_SH_FILES_RNA_EXPRESSION_BAM_FEATURE_COUNT_FOR_GA_5_ANALYSIS =
            "output/sh_files/RnaExpression_Bam_featureCount_for_GA5_analysis.sh";
    private static final String OUTPUT_SH_FILES_RNA_EXPRESSION_BAM_CUFFLINKS_FOR_GA_5_ANALYSIS =
            "output/sh_files/RnaExpression_Bam_cufflinks_for_GA5_analysis.sh";
    private static final String OUTPUT_SH_FILES_RNA_EXPRESSION_BAM_RSEM_FOR_GA_5_ANALYSIS =
            "output/sh_files/RnaExpression_Bam_rsem_for_GA5_analysis.sh";
    private static final String OUTPUT_SH_FILES_RNA_EXPRESSION_BAM_STRINGTIE_FOR_GA_5_ANALYSIS =
            "output/sh_files/RnaExpression_Bam_stringtie_for_GA5_analysis.sh";
    private static final String OUTPUT_SH_FILES_RNA_EXPRESSION_BAM_EXPRESSION_ESTIMATION_FOR_GA_5_ANALYSIS =
            "output/sh_files/RnaExpression_Bam_ExpressionEstimation_for_GA5_analysis.sh";
    private static final String RNA_EXPRESSION_BAM_SUFFIX = "RnaExpressionBam";
    private static final String RNA_EXPRESSION_BAM_FEATURE_COUNT_FOR_GA_5_ANALYSIS_TEMPLATE_PATH =
            format("%s/RnaExpression_Bam_featureCount_for_GA5_analysis", RNA_EXPRESSION_BAM_SUFFIX);
    private static final String RNA_EXPRESSION_BAM_CUFFLINKS_FOR_GA_5_ANALYSIS_TEMPLATE_PATH =
            format("%s/RnaExpression_Bam_cufflinks_for_GA5_analysis",
                    RNA_EXPRESSION_BAM_SUFFIX);
    private static final String RNA_EXPRESSION_BAM_RSEM_FOR_GA_5_ANALYSIS_TEMPLATE_PATH =
            format("%s/RnaExpression_Bam_rsem_for_GA5_analysis",
                    RNA_EXPRESSION_BAM_SUFFIX);
    private static final String RNA_EXPRESSION_BAM_STRINGTIE_FOR_GA_5_ANALYSIS_TEMPLATE_PATH =
            format("%s/RnaExpression_Bam_stringtie_for_GA5_analysis",
                    RNA_EXPRESSION_BAM_SUFFIX);
    private static final String RNA_EXPRESSION_BAM_EXPRESSION_ESTIMATION_FOR_GA_5_ANALYSIS_TEMPLATE_PATH =
            format("%s/RnaExpression_Bam_ExpressionEstimation_for_GA5_analysis",
                    RNA_EXPRESSION_BAM_SUFFIX);

    @ParameterizedTest(name = "{0}-test")
    @MethodSource("initParameters")
    public void testFeatureCountRsemCufflinksStringtie(String outputShFile, String templatePath)
            throws IOException, URISyntaxException {
        startAppWithConfigs(
                RNA_EXPRESSION_BAM_G_FEATURE_COUNT_RSEM_CUFFLINKS_STRINGTIE,
                RNA_EXPRESSION_BAM_S_RNA_EXPRESSION_BAM);
        String expectedCmd = TEMPLATE_ENGINE.process(templatePath, context);
        assertEquals(expectedCmd.trim(), getCmd(outputShFile).trim());
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initParameters() {
        return Stream.of(
                Arguments.of(
                        OUTPUT_SH_FILES_RNA_EXPRESSION_BAM_FEATURE_COUNT_FOR_GA_5_ANALYSIS,
                        RNA_EXPRESSION_BAM_FEATURE_COUNT_FOR_GA_5_ANALYSIS_TEMPLATE_PATH),
                Arguments.of(
                        OUTPUT_SH_FILES_RNA_EXPRESSION_BAM_CUFFLINKS_FOR_GA_5_ANALYSIS,
                        RNA_EXPRESSION_BAM_CUFFLINKS_FOR_GA_5_ANALYSIS_TEMPLATE_PATH),
                Arguments.of(
                        OUTPUT_SH_FILES_RNA_EXPRESSION_BAM_RSEM_FOR_GA_5_ANALYSIS,
                        RNA_EXPRESSION_BAM_RSEM_FOR_GA_5_ANALYSIS_TEMPLATE_PATH),
                Arguments.of(
                        OUTPUT_SH_FILES_RNA_EXPRESSION_BAM_STRINGTIE_FOR_GA_5_ANALYSIS,
                        RNA_EXPRESSION_BAM_STRINGTIE_FOR_GA_5_ANALYSIS_TEMPLATE_PATH),
                Arguments.of(
                        OUTPUT_SH_FILES_RNA_EXPRESSION_BAM_EXPRESSION_ESTIMATION_FOR_GA_5_ANALYSIS,
                        RNA_EXPRESSION_BAM_EXPRESSION_ESTIMATION_FOR_GA_5_ANALYSIS_TEMPLATE_PATH)
        );
    }
}
