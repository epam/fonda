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

import static org.junit.Assert.assertTrue;

@RunWith(DataProviderRunner.class)
public class DnaAmpliconVarFastqIntegrationTest extends AbstractIntegrationTest {
    private static final String OUTPUT_DIR = "output";
    private static final String ERROR_MESSAGE = "An error occurred with task %s. Expected: %s";
    private static final String POST_ALIGNMENT_SH_FILE =
            "output/sh_files/DnaAmpliconVar_Fastq_postalignment_for_GA5_analysis.sh";
    private static final String NULL = "null";
    private static final String FASTA_FILE =
            "/ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa";
    private static final String REMOVE_TEMP_DIRS_STEP = "Begin Step: Remove temporary directories...";
    private static final String SNPEFF_PATTERN = ".*?/usr/bin/python.+?vcf_snpeff_annotation.py.+?(\\n|$)";
    private static final String SINGLE_STUDY_CONFIG = "DnaAmpliconVarFastq/sSingle.txt";
    private static final String PAIRED_STUDY_CONFIG = "DnaAmpliconVarFastq/sPaired.txt";
    private static final String ALIGNMENT_OUTPUT_SH =
            "output/sh_files/DnaAmpliconVar_Fastq_alignment_for_GA5_1_analysis.sh";

    @Test
    public void testSingleXenomeYesTrimmomaticBwa() throws IOException {
        startAppWithConfigs("DnaAmpliconVarFastq/gSingleTrimmomaticBwaYes.txt",
                SINGLE_STUDY_CONFIG);

        //test for trimmomatic, xenome and bwa toolset
        File outputShFile = new File(this.getClass().getClassLoader().getResource(ALIGNMENT_OUTPUT_SH).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertXenome(lines);
            assertCommonTrimmomatic(lines);
            assertTrue(lines.stream()
                    .anyMatch(line -> line.contains("/usr/lib/jvm/java-8-openjdk-amd64/bin/java -jar ")));
            assertTrue(lines.stream()
                    .anyMatch(line -> line.contains("/usr/bin/trimmomatic PE -threads 4 -phred33 ")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("build/resources/" +
                    "integrationTest/output/GA5/fastq/GA5_1_classified_R1.fq.gz")));
            assertBwa(lines);
            assertTrue(lines.stream().noneMatch(line -> line.contains(NULL)));
        }
        assertPostAlignment();
    }

    @Test
    public void testSingleXenomeYesTrimmomaticNovoalign() throws IOException {
        startAppWithConfigs("DnaAmpliconVarFastq/gSingleTrimmomaticNovoalignYes.txt",
                SINGLE_STUDY_CONFIG);

        //test for trimmomatic, xenome and novoalign toolset
        File outputShFile = new File(this.getClass().getClassLoader().getResource(ALIGNMENT_OUTPUT_SH).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertXenome(lines);
            assertCommonTrimmomatic(lines);
            assertNovoalign(lines);
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    "-f build/resources/integrationTest/output/GA5/fastq/GA5_1.trimmed.R1.fastq.gz " +
                            "build/resources/integrationTest/output/GA5/fastq/GA5_1.trimmed.R2.fastq.gz")));
            assertTrue(lines.stream().noneMatch(line -> line.contains(NULL)));
        }
        assertPostAlignment();
    }

    @Test
    public void testPairedXenomeYesSeqpurgeBwa() throws IOException {
        startAppWithConfigs("DnaAmpliconVarFastq/gPairedSeqpurgeBwaXenomeYes.txt", PAIRED_STUDY_CONFIG);

        //test for xenome, seqpurge and bwa toolset
        File outputShFile = new File(this.getClass().getClassLoader().getResource(ALIGNMENT_OUTPUT_SH).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertXenome(lines);
            assertSequpurge(lines);
            assertBwa(lines);
        }
        assertPostAlignment();
    }

    @Test
    public void testPairedXenomeYesTrimmomaticBwa() throws IOException {
        startAppWithConfigs("DnaAmpliconVarFastq/gPairedTrimmomaticBwaXenomeYes.txt", PAIRED_STUDY_CONFIG);

        //test for xenome, trimmomatic and bwa toolset
        File outputShFile = new File(this.getClass().getClassLoader().getResource(ALIGNMENT_OUTPUT_SH).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertXenome(lines);
            assertCommonTrimmomatic(lines);
            assertTrue(lines.stream()
                    .anyMatch(line -> line.contains("/usr/lib/jvm/java-8-openjdk-amd64/bin/java -jar ")));
            assertTrue(lines.stream()
                    .anyMatch(line -> line.contains("/usr/bin/trimmomatic PE -threads 4 -phred33 ")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    "build/resources/integrationTest/output/GA5/fastq/GA5_1.trimmed.R1.fastq.gz ")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    "build/resources/integrationTest/output/GA5/fastq/GA5_1.trimmed.R2.fastq.gz")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("rm -rf " +
                    "build/resources/integrationTest/output/GA5/fastq/GA5_1.trimmed_unpaired.R2.fq.gz")));
            assertBwa(lines);
        }
        assertPostAlignment();
    }

    @Test
    public void testPairedXenomeYesSeqpurgeNovoalign() throws IOException {
        startAppWithConfigs("DnaAmpliconVarFastq/gPairedSeqpurgeNovoalignXenomeYes.txt",
                PAIRED_STUDY_CONFIG);

        //test for xenome, seqpurge and novoalign toolset
        File outputShFile = new File(this.getClass().getClassLoader().getResource(ALIGNMENT_OUTPUT_SH).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertXenome(lines);
            assertSequpurge(lines);
            assertNovoalign(lines);
        }
        assertPostAlignment();
    }

    @Test
    public void testPicardAbraGatkSingle() throws IOException {
        startAppWithConfigs("DnaAmpliconVarFastq/gAbraGatkPicardSingle.txt", SINGLE_STUDY_CONFIG);

        //test for toolset picard + gatk_realign + abra+realign
        File outputShFile = new File(this.getClass().getClassLoader().getResource(POST_ALIGNMENT_SH_FILE).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: ABRA realignment...")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    "/usr/lib/jvm/java-8-openjdk-amd64/bin/java -Xmx16g -jar /usr/bin/abra2")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    "--single --tmpdir build/resources/integrationTest/output/GA5/tmp")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("/opt/samtools/samtools-0.1.19/samtools index " +
                    "build/resources/integrationTest/output/GA5/bam/GA5.merged.sorted.realign.bam")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: GATK realignment...")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("-jar /usr/bin/gatk -T RealignerTargetCreator")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("-jar /usr/bin/gatk -T IndelRealigner")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: GATK recalibration..")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("-jar /usr/bin/gatk -T BaseRecalibrator -R")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("-jar /usr/bin/gatk -T PrintReads -R")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("-knownSites 10 -knownSites 100 ")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: Mark duplicates...")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    "/usr/lib/jvm/java-8-openjdk-amd64/bin/java -Xmx16g -jar /opt/picard/picard.jar MarkDuplicates")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: Index mkdup bam...")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: DNA QC metrics...")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("/usr/bin/bedtools coverage -abam " +
                    "build/resources/integrationTest/output/GA5/bam/GA5.merged.sorted.bam")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("CollectAlignmentSummaryMetrics")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("CollectHsMetrics")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("CollectGcBiasMetrics")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("CollectInsertSizeMetrics")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("CollectQualityYieldMetrics")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    "/opt/samtools/samtools-0.1.19/samtools mpileup")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: Merge DNA QC...")));
            assertTrue(lines.stream().anyMatch(line -> line.matches(
                    ".*?/usr/bin/python.+?dna_rna_variant_qc_metrics.py.+?(\\n|$)")));
            assertTrue(lines.stream().noneMatch(line -> line.contains(NULL)));
        }

        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @Test
    public void testPicardAbraGatkPaired() throws IOException {
        startAppWithConfigs("DnaAmpliconVarFastq/gAbraGatkPicardPaired.txt", PAIRED_STUDY_CONFIG);

        //test for toolset picard + gatk_realign + abra+realign
        File outputShFile = new File(this.getClass().getClassLoader().getResource(POST_ALIGNMENT_SH_FILE).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    "--insert build/resources/integrationTest/output/GA5/qc/GA5.merged.sorted.insertsize.metrics")));
            assertTrue(lines.stream().noneMatch(line -> line.contains("--single")));
            assertTrue(lines.stream().noneMatch(line -> line.contains(NULL)));
        }

        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @Test
    @UseDataProvider("controlSampleNAAllTasks")
    public void testControlSampleAllTasksXenomeNo(String task, String[] expectedStrings, String[] expectedPatterns)
            throws IOException {
        startAppWithConfigs("DnaAmpliconVarFastq/gSingleAllTasks.txt", SINGLE_STUDY_CONFIG);

        //tests for toolsets
        String outputShFilePath = "output/sh_files/DnaAmpliconVar_Fastq_" + task + "_for_GA5_analysis.sh";
        File outputShFile = new File(this.getClass().getClassLoader().getResource(outputShFilePath).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            for (String expectedString : expectedStrings) {
                assertTrue(String.format(ERROR_MESSAGE, task, expectedString),
                        lines.stream().anyMatch(line -> line.contains(expectedString)));
            }
            for (String expectedPattern : expectedPatterns) {
                assertTrue(String.format(ERROR_MESSAGE, task, expectedPattern),
                        lines.stream().anyMatch(line -> line.matches(expectedPattern)));
            }
            assertTrue(lines.stream().noneMatch(line -> line.contains(NULL)));
        }

        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @DataProvider
    public static Object[][] controlSampleNAAllTasks() {
        return new Object[][]{
                {"vardict", new String[]{"Begin Step: Vardict detection...", "/VarDict/var2vcf_valid.pl",
                    "/usr/bin/vardict/build/install/VarDict/bin/VarDict -G " + FASTA_FILE,
                    REMOVE_TEMP_DIRS_STEP},
                    new String[]{SNPEFF_PATTERN}},

                {"mutect1", new String[]{
                    "Begin Step: Mutect1 detection...",
                    "/usr/lib/jvm/java-8-openjdk-amd64/bin/java -Xmx10g -jar /usr/bin/mutect",
                    "--analysis_type MuTect --reference_sequence " + FASTA_FILE,
                    "--dbsnp /ngs/data/db/mutect.dbsnp --cosmic /ngs/data/db/cosmic ",
                    "--normal_panel ",
                    "--vcf build/resources/integrationTest/output/GA5/mutect1/GA5.mutect1.variants.vcf"},
                    new String[]{SNPEFF_PATTERN}},

                {"strelka2",
                    new String[]{
                        "Begin Step: Strelka2 detection...",
                        "/usr/bin/python /usr/bin/strelka2/configureStrelkaGermlineWorkflow.py",
                        "--referenceFasta=" + FASTA_FILE,
                        "/usr/bin/python build/resources/integrationTest/output/GA5/strelka2/runWorkflow.py" +
                                " -m local -j 8",
                        "zcat build/resources/integrationTest/output/GA5/strelka2/results/variants/" +
                                "variants.vcf.gz"},
                    new String[]{SNPEFF_PATTERN}},

                {"scalpel",
                    new String[]{
                        "Begin Step: Scalpel detection...",
                        "/usr/bin/scalpel/scalpel-discovery --single --ref " + FASTA_FILE,
                        "--bed /ngs/data/" +
                                "S03723314_Padded.bed"},
                    new String[]{SNPEFF_PATTERN}},

                {"freebayes",
                    new String[]{
                        "Begin Step: Freebayes detection...",
                        "/usr/bin/freebayes -f /ngs/data/reference_genome/hg19/hg19_decoy/" +
                                "hg19.decoy.fa", REMOVE_TEMP_DIRS_STEP},
                    new String[]{ SNPEFF_PATTERN}},

                {"gatkHaplotypeCaller",
                    new String[]{
                        "Begin Step: GATK haplotypecaller detection...",
                        "/usr/lib/jvm/java-8-openjdk-amd64/bin/java -Xmx10g",
                        "-jar /usr/bin/gatk -T HaplotypeCaller",
                        "--input_file build/resources/integrationTest/output/GA5/bam/" +
                                "GA5.merged.sorted.realign.realign.recal.bam",
                        "output/GA5/gatkHaplotypeCaller/GA5.gatkHaplotypeCaller.variants.vcf",
                        "output/GA5/gatkHaplotypeCaller/GA5.gatkHaplotypeCaller.variants.pass.annotation.tsv",
                        REMOVE_TEMP_DIRS_STEP},
                    new String[]{ SNPEFF_PATTERN}},

                {"lofreq",
                    new String[]{
                        "Begin Step: Lofreq detection...",
                        "/usr/bin/lofreq call -f " + FASTA_FILE,
                        "-o build/resources/integrationTest/output/GA5/lofreq/GA5.lofreq.variants.vcf",
                        "-l /ngs/data/" +
                                "S03723314_Padded.bed",
                        "-o build/resources/integrationTest/output/GA5/lofreq/" +
                                "GA5.lofreq.variants.pass.annotation.tsv"},
                    new String[]{SNPEFF_PATTERN}},
        };
    }

    @Test
    @UseDataProvider("controlSampleNotNAAllTasks")
    public void testControlSampleNotNAAllTasksXenomeNo(String task, String[] expectedStrings,
                                                       String[] expectedPatterns) throws IOException {
        startAppWithConfigs(
                "DnaAmpliconVarFastq/gPairedAllTasks.txt", "DnaAmpliconVarFastq/sControlSampleNotNA.txt");

        //tests for toolsets
        String outputShFilePath = "output/sh_files/DnaAmpliconVar_Fastq_" + task + "_for_GA5_analysis.sh";
        File outputShFile = new File(this.getClass().getClassLoader().getResource(outputShFilePath).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            for (String expectedString : expectedStrings) {
                assertTrue(String.format(ERROR_MESSAGE, task, expectedString),
                        lines.stream().anyMatch(line -> line.contains(expectedString)));
            }
            for (String expectedPattern : expectedPatterns) {
                assertTrue(String.format(ERROR_MESSAGE, task, expectedPattern),
                        lines.stream().anyMatch(line -> line.matches(expectedPattern)));
            }

            assertTrue(lines.stream().noneMatch(line -> line.contains(NULL)));
        }

        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @DataProvider
    public static Object[][] controlSampleNotNAAllTasks() {
        return new Object[][]{
                {"vardict",
                    new String[]{
                        "Begin Step: Vardict detection...",
                        "/VarDict/var2vcf_paired.pl",
                        "/usr/bin/vardict/build/install/VarDict/bin/VarDict -G " + FASTA_FILE,
                        REMOVE_TEMP_DIRS_STEP},
                    new String[]{SNPEFF_PATTERN}},

                {"contEst",
                    new String[]{
                        "Begin Step: Contamination estimation...",
                        "/usr/lib/jvm/java-8-openjdk-amd64/bin/java -Xmx10g " +
                                "-Djava.io.tmpdir=build/resources/integrationTest/output/GA5/contEst/tmp ",
                        "-pf 100 -pc 0.01 -o build/resources/integrationTest/output/GA5/contEst/" +
                                "GA5.contEst.result",
                        "--min_mapq 20 -U ALLOW_SEQ_DICT_INCOMPATIBILITY"},
                    new String[]{""}},

                {"exomecnv",
                    new String[]{
                        "Begin Step: ExomeCNV detection...",
                        "/usr/lib/jvm/java-8-openjdk-amd64/bin/java -Xmx10g -jar /usr/bin/gatk -T " +
                                "DepthOfCoverage",
                        "-I build/resources/integrationTest/output/N/bam/" +
                                "N.merged.sorted.realign.realign.recal.bam",
                        "-I build/resources/integrationTest/output/GA5/bam/" +
                                "GA5.merged.sorted.realign.realign.recal.bam",
                        "/usr/bin/Rscript /usr/bin/exomecnv/exome_cnv.R"},
                    new String[]{""}},

                {"mutect2",
                    new String[]{
                        "Begin Step: Mutect2 detection...",
                        "/usr/lib/jvm/java-8-openjdk-amd64/bin/java -Xmx10g -Djava.io.tmpdir=",
                        "-jar /usr/bin/gatk -T MuTect2",
                        "build/resources/integrationTest/output/GA5/mutect2/GA5.mutect2.somatic.variants.vcf",
                        REMOVE_TEMP_DIRS_STEP,
                        "--intervals"},
                    new String[]{SNPEFF_PATTERN}},

                {"sequenza",
                    new String[]{
                        "Begin Step: bam pileup...",
                        "/opt/samtools/samtools-0.1.19/samtools mpileup -q 10 -B -d 100000",
                        "Begin Step: Sequenza detection...",
                        "/usr/bin/python /usr/bin/sequenza/sequenza-utils.py pileup2seqz -gc 100",
                        "/usr/bin/python /usr/bin/sequenza/sequenza-utils.py seqz-binning -w 50",
                        "/usr/bin/Rscript /usr/bin/sequenza/sequenza.R -i",
                        "build/resources/integrationTest/output/N/bam/" +
                                "N.merged.sorted.realign.realign.recal.bam",
                        "build/resources/integrationTest/output/GA5/bam/" +
                                "GA5.merged.sorted.realign.realign.recal.bam"},
                    new String[]{""}},

                {"strelka2",
                    new String[]{
                        "Begin Step: Strelka2 detection...",
                        "/usr/bin/python /usr/bin/strelka2/configureStrelkaSomaticWorkflow.py",
                        "--referenceFasta=/ngs/data/reference_genome/hg19/hg19_decoy/" +
                                "hg19.decoy.fa",
                        "/usr/bin/python build/resources/integrationTest/output/GA5/strelka2/runWorkflow.py" +
                                " -m local -j 8",
                        "gunzip -c build/resources/integrationTest/output/GA5/strelka2/results/variants/" +
                                "somatic.snvs.vcf.gz",
                        "rm build/resources/integrationTest/output/GA5/strelka2/results/variants/" +
                                "somatic.snvs.vcf",
                        "rm build/resources/integrationTest/output/GA5/strelka2/results/variants/" +
                                "somatic.indels.vcf",
                        "gunzip -c build/resources/integrationTest/output/GA5/strelka2/results/variants/" +
                                "somatic.indels.vcf.gz"},
                    new String[]{SNPEFF_PATTERN}},

                {"scalpel",
                    new String[]{
                        "Begin Step: Scalpel detection...",
                        "/usr/bin/scalpel/scalpel-discovery --somatic --ref " + FASTA_FILE,
                        "/usr/bin/scalpel/scalpel-export --somatic --db",
                        "/GA5.scalpel.somatic.variants.vcf",
                        "--bed /ngs/data/" +
                                "S03723314_Padded.bed"},
                    new String[]{SNPEFF_PATTERN}},

                {"lofreq",
                    new String[]{
                        "Begin Step: Lofreq detection...",
                        "/usr/bin/lofreq somatic -f " + FASTA_FILE,
                        "gunzip -c build/resources/integrationTest/output/GA5/lofreq/" +
                                "GA5.somatic_final.snvs.vcf.gz",
                        "gunzip -c build/resources/integrationTest/output/GA5/lofreq/" +
                                "GA5.somatic_final.indels.vcf.gz",
                        "grep -v \"^#\" build/resources/integrationTest/output/GA5/lofreq/" +
                                "GA5.somatic_final.indels.vcf"},
                    new String[]{SNPEFF_PATTERN}},
        };
    }

    private void assertBwa(List<String> lines) {
        assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: BWA alignment...")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("/usr/bin/bwa mem -t 4 " + FASTA_FILE)));
    }

    private void assertXenome(List<String> lines) {
        assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step:  Xenome classification...")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("/usr/bin/xenome classify -T 8 -P ")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("/ngs/data/xenomeIdx/xenome.idx " +
                "--pairs --graft-name human --host-name mouse ")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("/ngs/data/xenomeIdx/xenome.idx " +
                "--pairs --graft-name human --host-name mouse ")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("--output-filename-prefix" +
                " build/resources/integrationTest/output/GA5/tmp/GA5_1 ")));
        assertTrue(lines.stream()
                .anyMatch(line -> line.contains("--tmp-dir build/resources/integrationTest/output/GA5/tmp -i ")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("/ngs/data/demo/test/fastq_data/" +
                "GA5_0001_L002_R1_001.fastq.gz")));
    }

    private void assertCommonTrimmomatic(List<String> lines) {
        assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: Trimmomatic trimming...")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("rm -rf " +
                "build/resources/integrationTest/output/GA5/fastq/GA5_1.trimmed_unpaired.R1.fq.gz")));
    }

    private void assertNovoalign(List<String> lines) {
        assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: Novoalign alignment...")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("/usr/bin/novoalign -c 4 -d " +
                "/ngs/data/novoindexDB/novoindex.nix -o SAM")));
    }

    private void assertSequpurge(List<String> lines) {
        assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: Seqpurge trimming...")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("/opt/ngs_bits/ngs-bits/bin/SeqPurge -threads 4 " +
                "-in1 build/resources/integrationTest/output/GA5/fastq/GA5_1_classified_R1.fq.gz")));
        assertTrue(lines.stream().anyMatch(line -> line.contains(
                "-in2 build/resources/integrationTest/output/GA5/fastq/GA5_1_classified_R2.fq.gz")));
        assertTrue(lines.stream().anyMatch(line -> line.contains(
                "-a2 AGATCGGAAGAGCGTCGTGTAGGGAAAGAGTGTAGATCTCGGTGGTCGCCGTATCATT")));
        assertTrue(lines.stream().noneMatch(line -> line.contains(NULL)));
    }

    private void assertPostAlignment() throws IOException {
        String outputShFilePath;
        File outputShFile;
        //test for qcSummary workflow in case of DnaAmpliconVarFastq
        outputShFilePath = "output/sh_files/DnaAmpliconVar_Fastq_qcsummary_for_cohort_analysis.sh";
        outputShFile = new File(this.getClass().getClassLoader().getResource(outputShFilePath).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            commonQcSummary(lines);
        }

        // test for single readType
        outputShFilePath = POST_ALIGNMENT_SH_FILE;
        outputShFile = new File(this.getClass().getClassLoader().getResource(outputShFilePath).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            commonPostalignment(lines);
        }

        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    private void commonQcSummary(List<String> lines) {
        assertTrue(lines.stream().anyMatch(line -> line.contains("(Successful Step: Merge DNA QC)")));
        assertTrue(lines.stream().anyMatch(line -> line.contains(
                "build/resources/integrationTest/output/log_files/" +
                        "DnaAmpliconVar_Fastq_postalignment_for_GA5_analysis.log")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("Confirm QC results from GA5")));
        assertTrue(lines.stream().anyMatch(line -> line.matches(
                ".*?\\/usr\\/bin\\/Rscript.+?QC_summary_analysis.R.+?(\\n|$)")));
        assertTrue(lines.stream().noneMatch(line -> line.contains(NULL)));
    }

    private void commonPostalignment(List<String> lines) {
        assertTrue(lines.stream().anyMatch(line -> line.contains(
                "build/resources/integrationTest/output/log_files/" +
                        "DnaAmpliconVar_Fastq_alignment_for_GA5_1_analysis.log")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: Merge DNA bams...")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("/usr/lib/jvm/java-8-openjdk-amd64/bin/java -Xmx10g " +
                "-jar /opt/picard/picard.jar MergeSamFiles" +
                " O=build/resources/integrationTest/output/GA5/bam/GA5.merged.sorted.bam")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: Index bam...")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("/opt/samtools/samtools-0.1.19/samtools index " +
                "build/resources/integrationTest/output/GA5/bam/GA5.merged.sorted.bam")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("Successful Step: Index bam")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("build/resources/integrationTest/output/log_files/" +
                "DnaAmpliconVar_Fastq_postalignment_for_GA5_analysis.log")));
        assertTrue(lines.stream().anyMatch(line -> line.contains(REMOVE_TEMP_DIRS_STEP)));
        assertTrue(lines.stream().noneMatch(line -> line.contains(NULL)));
    }
}
