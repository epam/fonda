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
import com.epam.fonda.tools.results.VariantsVcfOutput;
import com.epam.fonda.tools.results.VariantsVcfResult;
import com.epam.fonda.utils.PipelineUtils;
import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Objects;

class GatkHaplotypeCallerRnaFilterTest extends AbstractTest {
    private static final String GATK_HAPLOTYPE_CALLER_RNA_FILTER_TOOL_TEST_TEMPLATE_NAME =
            "templates/gatk_haplotype_caller_rna_filter_tool_test_output_data.txt";
    private Configuration expectedConfiguration;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private VariantsVcfResult variantsVcfResult;
    private String expectedCmd;

    @BeforeEach
    void setup() throws URISyntaxException, IOException {
        variantsVcfResult = VariantsVcfResult.builder()
                .variantsVcfOutput(VariantsVcfOutput.builder()
                        .variantsVcf("gatkHapRaw")
                        .build())
                .abstractCommand(BashCommand.withTool(""))
                .build();
        buildConfiguration();
        expectedCmd = readFile(Paths.get(Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource(GATK_HAPLOTYPE_CALLER_RNA_FILTER_TOOL_TEST_TEMPLATE_NAME)).toURI()));
        PipelineUtils.createDir(TEST_DIRECTORY);
    }

    @Test
    void generate() {
        String actualCmd = new GatkHaplotypeCallerRnaFilter("sampleName", TEST_DIRECTORY, variantsVcfResult).
                generate(expectedConfiguration, expectedTemplateEngine).getAbstractCommand().getToolCommand();
        Assertions.assertEquals(expectedCmd, actualCmd);
    }

    private void buildConfiguration() {
        GlobalConfig.DatabaseConfig databaseConfig = new GlobalConfig.DatabaseConfig();
        databaseConfig.setGenome("genome");
        GlobalConfig.ToolConfig toolConfig = new GlobalConfig.ToolConfig();
        toolConfig.setGatk("gatk");
        toolConfig.setJava("java");
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setToolConfig(toolConfig);
        globalConfig.setDatabaseConfig(databaseConfig);
        expectedConfiguration = new Configuration();
        expectedConfiguration.setGlobalConfig(globalConfig);
    }
}
