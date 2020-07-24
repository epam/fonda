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
import com.epam.fonda.entity.configuration.CommonOutdir;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.utils.CellRangerUtils;
import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VdjTest extends AbstractTest {
    private static final String VDJ_TEST_INPUT_DATA_PATH = "vdj_tool_test_output_data";
    private Vdj vdj;
    private Configuration expectedConfiguration;
    private String expectedCmd;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();

    @BeforeEach
    void setup() {
        expectedConfiguration = new Configuration();
        CommonOutdir expectedCommonOutdir = new CommonOutdir("output");
        expectedConfiguration.setCommonOutdir(expectedCommonOutdir);
        expectedCommonOutdir.createDirectory();
        FastqFileSample expectedSample = FastqFileSample.builder()
                .name("sampleName")
                .fastq1(Arrays.asList("fastq1", "fastq2"))
                .build();
        expectedSample.createDirectory();
        String fastqDirs = String.join(",", CellRangerUtils.extractFastqDir(expectedSample).getFastqDirs());

        Context context = new Context();
        context.setVariable("fastqDirs", fastqDirs);
        expectedCmd = expectedTemplateEngine.process(VDJ_TEST_INPUT_DATA_PATH, context);


        BamOutput bamOutput = BamOutput.builder()
                .build();
        BamResult bamResult = BamResult.builder()
                .bamOutput(bamOutput)
                .command(BashCommand.withTool(""))
                .build();
        vdj = new Vdj(expectedSample, bamResult);
        buildConfiguration();
    }

    @Test
    void shouldGenerate() {
        BamResult bamResult = vdj.generate(expectedConfiguration, expectedTemplateEngine);
        assertEquals(expectedCmd, bamResult.getCommand().getToolCommand());
    }

    private void buildConfiguration() {
        GlobalConfig expectedGlobalConfig = new GlobalConfig();
        GlobalConfig.ToolConfig toolConfig = new GlobalConfig.ToolConfig();
        GlobalConfig.DatabaseConfig databaseConfig = new GlobalConfig.DatabaseConfig();
        databaseConfig.setVdjGenome("genome");
        toolConfig.setCellranger("cellranger");
        expectedGlobalConfig.setToolConfig(toolConfig);
        expectedGlobalConfig.setDatabaseConfig(databaseConfig);
        GlobalConfig.CellrangerConfig cellrangerConfig = new GlobalConfig.CellrangerConfig();
        cellrangerConfig.setCellrangerLanes("lanes");
        cellrangerConfig.setCellrangerIndices("indices");
        cellrangerConfig.setCellrangerForcedCells("forcedCells");
        cellrangerConfig.setCellrangerDenovo("TRUE");
        expectedGlobalConfig.setCellrangerConfig(cellrangerConfig);
        GlobalConfig.QueueParameters queueParameters = new GlobalConfig.QueueParameters();
        queueParameters.setNumThreads(3);
        expectedGlobalConfig.setQueueParameters(queueParameters);
        expectedConfiguration.setGlobalConfig(expectedGlobalConfig);
    }
}
