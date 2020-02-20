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
import com.epam.fonda.entity.configuration.StudyConfig;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.junit.jupiter.api.Assertions.assertEquals;


class AbraRealignTest extends AbstractTest {
    private static final String AMPLICON_ABRA_REALIGN_TOOL_TEST_TEMPLATE_NAME =
            "amplicon_abra_realign_tool_test_output_data";
    private Configuration expectedConfiguration;
    private GlobalConfig.PipelineInfo expectedPipelineInfo;
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
        expectedPipelineInfo = new GlobalConfig.PipelineInfo();
        GlobalConfig.QueueParameters expectedQueueParameters = new GlobalConfig.QueueParameters();
        expectedQueueParameters.setNumThreads(5);
        expectedGlobalConfig.setQueueParameters(expectedQueueParameters);
        expectedGlobalConfig.setPipelineInfo(expectedPipelineInfo);
        expectedConfiguration.setGlobalConfig(expectedGlobalConfig);
        StudyConfig expectedStudyConfig = new StudyConfig();
        expectedConfiguration.setStudyConfig(expectedStudyConfig);
        BamOutput bamOutput = BamOutput.builder()
                .bam("sbamOutdir/sampleName.toolName.sorted.bamFile.bam")
                .build();
        bamResult = BamResult.builder()
                .bamOutput(bamOutput)
                .command(BashCommand.withTool(""))
                .build();
    }

    @Test
    void shouldGenerateWithPairedReadType() {
        AbraRealign abraRealign = new AbraRealign(expectedSample, bamResult);
        expectedPipelineInfo.setReadType("paired");
        Context context = new Context();
        context.setVariable("readType", expectedPipelineInfo.getReadType());
        String expectedCmd = expectedTemplateEngine.process(AMPLICON_ABRA_REALIGN_TOOL_TEST_TEMPLATE_NAME, context);
        bamResult = abraRealign.generate(expectedConfiguration, expectedTemplateEngine);
        assertEquals(expectedCmd, bamResult.getCommand().getToolCommand());
    }

    @Test
    void shouldGenerateWithSingleReadType() {
        AbraRealign abraRealign = new AbraRealign(expectedSample, bamResult);
        expectedPipelineInfo.setReadType("single");
        Context context = new Context();
        context.setVariable("readType", expectedPipelineInfo.getReadType());
        String expectedCmd = expectedTemplateEngine.process(AMPLICON_ABRA_REALIGN_TOOL_TEST_TEMPLATE_NAME, context);
        bamResult = abraRealign.generate(expectedConfiguration, expectedTemplateEngine);
        assertEquals(expectedCmd, bamResult.getCommand().getToolCommand());
    }

    private void constructExpectedSample() {
        expectedSample = new FastqFileSample();
        expectedSample.setName("sampleName");
        expectedSample.setTmpOutdir("stmpOutdir");
    }

    private void constructToolConfig(GlobalConfig expectedGlobalConfig) {
        GlobalConfig.ToolConfig expectedToolConfig = new GlobalConfig.ToolConfig();
        expectedToolConfig.setSamTools("samtools");
        expectedToolConfig.setJava("java");
        expectedToolConfig.setAbra2("abra2");
        expectedGlobalConfig.setToolConfig(expectedToolConfig);
    }

    private void constructDatabaseConfig(GlobalConfig expectedGlobalConfig) {
        GlobalConfig.DatabaseConfig expectedDatabaseConfig = new GlobalConfig.DatabaseConfig();
        expectedDatabaseConfig.setGenome("genome");
        expectedDatabaseConfig.setBed("bed");
        expectedGlobalConfig.setDatabaseConfig(expectedDatabaseConfig);
    }
}
