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
import java.util.Collections;
import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RnaMutationAnalysisTest extends AbstractTest {
    private static final String SAMPLE_NAME = "sampleName";
    private static final String RNA_MUTATION_ANALYSIS_WORKFLOW = "rnaMutationAnalysis";
    private static final String FASTQ_FILE_LIST = "study_config/RnaExpression_RNASeq_SampleFastqPaths.txt";
    private Flag testFlag;
    private RnaMutationAnalysis rnaMutationAnalysis;
    private Configuration expectedConfiguration;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();

    @BeforeEach
    void init() {
        CommonOutdir commonOutdir = new CommonOutdir("output");
        commonOutdir.createDirectory();
        testFlag = Flag.builder()
                .gatkHaplotypeCaller(true)
                .build();
        rnaMutationAnalysis = new RnaMutationAnalysis(testFlag, Collections.singletonList(SAMPLE_NAME));
        expectedConfiguration = new Configuration();
        expectedConfiguration.setGlobalConfig(new GlobalConfig());
        expectedConfiguration.setStudyConfig(new StudyConfig());
        expectedConfiguration.getGlobalConfig().getToolConfig().setRScript("rScript");
        expectedConfiguration.setCommonOutdir(commonOutdir);
        expectedConfiguration.getGlobalConfig().getPipelineInfo().setWorkflow(RNA_MUTATION_ANALYSIS_WORKFLOW);
        expectedConfiguration.getGlobalConfig().getPipelineInfo().setReadType(FastqReadType.PAIRED.getType());
        expectedConfiguration.getStudyConfig().setFastqList(new File(getClass().getClassLoader().
                getResource(FASTQ_FILE_LIST).getFile()).getAbsolutePath());
        final LinkedHashSet<String> toolset = new LinkedHashSet<>();
        toolset.add("cufflinks");
        toolset.add("conversion");
        expectedConfiguration.getGlobalConfig().getPipelineInfo().setToolset(toolset);
        expectedConfiguration.setTestMode(true);
    }

    @Test
    void generate() {
        rnaMutationAnalysis.generate(expectedConfiguration, expectedTemplateEngine);
        assertTrue(new File(String.format("output/sh_files/%s_mergeMutation_for_cohort_analysis.sh",
                RNA_MUTATION_ANALYSIS_WORKFLOW)).exists());
    }

    @Test
    void generateVoidOutput() {
        testFlag.setGatkHaplotypeCaller(false);
        rnaMutationAnalysis = new RnaMutationAnalysis(testFlag, Collections.singletonList(SAMPLE_NAME));
        rnaMutationAnalysis.generate(expectedConfiguration, expectedTemplateEngine);
        assertFalse(new File(String.format("output/sh_files/%s_mergeMutation_for_cohort_analysis.sh",
                RNA_MUTATION_ANALYSIS_WORKFLOW)).exists());
    }
}
