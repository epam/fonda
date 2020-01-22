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

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Bam2FastqIntegrationTest extends AbstractIntegrationTest {
    private static final String OUTPUT_DIR_ROOT = "build/resources/integrationTest/";
    private static final String OUTPUT_DIR = "output/";
    private static final String SAMPLE_NAME = "GA5/";
    private static final String OUTPUT_SH_FILE = "output/sh_files/Bam2Fastq_convert_for_GA5_analysis.sh";
    private static final String OUTPUT_FASTQ_FILE = "output/Example_project-run1234-031814-FastqPaths.txt";

    @Test
    public void testSortPairedNonPicard() throws IOException {
        startAppWithConfigs(
                "Bam2Fastq/gPairedNonPicard.txt", "Bam2Fastq/sPaired.txt");
        File outputShFile = new File(this.getClass().getClassLoader().getResource(OUTPUT_SH_FILE).getPath());
        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertTrue(lines.stream().anyMatch(line -> line.contains("SortSam INPUT=")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("SORT_ORDER=queryname")));
            assertFalse(lines.stream().anyMatch(line -> line.contains("FASTQ=")));
            assertTrue(lines.stream().noneMatch(line -> line.contains("null")));
        }

        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @Test
    public void testPairedPicard() throws IOException {
        startAppWithConfigs(
                "Bam2Fastq/gPairedPicard.txt", "Bam2Fastq/sPaired.txt");
        File outputShFile = new File(this.getClass().getClassLoader().getResource(OUTPUT_SH_FILE).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertTrue(lines.stream().anyMatch(line -> line.contains("UNPAIRED_FASTQ=")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("SECOND_END_FASTQ=")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(".R2.fastq")));
            assertTrue(lines.stream().noneMatch(line -> line.contains("null")));
        }

        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @Test
    public void testSinglePicard3Columns() throws IOException {
        startAppWithConfigs(
                "Bam2Fastq/gSinglePicard.txt", "Bam2Fastq/sSingle.txt");
        File outputShFile = new File(this.getClass().getClassLoader().getResource(OUTPUT_SH_FILE).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertTrue(lines.stream().anyMatch(line -> line.contains("FASTQ=")));
            assertFalse(lines.stream().anyMatch(line -> line.contains("UNPAIRED_FASTQ=")));
            assertFalse(lines.stream().anyMatch(line -> line.contains("SECOND_END_FASTQ=")));
            assertFalse(lines.stream().anyMatch(line -> line.contains(".R2.fastq")));
            assertTrue(lines.stream().noneMatch(line -> line.contains("null")));
        }

        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @Test
    public void testSinglePicard5Columns() throws IOException {
        startAppWithConfigs(
                "Bam2Fastq/gSinglePicard.txt", "Bam2Fastq/sSingleControlSample.txt");
        File outputShFile = new File(this.getClass().getClassLoader().
                getResource("output/sh_files/Bam2Fastq_convert_for_GA51_analysis.sh").getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertTrue(lines.stream().anyMatch(line -> line.contains("FASTQ=")));
            assertFalse(lines.stream().anyMatch(line -> line.contains("UNPAIRED_FASTQ=")));
            assertFalse(lines.stream().anyMatch(line -> line.contains("SECOND_END_FASTQ=")));
            assertFalse(lines.stream().anyMatch(line -> line.contains(".R2.fastq")));
            assertTrue(lines.stream().noneMatch(line -> line.contains("null")));
        }

        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @Test
    public void testGetFastqListSingle() throws IOException {
        startAppWithConfigs(
                "Bam2Fastq/gSinglePicard.txt", "Bam2Fastq/sSingle.txt");
        File outputFastqPathsFile = new File(this.getClass().getClassLoader().getResource(OUTPUT_FASTQ_FILE).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputFastqPathsFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertTrue(lines.get(0).contains("Parameter"));
            assertTrue(lines.get(1).contains("tumor\tNA"));
            assertTrue(lines.stream().noneMatch(line -> line.contains("null")));
        }
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @Test
    public void testGetFastqListPaired() throws IOException {
        startAppWithConfigs(
                "Bam2Fastq/gPairedPicard.txt", "Bam2Fastq/sPaired.txt");
        File outputFastqPathsFile = new File(this.getClass().getClassLoader().getResource(OUTPUT_FASTQ_FILE).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputFastqPathsFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertTrue(lines.get(0).contains("Parameter1\tParameter2"));
            assertTrue(lines.get(1).contains("sample\tGA52"));
            assertTrue(lines.get(2).contains("tumor\tGA51"));
            assertTrue(lines.get(3).contains("tumor\tNA"));
            assertTrue(lines.stream().noneMatch(line -> line.contains("null")));
            lines.remove(0);
            String fastqPairedPathPattern = ".+?R1.fastq.gz.+?R2.fastq.gz.+?(\\n|$)";
            assertTrue(lines.stream().filter(i -> !(i.isEmpty() && lines.get(lines.size() - 1).equals(i)))
                    .allMatch(line -> line.matches(fastqPairedPathPattern)));
        }
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @Test
    public void testSpecificDir() throws IOException {
        startAppWithConfigs("Bam2Fastq/gPairedPicard.txt", "Bam2Fastq/sPaired.txt");
        assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + SAMPLE_NAME).exists());
        assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + "sh_files").exists());
        assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + "log_files").exists());
        assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + "err_files").exists());
        assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + SAMPLE_NAME + "fastq").exists());
        cleanOutputDirForNextTest(OUTPUT_DIR, true);
    }
}
