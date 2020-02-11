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
import com.epam.fonda.workflow.PipelineType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;


class GatkHaplotypeCallerTest extends AbstractTest {
    private static final String GATK_HAPLOTYPE_CALLER_DNA_TOOL_TEST_TEMPLATE_NAME =
            "gatk_haplotype_caller_dna_tool_test_output_data";
    private static final String GATK_HAPLOTYPE_CALLER_RNA_TOOL_TEST_TEMPLATE_NAME =
            "templates/gatk_haplotype_caller_rna_tool_test_output_data.txt";
    private Configuration expectedConfiguration;
    private VariantsVcfResult variantsVcfResult;
    private String sampleName = "samplename";
    private String bam = "sbamOutdir/sampleName.toolName.sorted.file.bam";
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private Context context;
    private String expectedCmd;
    private GatkHaplotypeCaller gatkHaplotypeCaller;

    @BeforeEach
    void setup() {
        context = new Context();
        buildConfiguration();
        final CommonOutdir commonOutdir = new CommonOutdir(TEST_DIRECTORY);
        gatkHaplotypeCaller = new GatkHaplotypeCaller(sampleName, bam, TEST_DIRECTORY, false);
        commonOutdir.createDirectory();
    }

    @Test
    void shouldGenerateForGatkHaplotypeCallerUnpairedForRna() throws URISyntaxException, IOException {
        expectedConfiguration.getGlobalConfig().getPipelineInfo()
                .setWorkflow(PipelineType.RNA_CAPTURE_VAR_FASTQ.getName());
        expectedCmd = readFile(Paths.get(Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource(GATK_HAPLOTYPE_CALLER_RNA_TOOL_TEST_TEMPLATE_NAME)).toURI()));
        String actualCmd = new GatkHaplotypeCaller(sampleName, bam, TEST_DIRECTORY, true)
                .generate(expectedConfiguration, expectedTemplateEngine).getAbstractCommand().getToolCommand();
        assertEquals(expectedCmd, actualCmd);
    }

    @Test
    void shouldGenerateForGatkHaplotypeCallerUnpairedForDna() {
        expectedCmd = expectedTemplateEngine.process(GATK_HAPLOTYPE_CALLER_DNA_TOOL_TEST_TEMPLATE_NAME, context);
        variantsVcfResult = gatkHaplotypeCaller.generate(expectedConfiguration, expectedTemplateEngine);
        assertEquals(expectedCmd.trim(), variantsVcfResult.getAbstractCommand().getToolCommand().trim());
    }

    @Test
    void shouldGenerateForWgsGatkHaplotypeCallerUnpairedForDna() {
        expectedConfiguration.getGlobalConfig().getPipelineInfo().setWorkflow(PipelineType.DNA_WGS_VAR_BAM.getName());
        expectedConfiguration.getGlobalConfig().getDatabaseConfig().setBed(null);
        context.setVariable("isWgs", true);
        expectedCmd = expectedTemplateEngine.process(GATK_HAPLOTYPE_CALLER_DNA_TOOL_TEST_TEMPLATE_NAME, context);
        variantsVcfResult = gatkHaplotypeCaller.generate(expectedConfiguration, expectedTemplateEngine);
        assertEquals(expectedCmd.trim(), variantsVcfResult.getAbstractCommand().getToolCommand().trim());
    }

    private void buildConfiguration() {
        expectedConfiguration = new Configuration();
        GlobalConfig.ToolConfig expectedToolConfig = new GlobalConfig.ToolConfig();
        expectedToolConfig.setJava("java");
        expectedToolConfig.setGatk("gatk");
        GlobalConfig.DatabaseConfig expectedDatabaseConfig = new GlobalConfig.DatabaseConfig();
        expectedDatabaseConfig.setGenome("genome");
        expectedDatabaseConfig.setBed("bed");
        GlobalConfig.PipelineInfo pipelineInfo = new GlobalConfig.PipelineInfo();
        pipelineInfo.setWorkflow(PipelineType.DNA_CAPTURE_VAR_BAM.getName());
        GlobalConfig expectedGlobalConfig = new GlobalConfig();
        expectedGlobalConfig.setToolConfig(expectedToolConfig);
        expectedGlobalConfig.setDatabaseConfig(expectedDatabaseConfig);
        expectedGlobalConfig.setPipelineInfo(pipelineInfo);
        expectedConfiguration.setGlobalConfig(expectedGlobalConfig);
    }
}
