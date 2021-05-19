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

class FilterMutectCallsTest extends AbstractTest {

    private static final String HUMAN_FILTER_MUTECT_CALLS_TEMPLATE = "filter_mutect_calls_human_tool_template_test";
    private static final String MOUSE_FILTER_MUTECT_CALLS_TEMPLATE = "filter_mutect_calls_mouse_tool_template_test";
    private static final String SAMPLE_NAME = "sample1";
    private static final String OUTPUT_DIR = format("%s/%s", TEST_DIRECTORY, "mutect2");
    private static final String VCF = format("%s/%s.mutect2.somatic.variants.vcf", OUTPUT_DIR, SAMPLE_NAME);
    private static final String CONTAM_TABLE = format("%s/%s.contamination.table", OUTPUT_DIR, SAMPLE_NAME);
    private static final String SEGMENTS = format("%s/%s.segments.table", OUTPUT_DIR, SAMPLE_NAME);
    private static final String ARTIFACTS_PRIORS = format("%s/%s.artifacts-priors.tar.gz", OUTPUT_DIR, SAMPLE_NAME);
    private final TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private FilterMutectCalls filterMutectCallsHuman;
    private FilterMutectCalls filterMutectCallsMouse;
    private Configuration expectedConfiguration;
    private Context context;

    @BeforeEach
    void setUp() throws IOException, URISyntaxException {
        buildConfiguration();
        context = new Context();
        context.setVariable("output", TEST_DIRECTORY);
        final CommonOutdir commonOutdir = new CommonOutdir(TEST_DIRECTORY);
        commonOutdir.createDirectory();
        filterMutectCallsHuman = new FilterMutectCalls(SAMPLE_NAME, OUTPUT_DIR, VCF, CONTAM_TABLE, SEGMENTS,
                ARTIFACTS_PRIORS);
        filterMutectCallsMouse = new FilterMutectCalls(SAMPLE_NAME, OUTPUT_DIR, VCF);
    }

    @Test
    void testGenerateForHuman() {
        final String expectedCmd = expectedTemplateEngine.process(HUMAN_FILTER_MUTECT_CALLS_TEMPLATE, context);
        final String actualCmd = filterMutectCallsHuman.generate(expectedConfiguration, expectedTemplateEngine)
                .getAbstractCommand().getToolCommand();
        assertEquals(expectedCmd, actualCmd);
    }

    @Test
    void testGenerateForMouse() {
        expectedConfiguration.getGlobalConfig().getDatabaseConfig().setGenomeBuild("mm10");
        final String expectedCmd = expectedTemplateEngine.process(MOUSE_FILTER_MUTECT_CALLS_TEMPLATE, context);
        final String actualCmd = filterMutectCallsMouse.generate(expectedConfiguration, expectedTemplateEngine)
                .getAbstractCommand().getToolCommand();
        assertEquals(expectedCmd, actualCmd);
    }

    private void buildConfiguration() {
        GlobalConfig.ToolConfig toolConfig = new GlobalConfig.ToolConfig();
        toolConfig.setGatk("/opt/gatk/gatk");
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setToolConfig(toolConfig);
        final GlobalConfig.DatabaseConfig databaseConfig = new GlobalConfig.DatabaseConfig();
        databaseConfig.setGenome("/ngs/test/data/hg19.decoy.fa");
        databaseConfig.setGenomeBuild("hg19");
        globalConfig.setDatabaseConfig(databaseConfig);
        expectedConfiguration = new Configuration();
        expectedConfiguration.setGlobalConfig(globalConfig);
    }
}
