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
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.tools.results.FastqOutput;
import com.epam.fonda.tools.results.MetricsResult;
import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static com.epam.fonda.entity.configuration.GlobalConfigFormat.HISAT2INDEX;
import static com.epam.fonda.utils.PipelineUtils.getExecutionPath;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Hisat2Test extends AbstractTest {
    private static final String HISAT2_TOOL_WITH_RMDUP_TEST_OUTPUT_DATA_PATH =
            "templates/hisat2_tool_with_rmdup_test_output_data.txt";
    private static final String HISAT2_TOOL_TEST_WITH_QC_TEMPLATE_NAME = "hisat2_tool_with_qc_test_output_data";
    private static final String SBAM_OUTDIR_SAMPLE_NAME_HISAT_2_SORTED_BAM = "sbamOutdir/sampleName.hisat2.sorted.bam";
    private Configuration expectedConfiguration;
    private GlobalConfig expectedGlobalConfig;
    private GlobalConfig.ToolConfig expectedToolConfig;
    private GlobalConfig.DatabaseConfig expectedDatabaseConfig;
    private GlobalConfig.PipelineInfo expectedPipelineInfo;
    private StudyConfig expectedStudyConfig;
    private FastqFileSample expectedSample;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private String jarPath;
    private BamResult bamResult;
    private FastqOutput fastqOutput;

    @BeforeEach
    void setup() {
        expectedConfiguration = new Configuration();
        expectedSample = new FastqFileSample();
        expectedSample.setName("sampleName");
        expectedSample.setBamOutdir("sbamOutdir");
        expectedSample.setTmpOutdir("stmpOutdir");
        expectedSample.setQcOutdir("sqcOutdir");
        expectedGlobalConfig = new GlobalConfig();
        expectedPipelineInfo = new GlobalConfig.PipelineInfo();
        expectedPipelineInfo.setWorkflow("RnaExpression");
        GlobalConfig.QueueParameters expectedQueueParameters = new GlobalConfig.QueueParameters();
        expectedToolConfig = new GlobalConfig.ToolConfig();
        expectedDatabaseConfig = new GlobalConfig.DatabaseConfig();
        expectedDatabaseConfig.setHisat2Index(HISAT2INDEX);
        expectedQueueParameters.setNumThreads(5);
        expectedToolConfig.setHisat2("hisat2");
        expectedToolConfig.setSamTools("samtools");
        expectedToolConfig.setJava("java");
        expectedToolConfig.setPicardVersion("v2.10.3");
        expectedToolConfig.setPicard("picard");
        expectedGlobalConfig.setToolConfig(expectedToolConfig);
        expectedGlobalConfig.setQueueParameters(expectedQueueParameters);
        expectedGlobalConfig.setDatabaseConfig(expectedDatabaseConfig);
        expectedGlobalConfig.setPipelineInfo(expectedPipelineInfo);
        expectedConfiguration.setGlobalConfig(expectedGlobalConfig);
        expectedStudyConfig = new StudyConfig();
        expectedConfiguration.setStudyConfig(expectedStudyConfig);
        jarPath = getExecutionPath(expectedConfiguration);
        fastqOutput = FastqOutput.builder()
                .mergedFastq1("mergedFastq1")
                .mergedFastq2("mergedFastq2")
                .build();
        bamResult = BamResult.builder()
                .fastqOutput(fastqOutput)
                .bamOutput(BamOutput.builder().mkdupBam(SBAM_OUTDIR_SAMPLE_NAME_HISAT_2_SORTED_BAM).build())
                .command(BashCommand.withTool(""))
                .build();
    }

    @Test
    void shouldGenerateScriptWithRmdupFlag() throws URISyntaxException, IOException {
        Hisat2 hisat2 = new Hisat2(expectedSample, fastqOutput);
        Path path = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource(HISAT2_TOOL_WITH_RMDUP_TEST_OUTPUT_DATA_PATH)).toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        String expectedCmd = new String(fileBytes);
        bamResult = hisat2.generate(expectedConfiguration, expectedTemplateEngine);
        bamResult = new PicardMarkDuplicate(expectedSample, bamResult)
                .generate(expectedConfiguration, expectedTemplateEngine);
        bamResult = new PicardRemoveDuplicate(bamResult)
                .generate(expectedConfiguration, expectedTemplateEngine);
        assertEquals(expectedCmd, bamResult.getCommand().getToolCommand());
    }

    @Test
    void shouldGenerateScriptWithQcFlag() {
        Hisat2 hisat2 = new Hisat2(expectedSample, fastqOutput);
        expectedPipelineInfo.setReadType("not single");
        expectedToolConfig.setRnaseqc("rnaSeqc");
        expectedToolConfig.setRnaseqcJava("rnaSeqcJava");
        expectedToolConfig.setPython("python");
        expectedDatabaseConfig.setGenome("genome");
        expectedDatabaseConfig.setAnnotgene("annotgene");
        expectedDatabaseConfig.setRRNABED("rRnaBed");
        expectedGlobalConfig.setToolConfig(expectedToolConfig);
        expectedGlobalConfig.setDatabaseConfig(expectedDatabaseConfig);
        expectedGlobalConfig.setPipelineInfo(expectedPipelineInfo);
        expectedStudyConfig.setDate("date");
        expectedStudyConfig.setProject("project");
        expectedStudyConfig.setRun("run");
        Context context = new Context();
        context.setVariable("jarPath", jarPath);
        String expectedCmd = expectedTemplateEngine.process(HISAT2_TOOL_TEST_WITH_QC_TEMPLATE_NAME, context);
        bamResult = hisat2.generate(expectedConfiguration, expectedTemplateEngine);
        bamResult = new PicardMarkDuplicate(expectedSample, bamResult)
                .generate(expectedConfiguration, expectedTemplateEngine);
        MetricsResult metricsResult = new RNASeQC(expectedSample, bamResult.getBamOutput())
                .generate(expectedConfiguration, expectedTemplateEngine);
        assertEquals(expectedCmd, bamResult.getCommand().getToolCommand() +
                metricsResult.getCommand().getToolCommand());
    }
}
