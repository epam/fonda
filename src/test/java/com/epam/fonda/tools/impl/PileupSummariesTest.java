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
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.net.URISyntaxException;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PileupSummariesTest extends AbstractTest {

    private static final String PILEUP_SUMMARIES_TEMPLATE_TEST = "pileup_summaries_template_test";
    private static final String SAMPLE_NAME = "sample1";
    private static final String OUTPUT_DIR = format("%s/%s", TEST_DIRECTORY, "mutect2");
    private static final String BAM = format("%s/file.bam", TEST_DIRECTORY, SAMPLE_NAME);
    private final TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private PileupSummaries pileupSummaries;
    private Configuration expectedConfiguration;
    private Context context;

    @BeforeEach
    void setUp() throws IOException, URISyntaxException {
        initConfiguration();
        context = new Context();
        context.setVariable("output", TEST_DIRECTORY);
        final CommonOutdir commonOutdir = new CommonOutdir(TEST_DIRECTORY);
        commonOutdir.createDirectory();
        final BamOutput bamOutput = BamOutput.builder()
                .bam(BAM)
                .build();
        pileupSummaries = new PileupSummaries(SAMPLE_NAME, bamOutput, OUTPUT_DIR);
    }

    @Test
    void testGenerate() {
        final String expectedCmd = expectedTemplateEngine.process(PILEUP_SUMMARIES_TEMPLATE_TEST, context);
        final String actualCmd = pileupSummaries.generate(expectedConfiguration, expectedTemplateEngine)
                .getCommand().getToolCommand();
        assertEquals(expectedCmd, actualCmd);
    }

    private void initConfiguration() {
        final Configuration configuration = new Configuration();
        final GlobalConfig globalConfig = new GlobalConfig();
        final GlobalConfig.ToolConfig toolConfig = new GlobalConfig.ToolConfig();
        toolConfig.setGatk("/opt/gatk/gatk");
        globalConfig.setToolConfig(toolConfig);
        final GlobalConfig.DatabaseConfig databaseConfig = new GlobalConfig.DatabaseConfig();
        databaseConfig.setGenome("/ngs/test/data/hg19.decoy.fa");
        databaseConfig.setBed(format("%s/file.bed", TEST_DIRECTORY, SAMPLE_NAME));
        databaseConfig.setContaminationVCF("contamination.vcf");
        databaseConfig.setSequenceDictionary("sequence.dict");
        globalConfig.setDatabaseConfig(databaseConfig);
        expectedConfiguration = new Configuration();
        expectedConfiguration.setGlobalConfig(globalConfig);
    }
}