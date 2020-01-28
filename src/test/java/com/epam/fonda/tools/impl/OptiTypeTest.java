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

package com.epam.fonda.tools.impl;

import com.epam.fonda.entity.command.BashCommand;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.entity.configuration.StudyConfig;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.results.FastqOutput;
import com.epam.fonda.tools.results.FastqResult;
import com.epam.fonda.tools.results.OptiTypeResult;
import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OptiTypeTest extends AbstractTest {
    private static final String OPTY_TIPE_DNA_TEST_OUTPUT_DATA_PATH =
            "templates/optiType_tool_with_dna_lt_test_output_data.txt";
    private static final String OPTY_TIPE_RNA_TEST_OUTPUT_DATA_PATH =
            "templates/optiType_tool_with_rna_lt_test_output_data.txt";
    private static final String OPTY_TIPE_NO_LT_TEST_OUTPUT_DATA_PATH =
            "templates/optiType_tool_with_no_lt_test_output_data.txt";
    private OptiType optiType;
    private Configuration expectedConfiguration;
    private BashCommand expectedBashCommand;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();

    void setup(String libraryType, String pathToTemplate) throws URISyntaxException, IOException {
        optiType = initTool();
        expectedConfiguration = initConfiguration();
        expectedConfiguration.getStudyConfig().setLibraryType(libraryType);
        Path path = Paths.get(Objects.requireNonNull(
                this.getClass().getClassLoader().getResource(pathToTemplate)).toURI());
        String expectedCmd = readFile(path);
        expectedBashCommand = new BashCommand(expectedCmd);
    }

    @ParameterizedTest
    @MethodSource("initParameters")
    void shouldGenerate(String libraryType, String pathToTemplate)
            throws IOException, URISyntaxException
    {
        setup(libraryType, pathToTemplate);
        OptiTypeResult optiTypeResult = optiType.generate(expectedConfiguration, expectedTemplateEngine);
        assertEquals(expectedBashCommand.getToolCommand(), optiTypeResult.getCommand().getToolCommand());
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initParameters() {
        return Stream.of(
                Arguments.of("DNA", OPTY_TIPE_DNA_TEST_OUTPUT_DATA_PATH),
                Arguments.of("RNA", OPTY_TIPE_RNA_TEST_OUTPUT_DATA_PATH),
                Arguments.of("NULL", OPTY_TIPE_NO_LT_TEST_OUTPUT_DATA_PATH)
        );
    }

    private OptiType initTool() {
        FastqFileSample expectedSample = new FastqFileSample();
        expectedSample.setName("sampleName");
        expectedSample.setSampleOutputDir("output");
        expectedSample.createDirectory();
        FastqOutput fastqOutput = FastqOutput.builder()
                .mergedFastq1("merged_fastq1.gz")
                .mergedFastq2("merged_fastq2.gz")
                .build();
        FastqResult fastqResult = FastqResult.builder()
                .out(fastqOutput)
                .build();
        return new OptiType(expectedSample, fastqResult);
    }

    private Configuration initConfiguration() {
        Configuration configuration = new Configuration();
        GlobalConfig expectedGlobalConfig = new GlobalConfig();
        StudyConfig expectedStudyConfig = new StudyConfig();
        GlobalConfig.QueueParameters expectedQueueParameters = new GlobalConfig.QueueParameters();
        expectedQueueParameters.setNumThreads(5);
        GlobalConfig.ToolConfig expectedToolConfig = new GlobalConfig.ToolConfig();
        expectedToolConfig.setOptitype("optiType");
        expectedToolConfig.setPython("python");
        expectedGlobalConfig.setQueueParameters(expectedQueueParameters);
        expectedGlobalConfig.setToolConfig(expectedToolConfig);
        configuration.setGlobalConfig(expectedGlobalConfig);
        configuration.setStudyConfig(expectedStudyConfig);
        return configuration;
    }
}
