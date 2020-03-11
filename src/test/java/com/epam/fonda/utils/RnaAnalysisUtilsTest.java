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

package com.epam.fonda.utils;

import com.epam.fonda.entity.configuration.CommonOutdir;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.entity.configuration.StudyConfig;
import com.epam.fonda.tools.impl.AbstractTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

import static com.epam.fonda.utils.PipelineUtils.getExecutionPath;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RnaAnalysisUtilsTest extends AbstractTest {
    private static final String RNA_ANALYSIS_PERIODIC_CHECK_TEST_OUTPUT_DATA_PATH =
            "templates/rna_analysis_logFile_test_output.txt";
    private static final String RNA_ANALYSIS_DATA_ANALYSIS_TEST_OUTPUT_DATA_PATH =
            "rna_analysis_data_analysis_test_output";
    private Configuration expectedConfiguration;
    private static final String SAMPLE_NAME = "sampleName";
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private List<String> testSampleNames;

    @BeforeEach
    void init() {
        CommonOutdir commonOutdir = new CommonOutdir("output");
        commonOutdir.createDirectory();
        testSampleNames = new ArrayList<>(Collections.singletonList(SAMPLE_NAME));
        expectedConfiguration = new Configuration();
        expectedConfiguration.setGlobalConfig(new GlobalConfig());
        expectedConfiguration.setStudyConfig(new StudyConfig());
        expectedConfiguration.getGlobalConfig().getToolConfig().setRScript("rScript");
        expectedConfiguration.setCustTask("rnaAnalysis");
        expectedConfiguration.setCommonOutdir(commonOutdir);
        final LinkedHashSet<String> toolset = new LinkedHashSet<>(Arrays.asList("cufflinks", "conversion"));
        expectedConfiguration.getGlobalConfig().getPipelineInfo().setToolset(toolset);
    }

    @Test
    void testPeriodicStatusCheck() throws IOException, URISyntaxException {
        expectedConfiguration.getGlobalConfig().getPipelineInfo().setWorkflow("rnaExpressionFastq");

        Path path = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource(RNA_ANALYSIS_PERIODIC_CHECK_TEST_OUTPUT_DATA_PATH)).toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        String expectedCmd = new String(fileBytes);

        assertEquals(expectedCmd, RnaAnalysisUtils.periodicStatusCheck(expectedConfiguration, expectedTemplateEngine,
                "gene expression", "RSEM", testSampleNames));
    }

    @Test
    void testDataAnalysis() {
        expectedConfiguration.getStudyConfig().setFastqList("sampleList");
        Context context = new Context();
        context.setVariable("jarPath", getExecutionPath(expectedConfiguration));
        final String expectedCmd = expectedTemplateEngine.process(RNA_ANALYSIS_DATA_ANALYSIS_TEST_OUTPUT_DATA_PATH,
                context);
        final String actualBashCommand = RnaAnalysisUtils.dataAnalysis(expectedConfiguration, expectedTemplateEngine,
                "toolName");

        assertEquals(expectedCmd, actualBashCommand);
    }
}
