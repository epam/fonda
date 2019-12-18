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
import com.epam.fonda.entity.configuration.StudyConfig;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.tools.results.FastqOutput;
import com.epam.fonda.tools.results.MetricsResult;
import com.epam.fonda.utils.TemplateEngineUtils;
import com.epam.fonda.workflow.impl.Flag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;

import static com.epam.fonda.entity.configuration.GlobalConfigFormat.STARINDEX;
import static com.epam.fonda.utils.PipelineUtils.getExecutionPath;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StarTest extends AbstractTest {
    private static final String STAR_TOOL_WITH_RMDUP_TEST_OUTPUT_DATA_PATH =
            "templates/star_tool_with_rmdup_test_output_data.txt";
    private static final String STAR_TOOL_WITH_QC_TEST_OUTPUT_DATA_PATH =
             "star_tool_with_qc_test_output_data";
    private static final String SBAM_OUTDIR_SAMPLE_NAME_STAR_SORTED_BAM = "sbamOutdir/sampleName.star.sorted.bam";
    private Configuration expectedConfiguration;
    private GlobalConfig expectedGlobalConfig;
    private GlobalConfig.ToolConfig expectedToolConfig;
    private GlobalConfig.DatabaseConfig expectedDatabaseConfig;
    private GlobalConfig.PipelineInfo expectedPipelineInfo;
    private FastqFileSample expectedSample;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private String jarPath;
    private BamResult bamResult;
    private FastqOutput fastqOutput;

    @BeforeEach
    void setup() {
        expectedGlobalConfig = new GlobalConfig();
        StudyConfig expectedStudyConfig = new StudyConfig();
        expectedToolConfig = new GlobalConfig.ToolConfig();
        expectedPipelineInfo = new GlobalConfig.PipelineInfo();
        expectedPipelineInfo.setWorkflow("RnaExpression");
        expectedGlobalConfig.setPipelineInfo(expectedPipelineInfo);
        expectedToolConfig.setJava("java");
        expectedToolConfig.setPicard("picard");
        expectedToolConfig.setStar("star");
        expectedToolConfig.setSamTools("samtools");
        GlobalConfig.QueueParameters expectedQueueParameters = new GlobalConfig.QueueParameters();
        expectedQueueParameters.setNumThreads(5);
        expectedDatabaseConfig = new GlobalConfig.DatabaseConfig();
        expectedDatabaseConfig.setStarIndex(STARINDEX);
        expectedGlobalConfig.setQueueParameters(expectedQueueParameters);
        expectedGlobalConfig.setToolConfig(expectedToolConfig);
        expectedGlobalConfig.setDatabaseConfig(expectedDatabaseConfig);
        expectedStudyConfig.setProject("project");
        expectedStudyConfig.setRun("run");
        expectedStudyConfig.setDate("date");
        expectedConfiguration = new Configuration();
        expectedConfiguration.setGlobalConfig(expectedGlobalConfig);
        expectedConfiguration.setStudyConfig(expectedStudyConfig);
        expectedSample = new FastqFileSample();
        expectedSample.setName("sampleName");
        expectedSample.setBamOutdir("sbamOutdir");
        expectedSample.setTmpOutdir("stmpOutdir");
        expectedSample.setQcOutdir("sqcOutdir");
        jarPath = getExecutionPath();

        fastqOutput = FastqOutput.builder()
                .mergedFastq1("mergedFastq1")
                .mergedFastq2("mergedFastq2")
                .build();
        BamOutput bamOutput = BamOutput.builder()
                .mkdupBam(SBAM_OUTDIR_SAMPLE_NAME_STAR_SORTED_BAM)
                .mkdupMetric(SBAM_OUTDIR_SAMPLE_NAME_STAR_SORTED_BAM.replace("bam", "metrics"))
                .build();
        bamResult = BamResult.builder()
                .fastqOutput(fastqOutput)
                .bamOutput(bamOutput)
                .command(BashCommand.withTool(""))
                .build();
    }

    @Test
    void shouldGenerateScriptWithRmdupFlag() throws IOException, URISyntaxException {
        expectedPipelineInfo.setToolset(new LinkedHashSet<>(Collections.singletonList("rmdup")));
        expectedGlobalConfig.setPipelineInfo(expectedPipelineInfo);
        Flag flag = Flag.buildFlags(expectedConfiguration);
        Star star = new Star(flag, expectedSample, fastqOutput);
        Path path = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource(STAR_TOOL_WITH_RMDUP_TEST_OUTPUT_DATA_PATH)).toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        String expectedCmd = new String(fileBytes);
        bamResult = star.generate(expectedConfiguration, expectedTemplateEngine);
        bamResult = new PicardMarkDuplicate(expectedSample, bamResult)
                .generate(expectedConfiguration, expectedTemplateEngine);
        bamResult = new PicardRemoveDuplicate(bamResult).generate(expectedConfiguration, expectedTemplateEngine);
        assertEquals(expectedCmd, bamResult.getCommand().getToolCommand());
    }

    @Test
    void shouldGenerateScriptWithQcFlag() {
        expectedPipelineInfo.setToolset(new LinkedHashSet<>(Collections.singletonList("qc")));
        expectedGlobalConfig.setPipelineInfo(expectedPipelineInfo);
        Flag flag = Flag.buildFlags(expectedConfiguration);
        Star star = new Star(flag, expectedSample, fastqOutput);
        expectedPipelineInfo.setReadType("not single");
        expectedToolConfig.setRnaseqc("rnaSeqc");
        expectedToolConfig.setRnaseqcJava("rnaSeqcJava");
        expectedToolConfig.setPython("python");
        expectedDatabaseConfig.setGenome("genome");
        expectedDatabaseConfig.setAnnotgene("annotgene");
        expectedDatabaseConfig.setRRNABED("rRnaBed");
        expectedGlobalConfig.setToolConfig(expectedToolConfig);
        expectedGlobalConfig.setDatabaseConfig(expectedDatabaseConfig);
        Context context = new Context();
        context.setVariable("jarPath", jarPath);
        String expectedCmd = expectedTemplateEngine.process(STAR_TOOL_WITH_QC_TEST_OUTPUT_DATA_PATH, context);
        bamResult = star.generate(expectedConfiguration, expectedTemplateEngine);
        bamResult = new PicardMarkDuplicate(expectedSample, bamResult)
                .generate(expectedConfiguration, expectedTemplateEngine);
        MetricsResult metricsResult = new RNASeQC(expectedSample, bamResult.getBamOutput())
                .generate(expectedConfiguration, expectedTemplateEngine);
        assertEquals(expectedCmd, bamResult.getCommand().getToolCommand() +
                metricsResult.getCommand().getToolCommand());
    }
}
