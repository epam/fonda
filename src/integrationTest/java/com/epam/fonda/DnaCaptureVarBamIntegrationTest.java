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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.fonda.utils.PipelineUtils.getExecutionPath;
import static java.lang.String.format;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

//@RunWith(DataProviderRunner.class)
public class DnaCaptureVarBamIntegrationTest extends AbstractIntegrationTest {

    private static final String OUTPUT_DIR = "output";
    private static final String OUTPUT_DIR_ROOT = "build/resources/integrationTest/";
    private static final String QSUB = "qsub ";
    private static final String NULL = "null";
    private static final String SH_FILES = "/sh_files";
    private static final String PYTHON_VCF_SNPEFF_ANNOTATION =
            ".*?/usr/bin/python.+?vcf_snpeff_annotation.py.+?(\\n|$)";
    public static final String NA_ALL_TASKS_FOLDER = "DnaCaptureVarBam/testControlSampleNAAllTasks";
    public static final String NA_VARDICT_FOR_GA_5_ANALYSIS_TEST = String.format(
            "%s/DnaCaptureVar_Bam_vardict_for_GA5_analysis_test", NA_ALL_TASKS_FOLDER);
    public static final String NA_MUTECT_1_FOR_GA_5_ANALYSIS_TEST = String.format(
            "%s/DnaCaptureVar_Bam_mutect1_for_GA5_analysis_test", NA_ALL_TASKS_FOLDER);
    public static final String NA_STRELKA_2_FOR_GA_5_ANALYSIS_TEST = String.format(
            "%s/DnaCaptureVar_Bam_strelka2_for_GA5_analysis_test", NA_ALL_TASKS_FOLDER);
    public static final String NA_SCALPEL_FOR_GA_5_ANALYSIS_TEST = String.format(
            "%s/DnaCaptureVar_Bam_scalpel_for_GA5_analysis_test", NA_ALL_TASKS_FOLDER);
    public static final String NA_FREEBAYES_FOR_GA_5_ANALYSIS_TEST = String.format(
            "%s/DnaCaptureVar_Bam_freebayes_for_GA5_analysis_test", NA_ALL_TASKS_FOLDER);
    public static final String NA_GATK_HAPLOTYPE_CALLER_FOR_GA_5_ANALYSIS_TEST = String.format(
            "%s/DnaCaptureVar_Bam_gatkHaplotypeCaller_for_GA5_analysis_test", NA_ALL_TASKS_FOLDER);
    public static final String NA_LOFREQ_FOR_GA_5_ANALYSIS_TEST = String.format(
            "%s/DnaCaptureVar_Bam_lofreq_for_GA5_analysis_test", NA_ALL_TASKS_FOLDER);
    public static final String NA_VARIANT_DETECTION_FOR_GA_5_ANALYSIS_TEST = String.format(
            "%s/DnaCaptureVar_Bam_variantDetection_for_GA5_analysis_test", NA_ALL_TASKS_FOLDER);

    public static final String NOT_NA_ALL_TASKS_FOLDER = "DnaCaptureVarBam/testControlSampleNotNAAllTasks";
    public static final String CONT_EST_FOR_GA_51_ANALYSIS_TEST = String.format(
            "%s/DnaCaptureVar_Bam_contEst_for_GA51_analysis_test", NOT_NA_ALL_TASKS_FOLDER);
    public static final String EXOMECNV_FOR_GA_51_ANALYSIS_TEST = String.format(
            "%s/DnaCaptureVar_Bam_exomecnv_for_GA51_analysis_test", NOT_NA_ALL_TASKS_FOLDER);
    public static final String LOFREQ_FOR_GA_51_ANALYSIS_TEST = String.format(
            "%s/DnaCaptureVar_Bam_lofreq_for_GA51_analysis_test", NOT_NA_ALL_TASKS_FOLDER);
    public static final String MUTECT_2_FOR_GA_51_ANALYSIS_TEST = String.format(
            "%s/DnaCaptureVar_Bam_mutect2_for_GA51_analysis_test", NOT_NA_ALL_TASKS_FOLDER);
    public static final String SCALPEL_FOR_GA_51_ANALYSIS_TEST = String.format(
            "%s/DnaCaptureVar_Bam_scalpel_for_GA51_analysis_test", NOT_NA_ALL_TASKS_FOLDER);
    public static final String SEQUENZA_FOR_GA_51_ANALYSIS_TEST = String.format(
            "%s/DnaCaptureVar_Bam_sequenza_for_GA51_analysis_test", NOT_NA_ALL_TASKS_FOLDER);
    public static final String STRELKA_2_FOR_GA_51_ANALYSIS_TEST = String.format(
            "%s/DnaCaptureVar_Bam_strelka2_for_GA51_analysis_test", NOT_NA_ALL_TASKS_FOLDER);
    public static final String VARDICT_FOR_GA_51_ANALYSIS_TEST = String.format(
            "%s/DnaCaptureVar_Bam_vardict_for_GA51_analysis_test", NOT_NA_ALL_TASKS_FOLDER);
    public static final String VARIANT_DETECTION_FOR_GA_51_ANALYSIS_TEST = String.format(
            "%s/DnaCaptureVar_Bam_variantDetection_for_GA51_analysis_test", NOT_NA_ALL_TASKS_FOLDER);
    private static Context context;
    private TemplateEngine templateEngine = TemplateEngineUtils.init();

    @Test
    @UseDataProvider("controlSampleNAAllTasks")
    public void testControlSampleNAAllTasks(String task, String[] expectedStrings, String[] expectedPatterns)
            throws IOException {
        startAppWithConfigs(
                "DnaCaptureVarBam/gSingleAllTasks.txt", "DnaCaptureVarBam/sSingle.txt");

        //tests for toolsets
        String filePath = "output/sh_files/DnaCaptureVar_Bam_" + task + "_for_GA5_analysis.sh";
        File outputShFile = new File(this.getClass().getClassLoader().getResource(filePath).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            for (String expectedString : expectedStrings) {
                assertTrue(lines.stream().anyMatch(line -> line.contains(expectedString)));
            }
            for (String expectedPattern : expectedPatterns) {
                assertTrue(lines.stream().anyMatch(line -> line.matches(expectedPattern)));
            }
            assertTrue(lines.stream().noneMatch(line -> line.contains(NULL)));
        }

        //tests for variant detection
        filePath = "output/sh_files/DnaCaptureVar_Bam_variantDetection_for_GA5_analysis.sh";
        outputShFile = new File(this.getClass().getClassLoader().getResource(filePath).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    QSUB + SH_FILES + "DnaCaptureVar_Bam_vardict_for_GA5_analysis.sh")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    QSUB + SH_FILES + "DnaCaptureVar_Bam_mutect1_for_GA5_analysis.sh")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    QSUB + SH_FILES + "DnaCaptureVar_Bam_strelka2_for_GA5_analysis.sh")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    QSUB + SH_FILES + "DnaCaptureVar_Bam_gatkHaplotypeCaller_for_GA5_analysis.sh")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    QSUB + SH_FILES + "DnaCaptureVar_Bam_scalpel_for_GA5_analysis.sh")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    QSUB + SH_FILES + "DnaCaptureVar_Bam_lofreq_for_GA5_analysis.sh")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    QSUB + SH_FILES + "DnaCaptureVar_Bam_freebayes_for_GA5_analysis.sh")));
            assertTrue(lines.stream().noneMatch(line -> line.contains(NULL)));
        }
        Assertions.assertAll(
                () -> assertTrue(new File(format("%s%s/err_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
                () -> assertTrue(new File(format("%s%s/GA5", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
                () -> assertTrue(new File(format("%s%s/log_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
                () -> assertTrue(new File(format("%s%s/sh_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists())
        );
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @BeforeEach
    public void setup() {
        context = new Context();
        context.setVariable("jarPath", getExecutionPath());
    }

    @ParameterizedTest
    @MethodSource("initToolAndTemplateNAAllTasks")
    public void testControlSampleNAAllTasks(String task, String template) throws IOException, URISyntaxException {
        startAppWithConfigs("DnaCaptureVarBam/gSingleAllTasks.txt", "DnaCaptureVarBam/sSingle.txt");
        String filePath = String.format("output/sh_files/DnaCaptureVar_Bam_%s_for_GA5_analysis.sh", task);

        final String expectedCmd = templateEngine.process(template, context);

        assertEquals(expectedCmd.trim(), getCmd(filePath).trim());
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initToolAndTemplateNAAllTasks() {
        return Stream.of(
                Arguments.of("vardict", NA_VARDICT_FOR_GA_5_ANALYSIS_TEST),
                Arguments.of("mutect1", NA_MUTECT_1_FOR_GA_5_ANALYSIS_TEST),
                Arguments.of("strelka2", NA_STRELKA_2_FOR_GA_5_ANALYSIS_TEST),
                Arguments.of("scalpel", NA_SCALPEL_FOR_GA_5_ANALYSIS_TEST),
                Arguments.of("freebayes", NA_FREEBAYES_FOR_GA_5_ANALYSIS_TEST),
                Arguments.of("gatkHaplotypeCaller", NA_GATK_HAPLOTYPE_CALLER_FOR_GA_5_ANALYSIS_TEST),
                Arguments.of("lofreq", NA_LOFREQ_FOR_GA_5_ANALYSIS_TEST),
                Arguments.of("variantDetection", NA_VARIANT_DETECTION_FOR_GA_5_ANALYSIS_TEST)
        );
    }

    @ParameterizedTest
    @MethodSource("initToolAndTemplateNotNAAllTasks")
    public void testControlSampleNotNAAllTasks(String task, String template) throws IOException, URISyntaxException {
        startAppWithConfigs("DnaCaptureVarBam/gSingleAllTasks.txt", "DnaCaptureVarBam/sSingle.txt");
        String filePath = String.format("output/sh_files/DnaCaptureVar_Bam_%s_for_GA5_analysis.sh", task);
        final String expectedCmd = templateEngine.process(template, context);

        assertEquals(expectedCmd.trim(), getCmd(filePath).trim());
    }

    private static Stream<Arguments> initToolAndTemplateNotNAAllTasks() {
        return Stream.of(
                Arguments.of("contEst", CONT_EST_FOR_GA_51_ANALYSIS_TEST),
                Arguments.of("exomecnv", EXOMECNV_FOR_GA_51_ANALYSIS_TEST),
                Arguments.of("lofreq", LOFREQ_FOR_GA_51_ANALYSIS_TEST),
                Arguments.of("mutect2", MUTECT_2_FOR_GA_51_ANALYSIS_TEST),
                Arguments.of("scalpel", SCALPEL_FOR_GA_51_ANALYSIS_TEST),
                Arguments.of("sequenza", SEQUENZA_FOR_GA_51_ANALYSIS_TEST),
                Arguments.of("strelka2", STRELKA_2_FOR_GA_51_ANALYSIS_TEST),
                Arguments.of("vardict", VARDICT_FOR_GA_51_ANALYSIS_TEST),
                Arguments.of("variantDetection", VARIANT_DETECTION_FOR_GA_51_ANALYSIS_TEST)
        );
    }

    @DataProvider
    public static Object[][] controlSampleNAAllTasks() {
        return new Object[][] {
            {"vardict",
                new String[] {
                    "Begin Step: Vardict detection...",
                    "/VarDict/var2vcf_valid.pl",
                    "/usr/bin/vardict/build/install/VarDict/bin/VarDict " +
                            "-G /ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa",
                    "Begin Step: Remove temporary directories..."},
                new String[] {
                    PYTHON_VCF_SNPEFF_ANNOTATION}},

            {"mutect1", new String[] {
                "Begin Step: Mutect1 detection...",
                "/usr/lib/jvm/java-8-openjdk-amd64/bin/java -Xmx10g -jar /usr/bin/mutect",
                "--analysis_type MuTect " +
                        "--reference_sequence /ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa",
                "--dbsnp /ngs/data/db/mutect.dbsnp --cosmic /ngs/data/db/cosmic --normal_panel",
                "--vcf build/resources/integrationTest/output/GA5/mutect1/GA5.mutect1.variants.vcf",
                ""},
                new String[] {
                    PYTHON_VCF_SNPEFF_ANNOTATION}},

            {"strelka2",
                new String[] {
                    "Begin Step: Strelka2 detection...",
                    "/usr/bin/python /usr/bin/strelka2/configureStrelkaGermlineWorkflow.py",
                    "--referenceFasta=/ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa",
                    "/usr/bin/python build/resources/integrationTest/output/GA5/strelka2/runWorkflow.py " +
                            "-m local -j 8",
                    "zcat build/resources/integrationTest/output/GA5/strelka2/results/variants/variants.vcf.gz"},
                new String[] {
                    PYTHON_VCF_SNPEFF_ANNOTATION}},

            {"scalpel",
                new String[] {
                    "Begin Step: Scalpel detection...",
                    "/usr/bin/scalpel/scalpel-discovery --single " +
                            "--ref /ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa",
                    "--bed /ngs/data/S03723314_Padded.bed"},
                new String[] {
                    PYTHON_VCF_SNPEFF_ANNOTATION}},

            {"freebayes",
                new String[] {
                    "Begin Step: Freebayes detection...",
                    "/usr/bin/freebayes -f /ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa",
                    "Begin Step: Remove temporary directories..."},
                new String[] {
                    PYTHON_VCF_SNPEFF_ANNOTATION}},

            {"gatkHaplotypeCaller",
                new String[] {
                    "Begin Step: GATK haplotypecaller detection...",
                    "-jar /usr/bin/gatk -T HaplotypeCaller " +
                            "-R /ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa " +
                            "-mmq 20 --intervals /ngs/data/S03723314_Padded.bed " +
                            "--input_file /ngs/data/demo/test/fastq_data/GA5_0001_L002_R1_003.bam " +
                            "--out build/resources/integrationTest/output/GA5/gatkHaplotypeCaller/" +
                            "GA5.gatkHaplotypeCaller.variants.vcf " +
                            "--validation_strictness SILENT",
                    "output/GA5/gatkHaplotypeCaller/GA5.gatkHaplotypeCaller.variants.vcf",
                    "output/GA5/gatkHaplotypeCaller/GA5.gatkHaplotypeCaller.variants.pass.annotation.tsv",
                    "Begin Step: Remove temporary directories..."},
                new String[] {
                    PYTHON_VCF_SNPEFF_ANNOTATION}},

            {"lofreq",
                new String[] {
                    "Begin Step: Lofreq detection...",
                    "/usr/bin/lofreq call -f /ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa",
                    "-o build/resources/integrationTest/output/GA5/lofreq/GA5.lofreq.variants.vcf",
                    "-l /ngs/data/S03723314_Padded.bed",
                    "-o build/resources/integrationTest/output/GA5/lofreq/GA5.lofreq.variants.pass.annotation.tsv"},
                new String[] {
                    PYTHON_VCF_SNPEFF_ANNOTATION}},
        };
    }

    @Test
    @UseDataProvider("controlSampleNotNAAllTasks")
    public void testControlSampleNotNAAllTasks(String task, String[] expectedStrings, String[] expectedPatterns)
            throws IOException {
        startAppWithConfigs(
                "DnaCaptureVarBam/gAllTasksSampleNotNA.txt",
                "DnaCaptureVarBam/sControlSampleNotNA.txt");

        //tests for toolsets
        String filePath = "output/sh_files/DnaCaptureVar_Bam_" + task + "_for_GA51_analysis.sh";
        File outputShFile = new File(this.getClass().getClassLoader().getResource(filePath).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            for (String expectedString : expectedStrings) {
                assertTrue(lines.stream().anyMatch(line -> line.contains(expectedString)));
            }
            for (String expectedPattern : expectedPatterns) {
                assertTrue(lines.stream().anyMatch(line -> line.matches(expectedPattern)));
            }
            assertTrue(lines.stream().noneMatch(line -> line.contains(NULL)));
        }

        //tests for variant detection
        filePath = "output/sh_files/DnaCaptureVar_Bam_variantDetection_for_GA51_analysis.sh";
        outputShFile = new File(this.getClass().getClassLoader().getResource(filePath).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    QSUB + SH_FILES + "DnaCaptureVar_Bam_contEst_for_GA51_analysis.sh")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    QSUB + SH_FILES + "DnaCaptureVar_Bam_vardict_for_GA51_analysis.sh")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    QSUB + SH_FILES + "DnaCaptureVar_Bam_strelka2_for_GA51_analysis.sh")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    QSUB + SH_FILES + "DnaCaptureVar_Bam_mutect2_for_GA51_analysis.sh")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    QSUB + SH_FILES + "DnaCaptureVar_Bam_lofreq_for_GA51_analysis.sh")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    QSUB + SH_FILES + "DnaCaptureVar_Bam_sequenza_for_GA51_analysis.sh")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    QSUB + SH_FILES + "DnaCaptureVar_Bam_exomecnv_for_GA51_analysis.sh")));
            assertTrue(lines.stream().noneMatch(line -> line.contains(NULL)));
        }
        Assertions.assertAll(
                () -> assertTrue(new File(format("%s%s/err_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
                () -> assertTrue(new File(format("%s%s/GA51", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
                () -> assertTrue(new File(format("%s%s/GA52", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
                () -> assertTrue(new File(format("%s%s/log_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
                () -> assertTrue(new File(format("%s%s/sh_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists())
        );
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @DataProvider
    public static Object[][] controlSampleNotNAAllTasks() {
        return new Object[][] {
            {"vardict",
                new String[] {
                    "Begin Step: Vardict detection...",
                    "/usr/bin/vardict/build/install/VarDict/bin/VarDict " +
                            "-G /ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa " +
                            "-f 0.05 -r 3 -Q 20 -N GA51 -b \"/ngs/data/demo/test/fastq_data/GA5_0001_L002_R1_003.bam|" +
                            "/ngs/data/demo/test/fastq_data/GA5_0001_L002_R1_003.bam\" -z 1 -c 1 -S 2 -E 3 " +
                            "/ngs/data/S03723314_Padded.bed | " +
                            "/usr/bin/vardict/VarDict/testsomatic.R |/usr/bin/vardict/VarDict/var2vcf_paired.pl -M " +
                            "-N \"GA51|GA52\" -f 0.05 > " +
                            "build/resources/integrationTest/output/GA51/vardict/GA51.vardict.somatic.variants.vcf",
                    "build/resources/integrationTest/output/GA51/vardict/" +
                            "GA51.vardict.somatic.variants.pass.annotation.tsv "},
                new String[] {
                    PYTHON_VCF_SNPEFF_ANNOTATION}},

            {"strelka2",
                new String[] {
                    "Begin Step: Strelka2 detection...",
                    "/usr/bin/python /usr/bin/strelka2/configureStrelkaSomaticWorkflow.py " +
                            "--referenceFasta=/ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa " +
                            "--tumorBam=/ngs/data/demo/test/fastq_data/GA5_0001_L002_R1_003.bam " +
                            "--normalBam=/ngs/data/demo/test/fastq_data/GA5_0001_L002_R1_003.bam " +
                            "--callRegions=/ngs/data/S03723314_Padded.bed " +
                            "--runDir=build/resources/integrationTest/output/GA51/strelka2 --callMemMb=2048 --exome",
                    "/usr/bin/python build/resources/integrationTest/output/GA51/strelka2/runWorkflow.py -m local -j 8",
                    "gunzip -c build/resources/integrationTest/output/GA51/strelka2/results/variants/" +
                            "somatic.snvs.vcf.gz",
                    "gunzip -c build/resources/integrationTest/output/GA51/strelka2/results/variants/" +
                            "somatic.indels.vcf.gz",
                    "grep -v ^# build/resources/integrationTest/output/GA51/strelka2/results/variants/" +
                            "somatic.indels.vcf",
                    "rm build/resources/integrationTest/output/GA51/strelka2/results/variants/somatic.snvs.vcf"},
                new String[] {
                    PYTHON_VCF_SNPEFF_ANNOTATION}},

            {"scalpel",
                new String[] {
                    "Begin Step: Scalpel detection...",
                    "/usr/bin/scalpel/scalpel-discovery --somatic " +
                            "--ref /ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa " +
                            "--bed /ngs/data/S03723314_Padded.bed " +
                            "--normal /ngs/data/demo/test/fastq_data/GA5_0001_L002_R1_003.bam " +
                            "--tumor /ngs/data/demo/test/fastq_data/GA5_0001_L002_R1_003.bam " +
                            "--dir build/resources/integrationTest/output/GA51/scalpel --format vcf --mapscore 10 " +
                            "--numprocs 8",
                    "/usr/bin/scalpel/scalpel-export --somatic " +
                            "--db build/resources/integrationTest/output/GA51/scalpel/main/somatic.db " +
                            "--ref /ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa " +
                            "--bed /ngs/data/S03723314_Padded.bed " +
                            "--output-format vcf --variant-type indel --min-alt-count-tumor 5 " +
                            "--max-alt-count-normal 3 --min-vaf-tumor 0.05 --max-vaf-normal 0.02 " +
                            "--min-coverage-tumor 10 > build/resources/integrationTest/output/GA51/scalpel/" +
                            "GA51.scalpel.somatic.variants.vcf"},
                new String[] {
                    PYTHON_VCF_SNPEFF_ANNOTATION}},

            {"lofreq",
                new String[] {
                    "Begin Step: Lofreq detection...",
                    "/usr/bin/lofreq somatic -f /ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa --min-cov 3 " +
                            "--call-indels -n /ngs/data/demo/test/fastq_data/GA5_0001_L002_R1_003.bam " +
                            "-t /ngs/data/demo/test/fastq_data/GA5_0001_L002_R1_003.bam " +
                            "-l /ngs/data/S03723314_Padded.bed " +
                            "--threads 4 -o build/resources/integrationTest/output/GA51/lofreq/GA51.",
                    "gunzip -c build/resources/integrationTest/output/GA51/lofreq/GA51.somatic_final.snvs.vcf.gz",
                    "gunzip -c build/resources/integrationTest/output/GA51/lofreq/GA51.somatic_final.indels.vcf.gz",
                    "grep -v \"^#\" build/resources/integrationTest/output/GA51/lofreq/GA51.somatic_final.indels.vcf"},
                new String[] {
                    PYTHON_VCF_SNPEFF_ANNOTATION}},

            {"exomecnv",
                new String[] {
                    "echo `date` Begin Step: ExomeCNV detection...",
                    "/usr/lib/jvm/java-8-openjdk-amd64/bin/java -Xmx10g -jar /usr/bin/gatk -T DepthOfCoverage " +
                            "-R /ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa" +
                            " -I /ngs/data/demo/test/fastq_data/GA5_0001_L002_R1_003.bam " +
                            "-L /ngs/data/S03723314_Padded.bed " +
                            "-o build/resources/integrationTest/output/GA51/exomecnv/GA52",
                    "/usr/bin/Rscript /usr/bin/exomecnv/exome_cnv.R " +
                            "-t build/resources/integrationTest/output/GA51/exomecnv/GA51.sample_interval_summary",
                    "-o build/resources/integrationTest/output/GA51/exomecnv/GA51"},
                new String[] {}},

            {"sequenza",
                new String[] {
                    "Begin Step: Sequenza detection...",
                    "/opt/samtools/samtools-0.1.19/samtools mpileup -q 10 -B -d 100000 -f " +
                            "/ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa -l " +
                            "/ngs/data/S03723314_Padded.bed " +
                            "/ngs/data/demo/test/fastq_data/GA5_0001_L002_R1_003.bam | " +
                            "gzip > build/resources/integrationTest/output/GA51/pileup/GA52.pileup.gz",
                    "/usr/bin/python /usr/bin/sequenza/sequenza-utils.py pileup2seqz -gc 10 ",
                    "/usr/bin/python /usr/bin/sequenza/sequenza-utils.py seqz-binning -w 50",
                    "/usr/bin/Rscript /usr/bin/sequenza/sequenza.R -i"},
                new String[] {}},

            {"mutect2",
                new String[] {
                    "Begin Step: Mutect2 detection...",
                    "/usr/lib/jvm/java-8-openjdk-amd64/bin/java -Xmx10g " +
                            "-Djava.io.tmpdir=build/resources/integrationTest/output/GA51/mutect2/tmp " +
                            "-jar /usr/bin/gatk " +
                            "-T MuTect2 -R /ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa " +
                            "--intervals /ngs/data/S03723314_Padded.bed " +
                            "--input_file:normal /ngs/data/demo/test/fastq_data/GA5_0001_L002_R1_003.bam " +
                            "--input_file:tumor /ngs/data/demo/test/fastq_data/GA5_0001_L002_R1_003.bam " +
                            "--out build/resources/integrationTest/output/GA51/mutect2/" +
                            "GA51.mutect2.somatic.variants.vcf " +
                            "--validation_strictness SILENT"},
                new String[] {
                    PYTHON_VCF_SNPEFF_ANNOTATION}},

            {"contEst",
                new String[] {
                    "/usr/lib/jvm/java-8-openjdk-amd64/bin/java -Xmx10g " +
                        "-Djava.io.tmpdir=build/resources/integrationTest/output/GA51/contEst/tmp " +
                        "-jar /usr/bin/gatk -T ContEst -R /ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa " +
                        "-L /ngs/data/S03723314_Padded.bed " +
                        "-I:eval /ngs/data/demo/test/fastq_data/GA5_0001_L002_R1_003.bam " +
                        "-I:genotype /ngs/data/demo/test/fastq_data/GA5_0001_L002_R1_003.bam " +
                        "-pf 100 -pc 0.01 -o build/resources/integrationTest/output/GA51/contEst/GA51.contEst.result " +
                        "-isr INTERSECTION --min_mapq 20 -U ALLOW_SEQ_DICT_INCOMPATIBILITY " +
                        "--validation_strictness SILENT"},
            }};
    }

    @Test
    public void testNoTumorOrCase() throws IOException, URISyntaxException {
        startAppWithConfigs(
                "DnaCaptureVarBam/gSingleAllTasks.txt",
                "DnaCaptureVarBam/sSingleNotTumorOrCase.txt");

        List<File> outputFiles = Files.walk(Paths.get(
                Objects.requireNonNull(this.getClass().getClassLoader().getResource(OUTPUT_DIR)).toURI()),
                FileVisitOption.FOLLOW_LINKS)
                .map(Path::toFile)
                .sorted(Comparator.reverseOrder())
                .filter(File::isFile)
                .collect(Collectors.toList());
        assertTrue(outputFiles.isEmpty());
        Assertions.assertAll(
                () -> assertTrue(new File(format("%s%s/err_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
                () -> assertTrue(new File(format("%s%s/log_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
                () -> assertTrue(new File(format("%s%s/sh_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists())
        );
    }




    @Test
    public void testOutputDirectories() throws IOException {
        startAppWithConfigs(
                "DnaCaptureVarBam/gSingleAllTasks.txt",
                "DnaCaptureVarBam/sSingleNotTumorOrCase.txt");
        Assertions.assertAll(
                () -> assertTrue(new File(format("%s%s/err_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
                () -> assertTrue(new File(format("%s%s/log_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
                () -> assertTrue(new File(format("%s%s/sh_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists())
        );
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

}
