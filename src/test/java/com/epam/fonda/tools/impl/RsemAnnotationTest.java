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
import com.epam.fonda.tools.results.RsemOutput;
import com.epam.fonda.tools.results.RsemResult;
import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static com.epam.fonda.utils.PipelineUtils.getExecutionPath;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RsemAnnotationTest extends AbstractTest {
    private static final String RSEM_EXPRESSION_TEST_INPUT_DATA_PATH = "rsem_annotation_test_input_data.txt";
    private RsemAnnotation rsemAnnotation;
    private Configuration expectedConfiguration;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private RsemOutput rsemOutput;

    @BeforeEach
    void init() {
        rsemOutput = RsemOutput.builder()
                .rsemGeneResult("rsemGeneResult.gene")
                .rsemIsoformResult("rsem.isoform.expression.results")
                .build();
        RsemResult rsemResult = RsemResult.builder()
                .rsemOutput(rsemOutput)
                .command(BashCommand.withTool(""))
                .build();
        rsemAnnotation = new RsemAnnotation(rsemResult);
        expectedConfiguration = new Configuration();
        expectedConfiguration.setGlobalConfig(new GlobalConfig());
        expectedConfiguration.getGlobalConfig().getToolConfig().setPython("python");
        expectedConfiguration.getGlobalConfig().getDatabaseConfig().setAnnotgenesaf("annotGeneSaf");
    }

    @Test
    void generateTest() {
        Context context = new Context();
        context.setVariable("jarPath", getExecutionPath());
        final String expectedCmd = expectedTemplateEngine.process(RSEM_EXPRESSION_TEST_INPUT_DATA_PATH, context);
        final String actualBashCommand = rsemAnnotation.generate(expectedConfiguration, expectedTemplateEngine)
                .getCommand().getToolCommand();

        assertEquals(expectedCmd, actualBashCommand);
    }
}
