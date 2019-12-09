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
import com.epam.fonda.samples.fastq.FastqReadType;
import com.epam.fonda.utils.TemplateEngineUtils;
import com.epam.fonda.workflow.impl.Flag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RnaAnalysisTest extends AbstractTest {
    private static final String SAMPLE_NAME = "sampleName";
    private static final String FASTQ_FILE_LIST = "study_config/RnaExpression_RNASeq_SampleFastqPaths.txt";
    private static final String BAM_FILE_LIST = "study_config/DnaCaptureVar_WES_SampleBamPaths.txt";
    private RnaAnalysis rnaAnalysis;
    private Configuration expectedConfiguration;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();

    @BeforeEach
    void init() {
        CommonOutdir commonOutdir = new CommonOutdir("output");
        commonOutdir.createDirectory();
        Flag testFlag = Flag.builder()
                .conversion(true)
                .rsem(true)
                .cufflinks(true)
                .stringtie(true)
                .build();
        rnaAnalysis = new RnaAnalysis(testFlag, Collections.singletonList(SAMPLE_NAME));
        expectedConfiguration = new Configuration();
        expectedConfiguration.setGlobalConfig(new GlobalConfig());
        expectedConfiguration.setStudyConfig(new StudyConfig());
        expectedConfiguration.getGlobalConfig().getToolConfig().setRScript("rScript");
        expectedConfiguration.setCustTask("rnaAnalysis");
        final LinkedHashSet<String> toolset = new LinkedHashSet<>(Arrays.asList("cufflinks", "conversion"));
        expectedConfiguration.getGlobalConfig().getPipelineInfo().setToolset(toolset);
        expectedConfiguration.setCommonOutdir(commonOutdir);
        expectedConfiguration.setTestMode(true);
    }

    @Test
    void testGenerateWithNoFiles() {
        assertThrows(IllegalArgumentException.class, () ->
                rnaAnalysis.generate(expectedConfiguration, expectedTemplateEngine));
    }

    @Test
    void testGenerateFastq() {
        expectedConfiguration.getGlobalConfig().getPipelineInfo().setWorkflow("rnaExpressionFastq");
        expectedConfiguration.getGlobalConfig().getPipelineInfo().setReadType(FastqReadType.PAIRED.getType());
        expectedConfiguration.getStudyConfig().setFastqList(new File(Objects.requireNonNull(getClass().getClassLoader().
                getResource(FASTQ_FILE_LIST)).getFile()).getAbsolutePath());

        assertDoesNotThrow(() -> rnaAnalysis.generate(expectedConfiguration, expectedTemplateEngine));
    }

    @Test
    void testGenerateBam() {
        expectedConfiguration.getGlobalConfig().getPipelineInfo().setWorkflow("rnaExpressionBam");
        expectedConfiguration.getStudyConfig().setBamList(new File(Objects.requireNonNull(getClass().getClassLoader().
                getResource(BAM_FILE_LIST)).getFile()).getAbsolutePath());

        assertDoesNotThrow(() -> rnaAnalysis.generate(expectedConfiguration, expectedTemplateEngine));
    }

    @Test
    void testGenerateBamWrongPath() {
        expectedConfiguration.getGlobalConfig().getPipelineInfo().setWorkflow("rnaExpressionBam");

        assertThrows(IllegalArgumentException.class, () ->
                rnaAnalysis.generate(expectedConfiguration, expectedTemplateEngine));
    }
}
