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
import com.epam.fonda.entity.configuration.StudyConfig;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static com.epam.fonda.utils.PipelineUtils.getExecutionPath;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RNASeQCTest extends AbstractTest {
    private static final String RNASEQC_TOOL_TEST_TEMPLATE_NAME = "RNASeQC_tool_test_output_data";
    private static final String RNASEQC_TOOL_TEST_TEMPLATE_NAME_WITHOUT_RNABED =
            "RNASeQC_tool_test_output_data_without_RNABED";
    private Configuration expectedConfiguration;
    private FastqFileSample expectedSample;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private String jarPath;
    private BamOutput bamOutput;
    private RNASeQC rnaSeQC;
    private Context context;

    @BeforeEach
    void setup() {
        expectedConfiguration = new Configuration();
        expectedSample = new FastqFileSample();
        expectedSample.setBamOutdir("sbamOutdir");
        expectedSample.setTmpOutdir("stmpOutdir");
        expectedSample.setQcOutdir("sqcOutdir");
        GlobalConfig expectedGlobalConfig = new GlobalConfig();
        GlobalConfig.PipelineInfo expectedPipelineInfo = new GlobalConfig.PipelineInfo();
        GlobalConfig.QueueParameters expectedQueueParameters = new GlobalConfig.QueueParameters();
        GlobalConfig.ToolConfig expectedToolConfig = new GlobalConfig.ToolConfig();
        GlobalConfig.DatabaseConfig expectedDatabaseConfig = new GlobalConfig.DatabaseConfig();
        expectedToolConfig.setJava("java");
        expectedToolConfig.setPicard("picard");
        expectedToolConfig.setRnaseqc("rnaSeqc");
        expectedToolConfig.setRnaseqcJava("rnaSeqcJava");
        expectedToolConfig.setPython("python");
        expectedPipelineInfo.setReadType("not single");
        expectedDatabaseConfig.setGenome("genome");
        expectedDatabaseConfig.setAnnotgene("annotgene");
        expectedDatabaseConfig.setRRNABED("rRnaBed");
        expectedGlobalConfig.setToolConfig(expectedToolConfig);
        expectedGlobalConfig.setQueueParameters(expectedQueueParameters);
        expectedGlobalConfig.setDatabaseConfig(expectedDatabaseConfig);
        expectedGlobalConfig.setPipelineInfo(expectedPipelineInfo);
        expectedConfiguration.setGlobalConfig(expectedGlobalConfig);
        StudyConfig expectedStudyConfig = new StudyConfig();
        expectedStudyConfig.setDate("date");
        expectedStudyConfig.setProject("project");
        expectedStudyConfig.setRun("run");
        expectedConfiguration.setStudyConfig(expectedStudyConfig);
        expectedSample.setName("sampleName");
        jarPath = getExecutionPath(expectedConfiguration);

        bamOutput = BamOutput.builder()
                .mkdupBam("sbamOutdir/sampleName.toolName.sorted.mkdup.bam")
                .mkdupMetric("sbamOutdir/sampleName.toolName.sorted.mkdup.metrics")
                .build();
        rnaSeQC = new RNASeQC(expectedSample, bamOutput);
        context = new Context();
        context.setVariable("jarPath", jarPath);

    }

    @Test
    void shouldGenerate() {
        final String expectedCmd = expectedTemplateEngine.process(RNASEQC_TOOL_TEST_TEMPLATE_NAME, context);
        final String actualCmd = rnaSeQC.generate(expectedConfiguration, expectedTemplateEngine).getCommand()
                .getToolCommand();
        assertEquals(expectedCmd, actualCmd);
    }

    @Test
    void shouldGenerateWithoutRRNABED() {
        final String expectedCmd = expectedTemplateEngine.process(RNASEQC_TOOL_TEST_TEMPLATE_NAME_WITHOUT_RNABED,
                context);
        expectedConfiguration.getGlobalConfig().getDatabaseConfig().setRRNABED(null);
        final String actualCmd = rnaSeQC.generate(expectedConfiguration, expectedTemplateEngine).getCommand()
                .getToolCommand();
        assertEquals(expectedCmd, actualCmd);
    }
}
