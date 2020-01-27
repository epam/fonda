package com.epam.fonda.tools.impl;

import com.epam.fonda.entity.command.BashCommand;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.entity.configuration.StudyConfig;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.results.FastqOutput;
import com.epam.fonda.tools.results.FastqResult;
import com.epam.fonda.tools.results.OptiTypeResult;
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

public class OptiTypeTest extends AbstractTest {
    private static final String OPTY_TIPE_DNA_TEST_OUTPUT_DATA_PATH = "templates/optiType_dna_tool_test_output_data.txt";
    private static final String OPTY_TIPE_RNA_TEST_OUTPUT_DATA_PATH = "templates/optiType_rna_tool_test_output_data.txt";
    private OptiType optiType;
    private Configuration expectedConfigurationWithDnaLibraryType;
    private Configuration expectedConfigurationWithRnaLibraryType;
    private BashCommand expectedBashCommandForDnaLibraryType;
    private BashCommand expectedBashCommandForRnaLibraryType;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();

    @BeforeEach
    void setup() throws URISyntaxException, IOException {
        FastqFileSample expectedSample = new FastqFileSample();
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
        optiType = new OptiType(expectedSample, fastqResult);
        expectedConfigurationWithDnaLibraryType = new Configuration();
        expectedConfigurationWithRnaLibraryType = new Configuration();
        GlobalConfig expectedGlobalConfig = new GlobalConfig();
        StudyConfig expectedStudyConfigWithDna = new StudyConfig();
        StudyConfig expectedStudyConfigWithRna = new StudyConfig();
        expectedStudyConfigWithDna.setLibraryType("DNA");
        expectedStudyConfigWithRna.setLibraryType("RNA");
        GlobalConfig.QueueParameters expectedQueueParameters = new GlobalConfig.QueueParameters();
        expectedQueueParameters.setNumThreads(5);
        GlobalConfig.ToolConfig expectedToolConfig = new GlobalConfig.ToolConfig();
        expectedToolConfig.setOptitype("optiType");
        expectedGlobalConfig.setQueueParameters(expectedQueueParameters);
        expectedGlobalConfig.setToolConfig(expectedToolConfig);
        expectedConfigurationWithDnaLibraryType.setGlobalConfig(expectedGlobalConfig);
        expectedConfigurationWithDnaLibraryType.setStudyConfig(expectedStudyConfigWithDna);
        expectedConfigurationWithRnaLibraryType.setGlobalConfig(expectedGlobalConfig);
        expectedConfigurationWithRnaLibraryType.setStudyConfig(expectedStudyConfigWithRna);
        Path pathToOptiTypeWithDna = Paths.get(Objects.requireNonNull(
                this.getClass().getClassLoader().getResource(OPTY_TIPE_DNA_TEST_OUTPUT_DATA_PATH)).toURI());
        byte[] fileBytesForDnaLibraryType  = Files.readAllBytes(pathToOptiTypeWithDna);
        String expectedCmdForDnaLibraryType  = new String(fileBytesForDnaLibraryType );
        expectedBashCommandForDnaLibraryType = new BashCommand(expectedCmdForDnaLibraryType);
        Path pathToOptiTypeWithRna = Paths.get(Objects.requireNonNull(
                this.getClass().getClassLoader().getResource(OPTY_TIPE_RNA_TEST_OUTPUT_DATA_PATH)).toURI());
        byte[] fileBytesForRnaLibraryType  = Files.readAllBytes(pathToOptiTypeWithRna);
        String expectedCmdForRnaLibraryType  = new String(fileBytesForRnaLibraryType );
        expectedBashCommandForRnaLibraryType = new BashCommand(expectedCmdForRnaLibraryType);
    }

    @Test
    void shouldGenerate() {
        OptiTypeResult optiTypeResultForDnaLibraryType = optiType
                .generate(expectedConfigurationWithDnaLibraryType, expectedTemplateEngine);
        OptiTypeResult optiTypeResultForRnaLibraryType = optiType
                .generate(expectedConfigurationWithRnaLibraryType, expectedTemplateEngine);
        assertEquals(
                expectedBashCommandForDnaLibraryType.getToolCommand(),
                optiTypeResultForDnaLibraryType.getCommand().getToolCommand());
        assertEquals(
                expectedBashCommandForRnaLibraryType.getToolCommand(),
                optiTypeResultForRnaLibraryType.getCommand().getToolCommand());
    }
}
