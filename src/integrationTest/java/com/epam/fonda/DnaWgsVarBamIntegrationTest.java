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
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

@RunWith(DataProviderRunner.class)
public class DnaWgsVarBamIntegrationTest extends AbstractIntegrationTest {

    private static final String OUTPUT_DIR = "output";
    private static final String QSUB = "qsub ";
    private static final String NULL = "null";
    private static final String SH_FILES = "build/resources/integrationTest/output/sh_files/";
    private static final String PYTHON_VCF_SNPEFF_ANNOTATION =
            ".*?/usr/bin/python.+?vcf_snpeff_annotation.py.+?(\\n|$)";

    @Test
    @UseDataProvider("controlSampleNAAllTasks")
    public void testControlSampleAllTasks(String task, String[] expectedStrings, String[] expectedPatterns)
            throws IOException {
        startAppWithConfigs(
                "DnaWgsVarBam/gAllTasksSampleNA.txt", "DnaWgsVarBam/sSingle.txt");

        //tests for toolsets
        String filePath = "output/sh_files/DnaWgsVar_Bam_" + task + "_for_GA5_analysis.sh";
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
        filePath = "output/sh_files/DnaWgsVar_Bam_variantDetection_for_GA5_analysis.sh";
        outputShFile = new File(this.getClass().getClassLoader().getResource(filePath).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    QSUB + SH_FILES + "DnaWgsVar_Bam_gatkHaplotypeCaller_for_GA5_analysis.sh")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    QSUB + SH_FILES + "DnaWgsVar_Bam_strelka2_for_GA5_analysis.sh")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    QSUB + SH_FILES + "DnaWgsVar_Bam_lofreq_for_GA5_analysis.sh")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    QSUB + SH_FILES + "DnaWgsVar_Bam_freebayes_for_GA5_analysis.sh")));
            assertTrue(lines.stream().noneMatch(line -> line.contains(NULL)));
        }

        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @DataProvider
    public static Object[][] controlSampleNAAllTasks() {
        return new Object[][] {
            {"strelka2",
                new String[] {
                    "Begin Step: Strelka2 detection...",
                    "/usr/bin/python /usr/bin/strelka2/configureStrelkaGermlineWorkflow.py",
                    "--referenceFasta=/ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa",
                    "/usr/bin/python build/resources/integrationTest/output/GA5/strelka2/runWorkflow.py -m local -j 8",
                    "--runDir=build/resources/integrationTest/output/GA5/strelka2 --callMemMb=10240",
                    "zcat build/resources/integrationTest/output/GA5/strelka2/results/variants/variants.vcf.gz",
                    "-r hg19 --snpsift /usr/bin/snpsift --snpsift_db /ngs/data/SnpEff/snpEff_v4.3p/snpEff/db --dbnsfp"},
                new String[] {
                    PYTHON_VCF_SNPEFF_ANNOTATION}},

            {"freebayes",
                new String[] {
                    "Begin Step: Freebayes detection...",
                    "/usr/bin/freebayes -f /ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa -F 0.05",
                    "-m 20 /ngs/data/demo/test/fastq_data/GA5_0001_L002_R1_003.bam > " +
                            "build/resources/integrationTest/output/GA5/freebayes/GA5.freebayes.variants.vcf",
                    "python/vcf_snpeff_annotation.py -s GA5 " +
                            "-i build/resources/integrationTest/output/GA5/freebayes/GA5.freebayes.variants.vcf",
                    "--transvar /usr/bin/transvar -r hg19 --snpsift /usr/bin/snpsift " +
                            "--snpsift_db /ngs/data/SnpEff/snpEff_v4.3p/snpEff/db",
                    "-t freebayes --canonical /ngs/data/reference_genome/GRCh37/Annotation/" +
                            "prefer_ensembl_transcript.txt",
                    "-o build/resources/integrationTest/output/GA5/freebayes/" +
                            "GA5.freebayes.variants.pass.annotation.tsv"},
                new String[] {
                    PYTHON_VCF_SNPEFF_ANNOTATION}},

            {"gatkHaplotypeCaller",
                new String[] {
                    "Begin Step: GATK haplotypecaller detection...",
                    "/usr/lib/jvm/java-8-openjdk-amd64/bin/java -Xmx10g " +
                            "-Djava.io.tmpdir=build/resources/integrationTest/output/GA5/gatkHaplotypeCaller/tmp",
                    "-jar /usr/bin/gatk -T HaplotypeCaller " +
                            "-R /ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa -mmq 20",
                    "--input_file /ngs/data/demo/test/fastq_data/GA5_0001_L002_R1_003.bam",
                    "--out build/resources/integrationTest/output/GA5/gatkHaplotypeCaller/" +
                            "GA5.gatkHaplotypeCaller.variants.vcf --validation_strictness SILENT",
                    "-i build/resources/integrationTest/output/GA5/gatkHaplotypeCaller/" +
                            "GA5.gatkHaplotypeCaller.variants.vcf",
                    "-t gatkHaplotypeCaller --canonical /ngs/data/reference_genome/GRCh37/Annotation/" +
                            "prefer_ensembl_transcript.txt",
                    "-r hg19 --snpsift /usr/bin/snpsift --snpsift_db /ngs/data/SnpEff/snpEff_v4.3p/snpEff/db --dbnsfp"},
                new String[] {
                    PYTHON_VCF_SNPEFF_ANNOTATION}},

            {"lofreq",
                new String[] {
                    "Begin Step: Lofreq detection...",
                    "/usr/bin/lofreq call -f /ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa",
                    "--sig 0.05 --call-indels " +
                            "-o build/resources/integrationTest/output/GA5/lofreq/GA5.lofreq.variants.vcf",
                    "GA5 -i build/resources/integrationTest/output/GA5/lofreq/GA5.lofreq.variants.vcf",
                    "-t lofreq --canonical /ngs/data/reference_genome/GRCh37/Annotation/prefer_ensembl_transcript.txt",
                    "-o build/resources/integrationTest/output/GA5/lofreq/GA5.lofreq.variants.pass.annotation.tsv " +
                            "-t lofreq"},
                new String[] {
                    PYTHON_VCF_SNPEFF_ANNOTATION}},
        };
    }

    @Test
    @UseDataProvider("controlSampleNotNAAllTasks")
    public void testControlSampleNotNATasks(String task, String[] expectedStrings, String[] expectedPatterns)
            throws IOException {
        startAppWithConfigs("DnaWgsVarBam/gAllTasksSampleNotNA.txt",
                "DnaWgsVarBam/sControlSampleNotNA.txt");

        //tests for toolsets
        String filePath = "output/sh_files/DnaWgsVar_Bam_" + task + "_for_GA51_analysis.sh";
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
        filePath = "output/sh_files/DnaWgsVar_Bam_variantDetection_for_GA51_analysis.sh";
        outputShFile = new File(this.getClass().getClassLoader().getResource(filePath).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    QSUB + SH_FILES + "DnaWgsVar_Bam_contEst_for_GA51_analysis.sh")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    QSUB + SH_FILES + "DnaWgsVar_Bam_mutect2_for_GA51_analysis.sh")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    QSUB + SH_FILES + "DnaWgsVar_Bam_strelka2_for_GA51_analysis.sh")));
            assertTrue(lines.stream().anyMatch(line -> line.contains(
                    QSUB + SH_FILES + "DnaWgsVar_Bam_lofreq_for_GA51_analysis.sh")));
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
                    "-T ContEst -R /ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa -I:eval " +
                        "/ngs/data/demo/test/fastq_data/GA5_0001_L002_R1_003.bam -I:genotype " +
                        "/ngs/data/demo/test/fastq_data/GA5_0001_L002_R1_003.bam -pf 100 " +
                        "-pc 0.01 -o build/resources/integrationTest/output/GA51/contEst/GA51.contEst.result -isr " +
                        "INTERSECTION --min_mapq 20 -U ALLOW_SEQ_DICT_INCOMPATIBILITY --validation_strictness SILENT"},
                new String[] {""}},

            {"mutect2",
                new String[] {
                    "Begin Step: Mutect2 detection...",
                    "/usr/lib/jvm/java-8-openjdk-amd64/bin/java -Xmx10g -Djava.io.tmpdir=",
                    "-jar /usr/bin/gatk -T MuTect2",
                    "--out build/resources/integrationTest/output/GA51/mutect2/GA51.mutect2.somatic.variants.vcf",
                    "Begin Step: Remove temporary directories..."},
                new String[] {
                    PYTHON_VCF_SNPEFF_ANNOTATION}},

            {"strelka2",
                new String[] {
                    "Begin Step: Strelka2 detection...",
                    "/usr/bin/python /usr/bin/strelka2/configureStrelkaSomaticWorkflow.py",
                    "--referenceFasta=/ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa",
                    "/usr/bin/python build/resources/integrationTest/output/GA51/strelka2/runWorkflow.py -m local -j 8",
                    "gunzip -c build/resources/integrationTest/output/GA51/strelka2/results/variants/" +
                            "somatic.snvs.vcf.gz",
                    "rm build/resources/integrationTest/output/GA51/strelka2/results/variants/somatic.snvs.vcf",
                    "rm build/resources/integrationTest/output/GA51/strelka2/results/variants/somatic.indels.vcf",
                    "gunzip -c build/resources/integrationTest/output/GA51/strelka2/results/variants/" +
                            "somatic.indels.vcf.gz"},
                new String[] {
                    PYTHON_VCF_SNPEFF_ANNOTATION}},

            {"lofreq",
                new String[] {
                    "Begin Step: Lofreq detection...",
                    "/usr/bin/lofreq somatic -f /ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa",
                    "--threads 4 -o build/resources/integrationTest/output/GA51/lofreq/GA51.",
                    "gunzip -c build/resources/integrationTest/output/GA51/lofreq/GA51.somatic_final.snvs.vcf.gz",
                    "gunzip -c build/resources/integrationTest/output/GA51/lofreq/GA51.somatic_final.indels.vcf.gz",
                    "grep -v \"^#\" build/resources/integrationTest/output/GA51/lofreq/GA51.somatic_final.indels.vcf"},
                new String[] {
                    PYTHON_VCF_SNPEFF_ANNOTATION}},
        };
    }

    @Test
    public void testNoTumorOrCase() throws IOException {
        startAppWithConfigs("DnaWgsVarBam/gAllTasksSampleNA.txt",
                "DnaWgsVarBam/sSingleNotTumorOrCase.txt");

        List<File> outputFiles = Files.walk(Paths.get(
                this.getClass().getClassLoader().getResource(OUTPUT_DIR).getPath()),
                FileVisitOption.FOLLOW_LINKS)
                .map(Path::toFile)
                .sorted(Comparator.reverseOrder())
                .filter(File::isFile)
                .collect(Collectors.toList());
        assertTrue(outputFiles.isEmpty());
    }
}
