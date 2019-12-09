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
import com.epam.fonda.entity.configuration.StudyConfig;
import com.epam.fonda.utils.TemplateEngineUtils;
import com.epam.fonda.workflow.impl.Flag;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class QcSummaryTest extends AbstractTest {
    private static final String TEST_DIRECTORY = "output";
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();

    @Test
    void generate() throws IOException {
        Configuration configuration = new Configuration();
        GlobalConfig globalConfig = new GlobalConfig();
        GlobalConfig.PipelineInfo pipelineInfo = new GlobalConfig.PipelineInfo();
        pipelineInfo.setToolset(new LinkedHashSet<>());
        globalConfig.setPipelineInfo(pipelineInfo);
        StudyConfig studyConfig = new StudyConfig();
        configuration.setGlobalConfig(globalConfig);
        configuration.setStudyConfig(studyConfig);
        configuration.getGlobalConfig().getPipelineInfo().getToolset().add("qc");
        configuration.getGlobalConfig().getPipelineInfo().setWorkflow("RnaExpression_Fastq");
        configuration.getStudyConfig().setDirOut(TEST_DIRECTORY);
        configuration.getStudyConfig().setFastqList("fastq_list");
        configuration.getGlobalConfig().getToolConfig().setRScript("rScript");
        CommonOutdir commonOutdir = new CommonOutdir(studyConfig.getDirOut());
        commonOutdir.createDirectory();
        configuration.setCommonOutdir(commonOutdir);
        configuration.setTestMode(true);
        Flag testFlag = Flag.builder()
                .qc(true)
                .build();
        QcSummary qcSummary = new QcSummary(testFlag, Collections.singletonList("sample_name"));
        assertNotNull(expectedTemplateEngine);
        qcSummary.generate(configuration, expectedTemplateEngine);
    }
}
