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

class LofreqTest extends AbstractTest {
    private static final String SAMPLE_NAME = "sample_name";
    private static final String BAM = "file.bam";
    private static final String CONTROL_BAM = "control.bam";
    private static final String LOFREQ_UNPAIRED_TEST = "lofreq_unpaired_test";
    private static final String LOFREQ_PAIRED_TEST = "lofreq_paired_test";
    private final Context context = new Context();
    private final BamOutput bamOutput = BamOutput.builder()
            .bam(BAM)
            .build();
    private final BamOutput controlBamOutput = BamOutput.builder()
            .bam(BAM)
            .controlBam(CONTROL_BAM)
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
    void shouldGenerateUnpairedLofreq() {
        final Lofreq lofreq = new Lofreq(SAMPLE_NAME, bamOutput, TEST_DIRECTORY, false);
        final VariantsVcfResult result = lofreq.generate(configuration, expectedTemplateEngine);
        final String expectedCmd = expectedTemplateEngine.process(LOFREQ_UNPAIRED_TEST, context);
        assertEquals(expectedCmd, result.getAbstractCommand().getToolCommand());
    }

    @Test
    void shouldGeneratePairedLofreq() {
        final Lofreq lofreq = new Lofreq(SAMPLE_NAME, controlBamOutput, TEST_DIRECTORY, true);
        final VariantsVcfResult result = lofreq.generate(configuration, expectedTemplateEngine);
        final String expectedCmd = expectedTemplateEngine.process(LOFREQ_PAIRED_TEST, context);
        assertEquals(expectedCmd, result.getAbstractCommand().getToolCommand());
    }

    @Test
    void shouldFailIfBedNotSpecified() {
        final Configuration config = initConfiguration();
        config.getGlobalConfig().getDatabaseConfig().setBed(null);
        final Lofreq lofreq = new Lofreq(SAMPLE_NAME, bamOutput, TEST_DIRECTORY, false);
        assertThrows(NullPointerException.class, () -> lofreq.generate(config, expectedTemplateEngine));
    }

    @Test
    void shouldFailIfGenomeNotSpecified() {
        final Configuration config = initConfiguration();
        config.getGlobalConfig().getDatabaseConfig().setGenome(null);
        final Lofreq lofreq = new Lofreq(SAMPLE_NAME, bamOutput, TEST_DIRECTORY, false);
        assertThrows(NullPointerException.class, () -> lofreq.generate(config, expectedTemplateEngine));
    }

    @Test
    void shouldFailIfLofreqToolIsNotSpecified() {
        final Configuration config = initConfiguration();
        config.getGlobalConfig().getToolConfig().setLofreq(null);
        final Lofreq lofreq = new Lofreq(SAMPLE_NAME, bamOutput, TEST_DIRECTORY, false);
        assertThrows(NullPointerException.class, () -> lofreq.generate(config, expectedTemplateEngine));
    }

    @Test
    void shouldFailIfControlBamNotSpecified() {
        final Lofreq lofreq = new Lofreq(SAMPLE_NAME, bamOutput, TEST_DIRECTORY, true);
        assertThrows(NullPointerException.class, () -> lofreq.generate(configuration, expectedTemplateEngine));
    }

    private Configuration initConfiguration() {
        final Configuration configuration = new Configuration();
        final GlobalConfig globalConfig = new GlobalConfig();
        final GlobalConfig.ToolConfig toolConfig = new GlobalConfig.ToolConfig();
        toolConfig.setLofreq("lofreq");
        globalConfig.setToolConfig(toolConfig);
        final GlobalConfig.DatabaseConfig databaseConfig = new GlobalConfig.DatabaseConfig();
        databaseConfig.setGenome("GENOME");
        databaseConfig.setBed("BED");
        globalConfig.setDatabaseConfig(databaseConfig);
        final GlobalConfig.QueueParameters queueParameters = new GlobalConfig.QueueParameters();
        queueParameters.setNumThreads(4);
        globalConfig.setQueueParameters(queueParameters);
        configuration.setGlobalConfig(globalConfig);
        return configuration;
    }

}
