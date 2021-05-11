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

class FilterAlignmentArtifactsTest extends AbstractTest {
    private static final String FILTER_ALIGNMENT_ARTIFACTS_TEMPLATE = "filter_alignment_artifacts_tool_template_test";
    private static final String SAMPLE_NAME = "sample1";
    private static final String OUTPUT_DIR = format("%s/%s", TEST_DIRECTORY, "mutect2");
    private static final String FILTERED_VCF = format("%s/%s.filtered.vcf", OUTPUT_DIR, SAMPLE_NAME);
    private static final String SORTED_BAM = format("%s/%s.bam.sorted", OUTPUT_DIR, SAMPLE_NAME);
    private final TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private FilterAlignmentArtifacts filterAlignmentArtifacts;
    private Configuration expectedConfiguration;
    private Context context;

    @BeforeEach
    void setUp() throws IOException, URISyntaxException {
        buildConfiguration();
        context = new Context();
        context.setVariable("output", TEST_DIRECTORY);
        final CommonOutdir commonOutdir = new CommonOutdir(TEST_DIRECTORY);
        commonOutdir.createDirectory();
        filterAlignmentArtifacts = new FilterAlignmentArtifacts(SAMPLE_NAME, OUTPUT_DIR, FILTERED_VCF, SORTED_BAM);
    }

    @Test
    void testGenerate() {
        final String expectedCmd = expectedTemplateEngine.process(FILTER_ALIGNMENT_ARTIFACTS_TEMPLATE, context);
        final String actualCmd = filterAlignmentArtifacts.generate(expectedConfiguration, expectedTemplateEngine)
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
        databaseConfig.setBwaImg(format("%s.fasta.img", databaseConfig.getGenomeBuild()));
        globalConfig.setDatabaseConfig(databaseConfig);
        expectedConfiguration = new Configuration();
        expectedConfiguration.setGlobalConfig(globalConfig);
    }
}
