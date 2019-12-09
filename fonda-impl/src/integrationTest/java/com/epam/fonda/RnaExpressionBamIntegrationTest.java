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
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(DataProviderRunner.class)
public class RnaExpressionBamIntegrationTest extends AbstractIntegrationTest {

    private static final String OUTPUT_DIR = "output";
    private static final String QSUB_COMMAND = "qsub build/resources/integrationTest/output/sh_files/";
    private static final String BAM = "/ngs/data/demo/test/fastq_data/GA5_0001_L002_R1_003.bam";

    @DataProvider
    public static Object[][] getRnaExpressionBamExpectedStrings() {
        return new Object[][] {
                { "output/sh_files/RnaExpression_Bam_featureCount_for_GA5_analysis.sh", new String[] {
                    "feature_count -F SAF -M -s 0 -a",
                    "/common/reference_genome/GRCh38/Annotation/Gencode_v26/gencode.v26.annotation.saf -o",
                    "build/resources/integrationTest/output/GA5/feature_count/GA5_featureCount_gene.txt -Q 20 -T 4",
                    BAM
                }},
                { "output/sh_files/RnaExpression_Bam_cufflinks_for_GA5_analysis.sh", new String[] {
                    "/opt/cufflinks/cufflinks-2.2.1.Linux_x86_64/cufflinks ",
                    "--library-type fr-unstranded --num-threads 4 -b ",
                    "/common/reference_genome/GRCh38/Sequence/GRCh38.genome.fa --GTF ",
                    "/common/reference_genome/GRCh38/Annotation/Gencode_v26/gencode.v26.annotation.gtf ",
                    "--output-dir build/resources/integrationTest/output/GA5/cufflinks ",
                    BAM,
                    "mv build/resources/integrationTest/output/GA5/cufflinks/genes.fpkm_tracking ",
                    "build/resources/integrationTest/output/GA5/cufflinks/GA5.cufflinks.gene.expression.results",
                    "mv build/resources/integrationTest/output/GA5/cufflinks/isoforms.fpkm_tracking ",
                    "build/resources/integrationTest/output/GA5/cufflinks/GA5.cufflinks.isoform.expression.results"
                }},
                { "output/sh_files/RnaExpression_Bam_rsem_for_GA5_analysis.sh", new String[] {
                    "Begin Step: RSEM...",
                    "path/to/rsem/rsem-calculate-expression -p 4 --no-bam-output --paired-end --bam " +
                            "--estimate-rspd --seed 12345 " +
                            "/ngs/data/demo/test/fastq_data/GA5_0001_L002_R1_003.bam " +
                            "RSEMINDEX build/resources/integrationTest/output/GA5/rsem/GA5",
                    "mv build/resources/integrationTest/output/GA5/rsem/GA5.genes.results " +
                            "build/resources/integrationTest/output/GA5/rsem/GA5.rsem.gene.expression.results",
                    "mv build/resources/integrationTest/output/GA5/rsem/GA5.isoforms.results " +
                            "build/resources/integrationTest/output/GA5/rsem/GA5.rsem.isoform.expression.results",
                    "Begin Step: RSEM annotation...",
                    "/usr/bin/python",
                    "/main/src/python/rna_gene_annotation.py ",
                    "-t rsem -i build/resources/integrationTest/output/GA5/rsem/GA5.rsem.gene.expression.results ",
                    "-o build/resources/integrationTest/output/GA5/rsem/GA5.rsem.annotate.gene.expression.results ",
                    "-a /common/reference_genome/GRCh38/Annotation/Gencode_v26/gencode.v26.annotation.saf",
                    "mv build/resources/integrationTest/output/GA5/rsem/GA5.rsem.annotate.gene.expression.results ",
                    "build/resources/integrationTest/output/GA5/rsem/GA5.rsem.gene.expression.results"
                }},

                { "output/sh_files/RnaExpression_Bam_stringtie_for_GA5_analysis.sh", new String[] {
                    "path/to/stringtie -p 4 -G " +
                            "/common/reference_genome/GRCh38/Annotation/Gencode_v26/gencode.v26.annotation.gtf -A ",
                    "build/resources/integrationTest/output/GA5/stringtie/GA5.stringtie.gene.expression.results " +
                            "-o build/resources/integrationTest/output/GA5/stringtie/" +
                            "GA5.stringtie.assembly.transcripts.gtf " +
                            BAM
                }},
                { "output/sh_files/RnaExpression_Bam_ExpressionEstimation_for_GA5_analysis.sh", new String[] {
                    QSUB_COMMAND + "RnaExpression_Bam_featureCount_for_GA5_analysis.sh",
                    QSUB_COMMAND + "RnaExpression_Bam_rsem_for_GA5_analysis.sh",
                    QSUB_COMMAND + "RnaExpression_Bam_cufflinks_for_GA5_analysis.sh",
                    QSUB_COMMAND + "RnaExpression_Bam_stringtie_for_GA5_analysis.sh"
                }
                }};
    }

    @Test
    @UseDataProvider("getRnaExpressionBamExpectedStrings")
    public void testFeatureCountRsemCufflinksStringtie(String outputFile, String[] expectedStrings) throws IOException {
        startAppWithConfigs(
                "RnaExpressionBam/gFeatureCountRsemCufflinksStringtie.txt",
                "RnaExpressionBam/sRnaExpressionBam.txt");
        File outputShFile = new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource(outputFile))
                .getPath());
        try (BufferedReader reader = new BufferedReader(new FileReader(outputShFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            for (String expectedString : expectedStrings) {
                assertTrue(lines.stream().anyMatch(line -> line.contains(expectedString)));
            }
            assertFalse(lines.stream().anyMatch(line -> line.contains("null")));
        }

        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }
}
