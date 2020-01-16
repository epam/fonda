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
public class DnaAmpliconVarBamIntegrationTest extends AbstractIntegrationTest {

    private static final String OUTPUT_DIR = "output";
    private static final String OUTPUT_SH_FILES_PATH = "build/resources/integrationTest/output/sh_files";
    private static final String QSUB = "qsub ";
    private static final String NULL = "null";
    private static final String BEGIN_STEP_REMOVE_DIRECTORIES = "Begin Step: Remove temporary directories...";
    private static final String PYTHON_VCF_SNPEFF_ANNOTATION =
            ".*?/usr/bin/python.+?vcf_snpeff_annotation.py.+?(\\n|$)";

    @Test
    @UseDataProvider("controlSampleNAAllTasks")
    public void testControlSampleNAAllTasks(String task, String[] expectedStrings, String[] expectedPatterns)
            throws IOException {
        startAppWithConfigs("DnaAmpliconVarBam/gSingleAllTasks.txt",
                "DnaAmpliconVarBam/sSingle.txt");

        //tests for toolsets
        String filePath = "output/sh_files/DnaAmpliconVar_Bam_" + task + "_for_GA5_analysis.sh";
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
        filePath = "output/sh_files/DnaAmpliconVar_Bam_variantDetection_for_GA5_analysis.sh";
        outputShFile = new File(this.getClass().getClassLoader().getResource(filePath).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertTrue(lines.stream().anyMatch(line -> line.contains(QSUB + OUTPUT_SH_FILES_PATH +
                    "/DnaAmpliconVar_Bam_freebayes_for_GA5_analysis.sh")));
            assertTrue(lines.stream().noneMatch(line -> line.contains(NULL)));
            assertTrue(lines.stream().anyMatch(line -> line.contains(QSUB + OUTPUT_SH_FILES_PATH +
                    "/DnaAmpliconVar_Bam_mutect1_for_GA5_analysis.sh")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(QSUB + OUTPUT_SH_FILES_PATH +
                    "/DnaAmpliconVar_Bam_strelka2_for_GA5_analysis.sh")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(QSUB + OUTPUT_SH_FILES_PATH +
                    "/DnaAmpliconVar_Bam_gatkHaplotypeCaller_for_GA5_analysis.sh")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(QSUB + OUTPUT_SH_FILES_PATH +
                    "/DnaAmpliconVar_Bam_scalpel_for_GA5_analysis.sh")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(QSUB + OUTPUT_SH_FILES_PATH +
                    "/DnaAmpliconVar_Bam_vardict_for_GA5_analysis.sh")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(QSUB + OUTPUT_SH_FILES_PATH +
                    "/DnaAmpliconVar_Bam_lofreq_for_GA5_analysis.sh")));
        }

        filePath = "output/sh_files/DnaAmpliconVar_Bam_mergeMutation_for_cohort_analysis.sh";
        outputShFile = new File(this.getClass().getClassLoader().getResource(filePath).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: Merge mutation annotation...")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("dna_rna_mutation_data_analysis.R -i " +
                    "build/resources/integrationTest/bamListOneSample.tsv -d build/resources/integrationTest/output " +
                    "-t bwa+vardict+mutect1+strelka2+gatkHaplotypeCaller+scalpel+lofreq+freebayes")));
            assertTrue(lines.stream().noneMatch(line -> line.contains(NULL)));
        }

        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @DataProvider
    public static Object[][] controlSampleNAAllTasks() {
        return new Object[][] {
            {"freebayes",
                new String[] {
                    "Begin Step: Freebayes detection...",
                    "/usr/bin/freebayes -f /ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa",
                    BEGIN_STEP_REMOVE_DIRECTORIES},
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
                    BEGIN_STEP_REMOVE_DIRECTORIES},
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
            {"vardict",
                new String[] {
                    "Begin Step: Vardict detection...",
                    "/VarDict/var2vcf_valid.pl",
                    "/usr/bin/vardict/build/install/VarDict/bin/VarDict " +
                            "-G /ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa",
                    BEGIN_STEP_REMOVE_DIRECTORIES},
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
        };
    }

    @Test
    @UseDataProvider("controlSampleNotNAAllTasks")
    public void testControlSampleNotNAAllTasks(String task, String[] expectedStrings, String[] expectedPatterns)
            throws IOException {
        startAppWithConfigs("DnaAmpliconVarBam/gAllTasksSampleNotNA.txt",
                "DnaAmpliconVarBam/sControlSampleNotNA.txt");

        //tests for toolsets
        String filePath = "output/sh_files/DnaAmpliconVar_Bam_" + task + "_for_GA51_analysis.sh";
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
        filePath = "output/sh_files/DnaAmpliconVar_Bam_variantDetection_for_GA51_analysis.sh";
        outputShFile = new File(this.getClass().getClassLoader().getResource(filePath).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertTrue(lines.stream().anyMatch(line -> line.contains(QSUB + OUTPUT_SH_FILES_PATH +
                    "/DnaAmpliconVar_Bam_contEst_for_GA51_analysis.sh")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(QSUB + OUTPUT_SH_FILES_PATH +
                    "/DnaAmpliconVar_Bam_sequenza_for_GA51_analysis.sh")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(QSUB + OUTPUT_SH_FILES_PATH +
                    "/DnaAmpliconVar_Bam_exomecnv_for_GA51_analysis.sh")));
            assertTrue(lines.stream().noneMatch(line -> line.contains(NULL)));
            assertTrue(lines.stream().anyMatch(line -> line.contains(QSUB + OUTPUT_SH_FILES_PATH +
                    "/DnaAmpliconVar_Bam_mutect2_for_GA51_analysis.sh")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(QSUB + OUTPUT_SH_FILES_PATH +
                    "/DnaAmpliconVar_Bam_strelka2_for_GA51_analysis.sh")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(QSUB + OUTPUT_SH_FILES_PATH +
                    "/DnaAmpliconVar_Bam_scalpel_for_GA51_analysis.sh")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(QSUB + OUTPUT_SH_FILES_PATH +
                    "/DnaAmpliconVar_Bam_vardict_for_GA51_analysis.sh")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(QSUB + OUTPUT_SH_FILES_PATH +
                    "/DnaAmpliconVar_Bam_lofreq_for_GA51_analysis.sh")));
        }

        filePath = "output/sh_files/DnaAmpliconVar_Bam_mergeMutation_for_cohort_analysis.sh";
        outputShFile = new File(this.getClass().getClassLoader().getResource(filePath).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: Merge mutation annotation...")));
            assertTrue(lines.stream().anyMatch(line -> line.contains("dna_rna_mutation_data_analysis.R " +
                    "-i build/resources/integrationTest/bamListSampleNotNA.tsv " +
                    "-d build/resources/integrationTest/output -t bwa+contEst+sequenza+exomecnv+vardict+mutect2+" +
                    "strelka2+scalpel+lofreq")));
            assertTrue(lines.stream().noneMatch(line -> line.contains(NULL)));
        }

        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @DataProvider
    public static Object[][] controlSampleNotNAAllTasks() {
        return new Object[][] {
            {"contEst",
                new String[] {
                    "Begin Step: Contamination estimation...",
                    "/usr/lib/jvm/java-8-openjdk-amd64/bin/java -Xmx10g " +
                            "-Djava.io.tmpdir=build/resources/integrationTest/output/GA51/contEst/tmp",
                    "-pf 100 -pc 0.01 -o build/resources/integrationTest/output/GA51/contEst/GA51.contEst.result",
                    "-isr INTERSECTION --min_mapq 20 -U ALLOW_SEQ_DICT_INCOMPATIBILITY"},
                new String[] { "" }},

            {"exomecnv",
                new String[] {
                    "Begin Step: ExomeCNV detection...",
                    "/usr/lib/jvm/java-8-openjdk-amd64/bin/java -Xmx10g -jar /usr/bin/gatk -T DepthOfCoverage",
                    "-I /ngs/data/demo/test/fastq_data/GA5_0001_L002_R1_003.bam -L " +
                            "/ngs/data/S03723314_Padded.bed " +
                            "-o build/resources/integrationTest/output/GA51/exomecnv/GA51",
                    "/usr/bin/Rscript /usr/bin/exomecnv/exome_cnv.R " +
                            "-t build/resources/integrationTest/output/GA51/exomecnv/GA51.sample_interval_summary " +
                            "-n build/resources/integrationTest/output/GA51/exomecnv/GA52.sample_interval_summary " +
                            "-o build/resources/integrationTest/output/GA51/exomecnv -s GA51"},
                new String[] {""} },

            {"sequenza",
                new String[] {
                    "Begin Step: bam pileup...",
                    "/opt/samtools/samtools-0.1.19/samtools mpileup -q 10 -B -d 100000",
                    "Begin Step: Sequenza detection...",
                    "/usr/bin/Rscript /usr/bin/sequenza/sequenza.R -i " +
                            "build/resources/integrationTest/output/GA51/sequenza/GA51_small.seqz.gz",
                    "/usr/bin/python /usr/bin/sequenza/sequenza-utils.py seqz-binning -w 50 " +
                            "-s build/resources/integrationTest/output/GA51/sequenza/GA51.seqz.gz",
                    "/usr/bin/python /usr/bin/sequenza/sequenza-utils.py pileup2seqz -gc 10 " +
                            "-n build/resources/integrationTest/output/GA51/pileup/GA52.pileup.gz"},
                new String[] {""}},

            {"mutect2",
                new String[] {
                    "Begin Step: Mutect2 detection...",
                    "/usr/lib/jvm/java-8-openjdk-amd64/bin/java -Xmx10g -Djava.io.tmpdir=",
                    "-jar /usr/bin/gatk -T MuTect2",
                    "build/resources/integrationTest/output/GA51/mutect2/GA51.mutect2.somatic.variants.vcf",
                    BEGIN_STEP_REMOVE_DIRECTORIES,
                    "--intervals"},
                new String[] {
                    PYTHON_VCF_SNPEFF_ANNOTATION}},

            {"vardict",
                new String[] {
                    "Begin Step: Vardict detection...",
                    "/VarDict/var2vcf_paired.pl",
                    "/usr/bin/vardict/build/install/VarDict/bin/VarDict " +
                            "-G /ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa",
                    BEGIN_STEP_REMOVE_DIRECTORIES},
                new String[] {
                    PYTHON_VCF_SNPEFF_ANNOTATION}},

            {"strelka2",
                new String[] {
                    "Begin Step: Strelka2 detection...",
                    "/usr/bin/python /usr/bin/strelka2/configureStrelkaSomaticWorkflow.py",
                    "--referenceFasta=/ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa",
                    "/usr/bin/python build/resources/integrationTest/output/GA51/strelka2/runWorkflow.py " +
                            "-m local -j 8",
                    "gunzip -c build/resources/integrationTest/output/GA51/strelka2/results/variants/" +
                            "somatic.snvs.vcf.gz",
                    "rm build/resources/integrationTest/output/GA51/strelka2/results/variants/somatic.snvs.vcf",
                    "rm build/resources/integrationTest/output/GA51/strelka2/results/variants/somatic.indels.vcf",
                    "gunzip -c build/resources/integrationTest/output/GA51/strelka2/results/variants/" +
                            "somatic.indels.vcf.gz"},
                new String[] {
                    PYTHON_VCF_SNPEFF_ANNOTATION}},

            {"scalpel",
                new String[] {
                    "Begin Step: Scalpel detection...",
                    "/usr/bin/scalpel/scalpel-discovery --somatic " +
                            "--ref /ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa",
                    "/usr/bin/scalpel/scalpel-export --somatic --db",
                    "/GA51.scalpel.somatic.variants.vcf",
                    "--bed /ngs/data/S03723314_Padded.bed"},
                new String[] {
                    PYTHON_VCF_SNPEFF_ANNOTATION}},

            {"lofreq",
                new String[] {
                    "Begin Step: Lofreq detection...",
                    "/usr/bin/lofreq somatic -f /ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa",
                    "gunzip -c build/resources/integrationTest/output/GA51/lofreq/GA51.somatic_final.snvs.vcf.gz",
                    "gunzip -c build/resources/integrationTest/output/GA51/lofreq/GA51.somatic_final.indels.vcf.gz",
                    "grep -v \"^#\" build/resources/integrationTest/output/GA51/" +
                            "lofreq/GA51.somatic_final.indels.vcf"},
                new String[] {
                    PYTHON_VCF_SNPEFF_ANNOTATION}},
        };
    }
}
