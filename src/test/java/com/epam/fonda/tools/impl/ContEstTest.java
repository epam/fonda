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

class ContEstTest extends AbstractTest {
    private static final String CONTEST_TEST_OUTPUT_DATA_PATH = "templates/contest_tool_test_output_data.txt";
    private ContEst contEst;
    private Configuration expectedConfiguration;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private String expectedCmd;

    @BeforeEach
    void setup() throws IOException, URISyntaxException {
        FastqFileSample expectedSample = FastqFileSample.builder()
                .sampleOutputDir("output")
                .name("sampleName")
                .build();
        expectedSample.createDirectory();
        buildConfiguration();
        BamResult expectedBamResult = BamResult.builder()
                .bamOutput(BamOutput.builder()
                        .bam("bam")
                        .controlBam("controlBam")
                        .build())
                .build();
        contEst = new ContEst(expectedSample.getName(), expectedSample.getSampleOutputDir(), expectedBamResult);
        Path path = Paths.get(this.getClass().getClassLoader()
                .getResource(CONTEST_TEST_OUTPUT_DATA_PATH).toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        expectedCmd = new String(fileBytes);
    }

    @Test
    void testGenerate() {
        String actualCmd = contEst.generate(expectedConfiguration, expectedTemplateEngine).getCommand()
                .getToolCommand();
        assertEquals(expectedCmd, actualCmd);
    }

    private void buildConfiguration() {
        GlobalConfig.ToolConfig expectedToolConfig = new GlobalConfig.ToolConfig();
        expectedToolConfig.setJava("java");
        expectedToolConfig.setGatk("gatk");
        GlobalConfig.DatabaseConfig expectedDatabaseConfig = new GlobalConfig.DatabaseConfig();
        expectedDatabaseConfig.setGenome("genome");
        expectedDatabaseConfig.setContEstPopAF("popAF");
        expectedDatabaseConfig.setBed("bed");
        GlobalConfig expectedGlobalConfig = new GlobalConfig();
        expectedGlobalConfig.setToolConfig(expectedToolConfig);
        expectedGlobalConfig.setDatabaseConfig(expectedDatabaseConfig);
        expectedConfiguration = new Configuration();
        expectedConfiguration.setGlobalConfig(expectedGlobalConfig);
    }
}
