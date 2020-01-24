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

package com.epam.fonda.samples;

import com.epam.fonda.entity.configuration.CommonOutdir;
import com.epam.fonda.entity.configuration.StudyConfig;
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.samples.bam.BamFileSample;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.impl.AbstractTest;
import com.epam.fonda.workflow.PipelineType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SampleBuilderTest extends AbstractTest {
    private static final String PAIRED = "paired";
    private static final String SINGLE = "single";
    private static final String BAM_FILE_LIST = "study_config/DnaCaptureVar_WES_SampleBamPaths.txt";
    private static final String BAM_FILE_LIST_NA = "study_config/DnaCaptureVar_WES_SampleBamPathsOnlyNA.txt";
    private static final String FASTQ_FILE_LIST = "study_config/RnaExpression_RNASeq_SampleFastqPaths.txt";
    private static final String FASTQ_FILE_LIST_SINGLE = "study_config/RnaExpression_RNASeq_SampleFastqPathsSingle.txt";
    private static final String FASTQ_FILE_LIST_INCORRECT =
            "study_config/RnaExpression_RNASeq_SampleFastqPathsIncorrect.txt";
    private static final String WRONG_PATH = "wrong_path.txt";
    private static final String ROOT_DIR = "output";
    private SampleBuilder builder;
    private StudyConfig studyConfig;
    private GlobalConfig globalConfig;

    @BeforeEach
    void setUp() {
        studyConfig = new StudyConfig();
        globalConfig = new GlobalConfig();
        CommonOutdir commonOutdir = new CommonOutdir("output");
        commonOutdir.createDirectory();
    }

    @Test
    void buildBamSamples() throws IOException {
        globalConfig.getPipelineInfo().setReadType(SINGLE);
        globalConfig.getPipelineInfo().setWorkflow(PipelineType.BAM_2_FASTQ.getName());
        studyConfig.setBamList(new File(getClass().getClassLoader().
                getResource(BAM_FILE_LIST).getFile()).getAbsolutePath());

        builder = new SampleBuilder(globalConfig, studyConfig);
        List<BamFileSample> sampleList = builder.buildBamSamples(ROOT_DIR);

        assertEquals(6, sampleList.size());
    }

    @Test
    void buildBamSamplesNA() throws IOException {
        globalConfig.getPipelineInfo().setReadType(SINGLE);
        globalConfig.getPipelineInfo().setWorkflow(PipelineType.BAM_2_FASTQ.getName());
        studyConfig.setBamList(new File(getClass().getClassLoader().
                getResource(BAM_FILE_LIST_NA).getFile()).getAbsolutePath());

        builder = new SampleBuilder(globalConfig, studyConfig);
        List<BamFileSample> sampleList = builder.buildBamSamples(ROOT_DIR);

        assertNull(sampleList.get(0).getControlBam());
    }

    @Test
    void buildFastqSamplesSingle() throws IOException {
        globalConfig.getPipelineInfo().setReadType(SINGLE);
        studyConfig.setFastqList(new File(getClass().getClassLoader().
                getResource(FASTQ_FILE_LIST_SINGLE).getFile()).getAbsolutePath());
        builder = new SampleBuilder(globalConfig, studyConfig);
        List<FastqFileSample> sampleList = builder.buildFastqSamples(ROOT_DIR);

        assertEquals(5, sampleList.size());
    }

    @Test
    void buildFastqSamplesPaired() throws IOException {
        globalConfig.getPipelineInfo().setReadType(PAIRED);
        studyConfig.setFastqList(new File(getClass().getClassLoader().
                getResource(FASTQ_FILE_LIST).getFile()).getAbsolutePath());
        builder = new SampleBuilder(globalConfig, studyConfig);
        List<FastqFileSample> sampleList = builder.buildFastqSamples(ROOT_DIR);

        assertEquals(5, sampleList.size());
    }

    @Test
    void buildFastqSamplesPairedMultipleMatchControl() {
        globalConfig.getPipelineInfo().setReadType(PAIRED);
        studyConfig.setFastqList(new File(getClass().getClassLoader().
                getResource(FASTQ_FILE_LIST_INCORRECT).getFile()).getAbsolutePath());
        builder = new SampleBuilder(globalConfig, studyConfig);

        assertThrows(IllegalArgumentException.class, () -> builder.buildFastqSamples(ROOT_DIR));
    }

    @Test
    void buildFastqSamplesUnknownReadType() {
        studyConfig.setFastqList(new File(getClass().getClassLoader().
                getResource(FASTQ_FILE_LIST).getFile()).getAbsolutePath());
        builder = new SampleBuilder(globalConfig, studyConfig);

        assertThrows(IllegalArgumentException.class, () -> builder.buildFastqSamples(ROOT_DIR));
    }

    @Test
    void buildFastqSamplesNullList() {
        globalConfig.getPipelineInfo().setReadType(SINGLE);
        builder = new SampleBuilder(globalConfig, studyConfig);

        assertThrows(IllegalArgumentException.class, () -> builder.buildFastqSamples(ROOT_DIR));
    }

    @Test
    void buildFastqSamplesWrongPath() {
        globalConfig.getPipelineInfo().setReadType(PAIRED);
        studyConfig.setFastqList(WRONG_PATH);
        builder = new SampleBuilder(globalConfig, studyConfig);

        assertThrows(NoSuchFileException.class, () -> builder.buildFastqSamples(ROOT_DIR));
    }

    @Test
    void buildBamSamplesWrongPath() {
        studyConfig.setBamList(WRONG_PATH);
        builder = new SampleBuilder(globalConfig, studyConfig);

        assertThrows(NoSuchFileException.class, () -> builder.buildBamSamples(ROOT_DIR));
    }

    @Test
    void buildFastqSamplesEmptyList() {
        globalConfig.getPipelineInfo().setReadType(SINGLE);
        studyConfig.setFastqList("");
        builder = new SampleBuilder(globalConfig, studyConfig);

        assertThrows(IllegalArgumentException.class, () -> builder.buildFastqSamples(ROOT_DIR));
    }
}
