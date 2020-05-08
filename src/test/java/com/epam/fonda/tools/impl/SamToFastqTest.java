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
import com.epam.fonda.samples.fastq.FastqReadType;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SamToFastqTest extends AbstractTest {
    private static final String SAM_TO_FASTQ_TEST_OUTPUT_DATA_PATH_WITH_PAIRED_TYPE_READ =
            "templates/sam_to_fastq_tool_test_output_data_with_paired_type_read.txt";
    private static final String SAM_TO_FASTQ_TEST_OUTPUT_DATA_PATH_WITH_SINGLE_TYPE_READ =
            "templates/sam_to_fastq_tool_test_output_data_with_single_type_read.txt";

    private SamToFastq samToFastq;
    private TemplateEngine templateEngine = TemplateEngineUtils.init();

    @BeforeEach
    void setUp() {
        BamResult bamResult = BamResult.builder()
                .bamOutput(BamOutput.builder()
                        .bam("bam")
                        .build())
                .build();
        samToFastq = new SamToFastq("sampleName", "outDir", bamResult);
    }

    @Test
    void testGenerateSamToFastqWithSingleTypeRead() throws IOException, URISyntaxException {
        assertEquals(buildCmd(SAM_TO_FASTQ_TEST_OUTPUT_DATA_PATH_WITH_SINGLE_TYPE_READ),
                samToFastq.generate(buildConfiguration(FastqReadType.SINGLE.getType()), templateEngine).getCommand()
                        .getToolCommand());
    }

    @Test
    void testGenerateSamToFastqWithPairedTypeRead() throws IOException, URISyntaxException {
        assertEquals(buildCmd(SAM_TO_FASTQ_TEST_OUTPUT_DATA_PATH_WITH_PAIRED_TYPE_READ),
                samToFastq.generate(buildConfiguration(FastqReadType.PAIRED.getType()), templateEngine).getCommand()
                        .getToolCommand());
    }

    private String buildCmd(String pathToTemplate) throws URISyntaxException, IOException {
        Path path = Paths.get(this.getClass().getClassLoader()
                .getResource(pathToTemplate).toURI());
        return readFile(path);
    }

    private Configuration buildConfiguration(String readType) {
        GlobalConfig.ToolConfig expectedTool = new GlobalConfig.ToolConfig();
        expectedTool.setJava("java");
        expectedTool.setPicardVersion("v2.10.3");
        expectedTool.setPicard("picard");
        GlobalConfig.PipelineInfo expectedPipelineInfo = new GlobalConfig.PipelineInfo();
        expectedPipelineInfo.setReadType(readType);
        GlobalConfig expectedGlobalConfig = new GlobalConfig();
        expectedGlobalConfig.setToolConfig(expectedTool);
        expectedGlobalConfig.setPipelineInfo(expectedPipelineInfo);
        Configuration configuration = new Configuration();
        configuration.setGlobalConfig(expectedGlobalConfig);
        return configuration;
    }
}
