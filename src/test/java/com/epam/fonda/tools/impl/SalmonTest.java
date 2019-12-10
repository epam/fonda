/*
 * Copyright 2017-2019 Sanofi and EPAM Systems, Inc. (https://www.epam.com/)
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

import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.command.BashCommand;
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.results.FastqOutput;
import com.epam.fonda.tools.results.FastqResult;
import com.epam.fonda.tools.results.SalmonResult;
import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static com.epam.fonda.entity.configuration.GlobalConfigFormat.SALMONINDEX;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SalmonTest extends AbstractTest {
    private static final String SALMON_TEST_OUTPUT_DATA_PATH = "templates/salmon_tool_test_output_data.txt";
    private Salmon salmon;
    private Configuration expectedConfiguration;
    private BashCommand expectedBashCommand;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();

    @BeforeEach
    void setup() throws URISyntaxException, IOException {
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
        salmon = new Salmon(expectedSample, fastqResult);
        expectedConfiguration = new Configuration();
        GlobalConfig expectedGlobalConfig = new GlobalConfig();
        GlobalConfig.QueueParameters expectedQueueParameters = new GlobalConfig.QueueParameters();
        expectedQueueParameters.setNumThreads(5);
        GlobalConfig.ToolConfig expectedToolConfig = new GlobalConfig.ToolConfig();
        expectedToolConfig.setSalmon("salmon");
        expectedGlobalConfig.setQueueParameters(expectedQueueParameters);
        expectedGlobalConfig.setToolConfig(expectedToolConfig);
        GlobalConfig.DatabaseConfig expectedDatabaseConfig = new GlobalConfig.DatabaseConfig();
        expectedDatabaseConfig.setAnnotgene("annotgene");
        expectedDatabaseConfig.setSalmonIndex(SALMONINDEX);
        expectedGlobalConfig.setDatabaseConfig(expectedDatabaseConfig);
        expectedConfiguration.setGlobalConfig(expectedGlobalConfig);
        Path path = Paths.get(Objects.requireNonNull(
                this.getClass().getClassLoader().getResource(SALMON_TEST_OUTPUT_DATA_PATH)).toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        String expectedCmd = new String(fileBytes);
        expectedBashCommand = new BashCommand(expectedCmd);
    }

    @Test
    void shouldGenerate() {
        SalmonResult salmonResult = salmon.generate(expectedConfiguration, expectedTemplateEngine);
        assertEquals(expectedBashCommand.getToolCommand(), salmonResult.getCommand().getToolCommand());
    }
}
