/*
 * Copyright 2017-2020 Sanofi and EPAM Systems, Inc. (https://www.epam.com/)
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
import com.epam.fonda.entity.configuration.StudyConfig;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.samples.fastq.FastqReadType;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.MetricsOutput;
import com.epam.fonda.tools.results.MetricsResult;
import com.epam.fonda.utils.TemplateEngineUtils;
import com.epam.fonda.workflow.PipelineType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static com.epam.fonda.utils.PipelineUtils.getExecutionPath;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DnaPicardQcTest extends AbstractTest {
    private static final String DNA_PICARD_QC_TOOL_TEST_TEMPLATE_NAME =
            "dnaPicardQc_tool_test_output_data";
    private static final String RNA_PICARD_QC_TOOL_TEST_TEMPLATE_NAME =
            "RnaPicardQc_tool_test_output_data";
    private static final String DNA_AMPLICON_PICARD_QC_TOOL_TEST_TEMPLATE_NAME =
            "dnaAmpliconPicardQc_tool_test_output_data";
    private static final String DNA_CAPTURE_PICARD_QC_TOOL_TEST_TEMPLATE_NAME =
            "dnaCapturePicardQc_tool_test_output_data";
    private static final String JAR_PATH = "jarPath";
    private static final String READ_TYPE = "readType";
    private static final String BED_TOOLS = "bedtools";
    private Configuration expectedConfiguration;
    private StudyConfig expectedStudyConfig;
    private FastqFileSample expectedSample;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private String jarPath;
    private MetricsResult metricsResult;

    @BeforeEach
    void setup() {
        constructExpectedSample();
        expectedConfiguration = new Configuration();
        GlobalConfig.PipelineInfo expectedPipelineInfo = new GlobalConfig.PipelineInfo();
        GlobalConfig expectedGlobalConfig = new GlobalConfig();
        constructToolConfig(expectedGlobalConfig);
        constructDatabaseConfig(expectedGlobalConfig);
        expectedGlobalConfig.setPipelineInfo(expectedPipelineInfo);
        expectedConfiguration.setGlobalConfig(expectedGlobalConfig);
        expectedStudyConfig = new StudyConfig();
        constructStudyConfig(expectedStudyConfig);
        expectedConfiguration.setStudyConfig(expectedStudyConfig);
        jarPath = getExecutionPath();
        BamOutput bamOutput = BamOutput.builder()
                .mkdupBam("sbamOutdir/sampleName.toolName.sorted.mkdup.bam")
                .mkdupMetric("sbamOutdir/sampleName.toolName.sorted.mkdup.metrics")
                .bam("sbamOutdir/sampleName.toolName.sorted.file.bam")
                .build();
        metricsResult = MetricsResult.builder()
                .bamOutput(bamOutput)
                .metricsOutput(MetricsOutput.builder().build())
                .command(BashCommand.withTool(""))
                .build();
    }

    @Test
    void shouldGenerateForDnaPicardQcWithSingleReadType() {
        DnaPicardQc dnaPicardQc = new DnaPicardQc(expectedSample, metricsResult);
        GlobalConfig.PipelineInfo expectedPipelineInfo = new GlobalConfig.PipelineInfo();
        expectedPipelineInfo.setWorkflow(PipelineType.DNA_CAPTURE_VAR_FASTQ.getName());
        expectedConfiguration.getGlobalConfig().setPipelineInfo(expectedPipelineInfo);
        expectedPipelineInfo.setReadType(FastqReadType.SINGLE.getType());
        expectedStudyConfig.setLibraryType("exome");
        expectedConfiguration.setStudyConfig(expectedStudyConfig);
        Context context = new Context();
        context.setVariable(JAR_PATH, jarPath);
        context.setVariable(READ_TYPE, expectedPipelineInfo.getReadType());
        final String expectedCmd = expectedTemplateEngine
                .process(DNA_PICARD_QC_TOOL_TEST_TEMPLATE_NAME, context);
        final String actualCmd = dnaPicardQc.generate(expectedConfiguration, expectedTemplateEngine)
                .getCommand()
                .getToolCommand();
        assertEquals(expectedCmd, actualCmd);
    }

    @Test
    void shouldGenerateForDnaPicardQcWithPairedReadType() {
        DnaPicardQc dnaPicardQc = new DnaPicardQc(expectedSample, metricsResult);
        GlobalConfig.PipelineInfo expectedPipelineInfo = new GlobalConfig.PipelineInfo();
        expectedPipelineInfo.setWorkflow(PipelineType.DNA_CAPTURE_VAR_FASTQ.getName());
        expectedConfiguration.getGlobalConfig().setPipelineInfo(expectedPipelineInfo);
        expectedPipelineInfo.setReadType(FastqReadType.PAIRED.getType());
        expectedStudyConfig.setLibraryType("exome");
        expectedConfiguration.setStudyConfig(expectedStudyConfig);
        Context context = new Context();
        context.setVariable(JAR_PATH, jarPath);
        context.setVariable(READ_TYPE, expectedPipelineInfo.getReadType());
        final String expectedCmd = expectedTemplateEngine
                .process(DNA_PICARD_QC_TOOL_TEST_TEMPLATE_NAME, context);
        final String actualCmd = dnaPicardQc.generate(expectedConfiguration, expectedTemplateEngine)
                .getCommand()
                .getToolCommand();
        assertEquals(expectedCmd, actualCmd);
    }

    @Test
    void shouldGenerateForRnaPicardQcWithPairedReadType() {
        DnaPicardQc dnaPicardQc = new DnaPicardQc(expectedSample, metricsResult);
        GlobalConfig.PipelineInfo expectedPipelineInfo = new GlobalConfig.PipelineInfo();
        expectedPipelineInfo.setWorkflow(PipelineType.RNA_CAPTURE_VAR_FASTQ.getName());
        expectedConfiguration.getGlobalConfig().setPipelineInfo(expectedPipelineInfo);
        expectedPipelineInfo.setReadType(FastqReadType.PAIRED.getType());
        expectedStudyConfig.setLibraryType("exome");
        expectedConfiguration.setStudyConfig(expectedStudyConfig);
        Context context = new Context();
        context.setVariable(JAR_PATH, jarPath);
        context.setVariable(READ_TYPE, expectedPipelineInfo.getReadType());
        final String expectedCmd = expectedTemplateEngine
                .process(RNA_PICARD_QC_TOOL_TEST_TEMPLATE_NAME, context);
        final String actualCmd = dnaPicardQc.generate(expectedConfiguration, expectedTemplateEngine)
                .getCommand()
                .getToolCommand();
        assertEquals(expectedCmd, actualCmd);
    }

    @Test
    void shouldGenerateWithSingleReadTypeForDnaAmpliconPicardQc() {
        DnaPicardQc dnaPicardQc = new DnaPicardQc(expectedSample, metricsResult);
        GlobalConfig.PipelineInfo expectedPipelineInfo = new GlobalConfig.PipelineInfo();
        expectedPipelineInfo.setWorkflow(PipelineType.DNA_AMPLICON_VAR_FASTQ.getName());
        expectedConfiguration.getGlobalConfig().setPipelineInfo(expectedPipelineInfo);
        expectedPipelineInfo.setReadType(FastqReadType.SINGLE.getType());
        GlobalConfig.ToolConfig expectedToolConfig = expectedConfiguration.getGlobalConfig().getToolConfig();
        expectedToolConfig.setBedTools(BED_TOOLS);
        expectedConfiguration.getGlobalConfig().setToolConfig(expectedToolConfig);
        StudyConfig studyConfig = new StudyConfig();
        studyConfig.setLibraryType("DNAWholeExomeSeq_Paired");
        studyConfig.setDate("20200121");
        studyConfig.setProject("Example_project");
        studyConfig.setRun("run1");
        expectedConfiguration.setStudyConfig(studyConfig);
        Context context = new Context();
        context.setVariable(JAR_PATH, jarPath);
        context.setVariable(READ_TYPE, expectedPipelineInfo.getReadType());
        context.setVariable(BED_TOOLS, expectedToolConfig.getBedTools());
        final String expectedCmd = expectedTemplateEngine
                .process(DNA_AMPLICON_PICARD_QC_TOOL_TEST_TEMPLATE_NAME, context);
        final String actualCmd = dnaPicardQc.generate(expectedConfiguration, expectedTemplateEngine)
                .getCommand()
                .getToolCommand();
        assertEquals(expectedCmd, actualCmd);
    }

    @Test
    void shouldGenerateWithPairedReadTypeForDnaAmpliconPicardQc() {
        DnaPicardQc dnaPicardQc = new DnaPicardQc(expectedSample, metricsResult);
        GlobalConfig.PipelineInfo expectedPipelineInfo = new GlobalConfig.PipelineInfo();
        expectedPipelineInfo.setWorkflow(PipelineType.DNA_AMPLICON_VAR_FASTQ.getName());
        expectedConfiguration.getGlobalConfig().setPipelineInfo(expectedPipelineInfo);
        expectedPipelineInfo.setReadType(FastqReadType.PAIRED.getType());
        GlobalConfig.ToolConfig expectedToolConfig = expectedConfiguration.getGlobalConfig().getToolConfig();
        expectedToolConfig.setBedTools(BED_TOOLS);
        expectedConfiguration.getGlobalConfig().setToolConfig(expectedToolConfig);
        StudyConfig studyConfig = new StudyConfig();
        studyConfig.setLibraryType("DNAWholeExomeSeq_Paired");
        studyConfig.setDate("20200121");
        studyConfig.setProject("Example_project");
        studyConfig.setRun("run1");
        expectedConfiguration.setStudyConfig(studyConfig);
        Context context = new Context();
        context.setVariable(JAR_PATH, jarPath);
        context.setVariable(READ_TYPE, expectedPipelineInfo.getReadType());
        context.setVariable(BED_TOOLS, expectedToolConfig.getBedTools());
        final String expectedCmd = expectedTemplateEngine
                .process(DNA_AMPLICON_PICARD_QC_TOOL_TEST_TEMPLATE_NAME, context);
        final String actualCmd = dnaPicardQc.generate(expectedConfiguration, expectedTemplateEngine)
                .getCommand()
                .getToolCommand();
        assertEquals(expectedCmd, actualCmd);
    }

    @Test
    void shouldGenerateWithSingleReadTypeForDnaCapturePicardQc() {
        DnaPicardQc dnaPicardQc = new DnaPicardQc(expectedSample, metricsResult);
        GlobalConfig.PipelineInfo expectedPipelineInfo = new GlobalConfig.PipelineInfo();
        expectedPipelineInfo.setWorkflow(PipelineType.DNA_CAPTURE_VAR_FASTQ.getName());
        expectedConfiguration.getGlobalConfig().setPipelineInfo(expectedPipelineInfo);
        expectedPipelineInfo.setReadType(FastqReadType.SINGLE.getType());
        GlobalConfig.ToolConfig expectedToolConfig = expectedConfiguration.getGlobalConfig().getToolConfig();
        expectedToolConfig.setBedTools(BED_TOOLS);
        expectedConfiguration.getGlobalConfig().setToolConfig(expectedToolConfig);
        expectedStudyConfig.setLibraryType("target");
        expectedConfiguration.setStudyConfig(expectedStudyConfig);
        Context context = new Context();
        context.setVariable(JAR_PATH, jarPath);
        context.setVariable(READ_TYPE, expectedPipelineInfo.getReadType());
        context.setVariable(BED_TOOLS, expectedToolConfig.getBedTools());
        final String expectedCmd = expectedTemplateEngine
                .process(DNA_CAPTURE_PICARD_QC_TOOL_TEST_TEMPLATE_NAME, context);
        final String actualCmd = dnaPicardQc.generate(expectedConfiguration, expectedTemplateEngine)
                .getCommand()
                .getToolCommand();
        assertEquals(expectedCmd, actualCmd);
    }

    @Test
    void shouldGenerateWithPairedReadTypeForDnaCapturePicardQc() {
        DnaPicardQc dnaPicardQc = new DnaPicardQc(expectedSample, metricsResult);
        GlobalConfig.PipelineInfo expectedPipelineInfo = new GlobalConfig.PipelineInfo();
        expectedPipelineInfo.setWorkflow(PipelineType.DNA_CAPTURE_VAR_FASTQ.getName());
        expectedConfiguration.getGlobalConfig().setPipelineInfo(expectedPipelineInfo);
        expectedPipelineInfo.setReadType(FastqReadType.PAIRED.getType());
        GlobalConfig.ToolConfig expectedToolConfig = expectedConfiguration.getGlobalConfig().getToolConfig();
        expectedToolConfig.setBedTools(BED_TOOLS);
        expectedStudyConfig.setLibraryType("target");
        expectedConfiguration.setStudyConfig(expectedStudyConfig);
        expectedConfiguration.getGlobalConfig().setToolConfig(expectedToolConfig);
        Context context = new Context();
        context.setVariable(JAR_PATH, jarPath);
        context.setVariable(READ_TYPE, expectedPipelineInfo.getReadType());
        context.setVariable(BED_TOOLS, expectedToolConfig.getBedTools());
        final String expectedCmd = expectedTemplateEngine
                .process(DNA_CAPTURE_PICARD_QC_TOOL_TEST_TEMPLATE_NAME, context);
        final String actualCmd = dnaPicardQc.generate(expectedConfiguration, expectedTemplateEngine)
                .getCommand()
                .getToolCommand();
        assertEquals(expectedCmd, actualCmd);
    }

    private void constructExpectedSample() {
        expectedSample = new FastqFileSample();
        expectedSample.setName("sampleName");
        expectedSample.setBamOutdir("sbamOutdir");
        expectedSample.setTmpOutdir("stmpOutdir");
        expectedSample.setQcOutdir("sqcOutdir");
    }

    private void constructToolConfig(GlobalConfig expectedGlobalConfig) {
        GlobalConfig.ToolConfig expectedToolConfig = new GlobalConfig.ToolConfig();
        expectedToolConfig.setJava("java");
        expectedToolConfig.setPicard("picard");
        expectedToolConfig.setSamTools("samtools");
        expectedToolConfig.setPython("python");
        expectedGlobalConfig.setToolConfig(expectedToolConfig);
    }

    private void constructDatabaseConfig(GlobalConfig expectedGlobalConfig) {
        GlobalConfig.DatabaseConfig expectedDatabaseConfig = new GlobalConfig.DatabaseConfig();
        expectedDatabaseConfig.setBed("bed");
        expectedDatabaseConfig.setBedForCoverage("bedForCoverage");
        expectedDatabaseConfig.setBedWithHeader("bedWithHeader");
        expectedDatabaseConfig.setGenome("genome");
        expectedGlobalConfig.setDatabaseConfig(expectedDatabaseConfig);
    }

    private void constructStudyConfig(StudyConfig expectedStudyConfig) {
        expectedStudyConfig.setDate("date");
        expectedStudyConfig.setProject("project");
        expectedStudyConfig.setRun("run");
    }
}
