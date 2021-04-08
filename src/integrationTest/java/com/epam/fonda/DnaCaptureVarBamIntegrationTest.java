/*
 * Copyright 2017-2021 Sanofi and EPAM Systems, Inc. (https://www.epam.com/)
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
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

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DnaCaptureVarBamIntegrationTest extends AbstractIntegrationTest {

    private static final String OUTPUT_DIR_ROOT = "build/resources/integrationTest/";
    private static final String NULL = "null";

    private static final String NA_ALL_TASKS_FOLDER = "DnaCaptureVarBam/testControlSampleNAAllTasks";
    private static final String NA_VARDICT_FOR_GA_5_ANALYSIS_TEST = format(
            "%s/DnaCaptureVar_Bam_vardict_for_GA5_analysis_test", NA_ALL_TASKS_FOLDER);
    private static final String NA_MUTECT_1_FOR_GA_5_ANALYSIS_TEST = format(
            "%s/DnaCaptureVar_Bam_mutect1_for_GA5_analysis_test", NA_ALL_TASKS_FOLDER);
    private static final String NA_STRELKA_2_FOR_GA_5_ANALYSIS_TEST = format(
            "%s/DnaCaptureVar_Bam_strelka2_for_GA5_analysis_test", NA_ALL_TASKS_FOLDER);
    private static final String NA_SCALPEL_FOR_GA_5_ANALYSIS_TEST = format(
            "%s/DnaCaptureVar_Bam_scalpel_for_GA5_analysis_test", NA_ALL_TASKS_FOLDER);
    private static final String NA_FREEBAYES_FOR_GA_5_ANALYSIS_TEST = format(
            "%s/DnaCaptureVar_Bam_freebayes_for_GA5_analysis_test", NA_ALL_TASKS_FOLDER);
    private static final String NA_GATK_HAPLOTYPE_CALLER_FOR_GA_5_ANALYSIS_TEST = format(
            "%s/DnaCaptureVar_Bam_gatkHaplotypeCaller_for_GA5_analysis_test", NA_ALL_TASKS_FOLDER);
    private static final String NA_LOFREQ_FOR_GA_5_ANALYSIS_TEST = format(
            "%s/DnaCaptureVar_Bam_lofreq_for_GA5_analysis_test", NA_ALL_TASKS_FOLDER);
    private static final String NA_VARIANT_DETECTION_FOR_GA_5_ANALYSIS_TEST = format(
            "%s/DnaCaptureVar_Bam_variantDetection_for_GA5_analysis_test", NA_ALL_TASKS_FOLDER);

    private static final String NOT_NA_ALL_TASKS_FOLDER = "DnaCaptureVarBam/testControlSampleNotNAAllTasks";
    private static final String CONT_EST_FOR_GA_51_ANALYSIS_TEST = format(
            "%s/DnaCaptureVar_Bam_contEst_for_GA51_analysis_test", NOT_NA_ALL_TASKS_FOLDER);
    private static final String EXOMECNV_FOR_GA_51_ANALYSIS_TEST = format(
            "%s/DnaCaptureVar_Bam_exomecnv_for_GA51_analysis_test", NOT_NA_ALL_TASKS_FOLDER);
    private static final String LOFREQ_FOR_GA_51_ANALYSIS_TEST = format(
            "%s/DnaCaptureVar_Bam_lofreq_for_GA51_analysis_test", NOT_NA_ALL_TASKS_FOLDER);
    private static final String MUTECT_2_FOR_GA_51_ANALYSIS_TEST = format(
            "%s/DnaCaptureVar_Bam_mutect2_for_GA51_analysis_test", NOT_NA_ALL_TASKS_FOLDER);
    private static final String SCALPEL_FOR_GA_51_ANALYSIS_TEST = format(
            "%s/DnaCaptureVar_Bam_scalpel_for_GA51_analysis_test", NOT_NA_ALL_TASKS_FOLDER);
    private static final String SEQUENZA_FOR_GA_51_ANALYSIS_TEST = format(
            "%s/DnaCaptureVar_Bam_sequenza_for_GA51_analysis_test", NOT_NA_ALL_TASKS_FOLDER);
    private static final String STRELKA_2_FOR_GA_51_ANALYSIS_TEST = format(
            "%s/DnaCaptureVar_Bam_strelka2_for_GA51_analysis_test", NOT_NA_ALL_TASKS_FOLDER);
    private static final String VARDICT_FOR_GA_51_ANALYSIS_TEST = format(
            "%s/DnaCaptureVar_Bam_vardict_for_GA51_analysis_test", NOT_NA_ALL_TASKS_FOLDER);
    private static final String VARIANT_DETECTION_FOR_GA_51_ANALYSIS_TEST = format(
            "%s/DnaCaptureVar_Bam_variantDetection_for_GA51_analysis_test", NOT_NA_ALL_TASKS_FOLDER);

    @ParameterizedTest
    @MethodSource("initTaskAndTemplateNAAllTasks")
    public void testControlSampleNAAllTasks(String task, String template) throws IOException, URISyntaxException {
        startAppWithConfigs("DnaCaptureVarBam/gSingleAllTasks.txt", "DnaCaptureVarBam/sSingle.txt");
        String filePath = format("output/sh_files/DnaCaptureVar_Bam_%s_for_GA5_analysis.sh", task);

        final String expectedCmd = TEMPLATE_ENGINE.process(template, context).trim();
        final String actualCmd = getCmd(filePath).trim();

        assertFalse(actualCmd.contains(NULL));
        assertEquals(expectedCmd, actualCmd);
    }

    @Test
    public void testControlSampleNAAllTasksOutputDir() {
        startAppWithConfigs("DnaCaptureVarBam/gSingleAllTasks.txt", "DnaCaptureVarBam/sSingle.txt");

        assertAll(
            () -> assertTrue(new File(format("%s%s/err_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA5", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/log_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/sh_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists())
        );
    }

    @ParameterizedTest
    @MethodSource("initTaskAndTemplateNotNAAllTasks")
    public void testControlSampleNotNAAllTasks(String task, String template) throws IOException, URISyntaxException {
        startAppWithConfigs("DnaCaptureVarBam/gAllTasksSampleNotNA.txt", "DnaCaptureVarBam/sControlSampleNotNA.txt");
        String filePath = format("output/sh_files/DnaCaptureVar_Bam_%s_for_GA51_analysis.sh", task);

        final String expectedCmd = TEMPLATE_ENGINE.process(template, context).trim();
        final String actualCmd = getCmd(filePath).trim();

        assertFalse(actualCmd.contains(NULL));
        assertEquals(expectedCmd, actualCmd);
    }

    @Test
    public void testControlSampleNotNAAllTasksOutputDir() {
        startAppWithConfigs("DnaCaptureVarBam/gAllTasksSampleNotNA.txt", "DnaCaptureVarBam/sControlSampleNotNA.txt");

        assertAll(
            () -> assertTrue(new File(format("%s%s/err_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA51", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA52", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/log_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/sh_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists())
        );
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

        assertAll(
            () -> assertTrue(outputFiles.isEmpty()),
            () -> assertTrue(new File(format("%s%s/err_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/log_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/sh_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists())
        );
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initTaskAndTemplateNAAllTasks() {
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

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initTaskAndTemplateNotNAAllTasks() {
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
}
