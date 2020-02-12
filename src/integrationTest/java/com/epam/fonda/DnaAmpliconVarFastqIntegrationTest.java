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

import com.epam.fonda.utils.PipelineUtils;
import com.epam.fonda.utils.TemplateEngineUtils;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static java.lang.String.format;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@RunWith(DataProviderRunner.class)
public class DnaAmpliconVarFastqIntegrationTest extends AbstractIntegrationTest {

  private static final String OUTPUT_DIR = "output";
  private static final String ERROR_MESSAGE = "An error occurred with task %s";

  private static final String POST_ALIGNMENT_SH_FILE =
      "output/sh_files/DnaAmpliconVar_Fastq_postalignment_for_GA5_analysis.sh";
  private static final String ALIGNMENT_SH_FILE =
      "output/sh_files/DnaAmpliconVar_Fastq_alignment_for_GA5_1_analysis.sh";
  private static final String MERGE_MUTATION_SH_FILE =
      "output/sh_files/DnaAmpliconVar_Fastq_mergeMutation_for_cohort_analysis.sh";

  private static final String NULL = "null";
  private static final String FASTA_FILE =
      "/ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa";
  private static final String REMOVE_TEMP_DIRS_STEP = "Begin Step: Remove temporary directories...";
  private static final String SNPEFF_PATTERN = ".*?/usr/bin/python.+?vcf_snpeff_annotation.py.+?(\\n|$)";
  private static final String SINGLE_STUDY_CONFIG = "DnaAmpliconVarFastq/sSingle.txt";
  private static final String PAIRED_STUDY_CONFIG = "DnaAmpliconVarFastq/sPaired.txt";
  private static final String ALL_TASKS_FOLDER =
      "dnaAmpliconVarFastq/testControlSampleAllTasksXenomeNo";
  private static final String ALL_TASKS_FREEBAYES = String.format(
      "%s/DnaAmpliconVar_Fastq_freebayes_for_GA5_analysis", ALL_TASKS_FOLDER);
  private static final String ALL_TASKS_GATK_HAPLOTYPE_CALLER = String.format(
      "%s/DnaAmpliconVar_Fastq_gatkHaplotypeCaller_for_GA5_analysis", ALL_TASKS_FOLDER);
  private static final String ALL_TASKS_LOFREQ = String.format(
      "%s/DnaAmpliconVar_Fastq_lofreq_for_GA5_analysis", ALL_TASKS_FOLDER);
  private static final String ALL_TASKS_MUTECT1 = String.format(
      "%s/DnaAmpliconVar_Fastq_mutect1_for_GA5_analysis", ALL_TASKS_FOLDER);
  private static final String ALL_TASKS_POST_ALIGNMENT = String.format(
      "%s/DnaAmpliconVar_Fastq_postalignment_for_GA5_analysis", ALL_TASKS_FOLDER);
  private static final String ALL_TASKS_SCALPEL = String.format(
      "%s/DnaAmpliconVar_Fastq_scalpel_for_GA5_analysis", ALL_TASKS_FOLDER);
  private static final String ALL_TASKS_STRELKA2 = String.format(
      "%s/DnaAmpliconVar_Fastq_strelka2_for_GA5_analysis", ALL_TASKS_FOLDER);
  private static final String ALL_TASKS_VARDICT = String.format(
      "%s/DnaAmpliconVar_Fastq_vardict_for_GA5_analysis", ALL_TASKS_FOLDER);
  private static final String ALIGNMENT = "DnaAmpliconVar_Fastq_alignment_for_GA5_1_analysis.txt";
  private static final String POST_ALIGNMENT =
      "DnaAmpliconVar_Fastq_postalignment_for_GA5_analysis.txt";
  public static final String MERGE_MUTATION =
      "DnaAmpliconVar_Fastq_mergeMutation_for_cohort_analysis.txt";

  private Context context;
  public static final String G_SINGLE_ALL_TASKS = "DnaAmpliconVarFastq/gSingleAllTasks.txt";
  private TemplateEngine templateEngine = TemplateEngineUtils.init();

  @BeforeEach
  public void setup() {
    context = new Context();
    context.setVariable("jarPath", PipelineUtils.getExecutionPath());
  }

  @ParameterizedTest
  @MethodSource("initControlSampleAllTasks")
  public void testControlSampleAllTasksXenomeNo(String task, String template)
      throws IOException, URISyntaxException {
    startAppWithConfigs(
        G_SINGLE_ALL_TASKS, SINGLE_STUDY_CONFIG);
    String filePath = format(
        "output/sh_files/DnaAmpliconVar_Fastq_%s_for_GA5_analysis.sh", task);

    final String expectedCmd = templateEngine.process(template, context).trim();
    final String actualCmd = getCmd(filePath).trim();

    assertFalse(actualCmd.contains(NULL));
    assertEquals(expectedCmd, actualCmd, String.format(ERROR_MESSAGE, task));

    cleanOutputDirForNextTest(OUTPUT_DIR, false);
  }

  private static Stream<Arguments> initControlSampleAllTasks() {
    return Stream.of(
        Arguments.of("freebayes", ALL_TASKS_FREEBAYES),
        Arguments.of("gatkHaplotypeCaller", ALL_TASKS_GATK_HAPLOTYPE_CALLER),
        Arguments.of("lofreq", ALL_TASKS_LOFREQ),
        Arguments.of("mutect1", ALL_TASKS_MUTECT1),
        Arguments.of("postalignment", ALL_TASKS_POST_ALIGNMENT),
        Arguments.of("scalpel", ALL_TASKS_SCALPEL),
        Arguments.of("strelka2", ALL_TASKS_STRELKA2),
        Arguments.of("vardict", ALL_TASKS_VARDICT)
    );
  }

  @ParameterizedTest
  @MethodSource("initConfigsTestAlignmentAndMergeMutation")
  public void testStagesAndMergeMutation(String globalConfig, String studyConfig, String folder)
      throws IOException, URISyntaxException {
    startAppWithConfigs(globalConfig, studyConfig);

//    String expectedAlignmentCmd = getTestTemplate(folder, ALIGNMENT, context).trim();
//    String actualAlignmentCmd = getCmd(ALIGNMENT_SH_FILE).trim();
//    assertEquals(expectedAlignmentCmd, actualAlignmentCmd);

    String expectedPostAlignmentCmd = getTestTemplate(folder, POST_ALIGNMENT, context);
    String actualPostAlignmentCmd = getCmd(POST_ALIGNMENT_SH_FILE).trim();
    assertEquals(expectedPostAlignmentCmd, actualPostAlignmentCmd);
//
//    String expectedMergeMutationCmd = getTestTemplate(folder, MERGE_MUTATION, context);
//    String actualMergeMutationCmd = getCmd(MERGE_MUTATION_SH_FILE).trim();

//    assertEquals(expectedMergeMutationCmd, actualMergeMutationCmd);
    cleanOutputDirForNextTest(OUTPUT_DIR, false);
  }

  private String getTestTemplate(String folder, String task, Context context) {
    return templateEngine.process(String.format("%s/%s", folder, task), context).trim();
  }

  private static Stream<Arguments> initConfigsTestAlignmentAndMergeMutation() {
    return Stream.of(
        Arguments.of(G_SINGLE_ALL_TASKS, SINGLE_STUDY_CONFIG,
            "dnaAmpliconVarFastq/testControlSampleAllTasksXenomeNo"),
        Arguments.of("DnaAmpliconVarFastq/gPairedAllTasks.txt", "DnaAmpliconVarFastq/sControlSampleNotNA.txt",
            "dnaAmpliconVarFastq/testControlSampleNotNAAllTasksXenomeNo")
    );
  }

  @Test
  @UseDataProvider("controlSampleNotNAAllTasks")
  public void testControlSampleNotNAAllTasksXenomeNo(String task, String[] expectedStrings,
      String[] expectedPatterns) throws IOException {
    startAppWithConfigs(
        "DnaAmpliconVarFastq/gPairedAllTasks.txt", "DnaAmpliconVarFastq/sControlSampleNotNA.txt");

    //tests for toolsets
    String outputShFilePath =
        "output/sh_files/DnaAmpliconVar_Fastq_" + task + "_for_GA5_analysis.sh";
    File outputShFile = new File(
        this.getClass().getClassLoader().getResource(outputShFilePath).getPath());

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

  @Test
  public void testPairedPicardAbraGatk() throws IOException {
    startAppWithConfigs("DnaAmpliconVarFastq/gAbraGatkPicardPaired.txt", PAIRED_STUDY_CONFIG);

    //test for toolset picard + gatk_realign + abra+realign
    File outputShFile = new File(
        this.getClass().getClassLoader().getResource(POST_ALIGNMENT_SH_FILE).getPath());

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
  public void testPairedXenomeYesSeqpurgeBwa() throws IOException {
    startAppWithConfigs("DnaAmpliconVarFastq/gPairedSeqpurgeBwaXenomeYes.txt", PAIRED_STUDY_CONFIG);

    //test for xenome, seqpurge and bwa toolset
    File outputShFile = new File(
        this.getClass().getClassLoader().getResource(ALIGNMENT_SH_FILE).getPath());

    try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
      List<String> lines = reader.lines().collect(Collectors.toList());
      assertXenome(lines);
      assertSequpurge(lines);
      assertBwa(lines);
    }
    assertPostAlignment();
  }

  @Test
  public void testPairedXenomeYesSeqpurgeNovoalign() throws IOException {
    startAppWithConfigs("DnaAmpliconVarFastq/gPairedSeqpurgeNovoalignXenomeYes.txt",
        PAIRED_STUDY_CONFIG);

    //test for xenome, seqpurge and novoalign toolset
    File outputShFile = new File(
        this.getClass().getClassLoader().getResource(ALIGNMENT_SH_FILE).getPath());

    try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
      List<String> lines = reader.lines().collect(Collectors.toList());
      assertXenome(lines);
      assertSequpurge(lines);
      assertNovoalign(lines);
    }
    assertPostAlignment();
  }

  @Test
  public void testPairedXenomeYesTrimmomaticBwa() throws IOException {
    startAppWithConfigs("DnaAmpliconVarFastq/gPairedTrimmomaticBwaXenomeYes.txt",
        PAIRED_STUDY_CONFIG);

    //test for xenome, trimmomatic and bwa toolset
    File outputShFile = new File(
        this.getClass().getClassLoader().getResource(ALIGNMENT_SH_FILE).getPath());

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
  public void testSinglePicardAbraGatk() throws IOException {
    startAppWithConfigs("DnaAmpliconVarFastq/gAbraGatkPicardSingle.txt", SINGLE_STUDY_CONFIG);

    //test for toolset picard + gatk_realign + abra+realign
    File outputShFile = new File(
        this.getClass().getClassLoader().getResource(POST_ALIGNMENT_SH_FILE).getPath());

    try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
      List<String> lines = reader.lines().collect(Collectors.toList());
      assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: ABRA realignment...")));
      assertTrue(lines.stream().anyMatch(line -> line.contains(
          "/usr/lib/jvm/java-8-openjdk-amd64/bin/java -Xmx16g -jar /usr/bin/abra2")));
      assertTrue(lines.stream().anyMatch(line -> line.contains(
          "--single --tmpdir build/resources/integrationTest/output/GA5/tmp")));
      assertTrue(lines.stream()
          .anyMatch(line -> line.contains("/opt/samtools/samtools-0.1.19/samtools index " +
              "build/resources/integrationTest/output/GA5/bam/GA5.merged.sorted.realign.bam")));
      assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: GATK realignment...")));
      assertTrue(lines.stream()
          .anyMatch(line -> line.contains("-jar /usr/bin/gatk -T RealignerTargetCreator")));
      assertTrue(
          lines.stream().anyMatch(line -> line.contains("-jar /usr/bin/gatk -T IndelRealigner")));
      assertTrue(
          lines.stream().anyMatch(line -> line.contains("Begin Step: GATK recalibration..")));
      assertTrue(lines.stream()
          .anyMatch(line -> line.contains("-jar /usr/bin/gatk -T BaseRecalibrator -R")));
      assertTrue(
          lines.stream().anyMatch(line -> line.contains("-jar /usr/bin/gatk -T PrintReads -R")));
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
  public void testSingleXenomeYesTrimmomaticBwa() throws IOException {
    startAppWithConfigs("DnaAmpliconVarFastq/gSingleTrimmomaticBwaYes.txt",
        SINGLE_STUDY_CONFIG);

    //test for trimmomatic, xenome and bwa toolset
    File outputShFile = new File(
        this.getClass().getClassLoader().getResource(ALIGNMENT_SH_FILE).getPath());

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
    File outputShFile = new File(
        this.getClass().getClassLoader().getResource(ALIGNMENT_SH_FILE).getPath());

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


  private void assertBwa(List<String> lines) {
    assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: BWA alignment...")));
    assertTrue(
        lines.stream().anyMatch(line -> line.contains("/usr/bin/bwa mem -t 4 " + FASTA_FILE)));
  }

  private void assertXenome(List<String> lines) {
    assertTrue(
        lines.stream().anyMatch(line -> line.contains("Begin Step: Xenome classification...")));
    assertTrue(lines.stream().anyMatch(line -> line.contains("/usr/bin/xenome classify -T 8 -P ")));
    assertTrue(lines.stream().anyMatch(line -> line.contains("/ngs/data/xenomeIdx/xenome.idx " +
        "--pairs --graft-name human --host-name mouse ")));
    assertTrue(lines.stream().anyMatch(line -> line.contains("/ngs/data/xenomeIdx/xenome.idx " +
        "--pairs --graft-name human --host-name mouse ")));
    assertTrue(lines.stream().anyMatch(line -> line.contains("--output-filename-prefix" +
        " build/resources/integrationTest/output/GA5/tmp/GA5_1 ")));
    assertTrue(lines.stream()
        .anyMatch(
            line -> line.contains("--tmp-dir build/resources/integrationTest/output/GA5/tmp -i ")));
    assertTrue(lines.stream().anyMatch(line -> line.contains("/ngs/data/demo/test/fastq_data/" +
        "GA5_0001_L002_R1_001.fastq.gz")));
  }

  private void assertCommonTrimmomatic(List<String> lines) {
    assertTrue(
        lines.stream().anyMatch(line -> line.contains("Begin Step: Trimmomatic trimming...")));
    assertTrue(lines.stream().anyMatch(line -> line.contains("rm -rf " +
        "build/resources/integrationTest/output/GA5/fastq/GA5_1.trimmed_unpaired.R1.fq.gz")));
  }

  private void assertNovoalign(List<String> lines) {
    assertTrue(
        lines.stream().anyMatch(line -> line.contains("Begin Step: Novoalign alignment...")));
    assertTrue(lines.stream().anyMatch(line -> line.contains("/usr/bin/novoalign -c 4 -d " +
        "/ngs/data/novoindexDB/novoindex.nix -o SAM")));
  }

  private void assertSequpurge(List<String> lines) {
    assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: Seqpurge trimming...")));
    assertTrue(lines.stream()
        .anyMatch(line -> line.contains("/opt/ngs_bits/ngs-bits/bin/SeqPurge -threads 4 " +
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
    outputShFile = new File(
        this.getClass().getClassLoader().getResource(outputShFilePath).getPath());

    try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
      List<String> lines = reader.lines().collect(Collectors.toList());
      commonQcSummary(lines);
    }

    // test for single readType
    outputShFilePath = POST_ALIGNMENT_SH_FILE;
    outputShFile = new File(
        this.getClass().getClassLoader().getResource(outputShFilePath).getPath());

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
    assertTrue(lines.stream()
        .anyMatch(line -> line.contains("/usr/lib/jvm/java-8-openjdk-amd64/bin/java -Xmx10g " +
            "-jar /opt/picard/picard.jar MergeSamFiles" +
            " O=build/resources/integrationTest/output/GA5/bam/GA5.merged.sorted.bam")));
    assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: Index bam...")));
    assertTrue(lines.stream()
        .anyMatch(line -> line.contains("/opt/samtools/samtools-0.1.19/samtools index " +
            "build/resources/integrationTest/output/GA5/bam/GA5.merged.sorted.bam")));
    assertTrue(lines.stream().anyMatch(line -> line.contains("Successful Step: Index bam")));
    assertTrue(lines.stream()
        .anyMatch(line -> line.contains("build/resources/integrationTest/output/log_files/" +
            "DnaAmpliconVar_Fastq_postalignment_for_GA5_analysis.log")));
    assertTrue(lines.stream().anyMatch(line -> line.contains(REMOVE_TEMP_DIRS_STEP)));
    assertTrue(lines.stream().noneMatch(line -> line.contains(NULL)));
  }
}
