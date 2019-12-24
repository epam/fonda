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

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FastqListAnalysisTest extends AbstractTest {
    private static final String FASTQ_LIST_ANALYSIS_TOOL_TEST_OUTPUT_DATA_WITH_PAIRED_READ_TYPE_PATH =
            "templates/fastq_list_analysis_tool_test_output_data_with_paired_read_type.txt";
    private static final String FASTQ_LIST_ANALYSIS_TOOL_TEST_OUTPUT_DATA_WITH_SINGLE_READ_TYPE_PATH =
            "templates/fastq_list_analysis_tool_test_output_data_with_single_read_type.txt";

    private FastqListAnalysis fastqListAnalysis;
    private Configuration configurationWithPaired;
    private Configuration configurationWithSingle;
    private TemplateEngine templateEngine = TemplateEngineUtils.init();
    private String expectedCmdWithPaired;
    private String expectedCmdWithSingle;
    private String actualFastqPath;
    private String actualCmd;

    @BeforeEach
    void setUp() throws IOException, URISyntaxException {
        configurationWithPaired = buildConfiguration("paired");
        configurationWithSingle = buildConfiguration("single");
        List<FastqFileSample> samples = buildFastqFileSample();
        fastqListAnalysis = new FastqListAnalysis(samples);
        Path pathWithPaired = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource(FASTQ_LIST_ANALYSIS_TOOL_TEST_OUTPUT_DATA_WITH_PAIRED_READ_TYPE_PATH)).toURI());
        expectedCmdWithPaired = readFile(pathWithPaired);
        Path pathWithSingle = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource(FASTQ_LIST_ANALYSIS_TOOL_TEST_OUTPUT_DATA_WITH_SINGLE_READ_TYPE_PATH)).toURI());
        expectedCmdWithSingle = readFile(pathWithSingle);
        CommonOutdir commonOutdir = new CommonOutdir(TEST_DIRECTORY);
        commonOutdir.createDirectory();
        actualFastqPath = String.format("%s/%s-%s-%s-FastqPaths.txt",
                configurationWithPaired.getStudyConfig().getDirOut(),
                configurationWithPaired.getStudyConfig().getProject(),
                configurationWithPaired.getStudyConfig().getRun(),
                configurationWithPaired.getStudyConfig().getDate());
    }

    @Test
    void generateWithPaired() throws IOException {
        fastqListAnalysis.generate(configurationWithPaired, templateEngine);
        actualCmd = readFile(Paths.get(actualFastqPath));
        assertEquals(expectedCmdWithPaired, actualCmd);
    }

    @Test
    void generateWithSingle() throws IOException {
        fastqListAnalysis.generate(configurationWithSingle, templateEngine);
        actualCmd = readFile(Paths.get(actualFastqPath));
        assertEquals(expectedCmdWithSingle, actualCmd);
    }

    private List<FastqFileSample> buildFastqFileSample() {
        return Arrays.asList(FastqFileSample.builder()
                        .name("sampleName")
                        .sampleType("sampeType")
                        .matchControl("matchControl")
                        .build(),
                FastqFileSample.builder()
                        .name("sampleName")
                        .sampleType("sampeType")
                        .matchControl("matchControl")
                        .build());
    }

    private Configuration buildConfiguration(String readType) {
        Configuration configuration = new Configuration();
        StudyConfig studyConfig = new StudyConfig();
        studyConfig.setDirOut(TEST_DIRECTORY);
        studyConfig.setRun("run1234");
        studyConfig.setDate("20140318");
        studyConfig.setProject("Example_project");
        GlobalConfig globalConfig = new GlobalConfig();
        GlobalConfig.PipelineInfo pipelineInfo = new GlobalConfig.PipelineInfo();
        pipelineInfo.setWorkflow("Bam2Fastq");
        pipelineInfo.setReadType(readType);
        globalConfig.setPipelineInfo(pipelineInfo);
        configuration.setStudyConfig(studyConfig);
        configuration.setGlobalConfig(globalConfig);
        return configuration;
    }
}
