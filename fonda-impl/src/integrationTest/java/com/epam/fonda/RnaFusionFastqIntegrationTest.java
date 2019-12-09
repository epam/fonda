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
package com.epam.fonda;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@RunWith(DataProviderRunner.class)
public class RnaFusionFastqIntegrationTest extends AbstractIntegrationTest {
    private static final String OUTPUT_SH_FILE = "output/sh_files/RnaFusion_Fastq_fusion_for_smv1_analysis.sh";
    private static final String OUTPUT_DIR = "output/";
    private static final String OUTPUT_DIR_ROOT = "build/resources/integrationTest/";
    private static final String S_CONFIG_PATH = "RnaFusionFastq/sRnaFusionFastq.txt";
    private static final String SAMPLE_NAME = "smv1/";

    @Test
    public void testFlagXenomeYes() throws IOException {
        startAppWithConfigs(
                "RnaFusionFastq/gFlagXenomeYes.txt", S_CONFIG_PATH);
        File outputShFile = new File(this.getClass().getClassLoader().getResource(OUTPUT_SH_FILE).getPath());
        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            String[] expectedStrings = {
                "xenome classify -T 8 -P MOUSEXENOMEINDEX --pairs --graft-name human --host-name mouse " +
                        "--output-filename-prefix ",
                "build/resources/integrationTest/output/smv1/tmp/smv1 --tmp-dir " +
                        "build/resources/integrationTest/output/smv1/tmp -i " +
                        "build/resources/integrationTest/output/smv1/fastq/smv1.merged_R1.fastq.gz -i ",
                "build/resources/integrationTest/output/smv1/fastq/smv1.merged_R2.fastq.gz",
                "build/resources/integrationTest/output/smv1/tmp/smv1_human_1.fastq > " +
                        "build/resources/integrationTest/output/smv1/tmp/smv1_convert_human_1.fastq",
                "build/resources/integrationTest/output/smv1/tmp/smv1_human_2.fastq > " +
                        "build/resources/integrationTest/output/smv1/tmp/smv1_convert_human_2.fastq",
                "build/resources/integrationTest/output/smv1/tmp/smv1_both_1.fastq > " +
                        "build/resources/integrationTest/output/smv1/tmp/smv1_convert_both_1.fastq",
                "build/resources/integrationTest/output/smv1/tmp/smv1_both_2.fastq > " +
                        "build/resources/integrationTest/output/smv1/tmp/smv1_convert_both_2.fastq",
                "build/resources/integrationTest/output/smv1/tmp/smv1_ambiguous_1.fastq > " +
                        "build/resources/integrationTest/output/smv1/tmp/smv1_convert_ambiguous_1.fastq",
                "build/resources/integrationTest/output/smv1/tmp/smv1_ambiguous_2.fastq > " +
                        "build/resources/integrationTest/output/smv1/tmp/smv1_convert_ambiguous_2.fastq",
                "cat build/resources/integrationTest/output/smv1/tmp/smv1_convert_human_1.fastq " +
                        "build/resources/integrationTest/output/smv1/tmp/smv1_convert_both_1.fastq ",
                "build/resources/integrationTest/output/smv1/tmp/smv1_convert_ambiguous_1.fastq | gzip -c > " +
                        "build/resources/integrationTest/output/smv1/fastq/smv1_classified_R1.fq.gz",
                "cat build/resources/integrationTest/output/smv1/tmp/smv1_convert_human_2.fastq " +
                        "build/resources/integrationTest/output/smv1/tmp/smv1_convert_both_2.fastq ",
                "build/resources/integrationTest/output/smv1/tmp/smv1_convert_ambiguous_2.fastq | gzip -c > " +
                        "build/resources/integrationTest/output/smv1/fastq/smv1_classified_R2.fq.gz"
            };
            for (String expectedString : expectedStrings) {
                assertTrue(lines.stream().anyMatch(line -> line.contains(expectedString)));
            }
            assertFalse(lines.stream().anyMatch(line -> line.contains("null")));
        }
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @Test
    public void testStarFusionAndFusionCatcher() throws IOException {
        startAppWithConfigs(
                "RnaFusionFastq/gStarFusionAndFusionCatcher.txt", S_CONFIG_PATH);
        File outputShFile = new File(this.getClass().getClassLoader().getResource(OUTPUT_SH_FILE).getPath());
        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            String[] expectedStrings = {
                "Begin Step: STAR4FUSION alignment...",
                "path/to/star --genomeDir /common/reference_genome/GRCh38/Index/STAR_g26 --twopassMode " +
                        "Basic --genomeLoad NoSharedMemory --readFilesIn " +
                        "build/resources/integrationTest/output/smv1/fastq/smv1.merged_R1.fastq.gz " +
                        "build/resources/integrationTest/output/smv1/fastq/smv1.merged_R2.fastq.gz ",
                "--outFileNamePrefix build/resources/integrationTest/output/smv1/bam/smv1. " +
                        "--outReadsUnmapped None --chimSegmentMin 12 --chimJunctionOverhangMin 12 " +
                        "--alignSJDBoverhangMin 10 --alignMatesGapMax 200000 " +
                        "--alignIntronMax 200000 --chimSegmentReadGapMax parameter 3 " +
                        "--alignSJstitchMismatchNmax 5 -1 5 5 --runThreadN 4 --outSAMtype BAM Unsorted " +
                        "--outSAMattrRGline ID:smv1 SM:smv1 LB:RNA PL:Illumina CN:cr --readFilesCommand zcat",
                "Begin Step: STAR-Fusion detection...",
                "path/to/starFusion --genome_lib_dir path/to/starFusion/lib -J " +
                        "build/resources/integrationTest/output/smv1/bam/smv1.Chimeric.out.junction " +
                        "--output_dir build/resources/integrationTest/output/smv1/starFusion",
                "mv build/resources/integrationTest/output/smv1/starFusion/star-fusion.fusion_candidates." +
                        "final.abridged " +
                        "build/resources/integrationTest/output/smv1/starFusion/smv1.starFusion.fusion." +
                        "final.abridged",
                "Begin Step: FusionCatcher...",
                "path/to/starfusion --input " +
                        "build/resources/integrationTest/output/smv1/fastq/smv1.merged_R1.fastq.gz," +
                        "build/resources/integrationTest/output/smv1/fastq/smv1.merged_R2.fastq.gz ",
                "--output build/resources/integrationTest/output/smv1/fusionCatcher " +
                        "--tmp build/resources/integrationTest/output/smv1/fusionCatcher/tmp --threads 4",
                "mv build/resources/integrationTest/output/smv1/fusionCatcher/fusionCatcher.fusion_candidates." +
                        "final.abridged build/resources/integrationTest/output/smv1/fusionCatcher"
            };
            List<String> lines = reader.lines().collect(Collectors.toList());
            for (String expectedString : expectedStrings) {
                assertTrue(lines.stream().anyMatch(line -> line.contains(expectedString)));
            }
            assertFalse(lines.stream().anyMatch(line -> line.contains("null")));
        }
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @DataProvider
    public static Object[][] getSeqpurgeConfigAndStrings() {
        return new Object[][]{
                {"RnaFusionFastq/gSeqpurgeWithAdapters.txt", new String[]{
                    "/opt/ngs_bits/ngs-bits/bin/SeqPurge -threads 4 -in1",
                    "build/resources/integrationTest/output/smv1/fastq/smv1.merged_R1.fastq.gz -in2 " +
                            "build/resources/integrationTest/output/smv1/fastq/smv1.merged_R2.fastq.gz -out1 ",
                    "build/resources/integrationTest/output/smv1/fastq/smv1.trimmed.R1.fastq.gz -out2 " +
                            "build/resources/integrationTest/output/smv1/fastq/smv1.trimmed.R2.fastq.gz" +
                            " -qcut 20 -a1 ",
                    "AGATCGGAAGAGCACACGTCTGAACTCCAGTCAC -a2 " +
                            "AGATCGGAAGAGCGTCGTGTAGGGAAAGAGTGTAGATCTCGGTGGTCGCCGTATCATT"
                }},
                {"RnaFusionFastq/gSeqpurgeWithoutAdapters.txt", new String[]{
                    "/opt/ngs_bits/ngs-bits/bin/SeqPurge -threads 4 -in1 ",
                    "build/resources/integrationTest/output/smv1/fastq/smv1.merged_R1.fastq.gz -in2 " +
                            "build/resources/integrationTest/output/smv1/fastq/smv1.merged_R2.fastq.gz -out1 ",
                    "build/resources/integrationTest/output/smv1/fastq/smv1.trimmed.R1.fastq.gz -out2 " +
                            "build/resources/integrationTest/output/smv1/fastq/smv1.trimmed.R2.fastq.gz -qcut 20"
                }},
        };
    }

    @Test
    @UseDataProvider("getSeqpurgeConfigAndStrings")
    public void testSeqpurge(String gConfigPath, String[] expectedStrings) throws IOException {
        startAppWithConfigs(
                gConfigPath, S_CONFIG_PATH);
        File outputShFile = new File(this.getClass().getClassLoader().getResource(OUTPUT_SH_FILE).getPath());
        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            for (String expectedString : expectedStrings) {
                assertTrue(lines.stream().anyMatch(line -> line.contains(expectedString)));
            }
            assertFalse(lines.stream().anyMatch(line -> line.contains("null")));
        }
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @DataProvider
    public static Object[][] getTrimmomaticConfigAndStrings() {
        return new Object[][]{
                {"RnaFusionFastq/gTrimmomaticWithAdapter.txt", new String[]{
                    "/usr/lib/jvm/java-8-openjdk-amd64/bin/java -jar trimmomatic PE -threads 4 -phred33 ",
                    "build/resources/integrationTest/output/smv1/fastq/smv1.merged_R1.fastq.gz " +
                            "build/resources/integrationTest/output/smv1/fastq/smv1.merged_R2.fastq.gz " +
                            "build/resources/integrationTest/output/smv1/fastq/smv1.trimmed.R1.fastq.gz " +
                            "build/resources/integrationTest/output/smv1/fastq/smv1.trimmed_unpaired.R1.fq.gz " +
                            "build/resources/integrationTest/output/smv1/fastq/smv1.trimmed.R2.fastq.gz " +
                            "build/resources/integrationTest/output/smv1/fastq/smv1.trimmed_unpaired.R2.fq.gz ",
                    "ILLUMINACLIP:adapter_seq:2:30:10 LEADING:20 TRAILING:20 SLIDINGWINDOW:4:15 MINLEN:36",
                    "rm -rf build/resources/integrationTest/output/smv1/fastq/smv1.trimmed_unpaired.R1.fq.gz",
                    "rm -rf build/resources/integrationTest/output/smv1/fastq/smv1.trimmed_unpaired.R2.fq.gz"
                }},
                {"RnaFusionFastq/gTrimmomaticWithoutAdapter.txt", new String[]{
                    "echo `date` the adapter sequence trimming step was skipped since no adapter sequences" +
                            " were provided in the config files."
                }},
        };
    }

    @Test
    @UseDataProvider("getTrimmomaticConfigAndStrings")
    public void testTrimmomatic(String gConfigPath, String[] expectedStrings) throws IOException {
        startAppWithConfigs(
                gConfigPath, S_CONFIG_PATH);
        File outputShFile = new File(this.getClass().getClassLoader().getResource(OUTPUT_SH_FILE).getPath());
        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            for (String expectedString : expectedStrings) {
                assertTrue(lines.stream().anyMatch(line -> line.contains(expectedString)));
            }
            assertFalse(lines.stream().anyMatch(line -> line.contains("null")));
        }
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @DataProvider
    public static Object[] getNoToolsetExpectedStrings() {
        return new Object[][]{
                {"RnaFusionFastq/gNonFlagXenome.txt", "xenome classify -T 8 -P MOUSEXENOMEINDEX --pairs --graft-name" +
                        " human --host-name "},
                {"RnaFusionFastq/gNonSeqpurge.txt", "/opt/ngs_bits/ngs-bits/bin/SeqPurge -threads 4 -in1"},
                {"RnaFusionFastq/gNonTrimmomatic.txt", "-jar trimmomatic PE -threads 4 -phred33 "},
                {"RnaFusionFastq/gNonStarFusion.txt", "path/to/star --genomeDir /common/reference_genome/GRCh38/" +
                        "Index/STAR_g26 --twopassMode"},
                {"RnaFusionFastq/gNonFusionCatcher.txt", "path/to/starfusion --input "},

        };
    }

    @Test
    @UseDataProvider("getNoToolsetExpectedStrings")
    public void testNoToolset(String gConfigPath, String expectedString) throws IOException {
        startAppWithConfigs(
                gConfigPath, S_CONFIG_PATH);
        File outputShFile = new File(this.getClass().getClassLoader().getResource(OUTPUT_SH_FILE).getPath());
        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertFalse(lines.stream().anyMatch(line -> line.contains(expectedString)));
            assertFalse(lines.stream().anyMatch(line -> line.contains("null")));
        }
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @Test
    public void testCreateRnaFusionFastqSpecificDirFusionCatcherStarFusionToolset() throws IOException {
        startAppWithConfigs("RnaFusionFastq/gStarFusionAndFusionCatcher.txt", S_CONFIG_PATH);
        assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + "sh_files").exists());
        assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + "log_files").exists());
        assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + "err_files").exists());
        assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + SAMPLE_NAME).exists());
        assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + SAMPLE_NAME + "tmp")
                .exists());
        assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + SAMPLE_NAME + "fastq")
                .exists());
        assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + SAMPLE_NAME + "bam")
                .exists());
        assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + SAMPLE_NAME + "qc")
                .exists());
        assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + SAMPLE_NAME + "starFusion")
                .exists());
        assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + SAMPLE_NAME + "fusionCatcher")
                .exists());
        assertTrue(new File(OUTPUT_DIR_ROOT + OUTPUT_DIR + SAMPLE_NAME + "fusionCatcher/tmp").exists());
        cleanOutputDirForNextTest(OUTPUT_DIR, true);
    }
}
