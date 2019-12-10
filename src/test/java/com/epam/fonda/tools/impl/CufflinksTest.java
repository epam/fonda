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

import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.entity.configuration.StudyConfig;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CufflinksTest extends AbstractTest {
    private static final String SAMPLE_NAME = "smv1";
    private static final String CUFFLINKS_NAME = "cufflinks";
    private static final String HISAT_2 = "hisat2";
    private static final String CUFFLINKS_TEST_INPUT_DATA_PATH = "templates/cufflinks_tool_test_input_data.txt";
    private Configuration configuration;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();
    private Cufflinks cufflinks;
    private String expectedCommand;

    @BeforeEach
    void setConfiguration() throws IOException, URISyntaxException {
        configuration = createConfiguration();
        BamOutput bamOutput = BamOutput.builder()
                .bam("build/resources/integrationTest/output/smv1/bam/smv1.hisat2.sorted.rmdup.bam")
                .build();
        FastqFileSample sample = FastqFileSample.builder()
                .name(SAMPLE_NAME)
                .sampleOutputDir("output")
                .build();
        sample.createDirectory();
        expectedCommand = new String(getScript());
        cufflinks = new Cufflinks(sample.getName(), sample.getSampleOutputDir(), bamOutput);
    }

    @Test
    void testCufflinksHisat2() {
        configuration.getGlobalConfig().getPipelineInfo()
                .setToolset(new LinkedHashSet<>(Arrays.asList(HISAT_2, CUFFLINKS_NAME)));
        String actualScript = cufflinks.generate(configuration, expectedTemplateEngine).getCommand().getToolCommand();

        assertEquals(expectedCommand, actualScript);
    }

    private Configuration createConfiguration() {
        Configuration config = new Configuration();
        StudyConfig studyConfig = new StudyConfig();
        GlobalConfig globalConfig = new GlobalConfig();
        studyConfig.setCufflinksLibraryType("fr-unstranded");
        studyConfig.setDirOut("build/resources/integrationTest/output");
        studyConfig.setFastqList("build/resources/integrationTest/fastq_list.tsv");
        globalConfig.getQueueParameters().setNumThreads(4);
        globalConfig.getDatabaseConfig().setGenomeBuild("GRch38");
        globalConfig.getDatabaseConfig().setGenome("/common/reference_genome/GRCh38/Sequence/GRCh38.genome.fa");
        globalConfig.getToolConfig().setCufflinks("/opt/cufflinks/cufflinks-2.2.1.Linux_x86_64/cufflinks");
        globalConfig.getDatabaseConfig()
                .setAnnotgene("/common/reference_genome/GRCh38/Annotation/Gencode_v26/gencode.v26.annotation.gtf");
        config.setStudyConfig(studyConfig);
        config.setGlobalConfig(globalConfig);

        return config;
    }

    private byte[] getScript() throws URISyntaxException, IOException {
        Path pathToScript = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource(CUFFLINKS_TEST_INPUT_DATA_PATH)).toURI());
        return Files.readAllBytes(pathToScript);
    }
}
