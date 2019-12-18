package com.epam.fonda.tools.impl;

import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.entity.configuration.StudyConfig;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.utils.PipelineUtils;
import com.epam.fonda.utils.TemplateEngineUtils;
import com.epam.fonda.utils.ToolUtils;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

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
    }

    @Test
    void generateWithPaired() throws IOException {
        Assert.assertEquals(expectedCmdWithPaired, fastqListAnalysis.generate(configurationWithPaired, templateEngine).getCommand().getToolCommand());
    }

    @Test
    void generateWithSingle() throws IOException {
        Assert.assertEquals(expectedCmdWithSingle, fastqListAnalysis.generate(configurationWithSingle, templateEngine).getCommand().getToolCommand());
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
        studyConfig.setDirOut("outDir");
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