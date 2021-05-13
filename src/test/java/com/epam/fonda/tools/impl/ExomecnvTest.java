/*
 * Copyright 2017-2021 Sanofi and EPAM Systems, Inc. (https://www.epam.com/)
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

import com.epam.fonda.entity.command.AbstractCommand;
import com.epam.fonda.entity.configuration.CommonOutdir;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.ExomecnvResult;
import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExomecnvTest extends AbstractTest {
    private static final String EXOMECNV_TEMPLATE = "exomecnv_template_test";
    private static final String SAMPLE_NAME = "sample_name";
    private static final String CONTROL_SAMPLE_NAME = "control_sample_name";
    private static final String BAM = "file.bam";
    private static final String CONTROL_BAM = "control.bam";

    private final Context context = new Context();
    private final BamOutput controlBamOutput = BamOutput.builder()
            .bam(BAM)
            .controlBam(CONTROL_BAM)
            .build();

    private final TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();

    @BeforeEach
    void setup() {
        context.setVariable("output", TEST_DIRECTORY);

        final CommonOutdir commonOutdir = new CommonOutdir(TEST_DIRECTORY);
        commonOutdir.createDirectory();
    }

    @Test
    void shouldGeneratePairedExomecnv() {
        final String expectedCmd = expectedTemplateEngine.process(EXOMECNV_TEMPLATE, context);
        final String format = "%s/exomecnv/%s.sample_interval_summary";
        final String expectedTumorOutput = String.format(format, TEST_DIRECTORY, SAMPLE_NAME);
        final String expectedNormalOutput = String.format(format, TEST_DIRECTORY, CONTROL_SAMPLE_NAME);

        final Configuration configuration = initConfiguration();
        final Exomecnv exomecnv = new Exomecnv(SAMPLE_NAME, CONTROL_SAMPLE_NAME, controlBamOutput, TEST_DIRECTORY);
        final ExomecnvResult result = exomecnv.generate(configuration, expectedTemplateEngine);

        final AbstractCommand command = result.getCommand();
        assertEquals(expectedCmd, command.getToolCommand());
        assertEquals(expectedNormalOutput, result.getOutput().getControlReadDepthSummary());
        assertEquals(expectedTumorOutput, result.getOutput().getReadDepthSummary());
    }

    @Test
    void shouldFailIfBedIsNotSpecified() {
        final Configuration configuration = initConfiguration();
        configuration.getGlobalConfig().getDatabaseConfig().setBed(null);
        final Exomecnv exomecnv = new Exomecnv(SAMPLE_NAME, CONTROL_SAMPLE_NAME, controlBamOutput, TEST_DIRECTORY);
        assertThrows(NullPointerException.class, () -> exomecnv.generate(configuration, expectedTemplateEngine));
    }

    @Test
    void shouldFailIfGenomeIsNotSpecified() {
        final Configuration configuration = initConfiguration();
        configuration.getGlobalConfig().getDatabaseConfig().setGenome(null);
        final Exomecnv exomecnv = new Exomecnv(SAMPLE_NAME, CONTROL_SAMPLE_NAME, controlBamOutput, TEST_DIRECTORY);
        assertThrows(NullPointerException.class, () -> exomecnv.generate(configuration, expectedTemplateEngine));
    }

    @Test
    void shouldFailIfExomecnvIsNotSpecified() {
        final Configuration configuration = initConfiguration();
        configuration.getGlobalConfig().getToolConfig().setExomecnv(null);
        final Exomecnv exomecnv = new Exomecnv(SAMPLE_NAME, CONTROL_SAMPLE_NAME, controlBamOutput, TEST_DIRECTORY);
        assertThrows(NullPointerException.class, () -> exomecnv.generate(configuration, expectedTemplateEngine));
    }

    @Test
    void shouldFailIfJavaIsNotSpecified() {
        final Configuration configuration = initConfiguration();
        configuration.getGlobalConfig().getToolConfig().setJava(null);
        final Exomecnv exomecnv = new Exomecnv(SAMPLE_NAME, CONTROL_SAMPLE_NAME, controlBamOutput, TEST_DIRECTORY);
        assertThrows(NullPointerException.class, () -> exomecnv.generate(configuration, expectedTemplateEngine));
    }

    @Test
    void shouldFailIfGatkIsNotSpecified() {
        final Configuration configuration = initConfiguration();
        configuration.getGlobalConfig().getToolConfig().setGatk(null);
        final Exomecnv exomecnv = new Exomecnv(SAMPLE_NAME, CONTROL_SAMPLE_NAME, controlBamOutput, TEST_DIRECTORY);
        assertThrows(NullPointerException.class, () -> exomecnv.generate(configuration, expectedTemplateEngine));
    }

    @Test
    void shouldFailIfRscriptIsNotSpecified() {
        final Configuration configuration = initConfiguration();
        configuration.getGlobalConfig().getToolConfig().setRScript(null);
        final Exomecnv exomecnv = new Exomecnv(SAMPLE_NAME, CONTROL_SAMPLE_NAME, controlBamOutput, TEST_DIRECTORY);
        assertThrows(NullPointerException.class, () -> exomecnv.generate(configuration, expectedTemplateEngine));
    }

    @Test
    void shouldFailIfControlSampleIsNotSpecified() {
        final Configuration configuration = initConfiguration();
        final Vardict vardict = new Vardict(SAMPLE_NAME, null, controlBamOutput, TEST_DIRECTORY, true);
        assertThrows(NullPointerException.class, () -> vardict.generate(configuration, expectedTemplateEngine));
    }

    @Test
    void shouldFailIfControlBamNotSpecified() {
        final Configuration configuration = initConfiguration();
        final Vardict vardict = new Vardict(SAMPLE_NAME, CONTROL_SAMPLE_NAME,
                BamOutput.builder().bam(BAM).build(), TEST_DIRECTORY, true);
        assertThrows(NullPointerException.class, () -> vardict.generate(configuration, expectedTemplateEngine));
    }

    private Configuration initConfiguration() {
        final Configuration configuration = new Configuration();
        final GlobalConfig globalConfig = new GlobalConfig();
        final GlobalConfig.ToolConfig toolConfig = new GlobalConfig.ToolConfig();
        toolConfig.setExomecnv("exomecnv");
        toolConfig.setGatk("gatk");
        toolConfig.setRScript("Rscript");
        toolConfig.setJava("java");
        globalConfig.setToolConfig(toolConfig);
        final GlobalConfig.DatabaseConfig databaseConfig = new GlobalConfig.DatabaseConfig();
        databaseConfig.setGenome("GENOME");
        databaseConfig.setBed("BED");
        globalConfig.setDatabaseConfig(databaseConfig);
        configuration.setGlobalConfig(globalConfig);
        return configuration;
    }
}
