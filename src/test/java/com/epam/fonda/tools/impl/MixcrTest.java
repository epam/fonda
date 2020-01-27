/*
 * Copyright 2017-2020 Sanofi and EPAM Systems, Inc. (https://www.epam.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import com.epam.fonda.tools.results.FastqOutput;
import com.epam.fonda.tools.results.FastqResult;
import com.epam.fonda.tools.results.MixcrResult;
import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MixcrTest extends AbstractTest{
    public static final String HUMAN = "human";
    private static final String MIXCR_TEST_OUTPUT_DATA_HUMAN_RNA_PAIRED =
            "templates/mixcr_tool_test_human_rna_paired_output_data.txt";
    private static final String MIXCR_TEST_OUTPUT_DATA_HUMAN_DNA_PAIRED =
            "templates/mixcr_tool_test_human_dna_paired_output_data.txt";
    private static final String MIXCR_TEST_OUTPUT_DATA_HUMAN_DNA_SINGLE =
            "templates/mixcr_tool_test_human_dna_single_output_data.txt";
    private static final String MIXCR_TEST_OUTPUT_DATA_HUMAN_RNA_SINGLE =
            "templates/mixcr_tool_test_human_rna_single_output_data.txt";
    private static final String MIXCR_TEST_OUTPUT_DATA_MOUSE_RNA_PAIRED =
            "templates/mixcr_tool_test_mouse_rna_paired_output_data.txt";
    private static final String MIXCR_TEST_OUTPUT_DATA_MOUSE_DNA_PAIRED =
            "templates/mixcr_tool_test_mouse_dna_paired_output_data.txt";
    private static final String MIXCR_TEST_OUTPUT_DATA_MOUSE_RNA_SINGLE =
            "templates/mixcr_tool_test_mouse_rna_single_output_data.txt";
    private static final String MIXCR_TEST_OUTPUT_DATA_MOUSE_DNA_SINGLE =
            "templates/mixcr_tool_test_mouse_dna_single_output_data.txt";
    public static final String MOUSE = "mouse";
    public static final String RNA = "RNA";
    public static final String DNA = "DNA";

    private Mixcr mixcr;
    private FastqFileSample expectedSample = new FastqFileSample();
    private Configuration expectedConfiguration;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();

    @Test
    void shouldGenerateHumanDnaPaired() throws IOException, URISyntaxException {
        setup(HUMAN, DNA);
        BashCommand bashCommand = getExpectedBashCommandFromFile(MIXCR_TEST_OUTPUT_DATA_HUMAN_DNA_PAIRED);

        MixcrResult mixcrResult = mixcr.generate(expectedConfiguration, expectedTemplateEngine);

        assertEquals(bashCommand.getToolCommand(), mixcrResult.getCommand().getToolCommand());
    }

    @Test
    void shouldGenerateHumanDnaSingle() throws IOException, URISyntaxException {
        setup(HUMAN, DNA);
        mixcr = getSingleFastqMixcr();
        expectedConfiguration.getStudyConfig().setLibraryType(DNA);
        BashCommand bashCommand = getExpectedBashCommandFromFile(MIXCR_TEST_OUTPUT_DATA_HUMAN_DNA_SINGLE);

        MixcrResult mixcrResult = mixcr.generate(expectedConfiguration, expectedTemplateEngine);

        assertEquals(bashCommand.getToolCommand(), mixcrResult.getCommand().getToolCommand());
    }

    @Test
    void shouldGenerateHumanRnaPaired() throws IOException, URISyntaxException {
        setup(HUMAN, RNA);
        BashCommand bashCommand = getExpectedBashCommandFromFile(MIXCR_TEST_OUTPUT_DATA_HUMAN_RNA_PAIRED);

        MixcrResult mixcrResult = mixcr.generate(expectedConfiguration, expectedTemplateEngine);

        assertEquals(bashCommand.getToolCommand(), mixcrResult.getCommand().getToolCommand());
    }

    @Test
    void shouldGenerateHumanRnaSingle() throws IOException, URISyntaxException {
        setup(HUMAN, RNA);
        mixcr = getSingleFastqMixcr();
        BashCommand bashCommand = getExpectedBashCommandFromFile(MIXCR_TEST_OUTPUT_DATA_HUMAN_RNA_SINGLE);

        MixcrResult mixcrResult = mixcr.generate(expectedConfiguration, expectedTemplateEngine);

        assertEquals(bashCommand.getToolCommand(), mixcrResult.getCommand().getToolCommand());
    }

    @Test
    void shouldGenerateMouseDnaPaired() throws IOException, URISyntaxException {
        setup(MOUSE, DNA);
        BashCommand bashCommand = getExpectedBashCommandFromFile(MIXCR_TEST_OUTPUT_DATA_MOUSE_DNA_PAIRED);

        MixcrResult mixcrResult = mixcr.generate(expectedConfiguration, expectedTemplateEngine);

        assertEquals(bashCommand.getToolCommand(), mixcrResult.getCommand().getToolCommand());
    }

    @Test
    void shouldGenerateMouseDnaSingle() throws IOException, URISyntaxException {
        setup(MOUSE, DNA);
        mixcr = getSingleFastqMixcr();
        BashCommand bashCommand = getExpectedBashCommandFromFile(MIXCR_TEST_OUTPUT_DATA_MOUSE_DNA_SINGLE);

        MixcrResult mixcrResult = mixcr.generate(expectedConfiguration, expectedTemplateEngine);

        assertEquals(bashCommand.getToolCommand(), mixcrResult.getCommand().getToolCommand());
    }

    @Test
    void shouldGenerateMouseRnaPaired() throws IOException, URISyntaxException {
        setup(MOUSE, RNA);
        BashCommand bashCommand = getExpectedBashCommandFromFile(MIXCR_TEST_OUTPUT_DATA_MOUSE_RNA_PAIRED);

        MixcrResult mixcrResult = mixcr.generate(expectedConfiguration, expectedTemplateEngine);

        assertEquals(bashCommand.getToolCommand(), mixcrResult.getCommand().getToolCommand());
    }

    @Test
    void shouldGenerateMouseRnaSingle() throws IOException, URISyntaxException {
        setup(MOUSE, RNA);
        mixcr = getSingleFastqMixcr();
        BashCommand bashCommand = getExpectedBashCommandFromFile(MIXCR_TEST_OUTPUT_DATA_MOUSE_RNA_SINGLE);

        MixcrResult mixcrResult = mixcr.generate(expectedConfiguration, expectedTemplateEngine);

        assertEquals(bashCommand.getToolCommand(), mixcrResult.getCommand().getToolCommand());
    }

    void setup(String species, String libraryType) throws URISyntaxException, IOException {
        GlobalConfig globalConfig = new GlobalConfig();
        GlobalConfig.DatabaseConfig databaseConfig = new GlobalConfig.DatabaseConfig();
        databaseConfig.setSpecies(species);
        globalConfig.setDatabaseConfig(databaseConfig);
        StudyConfig studyConfig = new StudyConfig();
        studyConfig.setLibraryType(libraryType);
        expectedSample.setName("sampleName");
        expectedSample.setSampleOutputDir("output");
        expectedSample.createDirectory();
        FastqOutput fastqOutput = FastqOutput.builder()
                .mergedFastq1("merged_fastq1.gz")
                .mergedFastq2("merged_fastq2.gz")
                .build();
        FastqResult fastqResult = FastqResult.builder()
                .out(fastqOutput)
                .build();
        mixcr = new Mixcr(expectedSample, fastqResult);
        expectedConfiguration = new Configuration();
        GlobalConfig.QueueParameters expectedQueueParameters = new GlobalConfig.QueueParameters();
        expectedQueueParameters.setNumThreads(5);
        globalConfig.setQueueParameters(expectedQueueParameters);
        GlobalConfig.ToolConfig expectedToolConfig = new GlobalConfig.ToolConfig();
        expectedToolConfig.setMixcr("mixcr");
        globalConfig.setToolConfig(expectedToolConfig);
        expectedConfiguration.setStudyConfig(studyConfig);
        expectedConfiguration.setGlobalConfig(globalConfig);
    }

    private BashCommand getExpectedBashCommandFromFile(final String filePath) throws IOException, URISyntaxException {
        Path path = Paths.get(this.getClass().getClassLoader().getResource(
                filePath).toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        String expectedCmd = new String(fileBytes);

        return new BashCommand(expectedCmd);
    }

    private Mixcr getSingleFastqMixcr() {
        FastqOutput fastqOutput = FastqOutput.builder()
                .mergedFastq1("merged_fastq1.gz")
                .mergedFastq2(null)
                .build();
        FastqResult fastqResult = FastqResult.builder()
                .out(fastqOutput)
                .build();
        return new Mixcr(expectedSample, fastqResult);
    }
}
