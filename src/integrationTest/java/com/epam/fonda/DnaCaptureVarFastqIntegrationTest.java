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

import com.epam.fonda.workflow.TaskContainer;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.After;
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
public class DnaCaptureVarFastqIntegrationTest extends AbstractIntegrationTest {
    private static final String OUTPUT_DIR = "output";
    private static final String TEST_REFERENCE =
            "/ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa";
    private static final String TEST_OUTPUT = "build/resources/integrationTest/output/";
    private static final String SINGLE_STUDY_CONFIG = "DnaCaptureVarFastq/sSingle.txt";
    private static final String NULL = "null";
    private static final String ERROR_MESSAGE = "An error occurred with task %s. Expected: %s";
    private static final String TEST_POSTALIGNMENT_LOG =
            "log_files/DnaCaptureVar_Fastq_postalignment_for_GA5_analysis.log";
    private static final String TEST_POSTALIGNMENT_SH =
            "output/sh_files/DnaCaptureVar_Fastq_postalignment_for_GA5_analysis.sh";
    private static final String TEST_MERGED_BAM = "GA5/bam/GA5.merged.sorted.bam";
    private static final String REMOVE_TMP_DIRS = "Begin Step: Remove temporary directories...";
    private static final String VCF_SNPEFF_ANNOTATION = ".*?/usr/bin/python.+?vcf_snpeff_annotation.py.+?(\\n|$)";
    private static final String GUNZIP = "gunzip -c ";
    private static final String TEST_BED = "/ngs/data/test.bed";
    private static final String MERGE_DNA_QC = "Successful Step: Merge DNA QC";
    private static final String REMOVE_TEMP_DIRS = "Successful Step: Remove temporary directories";

    @After
    public void cleanup() {
        TaskContainer.getTasks().clear();
    }

    @Test
    public void testSingleXenomeYesTrimmomaticBwa() throws IOException {
        startAppWithConfigs(
                "DnaCaptureVarFastq/gSingleTrimmomaticBwaYes.txt",
                SINGLE_STUDY_CONFIG);

        //test for trimmomatic, xenome and bwa toolset
        String outputShFile = "output/sh_files/DnaCaptureVar_Fastq_alignment_for_GA5_1_analysis.sh";
        File outputShFromFile = new File(this.getClass().getClassLoader().getResource(outputShFile).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFromFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: Xenome classification...")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("/usr/bin/xenome classify -T 8 -P " +
                    "/ngs/data/xenomeIdx/xenome.idx --pairs --graft-name human --host-name mouse " +
                    "--output-filename-prefix " +  TEST_OUTPUT + "GA5/tmp/GA5_1 --tmp-dir " +
                    TEST_OUTPUT + "GA5/tmp -i " +
                    "/ngs/data/demo/test/fastq_data/GA5_0001_L002_R1_001.fastq.gz")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: Trimmomatic trimming...")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("/usr/lib/jvm/java-8-openjdk-amd64/bin/java" +
                    " -jar /usr/bin/trimmomatic PE -threads 4 -phred33 " +
                    TEST_OUTPUT + "GA5/fastq/GA5_1_classified_R1.fq.gz")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("rm -rf " +
                    TEST_OUTPUT + "GA5/fastq/GA5_1.trimmed_unpaired.R1.fq.gz")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: BWA alignment...")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("/usr/bin/bwa mem -t 4 " +
                    TEST_REFERENCE)));
            assertTrue(lines.stream().noneMatch(line -> line.contains(NULL)));
        }

        //test for qcSummary workflow in case of DnaCaptureVarFastq
        outputShFile = "output/sh_files/DnaCaptureVar_Fastq_qcsummary_for_cohort_analysis.sh";
        outputShFromFile = new File(this.getClass().getClassLoader().getResource(outputShFile).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFromFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertTrue(lines.stream().anyMatch(line -> line.contains(REMOVE_TEMP_DIRS)));
            assertTrue(lines.stream().anyMatch(line -> line.contains(TEST_OUTPUT + TEST_POSTALIGNMENT_LOG)));
            assertTrue(lines.stream().anyMatch(line -> line.contains("Confirm QC results from GA5")));
            assertTrue(lines.stream().anyMatch(line -> line.matches(
                    ".*?\\/usr\\/bin\\/Rscript.+?QC_summary_analysis.R.+?(\\n|$)")));
            assertTrue(lines.stream().noneMatch(line -> line.contains(NULL)));
        }

        // test for single readType
        outputShFromFile = new File(this.getClass().getClassLoader().getResource(TEST_POSTALIGNMENT_SH).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFromFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertTrue(lines.stream().anyMatch(line -> line.contains("Successful Step: Index bam")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(TEST_OUTPUT +
                    "log_files/DnaCaptureVar_Fastq_alignment_for_GA5_1_analysis.log")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: Merge DNA bams...")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("/usr/lib/jvm/java-8-openjdk-amd64/bin/java" +
                    " -Xmx10g -jar /opt/picard/picard.jar MergeSamFiles " +
                    "O=" + TEST_OUTPUT + TEST_MERGED_BAM)));
            assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: Index bam...")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("/opt/samtools/samtools-0.1.19/samtools index " +
                    TEST_OUTPUT + TEST_MERGED_BAM)));
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    "Successful Step: Index rmdup ")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(TEST_OUTPUT + TEST_POSTALIGNMENT_LOG)));
            assertTrue(lines.stream().anyMatch(line -> line.contains(REMOVE_TMP_DIRS)));
            assertTrue(lines.stream().noneMatch(line -> line.contains(NULL)));
        }

        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @Test
    public void testPairedXenomeYesNovoalignSeqpurge() throws IOException {
        startAppWithConfigs(
                "DnaCaptureVarFastq/gPairedNovoalignYesSeq.txt",
                "DnaCaptureVarFastq/sPaired.txt");

        //test for xenome, seqpurge and novoalign toolset
        String outputShFile = "output/sh_files/DnaCaptureVar_Fastq_alignment_for_GA5_1_analysis.sh";
        File outputShFromFile = new File(this.getClass().getClassLoader().getResource(outputShFile).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFromFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: Xenome classification...")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("/usr/bin/xenome classify -T 8 -P " +
                    "/ngs/data/xenomeIdx/xenome.idx --pairs --graft-name human --host-name mouse " +
                    "--output-filename-prefix " + TEST_OUTPUT + "GA5/tmp/GA5_1 --tmp-dir " +
                    TEST_OUTPUT + "GA5/tmp -i " +
                    "/ngs/data/demo/test/fastq_data/GA5_0001_L002_R1_001.fastq.gz")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: Novoalign alignment...")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("-f " + TEST_OUTPUT +
                    "GA5/fastq/GA5_1.trimmed.R1.fastq.gz " + TEST_OUTPUT +
                    "GA5/fastq/GA5_1.trimmed.R2.fastq.gz")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("/usr/bin/novoalign -c 4 -d " +
                    "/ngs/data/novoindexDB/novoindex.nix -o SAM")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: Seqpurge trimming...")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("/opt/ngs_bits/ngs-bits/bin/SeqPurge -threads 4 " +
                    "-in1 " + TEST_OUTPUT + "GA5/fastq/GA5_1_classified_R1.fq.gz")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("-in2 " + TEST_OUTPUT +
                    "GA5/fastq/GA5_1_classified_R2.fq.gz")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("-a2 " +
                    "AGATCGGAAGAGCGTCGTGTAGGGAAAGAGTGTAGATCTCGGTGGTCGCCGTATCATT")));
            assertTrue(lines.stream().noneMatch(line -> line.contains(NULL)));
        }

        //test for qcSummary workflow in case of DnaCaptureVarFastq
        outputShFile = "output/sh_files/DnaCaptureVar_Fastq_qcsummary_for_cohort_analysis.sh";
        outputShFromFile = new File(this.getClass().getClassLoader().getResource(outputShFile).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFromFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertTrue(lines.stream().anyMatch(line -> line.contains(REMOVE_TEMP_DIRS)));
            assertTrue(lines.stream().anyMatch(line -> line.contains(TEST_OUTPUT + TEST_POSTALIGNMENT_LOG)));
            assertTrue(lines.stream().anyMatch(line -> line.contains("Confirm QC results from GA5")));
            assertTrue(lines.stream().anyMatch(line -> line.matches(
                    ".*?\\/usr\\/bin\\/Rscript.+?QC_summary_analysis.R.+?(\\n|$)")));
            assertTrue(lines.stream().noneMatch(line -> line.contains(NULL)));
        }

        // test for paired readType
        outputShFromFile = new File(this.getClass().getClassLoader().getResource(TEST_POSTALIGNMENT_SH).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFromFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertTrue(lines.stream().anyMatch(line -> line.contains("echo `date` Begin check the existence of " +
                    "the individual sorted bam file...")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(MERGE_DNA_QC)));
            assertTrue(lines.stream().anyMatch(line -> line.contains(TEST_OUTPUT +
                    "log_files/DnaCaptureVar_Fastq_alignment_for_GA5_1_analysis.log")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: Merge DNA bams...")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("/usr/lib/jvm/java-8-openjdk-amd64/bin/java" +
                    " -Xmx10g -jar /opt/picard/picard.jar MergeSamFiles " +
                    "O=" + TEST_OUTPUT + TEST_MERGED_BAM)));
            assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: Index bam...")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("/opt/samtools/samtools-0.1.19/samtools index " +
                    TEST_OUTPUT + TEST_MERGED_BAM)));
            assertTrue(lines.stream().anyMatch(line -> line.contains(MERGE_DNA_QC)));
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    TEST_OUTPUT + TEST_POSTALIGNMENT_LOG)));
            assertTrue(lines.stream().anyMatch(line -> line.contains(REMOVE_TMP_DIRS)));
            assertTrue(lines.stream().noneMatch(line -> line.contains(NULL)));
        }

        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @Test
    public void testPicardLibraryTypeExome() throws IOException {
        startAppWithConfigs(
                "DnaCaptureVarFastq/gSingleSimplePicard.txt",
                SINGLE_STUDY_CONFIG);

        //test for exome library type
        File outputShFromFile = new File(this.getClass().getClassLoader().getResource(TEST_POSTALIGNMENT_SH).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFromFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: DNA QC metrics...")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("CollectAlignmentSummaryMetrics")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("CollectHsMetrics")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("CollectGcBiasMetrics")));
            assertTrue(lines.stream().anyMatch(line ->
                    line.contains("INPUT=" + TEST_OUTPUT + "GA5/bam/GA5.merged.sorted.mkdup.bam")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("CollectInsertSizeMetrics")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("CollectQualityYieldMetrics")));
            assertTrue(lines.stream().anyMatch(line ->
                    line.contains("/opt/samtools/samtools-0.1.19/samtools mpileup")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: Merge DNA QC...")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("--type wes --project Example_project")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("dna_rna_variant_qc_metrics.py")));
            assertTrue(lines.stream().noneMatch(line -> line.contains(NULL)));
        }

        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @Test
    public void testPicardLibraryTypeTarget() throws IOException {
        startAppWithConfigs(
                "DnaCaptureVarFastq/gSingleSimplePicard.txt",
                "DnaCaptureVarFastq/sSingleTarget.txt");

        //test for exome library type
        File outputShFromFile = new File(this.getClass().getClassLoader().getResource(TEST_POSTALIGNMENT_SH).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFromFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: DNA QC metrics...")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("CollectAlignmentSummaryMetrics")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("CollectHsMetrics")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("CollectGcBiasMetrics")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    "INPUT=" + TEST_OUTPUT + "GA5/bam/GA5.merged.sorted.mkdup.bam")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("CollectInsertSizeMetrics")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("CollectQualityYieldMetrics")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: Merge DNA QC...")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("--bedcov " +
                    TEST_OUTPUT + "GA5/qc/GA5.merged.sorted.rmdup.coverage.per.base.txt")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("/opt/samtools/samtools-0.1.19/samtools " +
                    "mpileup")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("/usr/bin/bedtools coverage -abam " +
                    TEST_OUTPUT + "GA5/bam/GA5.merged.sorted.rmdup.bam")));
            assertTrue(lines.stream().noneMatch(line -> line.contains(NULL)));
        }

        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @Test
    public void testPicardLibraryTypeNotExomeOrTarget() throws IOException {
        startAppWithConfigs(
                "DnaCaptureVarFastq/gSingleSimplePicard.txt",
                "DnaCaptureVarFastq/sSingleNotExomeOrTarget.txt");

        //test for exome library type
        File outputShFromFile = new File(this.getClass().getClassLoader().getResource(TEST_POSTALIGNMENT_SH).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFromFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertTrue(lines.stream().noneMatch(line -> line.contains("Begin Step: DNA QC metrics...")));
            assertTrue(lines.stream().noneMatch(line -> line.contains("Begin Step: Merge DNA QC...")));
            assertTrue(lines.stream().noneMatch(line -> line.contains(NULL)));
        }

        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @Test
    public void testPicardAbraGatkSingle() throws IOException {
        startAppWithConfigs(
                "DnaCaptureVarFastq/gAbraGatkPicardSingle.txt",
                SINGLE_STUDY_CONFIG);

        //test for toolset picard + gatk_realign + abra+realign
        File outputShFromFile = new File(this.getClass().getClassLoader().getResource(TEST_POSTALIGNMENT_SH).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFromFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: ABRA realignment...")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("/usr/lib/jvm/java-8-openjdk-amd64/bin/java " +
                    "-Xmx16g -jar /usr/bin/abra2")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("--single --tmpdir " +
                    TEST_OUTPUT + "GA5/tmp")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("/opt/samtools/samtools-0.1.19/samtools index " +
                    TEST_OUTPUT + "GA5/bam/GA5.merged.sorted.rmdup.realign.bam")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: GATK realignment...")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("-jar /usr/bin/gatk -T RealignerTargetCreator")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("-jar /usr/bin/gatk -T IndelRealigner")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: GATK recalibration..")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("-jar /usr/bin/gatk -T BaseRecalibrator -R")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("-jar /usr/bin/gatk -T PrintReads -R")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("-knownSites 10 -knownSites 100 ")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: Mark duplicates...")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("/usr/lib/jvm/java-8-openjdk-amd64/bin/java " +
                    "-Xmx16g -jar /opt/picard/picard.jar MarkDuplicates")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: Index mkdup bam...")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: Index rmdup bam...")));
            assertTrue(lines.stream().noneMatch(line -> line.contains(NULL)));
        }

        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @Test
    public void testPicardAbraGatkPaired() throws IOException {
        startAppWithConfigs(
                "DnaCaptureVarFastq/gAbraGatkPicardPaired.txt",
                "DnaCaptureVarFastq/sPaired.txt");

        //test for toolset picard + gatk_realign + abra+realign
        File outputShFromFile = new File(this.getClass().getClassLoader().getResource(TEST_POSTALIGNMENT_SH).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFromFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertTrue(lines.stream().anyMatch(line -> line.contains("--insert " +
                    TEST_OUTPUT + "GA5/qc/GA5.merged.sorted.mkdup.insertsize.metrics")));
            assertTrue(lines.stream().noneMatch(line -> line.contains("--single")));
            assertTrue(lines.stream().noneMatch(line -> line.contains(NULL)));
        }

        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @Test
    @UseDataProvider("controlSampleNAAllTasks")
    public void testControlSampleNAAllTasks(String task, String[] expectedStrings,
                                            String[] expectedPatterns) throws IOException {
        startAppWithConfigs(
                "DnaCaptureVarFastq/gSingleAllTasks.txt",
                SINGLE_STUDY_CONFIG);

        //tests for toolsets
        String outputShFile = "output/sh_files/DnaCaptureVar_Fastq_" + task + "_for_GA5_analysis.sh";
        File outputShFromFile = new File(this.getClass().getClassLoader().getResource(outputShFile).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFromFile))) {
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
            {"vardict",
                new String[]{
                    "Begin Step: Vardict detection...",
                    "/usr/bin/vardict/VarDict/var2vcf_valid.pl",
                    "/usr/bin/vardict/build/install/VarDict/bin/VarDict -G " + TEST_REFERENCE,
                    REMOVE_TMP_DIRS},
                new String[]{VCF_SNPEFF_ANNOTATION}},

            {"mutect1", new String[]{
                "Begin Step: Mutect1 detection...",
                "/usr/lib/jvm/java-8-openjdk-amd64/bin/java -Xmx10g -jar /usr/bin/mutect",
                "--analysis_type MuTect --reference_sequence " + TEST_REFERENCE,
                "--dbsnp /ngs/data/db/mutect.dbsnp --cosmic /ngs/data/db/cosmic --normal_panel",
                "--vcf " + TEST_OUTPUT + "GA5/mutect1/GA5.mutect1.variants.vcf"},
                new String[]{VCF_SNPEFF_ANNOTATION}},

            {"strelka2", new String[]{
                "Begin Step: Strelka2 detection...",
                "/usr/bin/python /usr/bin/strelka2/configureStrelkaGermlineWorkflow.py",
                "--referenceFasta=" + TEST_REFERENCE,
                "/usr/bin/python " + TEST_OUTPUT + "GA5/strelka2/runWorkflow.py -m local -j 8",
                "zcat " + TEST_OUTPUT + "GA5/strelka2/results/variants/variants.vcf.gz"},
                new String[]{VCF_SNPEFF_ANNOTATION}},

            {"scalpel",
                new String[]{
                    "Begin Step: Scalpel detection...",
                    "/usr/bin/scalpel/scalpel-discovery --single --ref " + TEST_REFERENCE,
                    "--bed " + TEST_BED},
                new String[]{VCF_SNPEFF_ANNOTATION}},

            {"freebayes",
                new String[]{
                    "Begin Step: Freebayes detection...",
                    "/usr/bin/freebayes -f " + TEST_REFERENCE,
                    REMOVE_TMP_DIRS},
                new String[]{VCF_SNPEFF_ANNOTATION}},

            {"gatkHaplotypeCaller",
                new String[]{
                    "Begin Step: GATK haplotypecaller detection...",
                    "/usr/lib/jvm/java-8-openjdk-amd64/bin/java -Xmx10g",
                    "-jar /usr/bin/gatk -T HaplotypeCaller",
                    "--input_file " + TEST_OUTPUT + "GA5/bam/GA5.merged.sorted.rmdup." +
                            "realign.realign.recal.bam",
                    "output/GA5/gatkHaplotypeCaller/GA5.gatkHaplotypeCaller.variants.vcf",
                    "output/GA5/gatkHaplotypeCaller/GA5.gatkHaplotypeCaller.variants.pass.annotation.tsv",
                    REMOVE_TMP_DIRS},
                new String[]{VCF_SNPEFF_ANNOTATION}},

            {"lofreq",
                new String[]{
                    "Begin Step: Lofreq detection...",
                    "/usr/bin/lofreq call -f " + TEST_REFERENCE,
                    "-o " + TEST_OUTPUT + "GA5/lofreq/GA5.lofreq.variants.vcf",
                    "-l " + TEST_BED,
                    "-o " + TEST_OUTPUT + "GA5/lofreq/GA5.lofreq.variants.pass.annotation.tsv"},
                new String[]{VCF_SNPEFF_ANNOTATION}},
        };
    }

    @Test
    @UseDataProvider("controlSampleNotNAAllTasks")
    public void testControlSampleNotNAAllTasks(String task, String[] expectedStrings,
                                               String[] expectedPatterns) throws IOException {
        startAppWithConfigs(
                "DnaCaptureVarFastq/gPairedAllTasks.txt",
                "DnaCaptureVarFastq/sControlSampleNotNA.txt");

        //tests for toolsets
        String outputShFile = "output/sh_files/DnaCaptureVar_Fastq_" + task + "_for_GA5_analysis.sh";
        File outputShFromFile = new File(this.getClass().getClassLoader().getResource(outputShFile).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFromFile))) {
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
                    "/usr/bin/vardict/VarDict/var2vcf_paired.pl",
                    "/usr/bin/vardict/build/install/VarDict/bin/VarDict -G " + TEST_REFERENCE,
                    REMOVE_TMP_DIRS},
                new String[]{VCF_SNPEFF_ANNOTATION}},

            {"contEst",
                new String[]{
                    "Begin Step: Contamination estimation...",
                    "/usr/lib/jvm/java-8-openjdk-amd64/bin/java -Xmx10g " +
                            "-Djava.io.tmpdir=" + TEST_OUTPUT + "GA5/contEst/tmp ",
                    "-pf 100 -pc 0.01 -o " + TEST_OUTPUT + "GA5/contEst/GA5.contEst.result",
                    "--min_mapq 20 -U ALLOW_SEQ_DICT_INCOMPATIBILITY"},
                new String[]{""}},

            {"exomecnv",
                new String[]{
                    "Begin Step: ExomeCNV detection...",
                    "/usr/lib/jvm/java-8-openjdk-amd64/bin/java -Xmx10g -jar /usr/bin/gatk " +
                            "-T DepthOfCoverage",
                    "-I " + TEST_OUTPUT + "N/bam/N.merged.sorted.rmdup.realign." +
                            "realign.recal.bam",
                    "-I " + TEST_OUTPUT + "GA5/bam/" +
                            "GA5.merged.sorted.rmdup.realign.realign.recal.bam",
                    "/usr/bin/Rscript /usr/bin/exomecnv/exome_cnv.R"},
                new String[]{""}},

            {"mutect2",
                new String[]{
                    "Begin Step: Mutect2 detection...",
                    "/usr/lib/jvm/java-8-openjdk-amd64/bin/java -Xmx10g -Djava.io.tmpdir=",
                    "-jar /usr/bin/gatk -T MuTect2", TEST_OUTPUT + "GA5/mutect2/GA5.mutect2.somatic.variants.vcf",
                    REMOVE_TMP_DIRS,
                    "--intervals"},
                new String[]{VCF_SNPEFF_ANNOTATION}},

            {"sequenza",
                new String[]{
                    "Begin Step: bam pileup...",
                    "/opt/samtools/samtools-0.1.19/samtools mpileup -q 10 -B -d 100000",
                    "Begin Step: Sequenza detection...",
                    "/usr/bin/python /usr/bin/sequenza/sequenza-utils.py pileup2seqz -gc 100",
                    "/usr/bin/python /usr/bin/sequenza/sequenza-utils.py seqz-binning -w 50",
                    "/usr/bin/Rscript /usr/bin/sequenza/sequenza.R -i", TEST_OUTPUT + "GA5/pileup/N.pileup.gz",
                    TEST_OUTPUT + "GA5/sequenza/GA5.seqz.gz"},
                new String[]{""}},

            {"strelka2",
                new String[]{
                    "Begin Step: Strelka2 detection...",
                    "/usr/bin/python /usr/bin/strelka2/configureStrelkaSomaticWorkflow.py",
                    "--referenceFasta=" + TEST_REFERENCE,
                    "/usr/bin/python " + TEST_OUTPUT + "GA5/strelka2/runWorkflow.py -m local -j 8",
                    GUNZIP + TEST_OUTPUT + "GA5/strelka2/results/" +
                            "variants/somatic.snvs.vcf.gz",
                    "rm " + TEST_OUTPUT + "GA5/strelka2/results/variants/somatic.snvs.vcf",
                    "rm " + TEST_OUTPUT + "GA5/strelka2/results/" +
                            "variants/somatic.indels.vcf",
                    GUNZIP + TEST_OUTPUT + "GA5/strelka2/results/" +
                            "variants/somatic.indels.vcf.gz"},
                new String[]{VCF_SNPEFF_ANNOTATION}},

            {"scalpel",
                new String[]{
                    "Begin Step: Scalpel detection...",
                    "/usr/bin/scalpel/scalpel-discovery --somatic --ref " + TEST_REFERENCE,
                    "/usr/bin/scalpel/scalpel-export --somatic --db",
                    "/GA5.scalpel.somatic.variants.vcf",
                    "--bed " + TEST_BED},
                new String[]{VCF_SNPEFF_ANNOTATION}},

            {"lofreq",
                new String[]{
                    "Begin Step: Lofreq detection...",
                    "/usr/bin/lofreq somatic -f " + TEST_REFERENCE,
                    GUNZIP + TEST_OUTPUT + "GA5/lofreq/GA5.somatic_final.snvs.vcf.gz",
                    GUNZIP + TEST_OUTPUT + "GA5/lofreq/GA5.somatic_final.indels.vcf.gz",
                    "grep -v \"^#\" " + TEST_OUTPUT + "GA5/lofreq/GA5.somatic_final.indels.vcf"},
                new String[]{VCF_SNPEFF_ANNOTATION}},
        };
    }
}
