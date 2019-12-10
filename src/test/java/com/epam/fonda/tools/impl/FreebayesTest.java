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

import com.epam.fonda.entity.configuration.CommonOutdir;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.VariantsVcfResult;
import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FreebayesTest extends AbstractTest {
    private static final String SAMPLE_NAME = "sample_name";
    private static final String BAM = "file.bam";
    private static final String FREEBAYES_UNPAIRED_TEST = "freebayes_test";
    private final Context context = new Context();
    private final BamOutput bamOutput = BamOutput.builder()
            .bam(BAM)
            .build();

    private Configuration configuration;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();

    @BeforeEach
    void setup() {
        configuration = initConfiguration();
        context.setVariable("output", TEST_DIRECTORY);

        final CommonOutdir commonOutdir = new CommonOutdir(TEST_DIRECTORY);
        commonOutdir.createDirectory();
    }

    @Test
    void shouldGenerateUnpairedFreebayes() {
        final Freebayes freebayes = new Freebayes(SAMPLE_NAME, bamOutput, TEST_DIRECTORY);
        final VariantsVcfResult result = freebayes.generate(configuration, expectedTemplateEngine);
        final String expectedCmd = expectedTemplateEngine.process(FREEBAYES_UNPAIRED_TEST, context);
        assertEquals(expectedCmd, result.getAbstractCommand().getToolCommand());
    }

    @Test
    void shouldFailIfBedNotSpecified() {
        final Configuration config = initConfiguration();
        config.getGlobalConfig().getDatabaseConfig().setBed(null);
        final Freebayes freebayes = new Freebayes(SAMPLE_NAME, bamOutput, TEST_DIRECTORY);
        assertThrows(NullPointerException.class, () -> freebayes.generate(config, expectedTemplateEngine));
    }

    @Test
    void shouldFailIfGenomeNotSpecified() {
        final Configuration config = initConfiguration();
        config.getGlobalConfig().getDatabaseConfig().setGenome(null);
        final Freebayes freebayes = new Freebayes(SAMPLE_NAME, bamOutput, TEST_DIRECTORY);
        assertThrows(NullPointerException.class, () -> freebayes.generate(config, expectedTemplateEngine));
    }

    @Test
    void shouldFailIfFreebayesToolIsNotSpecified() {
        final Configuration config = initConfiguration();
        config.getGlobalConfig().getToolConfig().setFreebayes(null);
        final Freebayes freebayes = new Freebayes(SAMPLE_NAME, bamOutput, TEST_DIRECTORY);
        assertThrows(NullPointerException.class, () -> freebayes.generate(config, expectedTemplateEngine));
    }

    private Configuration initConfiguration() {
        final Configuration configuration = new Configuration();
        final GlobalConfig globalConfig = new GlobalConfig();
        final GlobalConfig.ToolConfig toolConfig = new GlobalConfig.ToolConfig();
        toolConfig.setFreebayes("freebayes");
        globalConfig.setToolConfig(toolConfig);
        final GlobalConfig.DatabaseConfig databaseConfig = new GlobalConfig.DatabaseConfig();
        databaseConfig.setGenome("GENOME");
        databaseConfig.setBed("BED");
        globalConfig.setDatabaseConfig(databaseConfig);
        configuration.setGlobalConfig(globalConfig);
        return configuration;
    }
}
