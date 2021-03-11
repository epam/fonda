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

import com.epam.fonda.entity.command.BashCommand;
import com.epam.fonda.entity.configuration.CommonOutdir;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.entity.configuration.StudyConfig;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.samples.fastq.LibraryCsv;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.utils.CellRangerUtils;
import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import static com.epam.fonda.utils.PipelineUtils.NA;
import static com.epam.fonda.utils.PipelineUtils.getExecutionPath;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CountTest extends AbstractTest {
    private static final String COUNT_TEST_OUTPUT_DATA_PATH = "count_template_output";
    private static final String COUNT_TEST_OUTPUT_EXP_CELLS_DATA_PATH =
            "count_template_exp_cells_test_output";
    private static final String LIBRARY_TEST_OUTPUT_DATA_PATH = "library_csv_template_output.txt";
    private Configuration expectedConfiguration;
    private Count count;
    private String jarPath;
    private String fastqDirs;
    private FastqFileSample expectedSample;
    private TemplateEngine expectedTemplateEngine = TemplateEngineUtils.init();

    @BeforeEach
    void init() {
        constructConfiguration();
        expectedSample = new FastqFileSample();
        expectedSample.setFastq1(Arrays.asList("/path/to/sampleName/fastq1", "/path/to/sampleName/fastq2"));
        expectedSample.setName("sampleName");
        expectedSample.createDirectory();
        fastqDirs = String.join(",", CellRangerUtils.extractFastqDir(expectedSample).getFastqDirs());
        final LibraryCsv libraryCsv = LibraryCsv.builder()
                .sampleName("sampleName")
                .fastqDir(fastqDirs)
                .libraryType("Gene Expression")
                .build();
        final ArrayList<LibraryCsv> libraryList = new ArrayList<>();
        libraryList.add(libraryCsv);
        expectedSample.setLibrary(libraryList);
        BamOutput bamOutput = BamOutput.builder()
                .bam("sampleName.toolName.sorted.bam")
                .build();
        BamResult bamResult = BamResult.builder()
                .bamOutput(bamOutput)
                .command(BashCommand.withTool(""))
                .build();
        count = new Count(expectedSample, bamResult);
        jarPath = getExecutionPath(expectedConfiguration);
    }

    @Test
    void generate() throws IOException {
        Context context = new Context();
        context.setVariable("jarPath", jarPath);
        context.setVariable("fastqDirs", fastqDirs);
        final LibraryCsv libraryCsv = LibraryCsv.builder()
                .sampleName("sampleName")
                .fastqDir(fastqDirs)
                .libraryType("Antibody Capture")
                .build();
        expectedSample.getLibrary().add(libraryCsv);
        final String expectedCmd = expectedTemplateEngine.process(COUNT_TEST_OUTPUT_DATA_PATH, context);
        final String actualCmd = count.generate(expectedConfiguration, expectedTemplateEngine).getCommand()
                .getToolCommand();
        final String expectedLibraryCsv = expectedTemplateEngine.process(LIBRARY_TEST_OUTPUT_DATA_PATH, context);
        final String actualLibraryCsv = readFile(Paths.get(
                format("%s/sampleName_library.txt", expectedConfiguration.getCommonOutdir().getShOutdir())));

        assertEquals(expectedCmd, actualCmd);
        assertEquals(expectedLibraryCsv, actualLibraryCsv);
    }

    @Test
    void generateNullParameter() {
        expectedConfiguration.getGlobalConfig().getToolConfig().setCellranger(null);

        assertThrows(NullPointerException.class, () ->
                count.generate(expectedConfiguration, expectedTemplateEngine));
    }

    @Test
    void generateExpectedCells() {
        expectedConfiguration.getGlobalConfig().getCellrangerConfig().setCellrangerForcedCells(NA);
        Context context = new Context();
        context.setVariable("jarPath", jarPath);
        context.setVariable("fastqDirs", fastqDirs);
        final String expectedCmd = expectedTemplateEngine.process(COUNT_TEST_OUTPUT_EXP_CELLS_DATA_PATH, context);
        final String actualCmd = count.generate(expectedConfiguration, expectedTemplateEngine).getCommand()
                .getToolCommand();

        assertEquals(expectedCmd, actualCmd);
    }

    /**
     * Method sets all needed fields for {@link Configuration}
     */
    private void constructConfiguration() {
        expectedConfiguration = new Configuration();
        expectedConfiguration.setGlobalConfig(new GlobalConfig());
        expectedConfiguration.setStudyConfig(new StudyConfig());
        expectedConfiguration.getStudyConfig().setBamList("bam");
        expectedConfiguration.getGlobalConfig().getToolConfig().setCellranger("cellRanger");
        expectedConfiguration.getGlobalConfig().getToolConfig().setRScript("rScript");
        expectedConfiguration.getStudyConfig().setFastqList("fastqList");
        expectedConfiguration.getGlobalConfig().getDatabaseConfig().setTranscriptome("transcriptome");
        expectedConfiguration.getGlobalConfig().getDatabaseConfig().setFeatureRef("feature_ref.csv");
        expectedConfiguration.getGlobalConfig().getDatabaseConfig().setGenomeBuild("test1");
        CommonOutdir commonOutdir = new CommonOutdir("output");
        commonOutdir.createDirectory();
        expectedConfiguration.setCommonOutdir(commonOutdir);
        GlobalConfig.CellrangerConfig cellrangerConfig = expectedConfiguration.getGlobalConfig().getCellrangerConfig();
        cellrangerConfig.setCellrangerChemistry("chemistry");
        cellrangerConfig.setCellrangerExpectedCells("expectedCells");
        cellrangerConfig.setCellrangerForcedCells("forcedCells");
        cellrangerConfig.setCellrangerIndices("indices");
        cellrangerConfig.setCellrangerLanes("lanes");
        cellrangerConfig.setCellrangerNosecondary("nosecondary");
        cellrangerConfig.setCellrangerR1Length("r1Length");
        cellrangerConfig.setCellrangerR2Length("r2Length");
        GlobalConfig.QueueParameters queueParameters = expectedConfiguration.getGlobalConfig().getQueueParameters();
        queueParameters.setNumThreads(3);
        expectedConfiguration.getGlobalConfig().setCellrangerConfig(cellrangerConfig);
    }
}
