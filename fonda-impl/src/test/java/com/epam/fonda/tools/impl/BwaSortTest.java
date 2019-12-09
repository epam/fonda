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

import com.epam.fonda.entity.command.BashCommand;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.entity.configuration.StudyConfig;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.utils.TemplateEngineUtils;
import com.epam.fonda.workflow.PipelineType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BwaSortTest extends AbstractTest {
    private static final String AMPLICON_BWA_SORT_TEST_INPUT_DATA_WITH_FASTQ1_PATH =
            "templates/ampliconBwaSortTest_tool_test_input_data_with_fastq1.txt";
    private static final String AMPLICON_BWA_SORT_TEST_INPUT_DATA_WITH_FASTQ1_AND_FASTQ2_PATH =
            "templates/ampliconBwaSortTest_tool_test_input_data_with_fastq1_and_fastq2.txt";
    private static final String CAPTURE_BWA_SORT_TEST_INPUT_DATA_WITH_FASTQ1_PATH =
            "templates/captureBwaSortTest_tool_test_input_data_with_fastq1.txt";
    private static final String CAPTURE_BWA_SORT_TEST_INPUT_DATA_WITH_FASTQ1_AND_FASTQ2_PATH =
            "templates/captureBwaSortTest_tool_test_input_data_with_fastq1_and_fastq2.txt";

    private BwaSort bwaSort;
    private Configuration expectedConfiguration;
    private BashCommand expectedBashCommand;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();

    @BeforeEach
    void setUp() {
        FastqFileSample expectedSample = FastqFileSample.builder()
                .name("sampleName")
                .bamOutdir("sbamOutdir")
                .build();
        bwaSort = new BwaSort(expectedSample, "fastq1", "fastq2", 1);

        expectedConfiguration = new Configuration();
        expectedConfiguration.setGlobalConfig(new GlobalConfig());
        expectedConfiguration.setStudyConfig(new StudyConfig());
        expectedConfiguration.getGlobalConfig().getToolConfig().setBwa("bwa");
        expectedConfiguration.getGlobalConfig().getDatabaseConfig().setGenome("genome");
        expectedConfiguration.getGlobalConfig().getToolConfig().setSamTools("samTools");
        expectedConfiguration.getGlobalConfig().getQueueParameters().setNumThreads(4);
    }

    @Test
    void shouldGenerateWithFastq1AndFastq2ForAmplicon() throws URISyntaxException, IOException {
        expectedConfiguration.getGlobalConfig().getPipelineInfo()
                .setWorkflow(PipelineType.DNA_AMPLICON_VAR_FASTQ.getName());
        expectedBashCommand =
                getExpectedBashCommandFromFile(AMPLICON_BWA_SORT_TEST_INPUT_DATA_WITH_FASTQ1_AND_FASTQ2_PATH);
        BamResult actualBamResult = bwaSort.generate(expectedConfiguration, expectedTemplateEngine);

        assertEquals(expectedBashCommand.getToolCommand(), actualBamResult.getCommand().getToolCommand());
    }

    @Test
    void shouldGenerateWithOnlyFastq1ForAmplicon() throws URISyntaxException, IOException {
        bwaSort.setFastq2(null);
        expectedConfiguration.getGlobalConfig().getPipelineInfo()
                .setWorkflow(PipelineType.DNA_AMPLICON_VAR_FASTQ.getName());
        expectedBashCommand =
                getExpectedBashCommandFromFile(AMPLICON_BWA_SORT_TEST_INPUT_DATA_WITH_FASTQ1_PATH);
        BamResult actualBamResult = bwaSort.generate(expectedConfiguration, expectedTemplateEngine);

        assertEquals(expectedBashCommand.getToolCommand(), actualBamResult.getCommand().getToolCommand());
    }

    @Test
    void shouldGenerateWithFastq1AndFastq2ForCapture() throws URISyntaxException, IOException {
        expectedConfiguration.getGlobalConfig().getPipelineInfo()
                .setWorkflow(PipelineType.DNA_CAPTURE_VAR_FASTQ.getName());
        expectedBashCommand =
                getExpectedBashCommandFromFile(CAPTURE_BWA_SORT_TEST_INPUT_DATA_WITH_FASTQ1_AND_FASTQ2_PATH);
        BamResult actualBamResult = bwaSort.generate(expectedConfiguration, expectedTemplateEngine);

        assertEquals(expectedBashCommand.getToolCommand(), actualBamResult.getCommand().getToolCommand());
    }

    @Test
    void shouldGenerateWithOnlyFastq1ForCapture() throws URISyntaxException, IOException {
        bwaSort.setFastq2(null);
        expectedConfiguration.getGlobalConfig().getPipelineInfo()
                .setWorkflow(PipelineType.DNA_CAPTURE_VAR_FASTQ.getName());
        expectedBashCommand =
                getExpectedBashCommandFromFile(CAPTURE_BWA_SORT_TEST_INPUT_DATA_WITH_FASTQ1_PATH);
        BamResult actualBamResult = bwaSort.generate(expectedConfiguration, expectedTemplateEngine);

        assertEquals(expectedBashCommand.getToolCommand(), actualBamResult.getCommand().getToolCommand());
    }

    @Test
    void shouldThrowException() {
        expectedConfiguration.getGlobalConfig().getPipelineInfo()
                .setWorkflow(PipelineType.DNA_AMPLICON_VAR_FASTQ.getName());
        bwaSort.setFastq1(null);

        assertThrows(IllegalArgumentException.class, () -> bwaSort
                .generate(expectedConfiguration, expectedTemplateEngine)
                .getCommand().getToolCommand());
    }

    private BashCommand getExpectedBashCommandFromFile(final String filePath) throws IOException, URISyntaxException {
        Path path = Paths.get(this.getClass().getClassLoader().getResource(filePath).toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        String expectedCmd = new String(fileBytes);

        return new BashCommand(expectedCmd);
    }
}
