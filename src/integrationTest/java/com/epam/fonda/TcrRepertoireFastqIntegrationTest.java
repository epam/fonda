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

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TcrRepertoireFastqIntegrationTest extends AbstractIntegrationTest {

    private static final String OUTPUT_DIR_ROOT = "build/resources/integrationTest/";

    private static final String TCR_REPERTOIRE_FASTQ_TCR_DETECTION_FOR_SMV1_SH_FILE_PATH =
        "output/sh_files/TcrRepertoire_Fastq_TCR_detection_for_smv1_analysis.sh";
    private static final String TCR_REPERTOIRE_FASTQ_TCR_DETECTION_FOR_SMV2_SH_FILE_PATH =
        "output/sh_files/TcrRepertoire_Fastq_TCR_detection_for_smv2_analysis.sh";
    private static final String TCR_REPERTOIRE_FASTQ_TCR_DETECTION_FOR_SMV3_SH_FILE_PATH =
        "output/sh_files/TcrRepertoire_Fastq_TCR_detection_for_smv3_analysis.sh";
    private static final String TCR_REPERTOIRE_FASTQ_TCR_DETECTION_FOR_SMV4_SH_FILE_PATH =
        "output/sh_files/TcrRepertoire_Fastq_TCR_detection_for_smv4_analysis.sh";
    private static final String TCR_REPERTOIRE_FASTQ_TCR_DETECTION_FOR_UM_SH_FILE_PATH =
        "output/sh_files/TcrRepertoire_Fastq_TCR_detection_for_um_analysis.sh";

    private static final String TCR_REPERTOIRE_FASTQ_TCR_DETECTION_FOR_SMV1_TEMPLATE_PATH =
        "TcrRepertoireFastq/TcrRepertoire_Fastq_TCR_detection_for_smv1_template.txt";
    private static final String TCR_REPERTOIRE_FASTQ_TCR_DETECTION_FOR_SMV2_TEMPLATE_PATH =
        "TcrRepertoireFastq/TcrRepertoire_Fastq_TCR_detection_for_smv2_template.txt";
    private static final String TCR_REPERTOIRE_FASTQ_TCR_DETECTION_FOR_SMV3_TEMPLATE_PATH =
        "TcrRepertoireFastq/TcrRepertoire_Fastq_TCR_detection_for_smv3_template.txt";
    private static final String TCR_REPERTOIRE_FASTQ_TCR_DETECTION_FOR_SMV4_TEMPLATE_PATH =
        "TcrRepertoireFastq/TcrRepertoire_Fastq_TCR_detection_for_smv4_template.txt";
    private static final String TCR_REPERTOIRE_FASTQ_TCR_DETECTION_FOR_UM_TEMPLATE_PATH =
        "TcrRepertoireFastq/TcrRepertoire_Fastq_TCR_detection_for_um_template.txt";

    private static final String TCR_REPERTOIRE_FASTQ_GLOBAL_CONFIG_PATH =
        "TcrRepertoireFast/global_config_TcrRepertoire_Fastq_v1.1.txt";
    private static final String TCR_REPERTOIRE_FASTQ_STUDY_CONFIG_PATH =
        "TcrRepertoireFast/config_TcrRepertoire_Fastq_test.txt";


    @ParameterizedTest
    @MethodSource("initParameters")
    void testTcrRepertoireFastqWorkflow(String templatePath, String filePath) throws IOException, URISyntaxException {
        startAppWithConfigs(TCR_REPERTOIRE_FASTQ_GLOBAL_CONFIG_PATH, TCR_REPERTOIRE_FASTQ_STUDY_CONFIG_PATH);
        String expectedCmd = templateEngine.process(templatePath, context);
        assertEquals(expectedCmd.trim(), getCmd(filePath).trim());
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initParameters() {
        return Stream.of(
            Arguments.of(TCR_REPERTOIRE_FASTQ_TCR_DETECTION_FOR_SMV1_TEMPLATE_PATH,
                TCR_REPERTOIRE_FASTQ_TCR_DETECTION_FOR_SMV1_SH_FILE_PATH),
            Arguments.of(TCR_REPERTOIRE_FASTQ_TCR_DETECTION_FOR_SMV2_TEMPLATE_PATH,
                TCR_REPERTOIRE_FASTQ_TCR_DETECTION_FOR_SMV2_SH_FILE_PATH),
            Arguments.of(TCR_REPERTOIRE_FASTQ_TCR_DETECTION_FOR_SMV3_TEMPLATE_PATH,
                TCR_REPERTOIRE_FASTQ_TCR_DETECTION_FOR_SMV3_SH_FILE_PATH),
            Arguments.of(TCR_REPERTOIRE_FASTQ_TCR_DETECTION_FOR_SMV4_TEMPLATE_PATH,
                TCR_REPERTOIRE_FASTQ_TCR_DETECTION_FOR_SMV4_SH_FILE_PATH),
            Arguments.of(TCR_REPERTOIRE_FASTQ_TCR_DETECTION_FOR_UM_TEMPLATE_PATH,
                TCR_REPERTOIRE_FASTQ_TCR_DETECTION_FOR_UM_SH_FILE_PATH)
        );
    }

    @Test
    void testDir() {
        startAppWithConfigs(TCR_REPERTOIRE_FASTQ_GLOBAL_CONFIG_PATH, TCR_REPERTOIRE_FASTQ_STUDY_CONFIG_PATH);
        assertAll(
            () -> assertTrue(new File(format("%s%s/sh_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/log_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/err_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists())
        );
    }

    @ParameterizedTest
    @MethodSource("initParamsOfDirs")
    void testDirTree(String dirName) {
        startAppWithConfigs(TCR_REPERTOIRE_FASTQ_GLOBAL_CONFIG_PATH, TCR_REPERTOIRE_FASTQ_STUDY_CONFIG_PATH);
        assertAll(
            () -> assertTrue(new File(format("%s%s/%s", OUTPUT_DIR_ROOT, OUTPUT_DIR, dirName)).exists()),
            () -> assertTrue(new File(format("%s%s/%s/bam", OUTPUT_DIR_ROOT, OUTPUT_DIR, dirName)).exists()),
            () -> assertTrue(new File(format("%s%s/%s/fastq", OUTPUT_DIR_ROOT, OUTPUT_DIR, dirName)).exists()),
            () -> assertTrue(new File(format("%s%s/%s/mixcr", OUTPUT_DIR_ROOT, OUTPUT_DIR, dirName)).exists()),
            () -> assertTrue(new File(format("%s%s/%s/qc", OUTPUT_DIR_ROOT, OUTPUT_DIR, dirName)).exists()),
            () -> assertTrue(new File(format("%s%s/%s/tmp", OUTPUT_DIR_ROOT, OUTPUT_DIR, dirName)).exists())
        );
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initParamsOfDirs() {
        return Stream.of(
            Arguments.of("smv1"),
            Arguments.of("smv2"),
            Arguments.of("smv3"),
            Arguments.of("smv4"),
            Arguments.of("um")
        );
    }
}
