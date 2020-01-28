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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MixcrTest extends AbstractTest {
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
    public static final String HUMAN = "human";
    public static final String MOUSE = "mouse";
    public static final String RNA = "RNA";
    public static final String DNA = "DNA";
    public static final String SAMPLE_NAME = "sampleName";
    public static final String OUTPUT = "output";
    public static final String MIXCR = "mixcr";

    private Mixcr mixcr;
    private FastqFileSample expectedSample = initSample();
    private Configuration expectedConfiguration;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();

    @ParameterizedTest
    @MethodSource("speciesAndLibraryTypePairedFastq")
    void shouldGenerateWithPairedFastq(String species, String libraryType, String templatePath)
            throws IOException, URISyntaxException {
        setup(species, libraryType);
        mixcr = getPairedFastqMixcr();
        BashCommand bashCommand = getExpectedBashCommandFromFile(templatePath);

        MixcrResult mixcrResult = mixcr.generate(expectedConfiguration, expectedTemplateEngine);

        assertEquals(bashCommand.getToolCommand(), mixcrResult.getCommand().getToolCommand());
    }

    @ParameterizedTest
    @MethodSource("speciesAndLibraryTypeSingleFastq")
    void shouldGenerateWithSingleFastq(String species, String libraryType, String templatePath)
            throws IOException, URISyntaxException {
        setup(species, libraryType);
        mixcr = getSingleFastqMixcr();
        BashCommand bashCommand = getExpectedBashCommandFromFile(templatePath);

        MixcrResult mixcrResult = mixcr.generate(expectedConfiguration, expectedTemplateEngine);

        assertEquals(bashCommand.getToolCommand(), mixcrResult.getCommand().getToolCommand());
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> speciesAndLibraryTypePairedFastq(){
        return Stream.of(
                Arguments.of(HUMAN, DNA, MIXCR_TEST_OUTPUT_DATA_HUMAN_DNA_PAIRED),
                Arguments.of(HUMAN, RNA, MIXCR_TEST_OUTPUT_DATA_HUMAN_RNA_PAIRED),
                Arguments.of(MOUSE, DNA, MIXCR_TEST_OUTPUT_DATA_MOUSE_DNA_PAIRED),
                Arguments.of(MOUSE, RNA, MIXCR_TEST_OUTPUT_DATA_MOUSE_RNA_PAIRED)
        );
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> speciesAndLibraryTypeSingleFastq(){
        return Stream.of(
                Arguments.of(HUMAN, DNA, MIXCR_TEST_OUTPUT_DATA_HUMAN_DNA_SINGLE),
                Arguments.of(HUMAN, RNA, MIXCR_TEST_OUTPUT_DATA_HUMAN_RNA_SINGLE),
                Arguments.of(MOUSE, DNA, MIXCR_TEST_OUTPUT_DATA_MOUSE_DNA_SINGLE),
                Arguments.of(MOUSE, RNA, MIXCR_TEST_OUTPUT_DATA_MOUSE_RNA_SINGLE)
        );
    }

    private BashCommand getExpectedBashCommandFromFile(final String filePath) throws IOException, URISyntaxException {
        Path path = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource(
                filePath)).toURI());
        String expectedCmd = readFile(path);
        return new BashCommand(expectedCmd);
    }

    private void setup(String species, String libraryType) {
        expectedConfiguration = new Configuration();
        expectedConfiguration.setStudyConfig(initStudyConfig(libraryType));
        expectedConfiguration.setGlobalConfig(initGlobalConfig(species));
    }

    private GlobalConfig initGlobalConfig(String species) {
        initSample();
        GlobalConfig globalConfig = new GlobalConfig();
        GlobalConfig.DatabaseConfig databaseConfig = new GlobalConfig.DatabaseConfig();
        databaseConfig.setSpecies(species);
        globalConfig.setDatabaseConfig(databaseConfig);
        GlobalConfig.QueueParameters expectedQueueParameters = new GlobalConfig.QueueParameters();
        expectedQueueParameters.setNumThreads(5);
        globalConfig.setQueueParameters(expectedQueueParameters);
        GlobalConfig.ToolConfig expectedToolConfig = new GlobalConfig.ToolConfig();
        expectedToolConfig.setMixcr(MIXCR);
        globalConfig.setToolConfig(expectedToolConfig);
        return globalConfig;
    }

    private StudyConfig initStudyConfig(String libraryType) {
        StudyConfig studyConfig = new StudyConfig();
        studyConfig.setLibraryType(libraryType);
        return studyConfig;
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

    private FastqFileSample initSample() {
        FastqFileSample sample = new FastqFileSample();
        sample.setName(SAMPLE_NAME);
        sample.setSampleOutputDir(OUTPUT);
        sample.createDirectory();
        return sample;
    }

    private Mixcr getPairedFastqMixcr() {
        FastqOutput fastqOutput = FastqOutput.builder()
                .mergedFastq1("merged_fastq1.gz")
                .mergedFastq2("merged_fastq2.gz")
                .build();
        FastqResult fastqResult = FastqResult.builder()
                .out(fastqOutput)
                .build();
        return new Mixcr(expectedSample, fastqResult);
    }
}
