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

import com.epam.fonda.entity.configuration.GlobalConfigFormat;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DnaWgsVarBamIntegrationTest extends AbstractIntegrationTest {

    private static final String OUTPUT_DIR_ROOT = "build/resources/integrationTest/";

    private static final String FIRST_PART_OF_THE_TEST_SHELL_SCRIPT_PATH = "output/sh_files/DnaWgsVar_Bam_";

    private static final String DNA_WGS_VAR_BAM_G_ALL_TASKS_SAMPLE_NA_TXT_GLOBAL_CONFIG_PATH =
        "DnaWgsVarBam/gAllTasksSampleNA.txt";
    private static final String DNA_WGS_VAR_BAM_S_SINGLE_TXT_SAMPLE_STUDY_CONFIG_PATH =
        "DnaWgsVarBam/sSingle.txt";

    private static final String DNA_WGS_VAR_BAM_VARIANT_DETECTION_FOR_GA5_ANALYSIS_TEMPLATE_PATH =
        "DnaWgsVarBam/forGA5/DnaWgsVar_Bam_variantDetection_for_GA5_analysis_template.txt";
    private static final String DNA_WGS_VAR_BAM_STRELKA2_FOR_GA5_ANALYSIS_TEMPLATE_PATH =
        "DnaWgsVarBam/forGA5/DnaWgsVar_Bam_strelka2_for_GA5_analysis_template.txt";
    private static final String DNA_WGS_VAR_BAM_FREEBAYES_FOR_GA5_ANALYSIS_TEMPLATE_PATH =
        "DnaWgsVarBam/forGA5/DnaWgsVar_Bam_freebayes_for_GA5_analysis_template.txt";
    private static final String DNA_WGS_VAR_BAM_GATK_HAPLOTYPE_CALLER_FOR_GA5_ANALYSIS_TEMPLATE_PATH =
        "DnaWgsVarBam/forGA5/DnaWgsVar_Bam_gatkHaplotypeCaller_for_GA5_analysis_template.txt";
    private static final String DNA_WGS_VAR_BAM_LOFREQ_FOR_GA5_ANALYSIS_TEMPLATE_PATH =
        "DnaWgsVarBam/forGA5/DnaWgsVar_Bam_lofreq_for_GA5_analysis_template.txt";
    private static final String TEST_SHELL_SCRIPT_PATH_VARIANT_DETECTION_FOR_GA5 =
        "output/sh_files/DnaWgsVar_Bam_variantDetection_for_GA5_analysis.sh";
    private static final String SUFFIX_FOR_GA5_TEST_SHELL_SCRIPT_PATH = "_for_GA5_analysis.sh";

    private static final String DNA_WGS_VAR_BAM_G_ALL_TASKS_SAMPLE_NOT_NA_GLOBAL_CONFIG_PATH =
        "DnaWgsVarBam/gAllTasksSampleNotNA.txt";
    private static final String DNA_WGS_VAR_BAM_S_CONTROL_SAMPLE_NOT_NA_STUDY_CONFIG =
        "DnaWgsVarBam/sControlSampleNotNA.txt";

    private static final String DNA_WGS_VAR_BAM_VARIANT_DETECTION_FOR_GA51_ANALYSIS_TEMPLATE_PATH =
        "DnaWgsVarBam/forGA51/DnaWgsVar_Bam_variantDetection_for_GA51_analysis_template.txt";
    private static final String DNA_WGS_VAR_BAM_CONTEST_FOR_GA51_ANALYSIS_TEMPLATE_PATH =
        "DnaWgsVarBam/forGA51/DnaWgsVar_Bam_contEst_for_GA51_analysis_template.txt";
    private static final String DNA_WGS_VAR_BAM_LOFREQ_FOR_GA51_ANALYSIS_TEMPLATE_PATH =
        "DnaWgsVarBam/forGA51/DnaWgsVar_Bam_lofreq_for_GA51_analysis_template.txt";
    private static final String DNA_WGS_VAR_BAM_MUTEC2_FOR_GA51_ANALYSIS_TEMPLATE_PATH =
        "DnaWgsVarBam/forGA51/DnaWgsVar_Bam_mutect2_for_GA51_analysis_template.txt";
    private static final String DNA_WGS_VAR_BAM_STRELKA2_FOR_GA51_ANALYSIS_TEMPLATE_PATH =
        "DnaWgsVarBam/forGA51/DnaWgsVar_Bam_strelka2_for_GA51_analysis_template.txt";
    private static final String TEST_SHELL_SCRIPT_PATH_VARIANT_DETECTION_FOR_GA51 =
        "output/sh_files/DnaWgsVar_Bam_variantDetection_for_GA51_analysis.sh";
    private static final String SUFFIX_FOR_GA51_TEST_SHELL_SCRIPT_PATH = "_for_GA51_analysis.sh";

    private static final String DNA_WGS_VAR_BAM_VARIANT_DETECTION_FOR_GA52_ANALYSIS_TEMPLATE_PATH =
        "DnaWgsVarBam/forGA52/DnaWgsVar_Bam_variantDetection_for_GA52_analysis_template.txt";
    private static final String DNA_WGS_VAR_BAM_CONTEST_FOR_GA52_ANALYSIS_TEMPLATE_PATH =
        "DnaWgsVarBam/forGA52/DnaWgsVar_Bam_contEst_for_GA52_analysis_template.txt";
    private static final String DNA_WGS_VAR_BAM_LOFREQ_FOR_GA52_ANALYSIS_TEMPLATE_PATH =
        "DnaWgsVarBam/forGA52/DnaWgsVar_Bam_lofreq_for_GA52_analysis_template.txt";
    private static final String DNA_WGS_VAR_BAM_MUTEC2_FOR_GA52_ANALYSIS_TEMPLATE_PATH =
        "DnaWgsVarBam/forGA52/DnaWgsVar_Bam_mutect2_for_GA52_analysis_template.txt";
    private static final String DNA_WGS_VAR_BAM_STRELKA2_FOR_GA52_ANALYSIS_TEMPLATE_PATH =
        "DnaWgsVarBam/forGA52/DnaWgsVar_Bam_strelka2_for_GA52_analysis_template.txt";
    private static final String TEST_SHELL_SCRIPT_PATH_VARIANT_DETECTION_FOR_GA52 =
        "output/sh_files/DnaWgsVar_Bam_variantDetection_for_GA52_analysis.sh";
    private static final String SUFFIX_FOR_GA52_TEST_SHELL_SCRIPT_PATH = "_for_GA52_analysis.sh";

    private static final String STRELKA2_TASK_NAME = GlobalConfigFormat.STRELKA2;
    private static final String FREEBAYES_TASK_NAME = GlobalConfigFormat.FREEBAYES;
    private static final String GATK_HAPLOTYPE_CALLER_TASK_NAME = "gatkHaplotypeCaller";
    private static final String LOFREQ_TASK_NAME = GlobalConfigFormat.LOFREQ;
    private static final String CONTEST_TASK_NAME = "contEst";
    private static final String MUTECT_2_TASK_NAME = "mutect2";

    @ParameterizedTest
    @MethodSource("initParameters")
    void testControlSample(String globalConfigPath, String studyConfigPath, String taskName,
        String suffixForFilePath, String templatePath, String variantDetectionTemplatePath,
        String variantDetectionFilePath) throws IOException, URISyntaxException {
        startAppWithConfigs(globalConfigPath, studyConfigPath);

        String filePath = format("%s%s%s", FIRST_PART_OF_THE_TEST_SHELL_SCRIPT_PATH, taskName, suffixForFilePath);
        String expectedCmd = TEMPLATE_ENGINE.process(templatePath, context);
        assertEquals(expectedCmd.trim(), getCmd(filePath).trim());

        expectedCmd = TEMPLATE_ENGINE.process(variantDetectionTemplatePath, context);
        assertEquals(expectedCmd.trim(), getCmd(variantDetectionFilePath).trim());
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initParameters() {
        return Stream.of(streamOfGA5argumentsNASample(), streamOfGA51argumentsNotNASample(),
            streamOfGA52argumentsNotNASample()).flatMap(s -> s);
    }

    private static Stream<Arguments> streamOfGA5argumentsNASample() {
        return Stream.of(
            Arguments.of(
                DNA_WGS_VAR_BAM_G_ALL_TASKS_SAMPLE_NA_TXT_GLOBAL_CONFIG_PATH,
                DNA_WGS_VAR_BAM_S_SINGLE_TXT_SAMPLE_STUDY_CONFIG_PATH,
                STRELKA2_TASK_NAME,
                SUFFIX_FOR_GA5_TEST_SHELL_SCRIPT_PATH,
                DNA_WGS_VAR_BAM_STRELKA2_FOR_GA5_ANALYSIS_TEMPLATE_PATH,
                DNA_WGS_VAR_BAM_VARIANT_DETECTION_FOR_GA5_ANALYSIS_TEMPLATE_PATH,
                TEST_SHELL_SCRIPT_PATH_VARIANT_DETECTION_FOR_GA5
            ),
            Arguments.of(
                DNA_WGS_VAR_BAM_G_ALL_TASKS_SAMPLE_NA_TXT_GLOBAL_CONFIG_PATH,
                DNA_WGS_VAR_BAM_S_SINGLE_TXT_SAMPLE_STUDY_CONFIG_PATH,
                FREEBAYES_TASK_NAME,
                SUFFIX_FOR_GA5_TEST_SHELL_SCRIPT_PATH,
                DNA_WGS_VAR_BAM_FREEBAYES_FOR_GA5_ANALYSIS_TEMPLATE_PATH,
                DNA_WGS_VAR_BAM_VARIANT_DETECTION_FOR_GA5_ANALYSIS_TEMPLATE_PATH,
                TEST_SHELL_SCRIPT_PATH_VARIANT_DETECTION_FOR_GA5
            ),
            Arguments.of(
                DNA_WGS_VAR_BAM_G_ALL_TASKS_SAMPLE_NA_TXT_GLOBAL_CONFIG_PATH,
                DNA_WGS_VAR_BAM_S_SINGLE_TXT_SAMPLE_STUDY_CONFIG_PATH,
                GATK_HAPLOTYPE_CALLER_TASK_NAME,
                SUFFIX_FOR_GA5_TEST_SHELL_SCRIPT_PATH,
                DNA_WGS_VAR_BAM_GATK_HAPLOTYPE_CALLER_FOR_GA5_ANALYSIS_TEMPLATE_PATH,
                DNA_WGS_VAR_BAM_VARIANT_DETECTION_FOR_GA5_ANALYSIS_TEMPLATE_PATH,
                TEST_SHELL_SCRIPT_PATH_VARIANT_DETECTION_FOR_GA5
            ),
            Arguments.of(
                DNA_WGS_VAR_BAM_G_ALL_TASKS_SAMPLE_NA_TXT_GLOBAL_CONFIG_PATH,
                DNA_WGS_VAR_BAM_S_SINGLE_TXT_SAMPLE_STUDY_CONFIG_PATH,
                LOFREQ_TASK_NAME,
                SUFFIX_FOR_GA5_TEST_SHELL_SCRIPT_PATH,
                DNA_WGS_VAR_BAM_LOFREQ_FOR_GA5_ANALYSIS_TEMPLATE_PATH,
                DNA_WGS_VAR_BAM_VARIANT_DETECTION_FOR_GA5_ANALYSIS_TEMPLATE_PATH,
                TEST_SHELL_SCRIPT_PATH_VARIANT_DETECTION_FOR_GA5
            )
        );
    }

    private static Stream<Arguments> streamOfGA51argumentsNotNASample() {
        return Stream.of(
            Arguments.of(
                DNA_WGS_VAR_BAM_G_ALL_TASKS_SAMPLE_NOT_NA_GLOBAL_CONFIG_PATH,
                DNA_WGS_VAR_BAM_S_CONTROL_SAMPLE_NOT_NA_STUDY_CONFIG,
                CONTEST_TASK_NAME,
                SUFFIX_FOR_GA51_TEST_SHELL_SCRIPT_PATH,
                DNA_WGS_VAR_BAM_CONTEST_FOR_GA51_ANALYSIS_TEMPLATE_PATH,
                DNA_WGS_VAR_BAM_VARIANT_DETECTION_FOR_GA51_ANALYSIS_TEMPLATE_PATH,
                TEST_SHELL_SCRIPT_PATH_VARIANT_DETECTION_FOR_GA51
            ),
            Arguments.of(
                DNA_WGS_VAR_BAM_G_ALL_TASKS_SAMPLE_NOT_NA_GLOBAL_CONFIG_PATH,
                DNA_WGS_VAR_BAM_S_CONTROL_SAMPLE_NOT_NA_STUDY_CONFIG,
                MUTECT_2_TASK_NAME,
                SUFFIX_FOR_GA51_TEST_SHELL_SCRIPT_PATH,
                DNA_WGS_VAR_BAM_MUTEC2_FOR_GA51_ANALYSIS_TEMPLATE_PATH,
                DNA_WGS_VAR_BAM_VARIANT_DETECTION_FOR_GA51_ANALYSIS_TEMPLATE_PATH,
                TEST_SHELL_SCRIPT_PATH_VARIANT_DETECTION_FOR_GA51
            ),
            Arguments.of(
                DNA_WGS_VAR_BAM_G_ALL_TASKS_SAMPLE_NOT_NA_GLOBAL_CONFIG_PATH,
                DNA_WGS_VAR_BAM_S_CONTROL_SAMPLE_NOT_NA_STUDY_CONFIG,
                STRELKA2_TASK_NAME,
                SUFFIX_FOR_GA51_TEST_SHELL_SCRIPT_PATH,
                DNA_WGS_VAR_BAM_STRELKA2_FOR_GA51_ANALYSIS_TEMPLATE_PATH,
                DNA_WGS_VAR_BAM_VARIANT_DETECTION_FOR_GA51_ANALYSIS_TEMPLATE_PATH,
                TEST_SHELL_SCRIPT_PATH_VARIANT_DETECTION_FOR_GA51
            ),
            Arguments.of(
                DNA_WGS_VAR_BAM_G_ALL_TASKS_SAMPLE_NOT_NA_GLOBAL_CONFIG_PATH,
                DNA_WGS_VAR_BAM_S_CONTROL_SAMPLE_NOT_NA_STUDY_CONFIG,
                LOFREQ_TASK_NAME,
                SUFFIX_FOR_GA51_TEST_SHELL_SCRIPT_PATH,
                DNA_WGS_VAR_BAM_LOFREQ_FOR_GA51_ANALYSIS_TEMPLATE_PATH,
                DNA_WGS_VAR_BAM_VARIANT_DETECTION_FOR_GA51_ANALYSIS_TEMPLATE_PATH,
                TEST_SHELL_SCRIPT_PATH_VARIANT_DETECTION_FOR_GA51
            )

        );
    }

    private static Stream<Arguments> streamOfGA52argumentsNotNASample() {
        return Stream.of(
            Arguments.of(
                DNA_WGS_VAR_BAM_G_ALL_TASKS_SAMPLE_NOT_NA_GLOBAL_CONFIG_PATH,
                DNA_WGS_VAR_BAM_S_CONTROL_SAMPLE_NOT_NA_STUDY_CONFIG,
                CONTEST_TASK_NAME,
                SUFFIX_FOR_GA52_TEST_SHELL_SCRIPT_PATH,
                DNA_WGS_VAR_BAM_CONTEST_FOR_GA52_ANALYSIS_TEMPLATE_PATH,
                DNA_WGS_VAR_BAM_VARIANT_DETECTION_FOR_GA52_ANALYSIS_TEMPLATE_PATH,
                TEST_SHELL_SCRIPT_PATH_VARIANT_DETECTION_FOR_GA52
            ),
            Arguments.of(
                DNA_WGS_VAR_BAM_G_ALL_TASKS_SAMPLE_NOT_NA_GLOBAL_CONFIG_PATH,
                DNA_WGS_VAR_BAM_S_CONTROL_SAMPLE_NOT_NA_STUDY_CONFIG,
                MUTECT_2_TASK_NAME,
                SUFFIX_FOR_GA52_TEST_SHELL_SCRIPT_PATH,
                DNA_WGS_VAR_BAM_MUTEC2_FOR_GA52_ANALYSIS_TEMPLATE_PATH,
                DNA_WGS_VAR_BAM_VARIANT_DETECTION_FOR_GA52_ANALYSIS_TEMPLATE_PATH,
                TEST_SHELL_SCRIPT_PATH_VARIANT_DETECTION_FOR_GA52
            ),
            Arguments.of(
                DNA_WGS_VAR_BAM_G_ALL_TASKS_SAMPLE_NOT_NA_GLOBAL_CONFIG_PATH,
                DNA_WGS_VAR_BAM_S_CONTROL_SAMPLE_NOT_NA_STUDY_CONFIG,
                STRELKA2_TASK_NAME,
                SUFFIX_FOR_GA52_TEST_SHELL_SCRIPT_PATH,
                DNA_WGS_VAR_BAM_STRELKA2_FOR_GA52_ANALYSIS_TEMPLATE_PATH,
                DNA_WGS_VAR_BAM_VARIANT_DETECTION_FOR_GA52_ANALYSIS_TEMPLATE_PATH,
                TEST_SHELL_SCRIPT_PATH_VARIANT_DETECTION_FOR_GA52
            ),
            Arguments.of(
                DNA_WGS_VAR_BAM_G_ALL_TASKS_SAMPLE_NOT_NA_GLOBAL_CONFIG_PATH,
                DNA_WGS_VAR_BAM_S_CONTROL_SAMPLE_NOT_NA_STUDY_CONFIG,
                LOFREQ_TASK_NAME,
                SUFFIX_FOR_GA52_TEST_SHELL_SCRIPT_PATH,
                DNA_WGS_VAR_BAM_LOFREQ_FOR_GA52_ANALYSIS_TEMPLATE_PATH,
                DNA_WGS_VAR_BAM_VARIANT_DETECTION_FOR_GA52_ANALYSIS_TEMPLATE_PATH,
                TEST_SHELL_SCRIPT_PATH_VARIANT_DETECTION_FOR_GA52
            )
        );
    }

    @Test
    void testNoTumorOrCase() throws IOException, URISyntaxException {
        startAppWithConfigs("DnaWgsVarBam/gAllTasksSampleNA.txt",
            "DnaWgsVarBam/sSingleNotTumorOrCase.txt");
        List<File> outputFiles = Files.walk(
            Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource(OUTPUT_DIR)).toURI()),
            FileVisitOption.FOLLOW_LINKS)
            .map(Path::toFile)
            .sorted(Comparator.reverseOrder())
            .filter(File::isFile)
            .collect(Collectors.toList());
        assertTrue(outputFiles.isEmpty());
    }

    @Test
    void testDirTreeNotNASample() {
        startAppWithConfigs(DNA_WGS_VAR_BAM_G_ALL_TASKS_SAMPLE_NOT_NA_GLOBAL_CONFIG_PATH,
            DNA_WGS_VAR_BAM_S_CONTROL_SAMPLE_NOT_NA_STUDY_CONFIG);
        assertAll(
            () -> assertTrue(new File(format("%s%s/sh_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/log_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/err_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA51", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA52", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA51/contEst", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA51/contEst/tmp", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA51/lofreq", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA51/mutect2", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA51/mutect2/tmp", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA51/strelka2", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA51/tmp", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA52/contEst", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA52/contEst/tmp", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA52/lofreq", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA52/mutect2", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA52/mutect2/tmp", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA52/strelka2", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA52/tmp", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists())
        );
    }

    @Test
    void testDirTreeNASample() {
        startAppWithConfigs(DNA_WGS_VAR_BAM_G_ALL_TASKS_SAMPLE_NA_TXT_GLOBAL_CONFIG_PATH,
            DNA_WGS_VAR_BAM_S_SINGLE_TXT_SAMPLE_STUDY_CONFIG_PATH);
        assertAll(
            () -> assertTrue(new File(format("%s%s/sh_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/log_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/err_files", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA5", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA5/freebayes", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(
                new File(format("%s%s/GA5/gatkHaplotypeCaller", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(
                new File(format("%s%s/GA5/gatkHaplotypeCaller/tmp", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA5/lofreq", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA5/strelka2", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists()),
            () -> assertTrue(new File(format("%s%s/GA5/tmp", OUTPUT_DIR_ROOT, OUTPUT_DIR)).exists())
        );
    }
}
