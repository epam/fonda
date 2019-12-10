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

import com.epam.fonda.entity.command.BashCommand;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.results.FastqOutput;
import com.epam.fonda.tools.results.FastqResult;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class XenomeTest extends AbstractTest {
    private static final String XENOME_TEST_OUTPUT_DATA_PATH = "templates/xenome_tool_test_output_data.txt";
    private static final String XENOME_TEST_OUTPUT_DATA_INDEX_PATH = "templates/xenome_tool_test_output_data_index.txt";
    private Xenome xenome;
    private Configuration expectedConfiguration;
    private GlobalConfig expectedGlobalConfig;
    private GlobalConfig.ToolConfig expectedToolConfig;
    private FastqFileSample expectedSample;
    private String expectedCmd;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private FastqResult fastqResult;

    @BeforeEach
    void setup() throws URISyntaxException, IOException {
        expectedGlobalConfig = new GlobalConfig();
        expectedSample = new FastqFileSample();
        expectedSample.setName("sampleName");
        expectedSample.setFastqOutdir("sfqOutdir");
        expectedSample.setTmpOutdir("stmpOutdir");
        FastqOutput fastqOutput = FastqOutput.builder()
                .mergedFastq1("merged_fastq1")
                .mergedFastq2("merged_fastq2")
                .build();
        fastqResult = FastqResult.builder().command(BashCommand.withTool("")).out(fastqOutput).build();
        xenome = new Xenome(expectedSample, fastqResult, null);
        expectedConfiguration = new Configuration();
        expectedToolConfig = new GlobalConfig.ToolConfig();
        expectedToolConfig.setXenome("xenome");
        GlobalConfig.DatabaseConfig databaseConfig = new GlobalConfig.DatabaseConfig();
        databaseConfig.setMouseXenomeIndex("MOUSEXENOMEINDEX");
        expectedGlobalConfig.setToolConfig(expectedToolConfig);
        expectedGlobalConfig.setDatabaseConfig(databaseConfig);
        expectedConfiguration.setGlobalConfig(expectedGlobalConfig);
        Path path = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource(XENOME_TEST_OUTPUT_DATA_PATH)).toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        expectedCmd = new String(fileBytes);
    }

    @Test
    void testGenerate() {
        String actualCmd = xenome.generate(expectedConfiguration, expectedTemplateEngine).getCommand().getToolCommand();
        assertEquals(expectedCmd, actualCmd);
    }

    @Test
    void testGenerateIncorrect() {
        expectedToolConfig.setXenome("wrong");
        expectedGlobalConfig.setToolConfig(expectedToolConfig);
        expectedConfiguration.setGlobalConfig(expectedGlobalConfig);
        String actualCmd = xenome.generate(expectedConfiguration, expectedTemplateEngine).getCommand().getToolCommand();
        assertNotEquals(expectedCmd, actualCmd);
    }

    @Test
    void testGenerateWithIndex() throws IOException, URISyntaxException {
        GlobalConfig.DatabaseConfig expectedDatabaseConfig = new GlobalConfig.DatabaseConfig();
        expectedDatabaseConfig.setMouseXenomeIndex("MOUSEXENOMEINDEX");
        expectedGlobalConfig.setDatabaseConfig(expectedDatabaseConfig);
        expectedConfiguration.setGlobalConfig(expectedGlobalConfig);
        xenome = new Xenome(expectedSample, fastqResult, 1);
        Path path = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource(XENOME_TEST_OUTPUT_DATA_INDEX_PATH)).toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        String expectedCmd = new String(fileBytes);
        String actualCmd = xenome.generate(expectedConfiguration, expectedTemplateEngine).getCommand().getToolCommand();

        assertEquals(expectedCmd, actualCmd);
    }
}
