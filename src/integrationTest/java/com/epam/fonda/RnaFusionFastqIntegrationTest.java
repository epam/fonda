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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RnaFusionFastqIntegrationTest extends AbstractIntegrationTest {
    private static final String OUTPUT_SH_FILE = "output/sh_files/RnaFusion_Fastq_fusion_for_smv1_analysis.sh";
    private static final String OUTPUT_DIR_ROOT = "build/resources/integrationTest/";
    private static final String S_CONFIG_PATH = "RnaFusionFastq/sRnaFusionFastq.txt";
    private static final String SAMPLE_NAME = "smv1/";
    private static final String TEMPLATE_FOLDER = "rnaFusionFastq_templates";
    private static final String RNA_FUSION_XENOME_YES =
            String.format("%s/rnaFusionFastq_Flag_Xenome_Yes_template.txt", TEMPLATE_FOLDER);
    private static final String RNA_FUSION_NON_FLAG_XENOME =
            String.format("%s/rnaFusionFastq_Non_Flag_Xenome_template.txt", TEMPLATE_FOLDER);
    private static final String RNA_FUSION_NON_FUSION_CATCHER =
            String.format("%s/rnaFusionFastq_Non_Fusion_Catcher.txt", TEMPLATE_FOLDER);
    private static final String RNA_FUSION_NON_SEQPURGE = RNA_FUSION_NON_FUSION_CATCHER;
    private static final String RNA_FUSION_NON_STAR_FUSION = RNA_FUSION_NON_FUSION_CATCHER;
    private static final String RNA_FUSION_NON_TRIMMOMATIC =
            String.format("%s/rnaFusionFastq_Non_Trimmomatic_template.txt", TEMPLATE_FOLDER);
    private static final String RNA_FUSION_SEQPURGE_WITH_ADAPTERS =
            String.format("%s/rnaFusionFastq_Seqpurge_With_Adapters_template.txt", TEMPLATE_FOLDER);
    private static final String RNA_FUSION_SEQPURGE_WITHOUT_ADAPTERS =
            String.format("%s/rnaFusionFastq_Seqpurge_Without_Adapters_template.txt", TEMPLATE_FOLDER);
    private static final String RNA_FUSION_STARFUSION_FUSION_CATCHER =
            String.format("%s/rnaFusionFastq_StarFusion_And_FusionCatcher_template.txt", TEMPLATE_FOLDER);
    private static final String RNA_FUSION_TRIMMOMATIC_WITH_ADAPTER =
            String.format("%s/rnaFusionFastq_Trimmomatic_With_Adapter_template.txt", TEMPLATE_FOLDER);
    private static final String RNA_FUSION_TRIMMOMATIC_WITHOUT_ADAPTER =
            String.format("%s/rnaFusionFastq_Trimmomatic_Without_Adapter_template.txt", TEMPLATE_FOLDER);
    private static final String RNA_FUSION_FASTQ_G_STAR_FUSION_AND_FUSION_CATCHER_TXT =
            "RnaFusionFastq/gStarFusionAndFusionCatcher.txt";
    private static final String NULL = "null";

    @ParameterizedTest
    @MethodSource("initGlobalConfigAndTemplatePath")
    public void testWorkflowOutput(String globalConfigPath, String templatePath)
            throws IOException, URISyntaxException {
        startAppWithConfigs(globalConfigPath, S_CONFIG_PATH);

        String expectedCmd = templateEngine.process(templatePath, context);
        String actualCmd = getCmd(OUTPUT_SH_FILE);

        assertEquals(expectedCmd.trim(), actualCmd.trim());
        assertFalse(getCmd(OUTPUT_SH_FILE).contains(NULL));
    }

    @Test
    public void testCreateRnaFusionFastqSpecificDirFusionCatcherStarFusionToolset() {
        startAppWithConfigs(RNA_FUSION_FASTQ_G_STAR_FUSION_AND_FUSION_CATCHER_TXT, S_CONFIG_PATH);
        assertAll(
            () -> assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + "sh_files").exists()),
            () -> assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + "log_files").exists()),
            () -> assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + "err_files").exists()),
            () -> assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + SAMPLE_NAME).exists()),
            () -> assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + SAMPLE_NAME + "tmp").exists()),
            () -> assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + SAMPLE_NAME + "fastq").exists()),
            () -> assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + SAMPLE_NAME + "bam").exists()),
            () -> assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + SAMPLE_NAME + "qc").exists()),
            () -> assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + SAMPLE_NAME + "starFusion").exists()),
            () -> assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + SAMPLE_NAME + "fusionCatcher").exists()),
            () -> assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + SAMPLE_NAME + "fusionCatcher/tmp")
                    .exists())
        );
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initGlobalConfigAndTemplatePath() {
        return Stream.of(
                Arguments.of("RnaFusionFastq/gFlagXenomeYes.txt", RNA_FUSION_XENOME_YES),
                Arguments.of("RnaFusionFastq/gNonFlagXenome.txt", RNA_FUSION_NON_FLAG_XENOME),
                Arguments.of("RnaFusionFastq/gNonFusionCatcher.txt", RNA_FUSION_NON_FUSION_CATCHER),
                Arguments.of("RnaFusionFastq/gNonSeqpurge.txt", RNA_FUSION_NON_SEQPURGE),
                Arguments.of("RnaFusionFastq/gNonStarFusion.txt", RNA_FUSION_NON_STAR_FUSION),
                Arguments.of("RnaFusionFastq/gNonTrimmomatic.txt", RNA_FUSION_NON_TRIMMOMATIC),
                Arguments.of("RnaFusionFastq/gSeqpurgeWithAdapters.txt", RNA_FUSION_SEQPURGE_WITH_ADAPTERS),
                Arguments.of("RnaFusionFastq/gSeqpurgeWithoutAdapters.txt", RNA_FUSION_SEQPURGE_WITHOUT_ADAPTERS),
                Arguments.of("RnaFusionFastq/gStarFusionAndFusionCatcher.txt", RNA_FUSION_STARFUSION_FUSION_CATCHER),
                Arguments.of("RnaFusionFastq/gTrimmomaticWithAdapter.txt", RNA_FUSION_TRIMMOMATIC_WITH_ADAPTER),
                Arguments.of("RnaFusionFastq/gTrimmomaticWithoutAdapter.txt", RNA_FUSION_TRIMMOMATIC_WITHOUT_ADAPTER)
        );
    }
}
