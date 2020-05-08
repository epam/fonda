/*
 * Copyright 2017-2020 Sanofi and EPAM Systems, Inc. (https://www.epam.com/)
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

import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.samples.fastq.FastqFileSample;
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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PicardMergeDnaBamTest extends AbstractTest {
    private static final String PICARD_MERGE_DNA_BAM_TOOL_TEST_OUTPUT_DATA_PATH =
            "templates/picard_merge_dna_bam_tool_test_output_data.txt";
    private static final String BAM1 = "bamFile1.bam";
    private static final String BAM2 = "bamFile2.bam";

    private Configuration expectedConfiguration;
    private FastqFileSample expectedSample;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private List<String> bamList;

    @BeforeEach
    void setup() {
        expectedConfiguration = new Configuration();
        constructExpectedSample();
        GlobalConfig expectedGlobalConfig = new GlobalConfig();
        constructToolConfig(expectedGlobalConfig);
        expectedConfiguration.setGlobalConfig(expectedGlobalConfig);
        bamList = Arrays.asList(BAM1, BAM2);
    }

    @Test
    void shouldGenerate() throws URISyntaxException, IOException {
        PicardMergeDnaBam picardMergeDnaBam = new PicardMergeDnaBam(expectedSample, bamList);
        Path path = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource(PICARD_MERGE_DNA_BAM_TOOL_TEST_OUTPUT_DATA_PATH)).toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        String expectedCmd = new String(fileBytes);
        final BamResult result = picardMergeDnaBam.generate(expectedConfiguration, expectedTemplateEngine);
        assertEquals(expectedCmd, result.getCommand().getToolCommand());
    }

    private void constructExpectedSample() {
        expectedSample = new FastqFileSample();
        expectedSample.setName("sampleName");
        expectedSample.setBamOutdir("rootOutdir/sampleName/bam");
    }

    private void constructToolConfig(GlobalConfig expectedGlobalConfig) {
        GlobalConfig.ToolConfig expectedToolConfig = new GlobalConfig.ToolConfig();
        expectedToolConfig.setSamTools("samtools");
        expectedToolConfig.setJava("java");
        expectedToolConfig.setPicardVersion("v2.10.3");
        expectedToolConfig.setPicard("picard");
        expectedGlobalConfig.setToolConfig(expectedToolConfig);
    }
}
