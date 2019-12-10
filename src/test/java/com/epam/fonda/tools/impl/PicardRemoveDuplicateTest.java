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
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PicardRemoveDuplicateTest extends AbstractTest {
    private static final String PICARD_REMOVE_DUPLICATE_TEST_OUTPUT_DATA_PATH =
            "templates/picard_remove_duplicate_test_output_data.txt";
    private PicardRemoveDuplicate picardRemoveDuplicate;
    private Configuration expectedConfiguration;
    private String expectedCmd;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();

    @BeforeEach
    void setup() throws URISyntaxException, IOException {
        BamResult bamResult = BamResult.builder()
                .bamOutput(BamOutput.builder().mkdupBam("sbamOutdir/sampleName.toolName.sorted.mkdup.bam").build())
                .command(BashCommand.withTool(""))
                .build();
        picardRemoveDuplicate = new PicardRemoveDuplicate(bamResult);
        expectedConfiguration = new Configuration();
        GlobalConfig expectedGlobalConfig = new GlobalConfig();
        GlobalConfig.ToolConfig expectedToolConfig = new GlobalConfig.ToolConfig();
        expectedToolConfig.setSamTools("samtools");
        expectedGlobalConfig.setToolConfig(expectedToolConfig);
        expectedConfiguration.setGlobalConfig(expectedGlobalConfig);
        Path path = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource(PICARD_REMOVE_DUPLICATE_TEST_OUTPUT_DATA_PATH)).toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        expectedCmd = new String(fileBytes);
    }

    @Test
    void shouldGenerate() {
        final String actualCmd = picardRemoveDuplicate.generate(expectedConfiguration, expectedTemplateEngine)
                .getCommand().getToolCommand();
        assertEquals(expectedCmd, actualCmd);
    }
}
