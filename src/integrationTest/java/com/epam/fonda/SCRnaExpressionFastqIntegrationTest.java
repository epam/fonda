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

import com.epam.fonda.entity.configuration.Configuration;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SCRnaExpressionFastqIntegrationTest extends AbstractIntegrationTest {

    private static final String OUTPUT_DIR_ROOT = "build/resources/integrationTest/";
    private static final String STUDY_CONFIG_PATH = "scRnaExpressionFastq/sPaired.txt";
    private static final String G_RSEM_HISAT2_CONFIG_PATH = "scRnaExpressionFastq/gRsemAndHisat2.txt";
    private static final String G_RSEM_WITHOUT_HISAT2_PATH = "scRnaExpressionFastq/gRsemWithoutHisat2.txt";
    private static final String G_STAR_AND_RSEM_PATH = "scRnaExpressionFastq/gStarAndRsem.txt";
    private static final String G_STAR_WITHOUT_RSEM_PATH = "scRnaExpressionFastq/gStarWithoutRsem.txt";
    private static final String G_PAIRED_NON_FLAG_XENOME = "scRnaExpressionFastq/gPairedNonFlagXenome.txt";

    private static final String ALIGNMENT_FILE = "scRnaExpression_Fastq_alignment_for_pbmc4k_analysis";
    private static final String RSEM_FILE = "scRnaExpression_Fastq_rsem_for_pbmc4k_analysis";
    private static final String QC_SUMMARY_FILE = "scRnaExpression_Fastq_qcsummary_for_cohort_analysis";
    private static final String CUFFLINK_COHORT_FILE = "scRnaExpression_Fastq_cufflinks_for_cohort_analysis";
    private static final String CUFFLINK_PBMC4K_FILE = "scRnaExpression_Fastq_cufflinks_for_pbmc4k_analysis";
    private static final String FEATURE_COUNT_FILE = "scRnaExpression_Fastq_featureCount_for_pbmc4k_analysis";
    private static final String SH_FILE_PATH = "%ssh_files/%s.sh";

    private static final String ALIGNMENT_SH_PATH = format(SH_FILE_PATH, OUTPUT_DIR, ALIGNMENT_FILE);
    private static final String RSEM_SH_PATH = format(SH_FILE_PATH, OUTPUT_DIR, RSEM_FILE);
    private static final String QC_SUMMARY_SH_PATH = format(SH_FILE_PATH, OUTPUT_DIR, QC_SUMMARY_FILE);
    private static final String CUFFLINKS_COHORT_SH_PATH = format(SH_FILE_PATH, OUTPUT_DIR, CUFFLINK_COHORT_FILE);
    private static final String CUFFLINKS_PBMC4K_SH_PATH = format(SH_FILE_PATH, OUTPUT_DIR, CUFFLINK_PBMC4K_FILE);
    private static final String FEATURE_COUNT_SH_PATH = format(SH_FILE_PATH, OUTPUT_DIR, FEATURE_COUNT_FILE);

    private static final String INCOMPABILITY_ERROR_MESSAGE =
            "Error Step: Unfortunately, HISAT2 is not compatible with RSEM, please change either one!";

    @ParameterizedTest
    @MethodSource("initForAlignmentAndRsem")
    public void testAlignmentAndRsem(String globalConfig, String testFolder) throws IOException, URISyntaxException {
        startAppWithConfigs(globalConfig, STUDY_CONFIG_PATH);

        String alignmentTemplatePath = format("scRnaExpressionFastq/%s/%s", testFolder, ALIGNMENT_FILE);
        String rsemTemplatePath = format("scRnaExpressionFastq/%s/%s", testFolder, RSEM_FILE);

        final String expectedAlignmentCmd = templateEngine.process(alignmentTemplatePath, context).trim();
        final String expectedRsemCmd = templateEngine.process(rsemTemplatePath, context).trim();
        final String actualAlignmentCmd = getCmd(ALIGNMENT_SH_PATH).trim();
        final String actualRsemCmd = getCmd(RSEM_SH_PATH).trim();

        assertEquals(expectedAlignmentCmd, actualAlignmentCmd);
        assertEquals(expectedRsemCmd, actualRsemCmd);
        assertDirectories();
    }

    @Test
    public void testStarWithoutRsem() throws IOException, URISyntaxException {
        startAppWithConfigs(G_STAR_WITHOUT_RSEM_PATH, STUDY_CONFIG_PATH);

        String alignmentTemplatePath = format("scRnaExpressionFastq/StarWithoutRsem/%s", ALIGNMENT_FILE);
        String qcSummaryTemplatePath = format("scRnaExpressionFastq/StarWithoutRsem/%s", QC_SUMMARY_FILE);

        final String expectedAlignmentCmd = templateEngine.process(alignmentTemplatePath, context).trim();
        final String expectedQcSummaryCmd = templateEngine.process(qcSummaryTemplatePath, context).trim();
        final String actualAlignmentCmd = getCmd(ALIGNMENT_SH_PATH).trim();
        final String actualQcSummaryCmd = getCmd(QC_SUMMARY_SH_PATH).trim();

        assertEquals(expectedAlignmentCmd, actualAlignmentCmd);
        assertEquals(expectedQcSummaryCmd, actualQcSummaryCmd);
        assertDirectories();
    }

    @Test
    public void testRsemHisat2incompatibility() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                startAppWithIncompatibleTools(G_RSEM_HISAT2_CONFIG_PATH, STUDY_CONFIG_PATH));

        String message = exception.getMessage();
        assertEquals(INCOMPABILITY_ERROR_MESSAGE, message);
    }

    @Test
    public void testFeatureCountCufflinksConversion() throws IOException, URISyntaxException {
        startAppWithConfigs(G_PAIRED_NON_FLAG_XENOME, STUDY_CONFIG_PATH);
        String folderAndFile = "%s/%s";

        String testFolder = "scRnaExpressionFastq/featureCountCufflinksConversion";
        String alignmentTemplatePath = format(folderAndFile, testFolder, ALIGNMENT_FILE);
        String cufflinksCohortTemplatePath = format(folderAndFile, testFolder, CUFFLINK_COHORT_FILE);
        String cufflinksPbmc4kTemplatePath = format(folderAndFile, testFolder, CUFFLINK_PBMC4K_FILE);
        String featureCountTemplatePath = format(folderAndFile, testFolder, FEATURE_COUNT_FILE);

        final String expectedAlignmentCmd = templateEngine.process(alignmentTemplatePath, context).trim();
        final String expectedCufflinksCohortCmd = templateEngine.process(cufflinksCohortTemplatePath, context).trim();
        final String expectedCufflinksPbmc4kCmd = templateEngine.process(cufflinksPbmc4kTemplatePath, context).trim();
        final String expectedFeatureCountCmd = templateEngine.process(featureCountTemplatePath, context).trim();

        final String actualAlignmentCmd = getCmd(ALIGNMENT_SH_PATH).trim();
        final String actualCufflinksCohortCmd = getCmd(CUFFLINKS_COHORT_SH_PATH).trim();
        final String actualCufflinksPbmc4kCmd = getCmd(CUFFLINKS_PBMC4K_SH_PATH).trim();
        final String actualFeatureCountCmd = getCmd(FEATURE_COUNT_SH_PATH).trim();

        assertEquals(expectedAlignmentCmd, actualAlignmentCmd);
        assertEquals(expectedCufflinksCohortCmd, actualCufflinksCohortCmd);
        assertEquals(expectedCufflinksPbmc4kCmd, actualCufflinksPbmc4kCmd);
        assertEquals(expectedFeatureCountCmd, actualFeatureCountCmd);
        assertDirectories();
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initForAlignmentAndRsem() {
        return Stream.of(
                Arguments.of(G_RSEM_WITHOUT_HISAT2_PATH, "RsemWithoutHisat2"),
                Arguments.of(G_STAR_AND_RSEM_PATH, "StarAndRsem")
        );
    }

    private void assertDirectories() {
        assertAll(
            () -> assertTrue(new File(format("%s%s/sh_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/log_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/err_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/pbmc4k", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists())
        );
    }

    private void startAppWithIncompatibleTools(String globalConfigName, String studyConfigName)
            throws IOException, ParseException {

        String globalConfig;
        String studyConfig;
        try {
            globalConfig = Paths.get(this.getClass().getClassLoader().getResource(globalConfigName).toURI()).toString();
            studyConfig = Paths.get(this.getClass().getClassLoader().getResource(studyConfigName).toURI()).toString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Cannot parse globalConfig or StudyConfig " + e);
        }
        String[] args = new String[]{"-test", "-global_config", globalConfig, "-study_config", studyConfig};

        Configuration configuration = new CmdParser().parseArgs(args);
        new FondaLauncher(configuration).launch();
    }
}
