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

import com.epam.fonda.entity.command.AbstractCommand;
import com.epam.fonda.entity.configuration.CommonOutdir;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.tools.results.PileupOutput;
import com.epam.fonda.tools.results.SequenzaOutput;
import com.epam.fonda.tools.results.SequenzaResult;
import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SequenzaTest extends AbstractTest {
    private static final String SAMPLE_NAME = "sample_name";
    private static final String PILEUP_OUTPUT = "pileup.gz";
    private static final String CONTROL_PILEUP_OUTPUT = "control_pileup.gz";
    private static final String SEQUENZA_TEMPLATE_TEST = "sequenza_template_test";

    private final Context context = new Context();

    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private PileupOutput pileupOutput = PileupOutput.builder()
            .pileup(PILEUP_OUTPUT)
            .controlPileup(CONTROL_PILEUP_OUTPUT)
            .build();

    @BeforeEach
    void setup() {
        context.setVariable("output", TEST_DIRECTORY);

        final CommonOutdir commonOutdir = new CommonOutdir(TEST_DIRECTORY);
        commonOutdir.createDirectory();
    }

    @Test
    void shouldGeneratePairedSequenza() {
        final String expectedSeqz = String.format("%s/sequenza/%s.seqz.gz", TEST_DIRECTORY, SAMPLE_NAME);
        final String expectedSeqzReduce = String.format("%s/sequenza/%s_small.seqz.gz", TEST_DIRECTORY, SAMPLE_NAME);
        final String expectedSeg = String.format("%s/sequenza/%s_sequenza_segment.txt", TEST_DIRECTORY, SAMPLE_NAME);
        final String expectedInfor = String.format("%s/sequenza/%s_tumor_infor.txt", TEST_DIRECTORY, SAMPLE_NAME);
        final String expectedCmd = expectedTemplateEngine.process(SEQUENZA_TEMPLATE_TEST, context);

        final Configuration configuration = initConfiguration();
        final Sequenza sequenza = new Sequenza(SAMPLE_NAME, TEST_DIRECTORY, pileupOutput);
        final SequenzaResult result = sequenza.generate(configuration, expectedTemplateEngine);

        final AbstractCommand actualCommand = result.getCommand();
        assertEquals(expectedCmd, actualCommand.getToolCommand());
        assertEquals(Collections.singleton(expectedSeqz), actualCommand.getTempDirs());
        final SequenzaOutput sequenzaOutput = result.getSequenzaOutput();
        assertEquals(expectedSeqz, sequenzaOutput.getSequenzaSeqzOutput());
        assertEquals(expectedSeqzReduce, sequenzaOutput.getSequenzaSeqzReduceOutput());
        assertEquals(expectedSeg, sequenzaOutput.getSequenzaSegOutput());
        assertEquals(expectedInfor, sequenzaOutput.getSequenzaInforOutput());
    }

    @Test
    void shouldFailIfPythonIsNotSpecified() {
        final Configuration configuration = initConfiguration();
        configuration.getGlobalConfig().getToolConfig().setPython(null);
        final Sequenza sequenza = new Sequenza(SAMPLE_NAME, TEST_DIRECTORY, pileupOutput);
        assertThrows(NullPointerException.class, () -> sequenza.generate(configuration, expectedTemplateEngine));
    }

    @Test
    void shouldFailIfSequenzaIsNotSpecified() {
        final Configuration configuration = initConfiguration();
        configuration.getGlobalConfig().getToolConfig().setSequenza(null);
        final Sequenza sequenza = new Sequenza(SAMPLE_NAME, TEST_DIRECTORY, pileupOutput);
        assertThrows(NullPointerException.class, () -> sequenza.generate(configuration, expectedTemplateEngine));
    }

    @Test
    void shouldFailIfRScriptIsNotSpecified() {
        final Configuration configuration = initConfiguration();
        configuration.getGlobalConfig().getToolConfig().setRScript(null);
        final Sequenza sequenza = new Sequenza(SAMPLE_NAME, TEST_DIRECTORY, pileupOutput);
        assertThrows(NullPointerException.class, () -> sequenza.generate(configuration, expectedTemplateEngine));
    }

    @Test
    void shouldFailIfGcIsNotSpecified() {
        final Configuration configuration = initConfiguration();
        configuration.getGlobalConfig().getDatabaseConfig().setSequenzaGc50(null);
        final Sequenza sequenza = new Sequenza(SAMPLE_NAME, TEST_DIRECTORY, pileupOutput);
        assertThrows(NullPointerException.class, () -> sequenza.generate(configuration, expectedTemplateEngine));
    }

    @Test
    void shouldFailIfPileupIsNotSpecified() {
        final Configuration configuration = initConfiguration();
        final Sequenza sequenza = new Sequenza(SAMPLE_NAME, TEST_DIRECTORY, PileupOutput.builder().build());
        assertThrows(NullPointerException.class, () -> sequenza.generate(configuration, expectedTemplateEngine));
    }

    @Test
    void shouldFailIfControlPileupIsNotSpecified() {
        final Configuration configuration = initConfiguration();
        final Sequenza sequenza = new Sequenza(SAMPLE_NAME, TEST_DIRECTORY, PileupOutput.builder()
                .pileup(PILEUP_OUTPUT)
                .build());
        assertThrows(NullPointerException.class, () -> sequenza.generate(configuration, expectedTemplateEngine));
    }

    private Configuration initConfiguration() {
        final Configuration configuration = new Configuration();
        final GlobalConfig globalConfig = new GlobalConfig();
        final GlobalConfig.ToolConfig toolConfig = new GlobalConfig.ToolConfig();
        toolConfig.setSequenza("sequenza");
        toolConfig.setPython("python");
        toolConfig.setRScript("Rscript");
        globalConfig.setToolConfig(toolConfig);
        final GlobalConfig.DatabaseConfig databaseConfig = new GlobalConfig.DatabaseConfig();
        databaseConfig.setSequenzaGc50("gc.file");
        globalConfig.setDatabaseConfig(databaseConfig);
        configuration.setGlobalConfig(globalConfig);
        configuration.setTestMode(true);
        return configuration;
    }
}
