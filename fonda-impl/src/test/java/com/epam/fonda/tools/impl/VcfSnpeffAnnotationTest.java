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
import com.epam.fonda.tools.results.VariantsVcfOutput;
import com.epam.fonda.tools.results.VariantsVcfResult;
import com.epam.fonda.tools.results.VcfScnpeffAnnonationResult;
import com.epam.fonda.tools.results.VcfScnpeffAnnotationOutput;
import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static com.epam.fonda.utils.PipelineUtils.getExecutionPath;
import static org.junit.jupiter.api.Assertions.assertEquals;


class VcfSnpeffAnnotationTest extends AbstractTest {

    private static final String VCF_SNPEFF_ANNOTATION_TOOL_TEST_TEMPLATE_NAME =
            "vcf_snpeff_annotation_tool_test_output_data";
    private Configuration expectedConfiguration;
    private FastqFileSample expectedSample;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private VcfScnpeffAnnonationResult vcfScnpeffAnnonationResult;
    private VariantsVcfResult expectedVariantsVcfResult;

    @BeforeEach
    void setup() {
        expectedConfiguration = new Configuration();
        constructExpectedSample();
        GlobalConfig expectedGlobalConfig = new GlobalConfig();
        constructToolConfig(expectedGlobalConfig);
        constructDatabaseConfig(expectedGlobalConfig);
        expectedConfiguration.setGlobalConfig(expectedGlobalConfig);
        vcfScnpeffAnnonationResult = VcfScnpeffAnnonationResult.builder()
                .command(BashCommand.withTool(""))
                .vcfScnpeffAnnotationOutput(VcfScnpeffAnnotationOutput.builder().build())
                .build();
        VariantsVcfOutput expectedVariantsVcfoutput = VariantsVcfOutput.builder()
                .variantsVcf("varFiltered.vcf")
                .build();
        expectedVariantsVcfResult = VariantsVcfResult.builder()
                .variantsVcfOutput(expectedVariantsVcfoutput)
                .filteredTool("filteredTool")
                .build();
    }

    @Test
    void shoulGenerate() {
        VcfSnpeffAnnotation vcfSnpeffAnnotation = new VcfSnpeffAnnotation(expectedSample.getName(),
                expectedVariantsVcfResult);
        Context context = new Context();
        context.setVariable("jarPath", getExecutionPath());
        String expectedCmd = expectedTemplateEngine.process(VCF_SNPEFF_ANNOTATION_TOOL_TEST_TEMPLATE_NAME, context);
        vcfScnpeffAnnonationResult = vcfSnpeffAnnotation.generate(expectedConfiguration, expectedTemplateEngine);
        assertEquals(expectedCmd, vcfScnpeffAnnonationResult.getCommand().getToolCommand());
    }

    private void constructExpectedSample() {
        expectedSample = new FastqFileSample();
        expectedSample.setName("sampleName");
    }

    private void constructToolConfig(GlobalConfig expectedGlobalConfig) {
        GlobalConfig.ToolConfig expectedToolConfig = new GlobalConfig.ToolConfig();
        expectedToolConfig.setTransvar("transvar");
        expectedToolConfig.setPython("python");
        expectedToolConfig.setSnpsift("snpsift");
        expectedGlobalConfig.setToolConfig(expectedToolConfig);
    }

    private void constructDatabaseConfig(GlobalConfig expectedGlobalConfig) {
        GlobalConfig.DatabaseConfig expectedDatabaseConfig = new GlobalConfig.DatabaseConfig();
        expectedDatabaseConfig.setGenomeBuild("genomeBuild");
        expectedDatabaseConfig.setCanonicalTranscript("canonicalTranscript");
        expectedDatabaseConfig.setSnpsiftdb("snpsiftDb");
        expectedGlobalConfig.setDatabaseConfig(expectedDatabaseConfig);
    }
}
