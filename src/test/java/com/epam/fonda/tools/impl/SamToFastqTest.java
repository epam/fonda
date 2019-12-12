package com.epam.fonda.tools.impl;

import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.samples.fastq.FastqReadType;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SamToFastqTest extends AbstractTest {
    private static final String SAM_TO_FASTQ_TEST_OUTPUT_DATA_PATH_WITH_PAIRED_TYPE_READ =
            "templates/sam_to_fastq_tool_test_output_data_with_paired_type_read.txt";
    private static final String SAM_TO_FASTQ_TEST_OUTPUT_DATA_PATH_WITH_SINGLE_TYPE_READ =
            "templates/sam_to_fastq_tool_test_output_data_with_single_type_read.txt";

    private SamToFastq samToFastq;
    private TemplateEngine templateEngine = TemplateEngineUtils.init();

    @BeforeEach
    void setUp() {
        BamResult bamResult = BamResult.builder()
                .bamOutput(BamOutput.builder()
                        .bam("bam")
                        .build())
                .build();
        samToFastq = new SamToFastq("sampleName", "outDir", bamResult);
    }

    @Test
    void testGenerateSamToFastqWithSingleTypeRead() throws IOException, URISyntaxException {
        assertEquals(buildCmd(SAM_TO_FASTQ_TEST_OUTPUT_DATA_PATH_WITH_SINGLE_TYPE_READ),
                samToFastq.generate(buildConfigurationWithSingleReadType(), templateEngine).getCommand()
                        .getToolCommand());
    }

    @Test
    void testGenerateSamToFastqWithPairedTypeRead() throws IOException, URISyntaxException {
        assertEquals(buildCmd(SAM_TO_FASTQ_TEST_OUTPUT_DATA_PATH_WITH_PAIRED_TYPE_READ),
                samToFastq.generate(buildConfigurationWithPairedReadType(), templateEngine).getCommand()
                        .getToolCommand());
    }

    private String buildCmd(String pathToTemplate) throws URISyntaxException, IOException {
        Path path = Paths.get(this.getClass().getClassLoader()
                .getResource(pathToTemplate).toURI());
        return readFile(path);
    }

    private Configuration buildConfiguration() {
        GlobalConfig.ToolConfig expectedTool = new GlobalConfig.ToolConfig();
        expectedTool.setJava("java");
        expectedTool.setPicard("picard");
        GlobalConfig.PipelineInfo expectedPipelineInfo = new GlobalConfig.PipelineInfo();
        GlobalConfig expectedGlobalConfig = new GlobalConfig();
        expectedGlobalConfig.setToolConfig(expectedTool);
        expectedGlobalConfig.setPipelineInfo(expectedPipelineInfo);
        Configuration configuration = new Configuration();
        configuration.setGlobalConfig(expectedGlobalConfig);
        return configuration;
    }

    private Configuration buildConfigurationWithSingleReadType() {
        Configuration configurationWithSingleReadType = buildConfiguration();
        configurationWithSingleReadType.getGlobalConfig().getPipelineInfo().setReadType(FastqReadType.SINGLE.name());
        return configurationWithSingleReadType;
    }

    private Configuration buildConfigurationWithPairedReadType() {
        Configuration configurationWithPairedReadType = buildConfiguration();
        configurationWithPairedReadType.getGlobalConfig().getPipelineInfo().setReadType(FastqReadType.PAIRED.name());
        return configurationWithPairedReadType;
    }
}
