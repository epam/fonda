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
import com.epam.fonda.tools.results.VariantsVcfResult;
import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.junit.jupiter.api.Assertions.assertEquals;


class GatkHaplotypeCallerTest extends AbstractTest {
    private static final String GATK_HAPLOTYPE_CALLER_TOOL_TEST_TEMPLATE_NAME =
            "gatk_haplotype_caller_tool_test_output_data";
    private Configuration expectedConfiguration;
    private GlobalConfig expectedGlobalConfig;
    private GlobalConfig.DatabaseConfig expectedDatabaseConfig;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private VariantsVcfResult variantsVcfResult;

    @BeforeEach
    void setup() {
        expectedConfiguration = new Configuration();
        expectedGlobalConfig = new GlobalConfig();
        constructConfigs(expectedGlobalConfig);
        expectedConfiguration.setGlobalConfig(expectedGlobalConfig);
        final CommonOutdir commonOutdir = new CommonOutdir(TEST_DIRECTORY);
        commonOutdir.createDirectory();
    }

    @Test
    void shouldGenerateForGatkHaplotypeCallerUnpaired() {
        GatkHaplotypeCaller gatkHaplotypeCaller = new GatkHaplotypeCaller("samplename",
                "sbamOutdir/sampleName.toolName.sorted.file.bam", TEST_DIRECTORY);
        expectedDatabaseConfig.setBed("bed");
        Context context = new Context();
        context.setVariable("bed", expectedGlobalConfig.getDatabaseConfig().getBed());
        String expectedCmd = expectedTemplateEngine.process(GATK_HAPLOTYPE_CALLER_TOOL_TEST_TEMPLATE_NAME, context);
        variantsVcfResult = gatkHaplotypeCaller.generate(expectedConfiguration, expectedTemplateEngine);
        assertEquals(expectedCmd, variantsVcfResult.getAbstractCommand().getToolCommand());
    }

    @Test
    void shouldGenerateForWgsGatkHaplotypeCallerUnpaired() {
        GatkHaplotypeCaller gatkHaplotypeCaller = new GatkHaplotypeCaller("samplename",
                "sbamOutdir/sampleName.toolName.sorted.file.bam", TEST_DIRECTORY);
        expectedGlobalConfig.setDatabaseConfig(expectedDatabaseConfig);
        expectedConfiguration.getGlobalConfig().getPipelineInfo().setWorkflow("DnaWgsVar_Bam");
        Context context = new Context();
        context.setVariable("isWgs", true);
        String expectedCmd = expectedTemplateEngine.process(GATK_HAPLOTYPE_CALLER_TOOL_TEST_TEMPLATE_NAME, context);
        variantsVcfResult = gatkHaplotypeCaller.generate(expectedConfiguration, expectedTemplateEngine);
        assertEquals(expectedCmd, variantsVcfResult.getAbstractCommand().getToolCommand());
    }

    private void constructConfigs(GlobalConfig expectedGlobalConfig) {
        GlobalConfig.ToolConfig expectedToolConfig = new GlobalConfig.ToolConfig();
        expectedToolConfig.setJava("java");
        expectedToolConfig.setGatk("gatk");
        expectedDatabaseConfig = new GlobalConfig.DatabaseConfig();
        expectedDatabaseConfig.setGenome("genome");
        expectedGlobalConfig.setToolConfig(expectedToolConfig);
        expectedGlobalConfig.setDatabaseConfig(expectedDatabaseConfig);
        final GlobalConfig.PipelineInfo pipelineInfo = new GlobalConfig.PipelineInfo();
        pipelineInfo.setWorkflow("DnaCaptureVar_Bam");
        expectedGlobalConfig.setPipelineInfo(pipelineInfo);
    }
}
