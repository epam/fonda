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
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class RnaFusionFastqIntegrationTest extends AbstractIntegrationTest {
    private static final String OUTPUT_SH_FILE = "output/sh_files/RnaFusion_Fastq_fusion_for_smv1_analysis.sh";
    private static final String OUTPUT_DIR = "output/";
    private static final String OUTPUT_DIR_ROOT = "build/resources/integrationTest/";
    private static final String S_CONFIG_PATH = "RnaFusionFastq/sRnaFusionFastq.txt";
    private static final String SAMPLE_NAME = "smv1/";
    private static final String TEMPLATE_FOLDER = "templates/rnaFusionFastq_templates";
    private static final String RNA_FUSION_XENOME_YES =
            String.format("%s/rnaFusionFastq_Flag_Xenome_Yes_template.txt", TEMPLATE_FOLDER);
    private static final String RNA_FUSION_NON_FLAG_XENOME =
            String.format("%s/rnaFusionFastq_Non_Flag_Xenome_template.txt", TEMPLATE_FOLDER);
    private static final String RNA_FUSION_NON_FUSION_CATCHER =
            String.format("%s/rnaFusionFastq_Non_Fusion_Catcher.txt", TEMPLATE_FOLDER);
    private static final String RNA_FUSION_NON_SEQPURGE = RNA_FUSION_NON_FUSION_CATCHER;
    private static final String RNA_FUSION_NON_STAR_FUSION = RNA_FUSION_NON_FUSION_CATCHER;
    private static final String RNA_FUSION_NON_TRIMMOMATIC =
            String.format("%s/rnaFusionFastq_Non_Trimmomatic_template.txt", TEMPLATE_FOLDER);
    private static final String RNA_FUSION_SEQPURGE_WITH_ADAPTERS =
            String.format("%s/rnaFusionFastq_Seqpurge_With_Adapters_template.txt", TEMPLATE_FOLDER);
    private static final String RNA_FUSION_SEQPURGE_WITHOUT_ADAPTERS =
            String.format("%s/rnaFusionFastq_Seqpurge_Without_Adapters_template.txt", TEMPLATE_FOLDER);
    private static final String RNA_FUSION_STARFUSION_AND_FUSION_CATCHER =
            String.format("%s/rnaFusionFastq_StarFusion_And_FusionCatcher_template.txt", TEMPLATE_FOLDER);
    private static final String RNA_FUSION_TRIMMOMATIC_WITH_ADAPTER =
            String.format("%s/rnaFusionFastq_Trimmomatic_With_Adapter_template.txt", TEMPLATE_FOLDER);
    private static final String RNA_FUSION_TRIMMOMATIC_WITHOUT_ADAPTER =
            String.format("%s/rnaFusionFastq_Trimmomatic_Without_Adapter_template.txt", TEMPLATE_FOLDER);

    @ParameterizedTest
    @MethodSource("initGlobalConfigAndTemplatePath")
    public void testFlagXenomeYes(String globalConfigPath, String templatePath) throws IOException {
        startAppWithConfigs(globalConfigPath, S_CONFIG_PATH);

        File expectedFile = new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource(templatePath)).getPath());
        File outputShFile = new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource(OUTPUT_SH_FILE)).getPath());

        assertEquals(getFileContent(expectedFile), getFileContent(outputShFile));
        assertFalse(getFileContent(expectedFile).contains("null"));
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initGlobalConfigAndTemplatePath() {
        return Stream.of(
                Arguments.of("RnaFusionFastq/gFlagXenomeYes.txt", RNA_FUSION_XENOME_YES),
                Arguments.of("RnaFusionFastq/gNonFlagXenome.txt", RNA_FUSION_NON_FLAG_XENOME),
                Arguments.of("RnaFusionFastq/gNonFusionCatcher.txt", RNA_FUSION_NON_FUSION_CATCHER),
                Arguments.of("RnaFusionFastq/gNonSeqpurge.txt", RNA_FUSION_NON_SEQPURGE),
                Arguments.of("RnaFusionFastq/gNonStarFusion.txt", RNA_FUSION_NON_STAR_FUSION),
                Arguments.of("RnaFusionFastq/gNonTrimmomatic.txt", RNA_FUSION_NON_TRIMMOMATIC),
                Arguments.of("RnaFusionFastq/gSeqpurgeWithAdapters.txt", RNA_FUSION_SEQPURGE_WITH_ADAPTERS),
                Arguments.of("RnaFusionFastq/gSeqpurgeWithoutAdapters.txt", RNA_FUSION_SEQPURGE_WITHOUT_ADAPTERS),
                Arguments.of("RnaFusionFastq/gStarFusionAndFusionCatcher.txt", RNA_FUSION_STARFUSION_AND_FUSION_CATCHER),
                Arguments.of("RnaFusionFastq/gTrimmomaticWithAdapter.txt", RNA_FUSION_TRIMMOMATIC_WITH_ADAPTER),
                Arguments.of("RnaFusionFastq/gTrimmomaticWithoutAdapter.txt", RNA_FUSION_TRIMMOMATIC_WITHOUT_ADAPTER)
        );
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
    private static Object[][] getSeqpurgeConfigAndStrings() {
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
    private static Object[][] getTrimmomaticConfigAndStrings() {
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
    private static Object[] getNoToolsetExpectedStrings() {
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

    private String getFileContent(File file) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8)) {
            stream.forEach(s -> builder.append(s).append("\n"));
        }
        return builder.toString();
    }
}
