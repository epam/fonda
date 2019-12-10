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

import static org.junit.Assert.assertTrue;

@RunWith(DataProviderRunner.class)
public class DnaAnalysisIntegrationTest extends AbstractIntegrationTest {
    private static final String OUTPUT_DIR = "output";
    private static final String AMPLICON = "Amplicon";
    private static final String CAPTURE = "Capture";
    private static final String LOG_FILE_AMPLICON_FORMAT =
            "logFile=build/resources/integrationTest/output/log_files/DnaAmpliconVar_Fastq_%s_for_GA5_analysis.log";
    private static final String LOG_FILE_CAPTURE_FORMAT =
            "logFile=build/resources/integrationTest/output/log_files/DnaCaptureVar_Fastq_%s_for_GA5_analysis.log";
    private static final String ERROR_MESSAGE = "An error occurred with task %s. Expected: %s";
    private static final String VARDICT = "vardict";
    private static final String MUTECT1 = "mutect1";
    private static final String MUTECT2 = "mutect2";
    private static final String LOFREQ = "lofreq";
    private static final String STRELKA2 = "strelka2";
    private static final String GATK_HAPLOTYPE_CALLER = "gatkHaplotypeCaller";
    private static final String SCALPEL = "scalpel";

    @Test
    @UseDataProvider("periodicDnaMutationStatusAllTasks")
    public void testPeriodicDnaMutationStatusCheck(String workflow, String task, String[] expectedStrings)
            throws IOException {
        startAppWithConfigs("DnaAnalysis/gSingleDna" + workflow + ".txt",
                "DnaAnalysis/sSingle.txt");

        String outputShFilePath = "output/sh_files/Dna" + workflow + "Var_Fastq_mergeMutation_for_cohort_analysis.sh";
        File outputShFile = new File(this.getClass().getClassLoader().getResource(outputShFilePath).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertTrue(lines.stream()
                    .anyMatch(line -> line.contains("echo $(date) Error DNA mutation results from GA5:")));
            assertTrue(lines.stream()
                    .anyMatch(line -> line.contains("echo $(date) Confirm DNA mutation results from GA5")));

            for (String expectedString : expectedStrings) {
                assertTrue(String.format(ERROR_MESSAGE, task, expectedString),
                        lines.stream().anyMatch(line -> line.contains(expectedString)));
                assertTrue(String.format(ERROR_MESSAGE, task, expectedString),
                        lines.stream().anyMatch(line -> line.contains(task)));
            }
            assertTrue(lines.stream().noneMatch(line -> line.contains("null")));
        }
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @DataProvider
    public static Object[][] periodicDnaMutationStatusAllTasks() {
        return new Object[][]{
            {AMPLICON, VARDICT, new String[]{ String.format(LOG_FILE_AMPLICON_FORMAT, VARDICT)}},
            {AMPLICON, MUTECT1, new String[]{ String.format(LOG_FILE_AMPLICON_FORMAT, MUTECT1)}},
            {AMPLICON, MUTECT2, new String[]{ String.format(LOG_FILE_AMPLICON_FORMAT, MUTECT2)}},
            {AMPLICON, LOFREQ, new String[]{ String.format(LOG_FILE_AMPLICON_FORMAT, LOFREQ)}},
            {AMPLICON, STRELKA2, new String[]{ String.format(LOG_FILE_AMPLICON_FORMAT, STRELKA2)}},
            {AMPLICON, GATK_HAPLOTYPE_CALLER, new String[]{ String.format(LOG_FILE_AMPLICON_FORMAT,
                    GATK_HAPLOTYPE_CALLER)}},
            {AMPLICON, SCALPEL, new String[]{ String.format(LOG_FILE_AMPLICON_FORMAT, SCALPEL)}},
            {CAPTURE, VARDICT, new String [] { String.format(LOG_FILE_CAPTURE_FORMAT, VARDICT)}},
            {CAPTURE, MUTECT1, new String [] { String.format(LOG_FILE_CAPTURE_FORMAT, MUTECT1)}},
            {CAPTURE, MUTECT2, new String [] { String.format(LOG_FILE_CAPTURE_FORMAT, MUTECT2)}},
            {CAPTURE, LOFREQ, new String [] { String.format(LOG_FILE_CAPTURE_FORMAT, LOFREQ)}},
            {CAPTURE, STRELKA2, new String [] { String.format(LOG_FILE_CAPTURE_FORMAT, STRELKA2)}},
            {CAPTURE, GATK_HAPLOTYPE_CALLER, new String [] { String.format(LOG_FILE_CAPTURE_FORMAT,
                    GATK_HAPLOTYPE_CALLER)}},
            {CAPTURE, SCALPEL, new String [] { String.format(LOG_FILE_CAPTURE_FORMAT, SCALPEL)}},
        };
    }

    @Test
    @UseDataProvider("workflows")
    public void testDnaMutationDataAnalysis(String workflow) throws IOException {
        startAppWithConfigs(
                "DnaAnalysis/gSingleDna" + workflow + ".txt", "DnaAnalysis/sSingle.txt");

        String outputShFilePath = "output/sh_files/Dna" + workflow + "Var_Fastq_mergeMutation_for_cohort_analysis.sh";
        File outputShFile = new File(this.getClass().getClassLoader().getResource(outputShFilePath).getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            assertTrue(lines.stream().anyMatch(line -> line.contains("Begin Step: Merge mutation annotation...")));
            assertTrue(lines.stream()
                    .anyMatch(line -> line.matches("/usr/bin/Rscript.+?dna_rna_mutation_data_analysis.R -i.+?")));
            assertTrue(lines.stream()
                    .anyMatch(line -> line.contains("build/resources/integrationTest/fastqSingle.tsv " +
                            "-d build/resources/integrationTest/output -t "
                            + "bwa+vardict+mutect1+mutect2+strelka2+gatkHaplotypeCaller+scalpel+lofreq")));
            assertTrue(lines.stream()
                    .anyMatch(line -> line.contains("\techo `date` Successful Step: Merge mutation annotation.")));
            assertTrue(lines.stream().noneMatch(line -> line.contains("null")));
        }
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @DataProvider
    public static Object[][] workflows() {
        return new Object[][]{
                {AMPLICON},
                {CAPTURE}
        };
    }
}
