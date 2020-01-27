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
    private static final String OPTY_TIPE_DNA_TEST_OUTPUT_DATA_PATH =
            "templates/optiType_tool_with_dna_lt_test_output_data.txt";
    private static final String OPTY_TIPE_RNA_TEST_OUTPUT_DATA_PATH =
            "templates/optiType_tool_with_rna_lt_test_output_data.txt";
    private OptiType optiType;
    private Configuration expectedConfiguration;
    private BashCommand expectedBashCommand;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();

    void setup(String libraryType, String pathToTemplate) throws URISyntaxException, IOException {
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
        expectedConfiguration = new Configuration();
        GlobalConfig expectedGlobalConfig = new GlobalConfig();
        StudyConfig expectedStudyConfig = new StudyConfig();
        expectedStudyConfig.setLibraryType(libraryType);
        GlobalConfig.QueueParameters expectedQueueParameters = new GlobalConfig.QueueParameters();
        expectedQueueParameters.setNumThreads(5);
        GlobalConfig.ToolConfig expectedToolConfig = new GlobalConfig.ToolConfig();
        expectedToolConfig.setOptitype("optiType");
        expectedGlobalConfig.setQueueParameters(expectedQueueParameters);
        expectedGlobalConfig.setToolConfig(expectedToolConfig);
        expectedConfiguration.setGlobalConfig(expectedGlobalConfig);
        expectedConfiguration.setStudyConfig(expectedStudyConfig);
        Path path = Paths.get(Objects.requireNonNull(
                this.getClass().getClassLoader().getResource(pathToTemplate)).toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        String expectedCmd = new String(fileBytes);
        expectedBashCommand = new BashCommand(expectedCmd);
    }

    @Test
    void shouldGenerateWithDnaLibraryType() throws IOException, URISyntaxException {
        setup("DNA", OPTY_TIPE_DNA_TEST_OUTPUT_DATA_PATH);
        OptiTypeResult optiTypeResult = optiType.generate(expectedConfiguration, expectedTemplateEngine);
        assertEquals(expectedBashCommand.getToolCommand(), optiTypeResult.getCommand().getToolCommand());
    }

    @Test
    void shouldGenerateWithRnaLibraryType() throws IOException, URISyntaxException {
        setup("RNA", OPTY_TIPE_RNA_TEST_OUTPUT_DATA_PATH);
        OptiTypeResult optiTypeResult = optiType.generate(expectedConfiguration, expectedTemplateEngine);
        assertEquals(expectedBashCommand.getToolCommand(), optiTypeResult.getCommand().getToolCommand());
    }
}
