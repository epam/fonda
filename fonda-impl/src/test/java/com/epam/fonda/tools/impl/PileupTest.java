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
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PileupTest extends AbstractTest {
    private static final String PILEUP_TEST_OUTPUT_DATA_PATH = "templates/pileup_tool_test_output_data.txt";
    private static final String OUTPUT_DIR = "output";
    private Pileup pileup;
    private Configuration expectedConfiguration;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private String expectedCmd;

    @BeforeEach
    void setUp() throws IOException, URISyntaxException {
        buildConfiguration();
        BamResult expectedBamResult = BamResult.builder()
                .bamOutput(BamOutput.builder()
                        .controlBam("controlBam")
                        .bam("bam")
                        .build())
                .build();
        FastqFileSample expectedSample = FastqFileSample.builder()
                .sampleOutputDir(OUTPUT_DIR)
                .controlName("controlName")
                .name("sampleName")
                .build();
        expectedSample.createDirectory();
        pileup = new Pileup(expectedSample.getName(), expectedSample.getSampleOutputDir(),
                expectedSample.getControlName(), expectedBamResult);
        Path path = Paths.get(this.getClass().getClassLoader()
                .getResource(PILEUP_TEST_OUTPUT_DATA_PATH).toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        expectedCmd = new String(fileBytes);
    }

    @Test
    void testGenerate() {
        String actualCmd = pileup.generate(expectedConfiguration, expectedTemplateEngine).getCommand().getToolCommand();
        assertEquals(expectedCmd, actualCmd);
    }

    private void buildConfiguration() {
        GlobalConfig.ToolConfig toolConfig = new GlobalConfig.ToolConfig();
        toolConfig.setSamTools("samtools");
        GlobalConfig.DatabaseConfig databaseConfig = new GlobalConfig.DatabaseConfig();
        databaseConfig.setGenome("genome");
        databaseConfig.setBed("bed");
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setToolConfig(toolConfig);
        globalConfig.setDatabaseConfig(databaseConfig);
        expectedConfiguration = new Configuration();
        expectedConfiguration.setGlobalConfig(globalConfig);
    }
}
