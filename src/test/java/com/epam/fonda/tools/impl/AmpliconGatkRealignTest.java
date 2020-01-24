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
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AmpliconGatkRealignTest extends AbstractTest {
    private static final String AMPLICON_GATK_REALIGN_TOOL_TEST_OUTPUT_DATA_PATH =
            "templates/amplicon_gatk_realign_tool_test_output_data.txt";
    private Configuration expectedConfiguration;
    private FastqFileSample expectedSample;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private BamResult bamResult;

    @BeforeEach
    void setup() {
        constructExpectedSample();
        expectedConfiguration = new Configuration();
        GlobalConfig expectedGlobalConfig = new GlobalConfig();
        constructToolConfig(expectedGlobalConfig);
        constructDatabaseConfig(expectedGlobalConfig);
        expectedConfiguration.setGlobalConfig(expectedGlobalConfig);
        BamOutput bamOutput = BamOutput.builder()
                .bam("sbamOutdir/sampleName.toolName.sorted.file.bam")
                .build();
        bamResult = BamResult.builder()
                .bamOutput(bamOutput)
                .command(BashCommand.withTool(""))
                .build();
    }

    @Test
    void generate() throws URISyntaxException, IOException {
        AmpliconGatkRealign ampliconGatkRealign = new AmpliconGatkRealign(expectedSample, bamResult);
        Path path = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource(AMPLICON_GATK_REALIGN_TOOL_TEST_OUTPUT_DATA_PATH)).toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        String expectedCmd = new String(fileBytes);
        bamResult = ampliconGatkRealign.generate(expectedConfiguration, expectedTemplateEngine);
        assertEquals(expectedCmd, bamResult.getCommand().getToolCommand());
    }

    private void constructExpectedSample() {
        expectedSample = new FastqFileSample();
        expectedSample.setName("sampleName");
        expectedSample.setTmpOutdir("stmpOutdir");
    }

    private void constructToolConfig(GlobalConfig expectedGlobalConfig) {
        GlobalConfig.ToolConfig expectedToolConfig = new GlobalConfig.ToolConfig();
        expectedToolConfig.setJava("java");
        expectedToolConfig.setGatk("gatk");
        expectedGlobalConfig.setToolConfig(expectedToolConfig);
    }

    private void constructDatabaseConfig(GlobalConfig expectedGlobalConfig) {
        GlobalConfig.DatabaseConfig expectedDatabaseConfig = new GlobalConfig.DatabaseConfig();
        expectedDatabaseConfig.setGenome("genome");
        expectedDatabaseConfig.setKnownIndelsMills("knownIndelsMills");
        expectedDatabaseConfig.setKnownIndelsPhase1("knownIndelsPhase1");
        expectedDatabaseConfig.setDbsnp("dbsnp");
        expectedGlobalConfig.setDatabaseConfig(expectedDatabaseConfig);
    }
}
