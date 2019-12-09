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
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;

import static com.epam.fonda.utils.PipelineUtils.getExecutionPath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SCRNASeqDoubletDetectionTest extends AbstractTest {
    private static final String FASTQ1 = "fastq.file1";
    private static final String FASTQ2 = "fastq.file2";
    private static final String SAMPLE_NAME = "sampleName";
    private static final String DOUBLET_DETECTION = "doubletdetection";
    private static final String SCRUBLET = "scrublet";
    private static final String DOUBLET_DETECTION_TEST_OUTPUT_DATA_PATH = "doublet_detection_test_output";
    private static final String SCRUBLET_TEST_OUTPUT_DATA_PATH = "scrublet_detection_test_output";
    private String jarPath;
    private SCRNASeqDoubletDetection scrnaSeqDoubletDetection;
    private Configuration expectedConfiguration;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();

    @BeforeEach
    void init() {
        FastqFileSample sample1 = FastqFileSample.builder()
                .name(SAMPLE_NAME)
                .fastq1(Arrays.asList(FASTQ1, FASTQ2))
                .fastq2(Arrays.asList(FASTQ2, FASTQ1))
                .build();
        CommonOutdir commonOutdir = new CommonOutdir("output");
        commonOutdir.createDirectory();
        constructConfiguration(commonOutdir);
        scrnaSeqDoubletDetection = new SCRNASeqDoubletDetection(sample1);
        jarPath = getExecutionPath();
    }

    @Test
    void generateDoubletDetection() {
        Context context = new Context();
        context.setVariable("jarPath", jarPath);
        final String expectedCmd = expectedTemplateEngine.process(DOUBLET_DETECTION_TEST_OUTPUT_DATA_PATH, context);
        final String actualCmd = scrnaSeqDoubletDetection.generate(expectedConfiguration, expectedTemplateEngine);

        assertEquals(expectedCmd, actualCmd);
    }

    @Test
    void generateScrublet() {
        expectedConfiguration.getGlobalConfig().getPipelineInfo()
                .setToolset(new LinkedHashSet<>(Collections.singletonList(SCRUBLET)));
        Context context = new Context();
        context.setVariable("jarPath", jarPath);
        final String expectedCmd = expectedTemplateEngine.process(SCRUBLET_TEST_OUTPUT_DATA_PATH, context);
        final String actualCmd = scrnaSeqDoubletDetection.generate(expectedConfiguration, expectedTemplateEngine);

        assertEquals(expectedCmd, actualCmd);
    }

    @Test
    void generateWithInvalidConfiguration() {
        expectedConfiguration.getGlobalConfig().getToolConfig().setDoubleDetectionPython(null);
        assertThrows(NullPointerException.class, () ->
                scrnaSeqDoubletDetection.generate(expectedConfiguration, expectedTemplateEngine));
    }

    private void constructConfiguration(CommonOutdir commonOutdir){
        expectedConfiguration = new Configuration();
        expectedConfiguration.setCommonOutdir(commonOutdir);
        expectedConfiguration.setGlobalConfig(new GlobalConfig());
        expectedConfiguration.setStudyConfig(new StudyConfig());
        expectedConfiguration.getGlobalConfig().getToolConfig().setPython("python");
        expectedConfiguration.getGlobalConfig().getToolConfig().setDoubleDetectionPython("doubleDetectionPythonPath");
        expectedConfiguration.getGlobalConfig().getDatabaseConfig().setGenomeBuild("genomeBuild");
        expectedConfiguration.getGlobalConfig().getPipelineInfo().setWorkflow("scrnaSeqDoubletDetection");
        expectedConfiguration.getGlobalConfig().getPipelineInfo()
                .setToolset(new LinkedHashSet<>(Collections.singleton(DOUBLET_DETECTION)));
    }
}
