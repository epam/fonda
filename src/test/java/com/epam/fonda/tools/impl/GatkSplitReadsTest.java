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

import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class GatkSplitReadsTest {
    private static final String GATK_SPLIT_READS_TOOL_TEST_TEMPLATE_NAME =
            "templates/gatk_split_reads_tool_test_output_data.txt";
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private Configuration expectedConfoguration;
    private String expectedCmd;


    @BeforeEach
    void setup() throws URISyntaxException, IOException {
        expectedConfoguration = buildConfiguration();
        Path path = Paths.get(this.getClass().getClassLoader()
                .getResource(GATK_SPLIT_READS_TOOL_TEST_TEMPLATE_NAME).toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        expectedCmd = new String(fileBytes);
    }

    private Configuration buildConfiguration() {
        Configuration expectedConfoguration = new Configuration();
        GlobalConfig.DatabaseConfig databaseConfig = new GlobalConfig.DatabaseConfig();
        databaseConfig.setGenome("genome");
        GlobalConfig.ToolConfig toolConfig = new GlobalConfig.ToolConfig();
        toolConfig.setJava("java");
        toolConfig.setGatk("gatk");
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setDatabaseConfig(databaseConfig);
        globalConfig.setToolConfig(toolConfig);
        expectedConfoguration.setGlobalConfig(globalConfig);
        return expectedConfoguration;
    }

    @Test
    void generate() {
        BamResult bamResult = new GatkSplitReads("outDir", "GA5.star.sorted.rmdup.bam")
                .generate(expectedConfoguration, expectedTemplateEngine);
        String actualCmd = bamResult.getCommand().getToolCommand();
        Assert.assertEquals(expectedCmd, actualCmd);
    }
}
