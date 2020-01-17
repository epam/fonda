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
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.VariantsVcfResult;
import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Mutect2Test extends AbstractTest {

    private static final String MUTECT2_TEMPLATE_TEST = "mutect2_template_test";
    private static final String SAMPLE_NAME = "sample_name";
    private static final String BAM = "file.bam";
    private static final String CONTROL_BAM = "control.bam";
    private final Context context = new Context();
    private final BamOutput bamOutput = BamOutput.builder()
            .bam(BAM)
            .controlBam(CONTROL_BAM)
            .build();
    private final TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private final Mutect2 mutect2 = new Mutect2(SAMPLE_NAME, bamOutput, TEST_DIRECTORY);

    @BeforeEach
    void setup() {
        context.setVariable("output", TEST_DIRECTORY);

        final CommonOutdir commonOutdir = new CommonOutdir(TEST_DIRECTORY);
        commonOutdir.createDirectory();
    }

    @Test
    void shouldGeneratePairedMutect2() {
        final String expectedCmd = expectedTemplateEngine.process(MUTECT2_TEMPLATE_TEST, context);
        final String expectedOutput = String.format("%s/mutect2", TEST_DIRECTORY);
        final String expectedTmpOutput = String.format("%s/mutect2/tmp", TEST_DIRECTORY);
        final String expectedVcf = String.format("%s/mutect2/%s.mutect2.somatic.variants.vcf",
                TEST_DIRECTORY, SAMPLE_NAME);

        final Configuration configuration = initConfiguration();
        final VariantsVcfResult result = mutect2.generate(configuration, expectedTemplateEngine);

        final AbstractCommand command = result.getAbstractCommand();
        assertEquals(expectedCmd, command.getToolCommand());
        assertEquals(Collections.singletonList(expectedTmpOutput), command.getTempDirs());
        assertEquals(result.getVariantsVcfOutput().getVariantsOutputDir(), expectedOutput);
        assertEquals(result.getVariantsVcfOutput().getVariantsTmpOutputDir(), expectedTmpOutput);
        assertEquals(result.getVariantsVcfOutput().getVariantsVcf(), expectedVcf);
    }

    @Test
    void shouldFailIfBedNotSpecified() {
        final Configuration config = initConfiguration();
        config.getGlobalConfig().getDatabaseConfig().setBed(null);
        assertThrows(NullPointerException.class, () -> mutect2.generate(config, expectedTemplateEngine));
    }

    @Test
    void shouldFailIfGenomeNotSpecified() {
        final Configuration config = initConfiguration();
        config.getGlobalConfig().getDatabaseConfig().setGenome(null);
        assertThrows(NullPointerException.class, () -> mutect2.generate(config, expectedTemplateEngine));
    }

    @Test
    void shouldFailIfGatkToolNotSpecified() {
        final Configuration config = initConfiguration();
        config.getGlobalConfig().getToolConfig().setGatk(null);
        assertThrows(NullPointerException.class, () -> mutect2.generate(config, expectedTemplateEngine));
    }

    @Test
    void shouldFailIfMutectJavaToolNotSpecified() {
        final Configuration config = initConfiguration();
        config.getGlobalConfig().getToolConfig().setJava(null);
        assertThrows(NullPointerException.class, () -> mutect2.generate(config, expectedTemplateEngine));
    }

    @Test
    void shouldFailIfBamNotSpecified() {
        final Mutect2 mutect = new Mutect2(SAMPLE_NAME, BamOutput.builder().build(), TEST_DIRECTORY);
        final Configuration config = initConfiguration();
        assertThrows(NullPointerException.class, () -> mutect.generate(config, expectedTemplateEngine));
    }

    @Test
    void shouldFailIfControlBamIsNotSpecified() {
        final Mutect2 mutect = new Mutect2(SAMPLE_NAME, BamOutput.builder().bam(BAM).build(), TEST_DIRECTORY);
        final Configuration config = initConfiguration();
        assertThrows(NullPointerException.class, () -> mutect.generate(config, expectedTemplateEngine));
    }

    private Configuration initConfiguration() {
        final Configuration configuration = new Configuration();
        final GlobalConfig globalConfig = new GlobalConfig();
        final GlobalConfig.ToolConfig toolConfig = new GlobalConfig.ToolConfig();
        toolConfig.setGatk("gatk");
        toolConfig.setJava("java");
        globalConfig.setToolConfig(toolConfig);
        final GlobalConfig.DatabaseConfig databaseConfig = new GlobalConfig.DatabaseConfig();
        databaseConfig.setGenome("GENOME");
        databaseConfig.setBed("BED");
        globalConfig.setDatabaseConfig(databaseConfig);
        final GlobalConfig.PipelineInfo pipelineInfo = new GlobalConfig.PipelineInfo();
        pipelineInfo.setWorkflow("DnaCaptureVar_Bam");
        globalConfig.setPipelineInfo(pipelineInfo);
        configuration.setGlobalConfig(globalConfig);
        return configuration;
    }
}
