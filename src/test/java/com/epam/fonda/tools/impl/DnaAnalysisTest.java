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
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.utils.TemplateEngineUtils;
import com.epam.fonda.workflow.impl.Flag;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.fonda.utils.PipelineUtils.getExecutionPath;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DnaAnalysisTest extends AbstractTest {
    private static final String DNA_ANALYSIS_TEST_TEMPLATE_NAME = "dna_analysis_test_output_data";
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();

    @Test
    void shouldGenerate() throws IOException {
        List<FastqFileSample> samples = Collections.singletonList(FastqFileSample.builder()
                .name("sample_name")
                .sampleType("tumor")
                .fastq1(Collections.singletonList("fastq1"))
                .fastq2(Collections.singletonList("fastq2"))
                .build());
        Configuration configuration = new Configuration();
        GlobalConfig globalConfig = new GlobalConfig();
        StudyConfig studyConfig = new StudyConfig();
        configuration.setGlobalConfig(globalConfig);
        configuration.setStudyConfig(studyConfig);
        configuration.getStudyConfig().setFastqList("fastq_list");
        configuration.getGlobalConfig().getToolConfig().setRScript("rScript");
        configuration.getGlobalConfig().getPipelineInfo().setToolset(
                new LinkedHashSet<>(Collections.singletonList("vardict")));
        configuration.getGlobalConfig().getPipelineInfo().setWorkflow("DnaAmpliconVar_Fastq");
        configuration.getGlobalConfig().getQueueParameters().setQueue("queue");
        configuration.getStudyConfig().setDirOut(TEST_DIRECTORY);
        CommonOutdir commonOutdir = new CommonOutdir(TEST_DIRECTORY);
        commonOutdir.createDirectory();
        configuration.setCommonOutdir(commonOutdir);
        DnaAnalysis dnaAnalysis = new DnaAnalysis(samples, null, Flag.buildFlags(configuration));
        dnaAnalysis.generate(configuration, expectedTemplateEngine);
        String jarPath = getExecutionPath();
        Context context = new Context();
        context.setVariable("jarPath", jarPath);
        context.setVariable("output", TEST_DIRECTORY);
        final String expectedData = expectedTemplateEngine.process(DNA_ANALYSIS_TEST_TEMPLATE_NAME, context);
        String fileName = String.format("%s_%s_for_cohort_analysis.sh", configuration.getGlobalConfig()
                .getPipelineInfo().getWorkflow(), configuration.getCustTask());
        Path path = Paths.get(commonOutdir.getShOutdir() + "/" + fileName);
        Stream<String> lines = Files.lines(path);
        String actualData = lines.collect(Collectors.joining(System.lineSeparator()));
        lines.close();
        assertEquals(expectedData, actualData);
    }
}
