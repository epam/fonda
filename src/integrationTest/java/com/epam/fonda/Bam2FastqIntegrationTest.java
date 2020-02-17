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

package com.epam.fonda;

import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class Bam2FastqIntegrationTest extends AbstractIntegrationTest {
    private static final String OUTPUT_DIR_ROOT = "build/resources/integrationTest/";
    private static final String OUTPUT_DIR = "output/";
    private static final String OUTPUT_SH_FILE_GA5 = "output/sh_files/Bam2Fastq_convert_for_GA5_analysis.sh";
    private static final String OUTPUT_SH_FILE_GA51 = "output/sh_files/Bam2Fastq_convert_for_GA51_analysis.sh";
    private static final String OUTPUT_SH_FILE_GA52 = "output/sh_files/Bam2Fastq_convert_for_GA52_analysis.sh";
    private static final String OUTPUT_FASTQ_FILE = "output/Example_project-run1234-031814-FastqPaths.txt";
    private static final String NULL = "null";

    private static final String S_PAIRED = "Bam2Fastq/sPaired.txt";
    private static final String S_SINGLE = "Bam2Fastq/sSingle.txt";
    private static final String S_SINGLE_CONTROL_SAMPLE = "Bam2Fastq/sSingleControlSample.txt";
    private static final String G_SINGLE_PICARD = "Bam2Fastq/gSinglePicard.txt";
    private static final String G_PAIRED_NON_PICARD = "Bam2Fastq/gPairedNonPicard.txt";
    private static final String G_PAIRED_PICARD = "Bam2Fastq/gPairedPicard.txt";
    private static final String GA5_TEMPLATE = "Bam2Fastq_convert_for_GA5_analysis.txt";
    private static final String GA51_TEMPLATE = "Bam2Fastq_convert_for_GA51_analysis.txt";
    private static final String GA52_TEMPLATE = "Bam2Fastq_convert_for_GA52_analysis.txt";
    private static final String TEST_FASTQ_FILE = "Example_project-run1234-031814-FastqPaths.txt";

    private TemplateEngine templateEngine = TemplateEngineUtils.init();
    private Context context;

    @BeforeEach
    public void setup() {
        context = new Context();
    }

    @AfterEach
    public void cleanUp() throws IOException {
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @Test
    public void testSortPairedNonPicard() throws IOException, URISyntaxException {
        startAppWithConfigs(G_PAIRED_NON_PICARD, S_PAIRED);
        String testFolder = "bam2Fastq/testSortPairedNonPicard";

        String expectedGa5Cmd = getTemplate(testFolder, GA5_TEMPLATE);
        String expectedGa52Cmd = getTemplate(testFolder, GA52_TEMPLATE);

        String actualGa5Cmd = getCmd(OUTPUT_SH_FILE_GA5).trim();
        String actualGa52Cmd = getCmd(OUTPUT_SH_FILE_GA52).trim();

        assertAll(
            () -> assertFalse(actualGa5Cmd.contains(NULL)),
            () -> assertFalse(actualGa52Cmd.contains(NULL)),
            () -> assertEquals(expectedGa5Cmd, actualGa5Cmd),
            () -> assertEquals(expectedGa52Cmd, actualGa52Cmd),
            this::assertDirectories
        );
    }

    @Test
    public void testPairedPicard() throws IOException, URISyntaxException {
        startAppWithConfigs(G_PAIRED_PICARD, S_PAIRED);
        String testFolder = "bam2Fastq/testPairedPicard";

        String expectedGa5Cmd = getTemplate(testFolder, GA5_TEMPLATE);
        String expectedGa52Cmd = getTemplate(testFolder, GA52_TEMPLATE);
        String expectedFastqFile = getTestFastqFileString(testFolder);

        String actualGa5Cmd = getCmd(OUTPUT_SH_FILE_GA5).trim();
        String actualGa52Cmd = getCmd(OUTPUT_SH_FILE_GA52).trim();
        String actualFastqFile = getOutputFastqFileString();

        assertAll(
            () -> assertFalse(actualGa5Cmd.contains(NULL)),
            () -> assertFalse(actualGa52Cmd.contains(NULL)),
            () -> assertFalse(actualFastqFile.contains(NULL)),
            () -> assertEquals(expectedGa5Cmd, actualGa5Cmd),
            () -> assertEquals(expectedGa52Cmd, actualGa52Cmd),
            () -> assertEquals(expectedFastqFile, actualFastqFile),
            this::assertDirectories
        );
    }

    @Test
    public void testSinglePicard3Columns() throws IOException, URISyntaxException {
        startAppWithConfigs(G_SINGLE_PICARD, S_SINGLE);
        String testFolder = "bam2Fastq/testSinglePicard3Columns";

        String expectedGa5Cmd = getTemplate(testFolder, GA5_TEMPLATE).trim();
        String expectedFastqFile = getTestFastqFileString(testFolder);

        String actualGa5Cmd = getCmd(OUTPUT_SH_FILE_GA5).trim();
        String actualFastqFile = getOutputFastqFileString();

        assertAll(
            () -> assertFalse(actualGa5Cmd.contains(NULL)),
            () -> assertFalse(actualFastqFile.contains(NULL)),
            () -> assertEquals(expectedGa5Cmd, actualGa5Cmd),
            () -> assertEquals(expectedFastqFile, actualFastqFile),
            this::assertDirectories
        );
    }

    @Test
    public void testSinglePicard5Columns() throws IOException, URISyntaxException {
        startAppWithConfigs(G_SINGLE_PICARD, S_SINGLE_CONTROL_SAMPLE);
        String testFolder = "bam2Fastq/testSinglePicard5Columns";

        String expectedGa51Cmd = getTemplate(testFolder, GA51_TEMPLATE).trim();
        String expectedGa52Cmd = getTemplate(testFolder, GA52_TEMPLATE).trim();

        String actualGa51Cmd = getCmd(OUTPUT_SH_FILE_GA51).trim();
        String actualGa52Cmd = getCmd(OUTPUT_SH_FILE_GA52).trim();

        assertAll(
            () -> assertEquals(expectedGa51Cmd, actualGa51Cmd),
            () -> assertFalse(actualGa51Cmd.contains(NULL)),
            () -> assertEquals(expectedGa52Cmd, actualGa52Cmd),
            () -> assertFalse(actualGa52Cmd.contains(NULL)),
            this::assertDirectories
        );
    }

    private String getTemplate(String folder, String file) {
        return templateEngine.process(String.format("%s/%s", folder, file), context).trim();
    }

    private void assertDirectories() {
        assertAll(
            () -> assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + "sh_files").exists()),
            () -> assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + "log_files").exists()),
            () -> assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + "err_files").exists())
        );
    }

    private String getOutputFastqFileString() throws IOException, URISyntaxException {
        return getCmd(OUTPUT_FASTQ_FILE).replaceAll("\\r\\n", "\n").trim();
    }

    private String getTestFastqFileString(String testFolder) throws IOException, URISyntaxException {
        return getCmd(String.format("templates/%s/%s", testFolder, TEST_FASTQ_FILE)
                .replaceAll("\\r\\n", "\n")).trim();
    }
}
