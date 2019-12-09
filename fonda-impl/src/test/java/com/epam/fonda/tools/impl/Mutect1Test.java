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

class Mutect1Test extends AbstractTest {
    private static final String MUTECT1_TEMPLATE_TEST = "mutect1_template_test";
    private static final String SAMPLE_NAME = "sample_name";
    private static final String BAM = "file.bam";
    private final Context context = new Context();
    private final BamOutput bamOutput = BamOutput.builder()
            .bam(BAM)
            .build();
    private final TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private final Mutect1 mutect1 = new Mutect1(SAMPLE_NAME, bamOutput, TEST_DIRECTORY);

    @BeforeEach
    void setup() {
        context.setVariable("output", TEST_DIRECTORY);

        final CommonOutdir commonOutdir = new CommonOutdir(TEST_DIRECTORY);
        commonOutdir.createDirectory();
    }

    @Test
    void shouldGenerateUnpairedMutect1() {
        final Configuration configuration = initConfiguration();
        final VariantsVcfResult result = mutect1.generate(configuration, expectedTemplateEngine);
        final String expectedCmd = expectedTemplateEngine.process(MUTECT1_TEMPLATE_TEST, context);
        assertEquals(expectedCmd, result.getAbstractCommand().getToolCommand());
    }

    @Test
    void shouldFailIfBedNotSpecified() {
        final Configuration config = initConfiguration();
        config.getGlobalConfig().getDatabaseConfig().setBed(null);
        assertThrows(NullPointerException.class, () -> mutect1.generate(config, expectedTemplateEngine));
    }

    @Test
    void shouldFailIfGenomeNotSpecified() {
        final Configuration config = initConfiguration();
        config.getGlobalConfig().getDatabaseConfig().setGenome(null);
        assertThrows(NullPointerException.class, () -> mutect1.generate(config, expectedTemplateEngine));
    }

    @Test
    void shouldFailIfDbsnpNotSpecified() {
        final Configuration config = initConfiguration();
        config.getGlobalConfig().getDatabaseConfig().setDbsnp(null);
        assertThrows(NullPointerException.class, () -> mutect1.generate(config, expectedTemplateEngine));
    }

    @Test
    void shouldFailIfCosmicNotSpecified() {
        final Configuration config = initConfiguration();
        config.getGlobalConfig().getDatabaseConfig().setCosmic(null);
        assertThrows(NullPointerException.class, () -> mutect1.generate(config, expectedTemplateEngine));
    }

    @Test
    void shouldFailIfMutectToolNotSpecified() {
        final Configuration config = initConfiguration();
        config.getGlobalConfig().getToolConfig().setMutect(null);
        assertThrows(NullPointerException.class, () -> mutect1.generate(config, expectedTemplateEngine));
    }

    @Test
    void shouldFailIfMutectJavaToolNotSpecified() {
        final Configuration config = initConfiguration();
        config.getGlobalConfig().getToolConfig().setMutectJava(null);
        assertThrows(NullPointerException.class, () -> mutect1.generate(config, expectedTemplateEngine));
    }

    @Test
    void shouldFailIfMutectNormalPanelNotSpecified() {
        final Configuration config = initConfiguration();
        config.getGlobalConfig().getDatabaseConfig().setMutectNormalPanel(null);
        assertThrows(NullPointerException.class, () -> mutect1.generate(config, expectedTemplateEngine));
    }

    @Test
    void shouldFailIfBamNotSpecified() {
        final Mutect1 mutect = new Mutect1(SAMPLE_NAME, BamOutput.builder().build(), TEST_DIRECTORY);
        final Configuration config = initConfiguration();
        assertThrows(NullPointerException.class, () -> mutect.generate(config, expectedTemplateEngine));
    }

    private Configuration initConfiguration() {
        final Configuration configuration = new Configuration();
        final GlobalConfig globalConfig = new GlobalConfig();
        final GlobalConfig.ToolConfig toolConfig = new GlobalConfig.ToolConfig();
        toolConfig.setMutect("mutect1");
        toolConfig.setMutectJava("mutect_java");
        globalConfig.setToolConfig(toolConfig);
        final GlobalConfig.DatabaseConfig databaseConfig = new GlobalConfig.DatabaseConfig();
        databaseConfig.setGenome("GENOME");
        databaseConfig.setBed("BED");
        databaseConfig.setCosmic("cosmic");
        databaseConfig.setDbsnp("mutect.dbsnp");
        databaseConfig.setMutectNormalPanel("normal_panel");
        globalConfig.setDatabaseConfig(databaseConfig);
        configuration.setGlobalConfig(globalConfig);
        return configuration;
    }
}
