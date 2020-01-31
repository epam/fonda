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
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class RnaExpressionFastqIntegrationTest extends AbstractIntegrationTest {

    private static final String OUTPUT_DIR = "output";
    private static final String RNA_EXPRESSION_FASTQ_TEMPLATES_SUFFIX = "templates/RnaExpressionFastq/";
    private static final String OUTPUT_SH_FILE = "output/sh_files/RnaExpression_Fastq_alignment_for_smv1_analysis.sh";
    private static final String S_CONFIG_PATH = "RnaExpressionFastq/sRnaExpressionFastq.txt";
    private static final String TEST_FASTQ = "zcat /ngs/data/demo/test/fastq";
    private static final String RNA_EXPRESSION_FASTQ_ALIGNMENT_FOR_SMV1_ANALYSIS_TEMPLATE_PATH =
            "rnaExpression_Fastq_alignment_flag_Xenome_yes_template";
    private TemplateEngine templateEngine = TemplateEngineUtils.init(RNA_EXPRESSION_FASTQ_TEMPLATES_SUFFIX);
    private Context context = new Context();

    @Test
    public void testFlagXenomeYes() throws IOException, URISyntaxException {
        startAppWithConfigs("RnaExpressionFastq/gFlagXenomeYes.txt", S_CONFIG_PATH);
        String expectedCmd = templateEngine.process(RNA_EXPRESSION_FASTQ_ALIGNMENT_FOR_SMV1_ANALYSIS_TEMPLATE_PATH, context);
        assertEquals(expectedCmd, getCmd(OUTPUT_SH_FILE));
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initParameters() {
        return Stream.of(
                Arguments.of("RnaExpressionFastq/gSeqpurgeWithAdapters.txt", "rnaExpression_Fastq_Seqpurge_with_Adapters.txt"),
                Arguments.of("RnaExpressionFastq/gSeqpurgeWithoutAdapters.txt", "rnaExpression_Fastq_Seqpurge_without_Adapters.txt")
        );
    }

    @ParameterizedTest(name = "{1}-test")
    @MethodSource("initParameters")
    void testSeqpurge(String gConfigPath, String templatePath) throws IOException, URISyntaxException {
        startAppWithConfigs(gConfigPath, S_CONFIG_PATH);
        String expectedCmd = templateEngine.process(templatePath, context);
        assertEquals(expectedCmd.trim(), getCmd(OUTPUT_SH_FILE).trim());
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @DataProvider
    public static Object[][] getTrimmomaticConfigAndStrings() {
        return new Object[][] {
                { "RnaExpressionFastq/gTrimmomaticWithAdapter.txt", new String[] {
                    TEST_FASTQ,
                    "/smv1_GTGTTCTA_L007_R2_001.fastq.gz | " +
                            "gzip -c > build/resources/integrationTest/output/smv1/fastq/smv1.merged_R2.fastq.gz",
                    "/usr/lib/jvm/java-8-openjdk-amd64/bin/java -jar trimmomatic PE -threads 4 -phred33 ",
                    "build/resources/integrationTest/output/smv1/fastq/smv1.merged_R1.fastq.gz " +
                            "build/resources/integrationTest/output/smv1/fastq/smv1.merged_R2.fastq.gz " +
                            "build/resources/integrationTest/output/smv1/fastq/smv1.trimmed.R1.fastq.gz " +
                            "build/resources/integrationTest/output/smv1/fastq/smv1.trimmed_unpaired.R1.fq.gz " +
                            "build/resources/integratisonTest/output/smv1/fastq/smv1.trimmed.R2.fastq.gz " +
                            "build/resources/integrationTest/output/smv1/fastq/smv1.trimmed_unpaired.R2.fq.gz ",
                    "ILLUMINACLIP:adapter_seq:2:30:10 LEADING:20 TRAILING:20 SLIDINGWINDOW:4:15 MINLEN:36",
                    "rm -rf build/resources/integrationTest/output/smv1/fastq/smv1.trimmed_unpaired.R1.fq.gz",
                }},
                { "RnaExpressionFastq/gTrimmomaticWithoutAdapter.txt", new String[] {
                    TEST_FASTQ,
                    "/smv1_GTGTTCTA_L007_R2_001.fastq.gz | " +
                            "gzip -c > build/resources/integrationTest/output/smv1/fastq/smv1.merged_R2.fastq.gz",
                    "echo `date` the adapter sequence trimming step was skipped since " +
                            "no adapter sequences were provided in the config files.",
                    "rm -rf build/resources/integrationTest/output/smv1/tmp"
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
    public static Object[][] getStarWithRsemConfigAndStrings() {
        return new Object[][] {
                { OUTPUT_SH_FILE, new String[] {
                    TEST_FASTQ + "/smv1_GTGTTCTA_L004_R1_001.fastq.gz ",
                    "path/to/star --genomeDir /common/reference_genome/GRCh38/Index/STAR_g26 " +
                            "--sjdbGTFfile /common/reference_genome/GRCh38/Annotation/Gencode_v26/" +
                            "gencode.v26.annotation.gtf " +
                            "--sjdbOverhang 100 --genomeLoad NoSharedMemory --readFilesIn " +
                            "build/resources/integrationTest/output/smv1/fastq/smv1.merged_R1.fastq.gz " +
                            "build/resources/integrationTest/output/smv1/fastq/smv1.merged_R2.fastq.gz " +
                            "--outFileNamePrefix build/resources/integrationTest/output/smv1/bam/smv1. " +
                            "--outFilterMatchNmin 0 --outStd Log --outFilterMultimapNmax 5 " +
                            "--outFilterMatchNminOverLread " +
                            "0.66 --outFilterScoreMinOverLread 0.66 --outSAMunmapped Within " +
                            "--outFilterMultimapScoreRange 1 " +
                            "--outSAMstrandField intronMotif --outFilterScoreMin 0 --alignSJoverhangMin 8 " +
                            "--alignSJDBoverhangMin 1 --runThreadN 4 --outSAMtype None --quantMode " +
                            "TranscriptomeSAM --outSAMattrRGline ID:smv1 SM:smv1 LB:RNA PL:Illumina CN:cr " +
                            "--readFilesCommand zcat",
                    "qsub build/resources/integrationTest/output/sh_files/" +
                            "RnaExpression_Fastq_rsem_for_smv1_analysis.sh",
                    "rm -rf build/resources/integrationTest/output/smv1/tmp"
                }},
                { "output/sh_files/RnaExpression_Fastq_qcsummary_for_cohort_analysis.sh", new String[] {
                    "build/resources/integrationTest/output/log_files/" +
                            "RnaExpression_Fastq_alignment_for_smv1_analysis.log",
                }},
                { "output/sh_files/RnaExpression_Fastq_rsem_for_smv1_analysis.sh", new String[] {
                    "path/to/rsem/rsem-calculate-expression -p 4 --no-bam-output --paired-end --bam " +
                            "--estimate-rspd --seed 12345",
                    "build/resources/integrationTest/output/smv1/bam/smv1.Aligned.toTranscriptome.out.bam",
                    "RSEMINDEX build/resources/integrationTest/output/smv1/rsem/smv1",
                    "mv build/resources/integrationTest/output/smv1/rsem/smv1.genes.results ",
                    "build/resources/integrationTest/output/smv1/rsem/smv1.rsem.gene.expression.results",
                    "mv build/resources/integrationTest/output/smv1/rsem/smv1.isoforms.results",
                    "build/resources/integrationTest/output/smv1/rsem/smv1.rsem.isoform.expression.results",
                    "/usr/bin/python",
                    "/main/src/python/rna_gene_annotation.py",
                    "-t rsem -i build/resources/integrationTest/output/smv1/rsem/" +
                            "smv1.rsem.gene.expression.results",
                    "-o build/resources/integrationTest/output/smv1/rsem/" +
                            "smv1.rsem.annotate.gene.expression.results",
                    "-a /common/reference_genome/GRCh38/Annotation/Gencode_v26/gencode.v26.annotation.saf",
                    "mv build/resources/integrationTest/output/smv1/rsem/" +
                            "smv1.rsem.annotate.gene.expression.results ",
                    "build/resources/integrationTest/output/smv1/rsem/smv1.rsem.gene.expression.results",
                    "rm -rf build/resources/integrationTest/output/smv1/tmp"
                }},
        };
    }

    @Test
    @UseDataProvider("getStarWithRsemConfigAndStrings")
    public void testStarWithRsem(String outputFilePath, String[] expectedStrings) throws IOException {
        startAppWithConfigs(
                "RnaExpressionFastq/gStarWithRsem.txt", S_CONFIG_PATH);
        File outputShFile = new File(this.getClass().getClassLoader().getResource(outputFilePath).getPath());
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
    public static Object[][] getStarWithoutRsemConfigAndStrings() {
        return new Object[][] {
                { OUTPUT_SH_FILE, new String[] {
                    TEST_FASTQ,
                    "/smv1_GTGTTCTA_L004_R1_001.fastq.gz",
                    "| gzip -c > build/resources/integrationTest/output/smv1/fastq/smv1.merged_R1.fastq.gz",
                    "path/to/star --genomeDir /common/reference_genome/GRCh38/Index/STAR_g26 --genomeLoad ",
                    "NoSharedMemory --readFilesIn build/resources/integrationTest/output/smv1/fastq",
                    "/smv1.merged_R1.fastq.gz ",
                    "build/resources/integrationTest/output/smv1/fastq/smv1.merged_R2.fastq.gz ",
                    "--outFileNamePrefix build/resources/integrationTest/output/smv1/bam/smv1. ",
                    "--outFilterMatchNmin 0 --outStd Log --outFilterMultimapNmax 5 --outFilterMatchNminOverLread ",
                    "0.66 --outFilterScoreMinOverLread 0.66 --outSAMunmapped Within ",
                    "--outFilterMultimapScoreRange 1 --outSAMstrandField intronMotif --outFilterScoreMin ",
                    "0 --alignSJoverhangMin 8 --alignSJDBoverhangMin 1 --runThreadN 4 ",
                    "--outSAMtype BAM Unsorted --outSAMattrRGline ID:smv1 SM:smv1 LB:RNA PL:Illumina ",
                    "CN:cr --readFilesCommand zcat",
                    "/usr/lib/jvm/java-8-openjdk-amd64/bin/java -jar /opt/picard/picard.jar SortSam ",
                    "INPUT=build/resources/integrationTest/output/smv1/bam/smv1.Aligned.out.bam ",
                    "OUTPUT=build/resources/integrationTest/output/smv1/bam/smv1.star.sorted.bam ",
                    "SORT_ORDER=coordinate VALIDATION_STRINGENCY=SILENT",
                    "/opt/samtools/samtools-0.1.19/samtools index build/resources/integrationTest/output/smv1/" +
                            "bam/smv1.star.sorted.bam",
                    "/usr/lib/jvm/java-8-openjdk-amd64/bin/java -Xmx16g -jar /opt/picard/picard.jar ",
                    "MarkDuplicates INPUT=build/resources/integrationTest/output/smv1/bam/smv1.star.sorted.bam ",
                    "OUTPUT=build/resources/integrationTest/output/smv1/bam/smv1.star.sorted.mkdup.bam ",
                    "METRICS_FILE=build/resources/integrationTest/output/smv1/qc/smv1.star.sorted.mkdup.metrics ",
                    "ASSUME_SORTED=true REMOVE_DUPLICATES=false VALIDATION_STRINGENCY=SILENT ",
                    "TMP_DIR=build/resources/integrationTest/output/smv1/tmp",
                    "/opt/samtools/samtools-0.1.19/samtools index ",
                    "build/resources/integrationTest/output/smv1/bam/smv1.star.sorted.mkdup.bam",
                    "/opt/samtools/samtools-0.1.19/samtools index build/resources/integrationTest/output/smv1/bam",
                    "/smv1.star.sorted.mkdup.bam",
                    "/usr/lib/jvm/java-7-openjdk-amd64/bin/java -Xmx16g -jar /opt/rnaseqc/RNA-SeQC_v1.1.8.jar ",
                    "-r /common/reference_genome/GRCh38/Sequence/GRCh38.genome.fa -t ",
                    "/common/reference_genome/GRCh38/Annotation/Gencode_v26/gencode.v26.annotation.gtf ",
                    "-n 1000 -s 'smv1|build/resources/integrationTest/output/smv1/bam/" +
                            "smv1.star.sorted.mkdup.bam",
                    "|RNASEQC analysis' -o build/resources/integrationTest/output/smv1/qc ",
                    "VALIDATION_STRINGENCY=SILENT TMP_DIR=build/resources/integrationTest/output/smv1/tmp",
                    "/usr/bin/python",
                    "/main/src/python/rna_qc_metrics.py ",
                    "--sample smv1 --rnaseq build/resources/integrationTest/output/smv1/qc/metrics.tsv ",
                    "--duplicate build/resources/integrationTest/output/smv1/qc/smv1.star.sorted.mkdup.metrics ",
                    "--project Example_project --run run1234 --date 031814 --output build/resources",
                    "/integrationTest/output/smv1/qc/smv1.alignment.merged.QC.metric.txt",
                    "rm -rf build/resources/integrationTest/output/smv1/qc/smv1.star.sorted.mkdup.metrics",
                    "rm -rf build/resources/integrationTest/output/smv1/bam/smv1.Aligned.out.bam.bai",
                    "rm -rf build/resources/integrationTest/output/smv1/bam/smv1.star.sorted.bam.bai",
                    "rm -rf build/resources/integrationTest/output/smv1/tmp",
                    "rm -rf build/resources/integrationTest/output/smv1/bam/smv1.star.sorted.bam",
                    "rm -rf build/resources/integrationTest/output/smv1/bam/smv1.Aligned.out.bam"
                }},
                { "output/sh_files/RnaExpression_Fastq_qcsummary_for_cohort_analysis.sh", new String[] {
                    "build/resources/integrationTest/output/log_files/" +
                            "RnaExpression_Fastq_alignment_for_smv1_analysis.log",
                }},
        };
    }

    @Test
    @UseDataProvider("getStarWithoutRsemConfigAndStrings")
    public void testStarWithoutRsem(String outputFilePath, String[] expectedStrings) throws IOException {
        startAppWithConfigs(
                "RnaExpressionFastq/gStarWithoutRsem.txt", S_CONFIG_PATH);
        File outputShFile = new File(this.getClass().getClassLoader().getResource(outputFilePath).getPath());
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
    public static Object[][] getHisat2SalmonExpectedStrings() {
        return new Object[][] {
                { "RnaExpressionFastq/gHisat2.txt", new String[] {
                    TEST_FASTQ + "/smv1_GTGTTCTA_L004_R1_001.fastq.gz ",
                    "/opt/samtools/samtools-0.1.19/samtools index " +
                            "build/resources/integrationTest/output/smv1/bam/smv1.hisat2.sorted.bam",
                    "/usr/lib/jvm/java-8-openjdk-amd64/bin/java -Xmx16g -jar /opt/picard/picard.jar " +
                            "MarkDuplicates INPUT=build/resources/integrationTest/output/smv1/bam/" +
                            "smv1.hisat2.sorted.bam " +
                            "OUTPUT=build/resources/integrationTest/output/smv1/bam/" +
                            "smv1.hisat2.sorted.mkdup.bam " +
                            "METRICS_FILE=build/resources/integrationTest/output/smv1/qc/" +
                            "smv1.hisat2.sorted.mkdup.metrics " +
                            "ASSUME_SORTED=true REMOVE_DUPLICATES=false VALIDATION_STRINGENCY=SILENT " +
                            "TMP_DIR=build/resources/integrationTest/output/smv1/tmp",
                    "/opt/samtools/samtools-0.1.19/samtools index " +
                            "build/resources/integrationTest/output/smv1/bam/smv1.hisat2.sorted.mkdup.bam",
                    "/opt/samtools/samtools-0.1.19/samtools view -bS -|" +
                            "/opt/samtools/samtools-0.1.19/samtools sort - " +
                            "build/resources/integrationTest/output/smv1/bam/smv1.hisat2.sorted"
                }},
                { "RnaExpressionFastq/gSalmon.txt", new String[] {
                    TEST_FASTQ + "/smv1_GTGTTCTA_L004_R1_001.fastq.gz ",
                    "gunzip -c build/resources/integrationTest/output/smv1/fastq/smv1.merged_R1.fastq.gz " +
                            "> build/resources/integrationTest/output/smv1/fastq/smv1.merged_R1.fastq",
                    "gunzip -c build/resources/integrationTest/output/smv1/fastq/smv1.merged_R2.fastq.gz > ",
                    "build/resources/integrationTest/output/smv1/fastq/smv1.merged_R2.fastq",
                    "path/to/salmon quant -i SALMONINDEX -l IU -p 4 -1 build/resources/integrationTest" +
                            "/output/smv1/fastq/smv1.merged_R1.fastq -2 build/resources/integrationTest/output" +
                            "/smv1/fastq/smv1.merged_R2.fastq -g /common/reference_genome/GRCh38/Annotation" +
                            "/Gencode_v26/gencode.v26.annotation.gtf -o build/resources/integrationTest/output" +
                            "/smv1/salmon",
                    "mv build/resources/integrationTest/output/smv1/salmon/quant.genes.sf ",
                    "build/resources/integrationTest/output/smv1/salmon/smv1.salmon.gene.results",
                    "mv build/resources/integrationTest/output/smv1/salmon/quant.sf ",
                    "build/resources/integrationTest/output/smv1/salmon/smv1.salmon.transcript.results",
                    "rm -rf build/resources/integrationTest/output/smv1/fastq/smv1.merged_R1.fastq",
                    "rm -rf build/resources/integrationTest/output/smv1/fastq/smv1.merged_R2.fastq",
                    "rm -rf build/resources/integrationTest/output/smv1/tmp"
                }},
        };
    }

    @Test
    @UseDataProvider("getHisat2SalmonExpectedStrings")
    public void testHisat2Salmon(String gConfigPath, String[] expectedStrings) throws IOException {
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
    public static Object[][] getFeatureCountCufflinksStringtieExpectedStrings() {
        return new Object[][] {
                { "RnaExpressionFastq/gFeatureCount.txt", "output/sh_files/" +
                        "RnaExpression_Fastq_featureCount_for_smv1_analysis.sh", new String[] {
                            "/opt/subread/subread-1.4.5-p1-Linux-x86_64/bin/featureCounts -F SAF -M -s 0 -a ",
                            "/common/reference_genome/GRCh38/Annotation/Gencode_v26/gencode.v26.annotation.saf",
                            "-o build/resources/integrationTest/output/smv1/feature_count/smv1_featureCount_gene.txt",
                            "-Q 20 -T 4 build/resources/integrationTest/output/smv1/bam/smv1.hisat2.sorted.mkdup.bam",
                            "rm -rf build/resources/integrationTest/output/smv1/qc/smv1.hisat2.sorted.mkdup.metrics",
                            "rm -rf build/resources/integrationTest/output/smv1/bam/smv1.hisat2.sorted.bam.bai",
                            "rm -rf build/resources/integrationTest/output/smv1/tmp",
                            "rm -rf build/resources/integrationTest/output/smv1/bam/smv1.hisat2.sorted.bam"
                        }},
                { "RnaExpressionFastq/gCufflinks.txt", "output/sh_files/" +
                        "RnaExpression_Fastq_cufflinks_for_smv1_analysis.sh", new String[] {
                            "/opt/cufflinks/cufflinks-2.2.1.Linux_x86_64/cufflinks --library-type fr-unstranded ",
                            "--num-threads 4 -b /common/reference_genome/GRCh38/Sequence/GRCh38.genome.fa ",
                            "--GTF /common/reference_genome/GRCh38/Annotation/Gencode_v26/gencode.v26.annotation.gtf ",
                            "--output-dir build/resources/integrationTest/output/smv1/cufflinks ",
                            "build/resources/integrationTest/output/smv1/bam/smv1.hisat2.sorted.mkdup.bam",
                            "mv build/resources/integrationTest/output/smv1/cufflinks/genes.fpkm_tracking ",
                            "build/resources/integrationTest/output/smv1/cufflinks/" +
                                    "smv1.cufflinks.gene.expression.results",
                            "mv build/resources/integrationTest/output/smv1/cufflinks/isoforms.fpkm_tracking ",
                            "build/resources/integrationTest/output/smv1/cufflinks/" +
                                    "smv1.cufflinks.isoform.expression.results",
                            "rm -rf build/resources/integrationTest/output/smv1/qc/smv1.hisat2.sorted.mkdup.metrics",
                            "rm -rf build/resources/integrationTest/output/smv1/bam/smv1.hisat2.sorted.bam.bai",
                            "rm -rf build/resources/integrationTest/output/smv1/tmp",
                            "rm -rf build/resources/integrationTest/output/smv1/bam/smv1.hisat2.sorted.bam"
                        }},
                { "RnaExpressionFastq/gStringtie.txt", "output/sh_files/" +
                        "RnaExpression_Fastq_stringtie_for_smv1_analysis.sh", new String[] {
                            "path/to/stringtie -p 4 -G /common/reference_genome/GRCh38/Annotation/Gencode_v26/" +
                                    "gencode.v26.annotation.gtf ",
                            "-A build/resources/integrationTest/output/smv1/stringtie/" +
                                    "smv1.stringtie.gene.expression.results ",
                            "-o build/resources/integrationTest/output/smv1/stringtie/" +
                                    "smv1.stringtie.assembly.transcripts.gtf ",
                            "build/resources/integrationTest/output/smv1/bam/smv1.hisat2.sorted.mkdup.bam",
                            "rm -rf build/resources/integrationTest/output/smv1/qc/smv1.hisat2.sorted.mkdup.metrics",
                            "rm -rf build/resources/integrationTest/output/smv1/bam/smv1.hisat2.sorted.bam.bai",
                            "rm -rf build/resources/integrationTest/output/smv1/tmp",
                            "rm -rf build/resources/integrationTest/output/smv1/bam/smv1.hisat2.sorted.bam"
                        }},
        };
    }

    @Test
    @UseDataProvider("getFeatureCountCufflinksStringtieExpectedStrings")
    public void testFeatureCountCufflinksStringtie(String gConfigPath,
                                                   String outputFilePath,
                                                   String[] expectedStrings) throws IOException {
        startAppWithConfigs(
                gConfigPath, S_CONFIG_PATH);
        File outputShFile = new File(this.getClass().getClassLoader().getResource(outputFilePath).getPath());
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
    public static Object[][] getRsemWithoutHisat2ExpectedStrings() {
        return new Object[][] {
                { "output/sh_files/RnaExpression_Fastq_rsem_for_smv1_analysis.sh", new String[] {
                    "path/to/rsem/rsem-calculate-expression -p 4 ",
                    "--no-bam-output --paired-end --bam --estimate-rspd --seed 12345 ",
                    "build/resources/integrationTest/output/smv1/bam/smv1.Aligned.toTranscriptome.out.bam ",
                    "RSEMINDEX build/resources/integrationTest/output/smv1/rsem/smv1",
                    "mv build/resources/integrationTest/output/smv1/rsem/smv1.genes.results ",
                    "build/resources/integrationTest/output/smv1/rsem/smv1.rsem.gene.expression.results",
                    "mv build/resources/integrationTest/output/smv1/rsem/smv1.isoforms.results " +
                            "build/resources/integrationTest/output/smv1/rsem/" +
                            "smv1.rsem.isoform.expression.results",
                    "/usr/bin/python",
                    "/main/src/python/rna_gene_annotation.py -t rsem -i ",
                    "build/resources/integrationTest/output/smv1/rsem/smv1.rsem.gene.expression.results -o ",
                    "build/resources/integrationTest/output/smv1/rsem/" +
                            "smv1.rsem.annotate.gene.expression.results -a ",
                    "/common/reference_genome/GRCh38/Annotation/Gencode_v26/gencode.v26.annotation.saf",
                    "mv build/resources/integrationTest/output/smv1/rsem/" +
                            "smv1.rsem.annotate.gene.expression.results ",
                    "build/resources/integrationTest/output/smv1/rsem/smv1.rsem.gene.expression.results",
                    "rm -rf build/resources/integrationTest/output/smv1/tmp"
                }},
                { OUTPUT_SH_FILE, new String[] {
                    "qsub build/resources/integrationTest/output/sh_files/" +
                            "RnaExpression_Fastq_rsem_for_smv1_analysis.sh"
                }},
                { "output/sh_files/RnaExpression_Fastq_qcsummary_for_cohort_analysis.sh", new String[] {
                    "Merge RNA QC",
                    "build/resources/integrationTest/output/log_files/" +
                            "RnaExpression_Fastq_alignment_for_smv1_analysis.log"
                }},

        };
    }

//    @Test
    @UseDataProvider("getRsemWithoutHisat2ExpectedStrings")
    public void testRsemWithoutHisat2(String outputFilePath, String[] expectedStrings) throws IOException {
        startAppWithConfigs(
                "RnaExpressionFastq/gRsemWithoutHisat2.txt", S_CONFIG_PATH);
        File outputShFile = new File(this.getClass().getClassLoader().getResource(outputFilePath).getPath());
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
    public static Object[][] getNoToolsetExpectedStrings() {
        return new Object[][] {
                { "RnaExpressionFastq/gNonFlagXenome.txt", "xenome classify -T 8 -P MOUSEXENOMEINDEX --pairs " +
                        "--graft-name human --host-name mouse",
                },
                { "RnaExpressionFastq/gNonSeqpurge.txt", "/opt/ngs_bits/ngs-bits/bin/SeqPurge -threads 4 -in1"
                },
                { "RnaExpressionFastq/gNonTrimmomatic.txt", "/usr/lib/jvm/java-8-openjdk-amd64/bin/java -jar " +
                        "trimmomatic PE -threads 4"
                },
                { "RnaExpressionFastq/gNonStar.txt", "path/to/star --genomeDir" +
                        " /common/reference_genome/GRCh38/Index/STAR_g26 --sjdbGTFfile"
                },
                { "RnaExpressionFastq/gNonHisat2.txt", "REMOVE_DUPLICATES=false VALIDATION_STRINGENCY=SILENT"
                },
                { "RnaExpressionFastq/gNonSalmon.txt", "path/to/salmon quant -i SALMONINDEX -l IU -p 4 -1"
                },
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
}
