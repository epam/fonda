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
import com.epam.fonda.samples.bam.BamFileSample;
import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SortBamByReadNameTest extends AbstractTest {
    private static final String SORT_BAM_BY_READ_NAME_TEST_OUTPUT_DATA_PATH =
            "templates/sort_bam_by_read_name_tool_test_output_data.txt";
    private SortBamByReadName sortBamByReadname;
    private Configuration expectedConfiguration;
    private TemplateEngine expectedEngine = TemplateEngineUtils.init();
    private String expectedCmd;

    @BeforeEach
    void setUp() throws IOException, URISyntaxException {
        buildConfiguration();
        sortBamByReadname = new SortBamByReadName("outDir",
                BamFileSample.builder()
                        .bam("bam")
                        .name("sampleName")
                        .build());
        Path path = Paths.get(this.getClass().getClassLoader()
                .getResource(SORT_BAM_BY_READ_NAME_TEST_OUTPUT_DATA_PATH).toURI());
        expectedCmd = readFile(path);
    }

    @Test
    void generate() {
        String actualCmd = sortBamByReadname.generate(expectedConfiguration, expectedEngine)
                .getCommand().getToolCommand();
        assertEquals(expectedCmd, actualCmd);
    }

    private void buildConfiguration() {
        expectedConfiguration = new Configuration();
        GlobalConfig expectedGlobalConfig = new GlobalConfig();
        GlobalConfig.ToolConfig expectedToolConfig = new GlobalConfig.ToolConfig();
        expectedToolConfig.setJava("java");
        expectedToolConfig.setPicardVersion("v2.10.3");
        expectedToolConfig.setPicard("picard");
        expectedToolConfig.setSamTools("samtools");
        expectedGlobalConfig.setToolConfig(expectedToolConfig);
        expectedConfiguration.setGlobalConfig(expectedGlobalConfig);
    }
}
