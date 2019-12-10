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
import com.epam.fonda.tools.results.FastqOutput;
import com.epam.fonda.tools.results.FastqResult;
import com.epam.fonda.tools.results.FusionCatcherResult;
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

class FusionCatcherTest extends AbstractTest {
    private static final String FUSION_CATCHER_TEST_OUTPUT_DATA_PATH =
            "templates/fusion_catcher_tool_test_output_data.txt";

    private FusionCatcher fusionCatcher;
    private Configuration expectedConfiguration;
    private String expectedCmd;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();

    @BeforeEach
    void setup() throws IOException, URISyntaxException {
        FastqFileSample expectedSample = FastqFileSample.builder()
                .sampleOutputDir("output")
                .build();
        expectedSample.createDirectory();
        FastqOutput fastqOutput = FastqOutput.builder()
                .mergedFastq1("mergedFastq1")
                .mergedFastq2("mergedFastq2")
                .build();
        FastqResult fastqResult = FastqResult.builder()
                .out(fastqOutput)
                .build();
        fusionCatcher = new FusionCatcher(expectedSample, fastqResult);
        buildConfiguration();
        Path path = Paths.get(this.getClass().getClassLoader().
                getResource(FUSION_CATCHER_TEST_OUTPUT_DATA_PATH).toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        expectedCmd = new String(fileBytes);
    }

    @Test
    void testGenerate() {
        FusionCatcherResult fusionCatcherResult = fusionCatcher.generate(expectedConfiguration, expectedTemplateEngine);
        assertEquals(expectedCmd, fusionCatcherResult.getCommand().getToolCommand());
    }

    private void buildConfiguration() {
        expectedConfiguration = new Configuration();
        GlobalConfig expectedGlobalConfig = new GlobalConfig();
        GlobalConfig.ToolConfig expectedToolConfig = new GlobalConfig.ToolConfig();
        expectedToolConfig.setFusionCatcher("fusionCatcher");
        GlobalConfig.QueueParameters expectedQueueParameters = new GlobalConfig.QueueParameters();
        expectedQueueParameters.setNumThreads(4);
        expectedGlobalConfig.setToolConfig(expectedToolConfig);
        expectedGlobalConfig.setQueueParameters(expectedQueueParameters);
        expectedConfiguration.setGlobalConfig(expectedGlobalConfig);
    }
}
