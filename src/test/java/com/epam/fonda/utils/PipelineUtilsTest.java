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
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.impl.AbstractTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PipelineUtilsTest extends AbstractTest {
    private static final String TEST_DIRECTORY = "output";
    private static final String ADD_TASK_TEST_TEMPLATE_NAME = "add_task_test_output_data";
    private static final String CLEAN_UP_TMPDIR_TEST_TEMPLATE_NAME = "clean_up_tmpdir_test_output_data";
    private static final String MERGE_FASTQ_WITH_FASTQ2_TEST_OUTPUT_DATA_PATH =
            "templates/merge_fastq_with_fastq2_test_output_data.txt";
    private static final String CMD = "test command";
    private Configuration expectedConfiguration;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private FastqFileSample expectedSample;


    @BeforeEach
    void setup() {
        expectedConfiguration = new Configuration();
        GlobalConfig.PipelineInfo expectedPipelineInfo = new GlobalConfig.PipelineInfo();
        GlobalConfig expectedGlobalConfig = new GlobalConfig();
        expectedSample = new FastqFileSample();
        expectedPipelineInfo.setWorkflow("workflow");
        expectedGlobalConfig.setPipelineInfo(expectedPipelineInfo);
        CommonOutdir commonOutdir = new CommonOutdir(TEST_DIRECTORY);
        commonOutdir.createDirectory();
        expectedConfiguration.setCommonOutdir(commonOutdir);
        expectedConfiguration.setGlobalConfig(expectedGlobalConfig);
        expectedSample.setName("sampleName");
        expectedSample.setFastq1(Arrays.asList("fastqs_1", "fastqs_1"));
        expectedSample.setFastq2(Collections.singletonList("fastqs_2"));
        expectedConfiguration.setTestMode(true);
    }

    @Test
    void shouldAddTaskWithoutLocalMode() {
        expectedConfiguration.setLocalMode(false);
        Context context = new Context();
        context.setVariable("local", expectedConfiguration.isLocalMode());
        String expectedCmd = expectedTemplateEngine.process(ADD_TASK_TEST_TEMPLATE_NAME, context);
        String actualCmd = PipelineUtils.addTask(expectedConfiguration, "task",
                expectedSample.getName());
        assertEquals(expectedCmd, actualCmd);
    }

    @Test
    void shouldAddTaskWithLocalMode() {
        expectedConfiguration.setLocalMode(true);
        Context context = new Context();
        context.setVariable("local", expectedConfiguration.isLocalMode());
        String expectedCmd = expectedTemplateEngine.process(ADD_TASK_TEST_TEMPLATE_NAME, context);
        String actualCmd = PipelineUtils.addTask(expectedConfiguration, "task",
                expectedSample.getName());
        assertEquals(expectedCmd, actualCmd);
    }

    @Test
    void shouldCleanUpTmpDir() {
        Set<String> fields = new LinkedHashSet<>();
        fields.add("testField1");
        fields.add("testField2");
        Context context = new Context();
        context.setVariable("fields", fields);
        String expectedCmd = expectedTemplateEngine.process(CLEAN_UP_TMPDIR_TEST_TEMPLATE_NAME, context);
        String actualCmd = PipelineUtils.cleanUpTmpDir(fields);
        assertEquals(expectedCmd, actualCmd);
    }

    @Test
    void shouldMergeFastqsWithFastq2() throws URISyntaxException, IOException {
        expectedSample.setFastqOutdir("sfqOutdir");
        Path path = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource(MERGE_FASTQ_WITH_FASTQ2_TEST_OUTPUT_DATA_PATH)).toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        String expectedCmd = new String(fileBytes);
        String actualCmd = PipelineUtils.mergeFastq(expectedSample).getCommand().getToolCommand();
        assertEquals(expectedCmd, actualCmd);
    }

    @Test
    void createDir() {
        assertDoesNotThrow(() -> PipelineUtils.createDir(TEST_DIRECTORY));
    }

    @Test
    void createNestedDirectory() {
        assertDoesNotThrow(() -> PipelineUtils.createDir(String.format("%s/%s", TEST_DIRECTORY, TEST_DIRECTORY)));
    }

    @Test
    void printShellWithSampleName() {
        assertDoesNotThrow(() -> PipelineUtils.printShell(expectedConfiguration, CMD,
                expectedSample.getName(), null));
    }

    @Test
    void printShellWithSampleNameAndIndex() {
        assertDoesNotThrow(() -> PipelineUtils.printShell(expectedConfiguration, CMD,
                expectedSample.getName(), "index"));
    }

    @Test
    void printShellWithoutSampleName() {
        assertDoesNotThrow(() -> PipelineUtils.printShell(expectedConfiguration, CMD, null, null));
    }

    @Test
    void cleanUpTmpDirEmptyFields() {
        final String expectedCmd = expectedTemplateEngine.process(CLEAN_UP_TMPDIR_TEST_TEMPLATE_NAME, new Context());
        assertEquals(expectedCmd, PipelineUtils.cleanUpTmpDir(null));
    }

    @Test
    void createStaticShell() {
        assertDoesNotThrow(() -> PipelineUtils.createStaticShell(expectedConfiguration, "task",
                CMD, expectedSample.getName()));
    }

    @Test
    void getExecutionPath() {
        assertTrue(StringUtils.isNotBlank(PipelineUtils.getExecutionPath(expectedConfiguration)));
    }
}
