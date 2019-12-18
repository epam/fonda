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

import com.epam.fonda.entity.configuration.CommonOutdir;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.entity.configuration.GlobalConfigFormat;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.results.FastqOutput;
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

public class StarFusionTest extends AbstractTest {
    private static final String OUTPUT_FOLDER = "output";
    private static final String STAR_FUSION_TEST_OUTPUT_DATA_PATH = "templates/star_fusion_tool_test_output_data.txt";
    private StarFusion starFusion;
    private Configuration expectedConfiguration;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private String expectedCmd;

    @BeforeEach
    void setup() throws IOException, URISyntaxException {
        buildConfiguration();
        FastqFileSample expectedSample = FastqFileSample.builder()
                .bamOutdir(OUTPUT_FOLDER + "/bamOutdir")
                .name("sampleName")
                .sampleOutputDir(OUTPUT_FOLDER + "/sampleOutputDir")
                .build();
        expectedSample.createDirectory();
        FastqOutput fastqOutput = FastqOutput.builder()
                .mergedFastq1("mergedFastq1")
                .mergedFastq2("mergedFastq2")
                .build();
        starFusion = new StarFusion(expectedSample, fastqOutput);
        Path path = Paths.get(this.getClass().getClassLoader()
                .getResource(STAR_FUSION_TEST_OUTPUT_DATA_PATH).toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        expectedCmd = new String(fileBytes);
    }

    @Test
    void testGenerate() {
        String actualCmd = starFusion.generate(expectedConfiguration, expectedTemplateEngine)
                .getCommand().getToolCommand();
        assertEquals(expectedCmd, actualCmd);
    }

    private void buildConfiguration() {
        GlobalConfig.ToolConfig expectedToolConfig = new GlobalConfig.ToolConfig();
        expectedToolConfig.setJava("java");
        expectedToolConfig.setPicard("picard");
        expectedToolConfig.setStar("star");
        expectedToolConfig.setSamTools("samtools");
        expectedToolConfig.setStarFusion("starFusion");
        GlobalConfig.DatabaseConfig expectedDatabaseConfig = new GlobalConfig.DatabaseConfig();
        expectedDatabaseConfig.setStarIndex(GlobalConfigFormat.STARINDEX);
        expectedDatabaseConfig.setStarFusionLib("starFusionLib");
        GlobalConfig.QueueParameters expectedQueueParameters = new GlobalConfig.QueueParameters();
        expectedQueueParameters.setNumThreads(4);
        GlobalConfig expectedGlobalConfig = new GlobalConfig();
        expectedGlobalConfig.setQueueParameters(expectedQueueParameters);
        expectedGlobalConfig.setToolConfig(expectedToolConfig);
        expectedGlobalConfig.setDatabaseConfig(expectedDatabaseConfig);
        expectedConfiguration = new Configuration();
        expectedConfiguration.setGlobalConfig(expectedGlobalConfig);
        CommonOutdir commonOutdir = new CommonOutdir(OUTPUT_FOLDER);
        commonOutdir.createDirectory();
        expectedConfiguration.setCommonOutdir(commonOutdir);
    }
}
