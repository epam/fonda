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
import com.epam.fonda.entity.configuration.CommonOutdir;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.entity.configuration.StudyConfig;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.tools.results.FastqOutput;
import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FeatureCountTest extends AbstractTest {
    private static final String FEATURECOUNT_TEST_INPUT_DATA_PATH =
            "featurecount_tool_test_output_data";
    private Configuration expectedConfiguration;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private FeatureCount featureCount;

    @BeforeEach
    void setup() {
        FastqFileSample expectedSample = new FastqFileSample();
        expectedSample.setName("sampleName");
        expectedSample.setSampleOutputDir("output");
        expectedSample.createDirectory();
        FastqOutput fastqOutput = FastqOutput.builder()
                .mergedFastq1("mergedFastq1")
                .mergedFastq2("mergedFastq2")
                .build();
        BamOutput bamOutput = BamOutput.builder()
                .bam("sampleName.toolName.sorted.bam")
                .build();
        BamResult bamResult = BamResult.builder()
                .fastqOutput(fastqOutput)
                .bamOutput(bamOutput)
                .command(BashCommand.withTool(""))
                .build();
        featureCount = new FeatureCount(expectedSample.getName(), expectedSample.getSampleOutputDir(), bamResult);
        expectedConfiguration = new Configuration();
        GlobalConfig expectedGlobalConfig = new GlobalConfig();
        GlobalConfig.PipelineInfo expectedPipelineInfo = new GlobalConfig.PipelineInfo();
        expectedPipelineInfo.setWorkflow("rnaExpressionFastq");
        GlobalConfig.QueueParameters expectedQueueParameters = new GlobalConfig.QueueParameters();
        GlobalConfig.ToolConfig expectedToolConfig = new GlobalConfig.ToolConfig();
        GlobalConfig.DatabaseConfig expectedDatabaseConfig = new GlobalConfig.DatabaseConfig();
        expectedQueueParameters.setNumThreads(5);
        expectedQueueParameters.setPe("pe");
        expectedQueueParameters.setQueue("queue");
        expectedToolConfig.setFeatureCount("featureCount");
        expectedDatabaseConfig.setAnnotgenesaf("annotgeneSaf");
        expectedGlobalConfig.setToolConfig(expectedToolConfig);
        expectedGlobalConfig.setPipelineInfo(expectedPipelineInfo);
        expectedGlobalConfig.setDatabaseConfig(expectedDatabaseConfig);
        expectedGlobalConfig.setQueueParameters(expectedQueueParameters);
        StudyConfig studyConfig = new StudyConfig();
        studyConfig.setCufflinksLibraryType("fr-firststrand");
        expectedConfiguration.setGlobalConfig(expectedGlobalConfig);
        expectedConfiguration.setStudyConfig(studyConfig);
        expectedConfiguration.setTestMode(true);
        CommonOutdir commonOutdir = new CommonOutdir("output");
        commonOutdir.createDirectory();
        expectedConfiguration.setCommonOutdir(commonOutdir);
    }

    @Test
    void shouldGenerate() {
        final String expectedCmd = expectedTemplateEngine.process(FEATURECOUNT_TEST_INPUT_DATA_PATH, new Context());
        final String actualCmd = featureCount.generate(expectedConfiguration, expectedTemplateEngine).getCommand()
                .getToolCommand();
        assertEquals(expectedCmd, actualCmd);
    }
}
