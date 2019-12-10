package com.epam.fonda.tools.impl;

import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SortBamByReadNameTest extends AbstractTest {
    private static final String SORT_BAM_BY_READ_NAME_TEST_OUTPUT_DATA_PATH =
            "templates/sort_bam_by_read_name_tool_test_output_data.txt";
    private SortBamByReadName sortBamByReadname;
    private Configuration expectedConfiguration;
    private TemplateEngine expectedEngine = TemplateEngineUtils.init();
    private String expectedCmd;

    @BeforeEach
    void setUp() throws IOException, URISyntaxException {
        buildConfiguration();
        sortBamByReadname = new SortBamByReadName("sampleName", "outDir", BamResult.builder()
                .bamOutput(BamOutput.builder()
                        .bam("bam")
                        .build())
                .build());
        Path path = Paths.get(this.getClass().getClassLoader()
                .getResource(SORT_BAM_BY_READ_NAME_TEST_OUTPUT_DATA_PATH).toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        expectedCmd = new String(fileBytes);
    }

    @Test
    void generate() {
        String actualCmd = sortBamByReadname.generate(expectedConfiguration, expectedEngine)
                .getCommand().getToolCommand();
        assertEquals(expectedCmd, actualCmd);
    }

    private void buildConfiguration() {
        expectedConfiguration = new Configuration();
        GlobalConfig expectedGlobalConfig = new GlobalConfig();
        GlobalConfig.ToolConfig expectedToolConfig = new GlobalConfig.ToolConfig();
        expectedToolConfig.setJava("java");
        expectedToolConfig.setPicard("picard");
        expectedToolConfig.setSamTools("samtools");
        expectedGlobalConfig.setToolConfig(expectedToolConfig);
        expectedConfiguration.setGlobalConfig(expectedGlobalConfig);
    }
}
