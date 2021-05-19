/*
 * Copyright 2017-2021 Sanofi and EPAM Systems, Inc. (https://www.epam.com/)
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
import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.net.URISyntaxException;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LearnReadOrientationModelTest extends AbstractTest {
    private static final String ORIENTATION_MODEL_TEMPLATE = "learnReadOrientationModel_tool_template_test";
    private static final String SAMPLE_NAME = "sample1";
    private static final String OUTPUT_DIR = format("%s/%s", TEST_DIRECTORY, "mutect2");
    private static final String F1R2_TAR_GZ = format("%s/%s.mutect2.f1r2.tar.gz", OUTPUT_DIR, SAMPLE_NAME);
    private final TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private LearnReadOrientationModel learnReadOrientationModel;
    private Configuration expectedConfiguration;
    private Context context;

    @BeforeEach
    void setUp() throws IOException, URISyntaxException {
        buildConfiguration();
        context = new Context();
        context.setVariable("output", TEST_DIRECTORY);
        final CommonOutdir commonOutdir = new CommonOutdir(TEST_DIRECTORY);
        commonOutdir.createDirectory();
        learnReadOrientationModel = new LearnReadOrientationModel(SAMPLE_NAME, F1R2_TAR_GZ, OUTPUT_DIR);
    }

    @Test
    void testGenerate() {
        final String expectedCmd = expectedTemplateEngine.process(ORIENTATION_MODEL_TEMPLATE, context);
        final String actualCmd = learnReadOrientationModel.generate(expectedConfiguration, expectedTemplateEngine)
                .getCommand().getToolCommand();
        assertEquals(expectedCmd, actualCmd);
    }

    private void buildConfiguration() {
        GlobalConfig.ToolConfig toolConfig = new GlobalConfig.ToolConfig();
        toolConfig.setGatk("/opt/gatk/gatk");
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setToolConfig(toolConfig);
        expectedConfiguration = new Configuration();
        expectedConfiguration.setGlobalConfig(globalConfig);
    }
}
