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

import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.entity.configuration.StudyConfig;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RsemExpressionTest extends AbstractTest {
    private static final String RSEM_EXPRESSION_TEST_INPUT_DATA_PATH =
            "templates/rsem_expression_test_input_data.txt";
    private RsemExpression rsemExpression;
    private Configuration expectedConfiguration;
    private String expectedCmd;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();

    @BeforeEach
    void init() throws IOException, URISyntaxException {
        FastqFileSample expectedSample = new FastqFileSample();
        expectedSample.setName("sampleName");
        expectedSample.setSampleOutputDir("output");
        expectedSample.createDirectory();
        BamOutput bamOutput = BamOutput.builder()
                .bam("sampleName.toolName.sorted.bam")
                .build();
        rsemExpression = new RsemExpression(expectedSample.getName(), expectedSample.getSampleOutputDir(), bamOutput);
        expectedConfiguration = new Configuration();
        expectedConfiguration.setGlobalConfig(new GlobalConfig());
        expectedConfiguration.setStudyConfig(new StudyConfig());
        expectedConfiguration.getGlobalConfig().getQueueParameters().setNumThreads(1);
        expectedConfiguration.getGlobalConfig().getToolConfig().setRsem("rsem");
        expectedConfiguration.getStudyConfig().setBamList("bam");
        expectedConfiguration.getGlobalConfig().getToolConfig().setRsemIndex("index");
        Path path = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource(RSEM_EXPRESSION_TEST_INPUT_DATA_PATH)).toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        expectedCmd = new String(fileBytes);
    }

    @Test
    void generateTest() {
        final String actualCmd = rsemExpression.generate(expectedConfiguration, expectedTemplateEngine).getCommand()
                .getToolCommand();
        assertEquals(expectedCmd, actualCmd);
    }

    @Test
    void generateTestNullParameter() {
        expectedConfiguration.getGlobalConfig().getToolConfig().setRsemIndex(null);
        assertThrows(NullPointerException.class, () ->
                rsemExpression.generate(expectedConfiguration, expectedTemplateEngine));
    }
}
