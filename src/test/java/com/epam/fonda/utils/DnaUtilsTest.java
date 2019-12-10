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

package com.epam.fonda.utils;

import com.epam.fonda.entity.configuration.CommonOutdir;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.samples.bam.BamFileSample;
import com.epam.fonda.tools.impl.AbstractTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static com.epam.fonda.utils.DnaUtils.checkPeriodicBamStatus;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DnaUtilsTest extends AbstractTest {

    private static final String TEST_OUTPUT_DIRECTORY = "output";
    private static final String LOGFILE_SCANNING_SHELL_SCRIPT_TEST_TEMPLATE_NAME =
        "logfile_scanning_shell_script_output_data";
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private Configuration expectedConfiguration;
    private BamFileSample expectedBamFileSample;
    private String expectedLogFileWithSampleName;

    @BeforeEach
    void setup() {
        expectedConfiguration = new Configuration();
        GlobalConfig expectedGlobalConfig = new GlobalConfig();
        GlobalConfig.PipelineInfo expectedPipelineInfo = new GlobalConfig.PipelineInfo();
        expectedBamFileSample = new BamFileSample();
        expectedConfiguration.setCustTask("custTask");
        expectedPipelineInfo.setWorkflow("DnaAmpliconVarFastq");
        expectedGlobalConfig.setPipelineInfo(expectedPipelineInfo);
        expectedConfiguration.setGlobalConfig(expectedGlobalConfig);
        expectedBamFileSample.setName("sampleName");
        expectedBamFileSample.setBam("sampleName.bam");
        CommonOutdir commonOutdir = new CommonOutdir(TEST_OUTPUT_DIRECTORY);
        commonOutdir.createDirectory();
        expectedConfiguration.setCommonOutdir(commonOutdir);
        expectedLogFileWithSampleName = "output/log_files/DnaAmpliconVarFastq_custTask_for_sampleName_analysis.log";
    }

    @Test
    void shouldCheckPeriodicDoubleBamStatus() {
        Context expectedContext = new Context();
        buildCommonContext(expectedContext);
        expectedBamFileSample.setControlName("controlSampleName");
        expectedBamFileSample.setControlBam("controlSampleName.bam");
        expectedContext.setVariable("controlSampleName", expectedBamFileSample.getControlName());
        expectedContext.setVariable("logFileWithSampleName", expectedLogFileWithSampleName);
        String expectedCmd = expectedTemplateEngine.process(LOGFILE_SCANNING_SHELL_SCRIPT_TEST_TEMPLATE_NAME,
                expectedContext);
        String actualCmd = checkPeriodicBamStatus("tag", expectedBamFileSample.getName(), expectedBamFileSample
                        .getControlName(), expectedConfiguration, null);
        assertEquals(expectedCmd, actualCmd);
    }

    @Test
    void shouldCheckPeriodicSingleBamStatus() {
        Context expectedContext = new Context();
        buildCommonContext(expectedContext);
        expectedContext.setVariable("logFileWithSampleName", expectedLogFileWithSampleName);
        String expectedCmd = expectedTemplateEngine.process(LOGFILE_SCANNING_SHELL_SCRIPT_TEST_TEMPLATE_NAME,
                expectedContext);
        String actualCmd = checkPeriodicBamStatus("tag", expectedBamFileSample.getName(), expectedBamFileSample
                        .getControlName(), expectedConfiguration, null);
        assertEquals(expectedCmd, actualCmd);
    }

    @Test
    void shouldCheckPeriodicIndexBamStatus() {
        Context expectedContext = new Context();
        buildCommonContext(expectedContext);
        String expectedLogFileWithSampleNameIndex =
                "output/log_files/DnaAmpliconVarFastq_custTask_for_sampleName_index_analysis.log";
        expectedContext.setVariable("index", "index");
        expectedContext.setVariable("logFileWithSampleNameIndex", expectedLogFileWithSampleNameIndex);
        String expectedCmd = expectedTemplateEngine.process(LOGFILE_SCANNING_SHELL_SCRIPT_TEST_TEMPLATE_NAME,
                expectedContext);
        String actualCmd = checkPeriodicBamStatus("tag", expectedBamFileSample.getName(), expectedBamFileSample
                        .getControlName(), expectedConfiguration, "index");
        assertEquals(expectedCmd, actualCmd);
    }

    private void buildCommonContext(Context context) {
        String ifScript1 = "if [[ $str == \"*Error Step: ";
        String ifScript2 = "if [[ -f $logFile  ]];";
        String whileScript = "while [[ $str = \"\" ]]";
        context.setVariable("ifScript1", ifScript1);
        context.setVariable("ifScript2", ifScript2);
        context.setVariable("whileScript", whileScript);
    }
}
