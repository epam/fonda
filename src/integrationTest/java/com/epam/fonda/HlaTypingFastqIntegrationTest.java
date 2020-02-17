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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HlaTypingFastqIntegrationTest extends AbstractIntegrationTest {

    private static final String OUTPUT_DIR = "output";
    private static final String G_CONFIG_PATH = "HlaTypingFastq/gPairedSeqpurgeOptiType.txt";
    private static final String S_CONFIG_PATH = "HlaTypingFastq/sHlaTypingFastq.txt";
    private static final String HLA_TYPE_FASTQ_SUFFIX = "HlaTypingFastq/";
    private static final String OUTPUT_SH_FILE_SUFFIX = "output/sh_files/";
    private static final String SH_EXTENSION_SUFFIX = ".sh";
    private static final String TXT_EXTENSION_SUFFIX = ".txt";
    private static final String TEMPLATE_OUTPUT_SMV1_SUFFIX = "HlaTyping_Fastq_hlatyping_for_smv1_analysis";
    private static final String TEMPLATE_OUTPUT_SMV2_SUFFIX = "HlaTyping_Fastq_hlatyping_for_smv2_analysis";
    private static final String TEMPLATE_OUTPUT_SMV3_SUFFIX = "HlaTyping_Fastq_hlatyping_for_smv3_analysis";
    private static final String TEMPLATE_OUTPUT_SMV5_SUFFIX = "HlaTyping_Fastq_hlatyping_for_smv5_analysis";
    private TemplateEngine templateEngine = TemplateEngineUtils.init();
    private Context context = new Context();

    @AfterEach
    public void cleanupDir() throws IOException {
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @ParameterizedTest(name = "{1}-test")
    @MethodSource("initParameters")
    void testHlaTypingFastq(final String outputShFile, final String templatePath)
            throws IOException, URISyntaxException {
        startAppWithConfigs(G_CONFIG_PATH, S_CONFIG_PATH);
        final String expectedCmd = templateEngine.process(templatePath, context);
        assertEquals(expectedCmd.trim(), getCmd(outputShFile).trim());
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initParameters() {
        return Stream.of(
                Arguments.of(
                        String.format("%s%s%s",
                                OUTPUT_SH_FILE_SUFFIX, TEMPLATE_OUTPUT_SMV1_SUFFIX, SH_EXTENSION_SUFFIX),
                        String.format("%s%s%s",
                                HLA_TYPE_FASTQ_SUFFIX, TEMPLATE_OUTPUT_SMV1_SUFFIX, TXT_EXTENSION_SUFFIX)
                ),
                Arguments.of(
                        String.format("%s%s%s",
                                OUTPUT_SH_FILE_SUFFIX, TEMPLATE_OUTPUT_SMV2_SUFFIX, SH_EXTENSION_SUFFIX),
                        String.format("%s%s%s",
                                HLA_TYPE_FASTQ_SUFFIX, TEMPLATE_OUTPUT_SMV2_SUFFIX, TXT_EXTENSION_SUFFIX)
                ),
                Arguments.of(
                        String.format("%s%s%s",
                                OUTPUT_SH_FILE_SUFFIX, TEMPLATE_OUTPUT_SMV3_SUFFIX, SH_EXTENSION_SUFFIX),
                        String.format("%s%s%s",
                                HLA_TYPE_FASTQ_SUFFIX, TEMPLATE_OUTPUT_SMV3_SUFFIX, TXT_EXTENSION_SUFFIX)
                ),
                Arguments.of(
                        String.format("%s%s%s",
                                OUTPUT_SH_FILE_SUFFIX, TEMPLATE_OUTPUT_SMV5_SUFFIX, SH_EXTENSION_SUFFIX),
                        String.format("%s%s%s",
                                HLA_TYPE_FASTQ_SUFFIX, TEMPLATE_OUTPUT_SMV5_SUFFIX, TXT_EXTENSION_SUFFIX)
                )
        );
    }

}
