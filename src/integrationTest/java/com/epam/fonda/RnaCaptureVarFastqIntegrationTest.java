package com.epam.fonda;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RnaCaptureVarFastqIntegrationTest extends AbstractIntegrationTest {
    private static final String RNA_CAPTURE_VAR_FASTQ_DIR = "RnaCaptureVarFastq";
    private static final String OUTPUT_SH_FILES_DIR = "output/sh_files";
    private static final String ALL_TASKS_WITHOUT_TRIMMOMATIC_PAIRED = "AllTasksWithoutTrimmomaticPaired";
    private static final String TRIMMOMATIC_STAR_PAIRED = "TrimmomaticStarPaired";

    @ParameterizedTest
    @MethodSource("initParameters")
    void testRnaCaptureVarFastq(final String gConfigPath, final String sCongfigPath, final String outputShFile,
                                final String templatePath) throws IOException, URISyntaxException {
        startAppWithConfigs(gConfigPath, sCongfigPath);
        final String expectedCmd = templateEngine.process(templatePath, context);
        assertEquals(expectedCmd.trim(), getCmd(outputShFile).trim());
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initParameters() {
        return Stream.of(
                Arguments.of(
                        format("%s/gAllTasksWithoutTrimmomaticPaired.txt", RNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPaired.txt", RNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/RnaCaptureVar_Fastq_alignment_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/rnaCaptureVar_Fastq_alignment_for_GA5_analysis_template",
                                RNA_CAPTURE_VAR_FASTQ_DIR, ALL_TASKS_WITHOUT_TRIMMOMATIC_PAIRED)),
                //=======Not ready for two were mixed fields in GatkHaplotypeCaller и GatkHaplotypeCallerRnaFilter
//                Arguments.of(
//                        format("%s/gAllTasksWithoutTrimmomaticPaired.txt", RNA_CAPTURE_VAR_FASTQ_DIR),
//                        format("%s/sPaired.txt", RNA_CAPTURE_VAR_FASTQ_DIR),
//                        format("%s/RnaCaptureVar_Fastq_gatkHaplotypeCaller_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
//                        format("%s/%s/rnaCaptureVar_Fastq_gatkHaplotypeCaller_for_GA5_analysis_template",
//                                RNA_CAPTURE_VAR_FASTQ_DIR, ALL_TASKS_WITHOUT_TRIMMOMATIC_PAIRED))
                //======
                //test hasn't 1 test mergeMutation_for_cohort_analysis.sh more
                Arguments.of(
                        format("%s/gAllTasksWithoutTrimmomaticPaired.txt", RNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPaired.txt", RNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/RnaCaptureVar_Fastq_qcsummary_for_cohort_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/rnaCaptureVar_Fastq_qcsummary_for_cohort_analysis_template",
                                RNA_CAPTURE_VAR_FASTQ_DIR, ALL_TASKS_WITHOUT_TRIMMOMATIC_PAIRED)),
                Arguments.of(
                        format("%s/gTrimmomaticStarPaired.txt.txt", RNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/sPaired.txt", RNA_CAPTURE_VAR_FASTQ_DIR),
                        format("%s/RnaCaptureVar_Fastq_alignment_for_GA5_analysis.sh", OUTPUT_SH_FILES_DIR),
                        format("%s/%s/rnaCaptureVar_Fastq_alignment_for_GA5_analysis_template",
                                RNA_CAPTURE_VAR_FASTQ_DIR, TRIMMOMATIC_STAR_PAIRED))

        );
    }
}
