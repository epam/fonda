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

import com.epam.fonda.entity.configuration.CommonOutdir;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.entity.configuration.StudyConfig;
import com.epam.fonda.utils.TemplateEngineUtils;
import com.epam.fonda.workflow.impl.Flag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Objects;

import static com.epam.fonda.utils.PipelineUtils.getExecutionPath;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SCRnaAnalysisTest extends AbstractTest {
    private static final String SAMPLE_NAME = "sampleName";
    private static final String COUNT_TOOL = "count";
    private static final String SCRNA_ANALYSIS_PERIODIC_CHECK_TEST_OUTPUT_DATA_PATH =
            "templates/scrna_analysis_logFile_test_output.txt";
    private static final String SCRNA_ANALYSIS_EXPRESS_DATA_TEST_OUTPUT_DATA_PATH =
            "scRna_analysis_count_tool_test_output";
    private static final String JAR_PATH = "jarPath";
    private SCRnaAnalysis scRnaAnalysis;
    private Configuration expectedConfiguration;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private String jarPath;

    @BeforeEach
    void init() {
        Flag testFlag = Flag.builder()
                .conversion(true)
                .count(true)
                .build();
        scRnaAnalysis = new SCRnaAnalysis(testFlag, Collections.singletonList(SAMPLE_NAME));
        CommonOutdir commonOutdir = new CommonOutdir("output");
        commonOutdir.createDirectory();
        constructConfiguration(commonOutdir);
        jarPath = getExecutionPath(expectedConfiguration);
    }

    @Test
    void generate() {
        assertDoesNotThrow(() -> scRnaAnalysis.generate(expectedConfiguration, expectedTemplateEngine));
    }

    @Test
    void expressDataFastq() {
        Context context = new Context();
        context.setVariable(JAR_PATH, jarPath);
        final String expectedCmd = expectedTemplateEngine
                .process(SCRNA_ANALYSIS_EXPRESS_DATA_TEST_OUTPUT_DATA_PATH, context);
        final String actualCmd = scRnaAnalysis.expressData(expectedConfiguration, expectedTemplateEngine);

        assertEquals(expectedCmd, actualCmd);
    }

    @Test
    void expressDataBam() {
        expectedConfiguration.getStudyConfig().setFastqList(null);
        expectedConfiguration.getStudyConfig().setBamList("sampleList");

        Context context = new Context();
        context.setVariable(JAR_PATH, jarPath);
        final String expectedCmd = expectedTemplateEngine
                .process(SCRNA_ANALYSIS_EXPRESS_DATA_TEST_OUTPUT_DATA_PATH, context);
        final String actualCmd = scRnaAnalysis.expressData(expectedConfiguration, expectedTemplateEngine);

        assertEquals(expectedCmd, actualCmd);
    }

    @Test
    void testPeriodicStatusCheck() throws IOException, URISyntaxException {
        expectedConfiguration.getGlobalConfig().getPipelineInfo().setWorkflow("scrnaExpressionFastq");

        Path path = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource(SCRNA_ANALYSIS_PERIODIC_CHECK_TEST_OUTPUT_DATA_PATH)).toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        String expectedCmd = new String(fileBytes);
        String actualCmd = scRnaAnalysis.periodicStatusCheck(expectedConfiguration, expectedTemplateEngine);

        assertEquals(expectedCmd, actualCmd);
    }

    private void constructConfiguration(CommonOutdir commonOutdir) {
        expectedConfiguration = new Configuration();
        expectedConfiguration.setCommonOutdir(commonOutdir);
        expectedConfiguration.setGlobalConfig(new GlobalConfig());
        expectedConfiguration.setStudyConfig(new StudyConfig());
        expectedConfiguration.getStudyConfig().setFastqList("sampleList");
        expectedConfiguration.getGlobalConfig().getToolConfig().setJava(JAR_PATH);
        expectedConfiguration.getGlobalConfig().getToolConfig().setRScript("rScript");
        expectedConfiguration.getGlobalConfig().getDatabaseConfig().setGenomeBuild("genomeBuild");
        expectedConfiguration.getGlobalConfig().getPipelineInfo().setWorkflow(COUNT_TOOL);
        expectedConfiguration.setTestMode(true);
    }
}
