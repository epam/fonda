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

class NovoalignSortTest extends AbstractTest {
    private static final String AMPLICON_NOVOALIGN_SORT_TEST_INPUT_DATA_WITH_FASTQ1_PATH =
            "templates/amplicon_novoalign_sort_test_tool_test_input_data_with_fastq1.txt";
    private static final String AMPLICON_NOVOALIGN_SORT_TEST_INPUT_DATA_WITH_FASTQ1_WITHOUT_BEDPRIMER_PATH =
            "templates/amplicon_novoalign_sort_test_tool_test_input_data_with_fastq1_without_bedprimer.txt";
    private static final String AMPLICON_NOVOALIGN_SORT_TEST_INPUT_DATA_WITH_FASTQ1_AND_FASTQ2_PATH =
            "templates/amplicon_novoalign_sort_test_tool_test_input_data_with_fastq1_and_fastq2.txt";
    private static final String AMPLICON_NOVOALIGN_SORT_TEST_INPUT_DATA_WITH_FASTQ1_AND_FASTQ1_WITHOUT_BEDPRIMER_PATH =
            "templates/amplicon_novoalign_sort_test_tool_test_input_data_with_fastq1_and_fastq2_without_bedprimer.txt";
    private static final String CAPTURE_NOVOALIGN_SORT_TEST_INPUT_DATA_WITH_FASTQ1_PATH =
            "templates/capture_novoalign_sort_test_tool_test_input_data_with_fastq1.txt";
    private static final String CAPTURE_NOVOALIGN_SORT_TEST_INPUT_DATA_WITH_FASTQ1_AND_FASTQ2_PATH =
            "templates/capture_novoalign_sort_test_tool_test_input_data_with_fastq1_and_fastq2.txt";

    private NovoalignSort novoalignSort;
    private Configuration expectedConfiguration;
    private BashCommand expectedBashCommand;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();

    @BeforeEach
    void setUp() {
        FastqFileSample expectedSample = FastqFileSample.builder()
                .name("sampleName")
                .bamOutdir("sbamOutdir")
                .build();
        novoalignSort = new NovoalignSort(expectedSample, "fastq1", "fastq2", 1);

        expectedConfiguration = new Configuration();
        expectedConfiguration.setGlobalConfig(new GlobalConfig());
        expectedConfiguration.setStudyConfig(new StudyConfig());
        expectedConfiguration.getGlobalConfig().getToolConfig().setNovoalign("novoalign");
        expectedConfiguration.getGlobalConfig().getDatabaseConfig().setNovoIndex("novoindex");
        expectedConfiguration.getGlobalConfig().getToolConfig().setSamTools("samtools");
        expectedConfiguration.getGlobalConfig().getQueueParameters().setNumThreads(5);
    }

    @Test
    void shouldGenerateWithFastq1ForAmpliconVarFastq() throws URISyntaxException, IOException {
        expectedConfiguration.getGlobalConfig().getPipelineInfo()
                .setWorkflow(PipelineType.DNA_AMPLICON_VAR_FASTQ.getName());
        novoalignSort.setFastq2(null);
        expectedConfiguration.getGlobalConfig().getDatabaseConfig().setBedPrimer("bedprimer");
        expectedBashCommand =
                getExpectedBashCommandFromFile(AMPLICON_NOVOALIGN_SORT_TEST_INPUT_DATA_WITH_FASTQ1_PATH);
        BamResult actualBamResult = novoalignSort.generate(expectedConfiguration, expectedTemplateEngine);

        assertEquals(expectedBashCommand.getToolCommand(), actualBamResult.getCommand().getToolCommand());
    }

    @Test
    void shouldGenerateWithFastq1WithoutBedPrimerForAmpliconVarFastq() throws URISyntaxException, IOException {
        expectedConfiguration.getGlobalConfig().getPipelineInfo()
                .setWorkflow(PipelineType.DNA_AMPLICON_VAR_FASTQ.getName());
        novoalignSort.setFastq2(null);
        expectedBashCommand =
                getExpectedBashCommandFromFile(
                        AMPLICON_NOVOALIGN_SORT_TEST_INPUT_DATA_WITH_FASTQ1_WITHOUT_BEDPRIMER_PATH);
        BamResult actualBamResult = novoalignSort.generate(expectedConfiguration, expectedTemplateEngine);

        assertEquals(expectedBashCommand.getToolCommand(), actualBamResult.getCommand().getToolCommand());
    }

    @Test
    void shouldGenerateWithFastq1AndFastq2ForAmpliconVarFastq() throws URISyntaxException, IOException {
        expectedConfiguration.getGlobalConfig().getPipelineInfo()
                .setWorkflow(PipelineType.DNA_AMPLICON_VAR_FASTQ.getName());
        expectedConfiguration.getGlobalConfig().getDatabaseConfig().setBedPrimer("bedprimer");
        expectedBashCommand =
                getExpectedBashCommandFromFile(AMPLICON_NOVOALIGN_SORT_TEST_INPUT_DATA_WITH_FASTQ1_AND_FASTQ2_PATH);
        BamResult actualBamResult = novoalignSort.generate(expectedConfiguration, expectedTemplateEngine);

        assertEquals(expectedBashCommand.getToolCommand(), actualBamResult.getCommand().getToolCommand());
    }

    @Test
    void shouldGenerateWithFastq1AndFastq2WithoutBedPrimerForAmpliconVarFastq() throws URISyntaxException, IOException {
        expectedConfiguration.getGlobalConfig().getPipelineInfo()
                .setWorkflow(PipelineType.DNA_AMPLICON_VAR_FASTQ.getName());
        expectedBashCommand =
                getExpectedBashCommandFromFile(
                        AMPLICON_NOVOALIGN_SORT_TEST_INPUT_DATA_WITH_FASTQ1_AND_FASTQ1_WITHOUT_BEDPRIMER_PATH);
        BamResult actualBamResult = novoalignSort.generate(expectedConfiguration, expectedTemplateEngine);

        assertEquals(expectedBashCommand.getToolCommand(), actualBamResult.getCommand().getToolCommand());
    }


    @Test
    void shouldGenerateWithFastq1ForCaptureVarFastq() throws URISyntaxException, IOException {
        expectedConfiguration.getGlobalConfig().getPipelineInfo()
                .setWorkflow(PipelineType.DNA_CAPTURE_VAR_FASTQ.getName());
        novoalignSort.setFastq2(null);
        expectedBashCommand =
                getExpectedBashCommandFromFile(CAPTURE_NOVOALIGN_SORT_TEST_INPUT_DATA_WITH_FASTQ1_PATH);
        BamResult actualBamResult = novoalignSort.generate(expectedConfiguration, expectedTemplateEngine);

        assertEquals(expectedBashCommand.getToolCommand(), actualBamResult.getCommand().getToolCommand());
    }

    @Test
    void shouldGenerateWithFastq1AndFastq2ForCaptureVarFastq() throws URISyntaxException, IOException {
        expectedConfiguration.getGlobalConfig().getPipelineInfo()
                .setWorkflow(PipelineType.DNA_CAPTURE_VAR_FASTQ.getName());
        expectedBashCommand = getExpectedBashCommandFromFile(
                CAPTURE_NOVOALIGN_SORT_TEST_INPUT_DATA_WITH_FASTQ1_AND_FASTQ2_PATH);
        BamResult actualBamResult = novoalignSort.generate(expectedConfiguration, expectedTemplateEngine);

        assertEquals(expectedBashCommand.getToolCommand(), actualBamResult.getCommand().getToolCommand());
    }

    private BashCommand getExpectedBashCommandFromFile(final String filePath) throws IOException, URISyntaxException {
        Path path = Paths.get(this.getClass().getClassLoader().getResource(
                filePath).toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        String expectedCmd = new String(fileBytes);

        return new BashCommand(expectedCmd);
    }
}
