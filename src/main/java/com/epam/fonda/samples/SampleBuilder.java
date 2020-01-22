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

package com.epam.fonda.samples;

import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.entity.configuration.StudyConfig;
import com.epam.fonda.samples.bam.BamFileSample;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.samples.fastq.FastqReadType;
import com.epam.fonda.samples.parameters.Parameters;
import com.epam.fonda.utils.PipelineUtils;
import com.epam.fonda.workflow.PipelineType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * SampleBuilder is the one of the base configuration class
 * which allows an application to build input parameters to {@link FastqFileSample} or {@link BamFileSample} sample. -
 */
@Slf4j
@AllArgsConstructor
public class SampleBuilder {
    private static final String EMPTY_INPUT_EXCEPTION = "Empty input parameters";
    private static final String FASTQ = "fastq";
    private static final String BAM = "bam";
    private static final String DIR_FORMAT = "%s/%s";
    private static final String TMP = "tmp";

    private GlobalConfig globalConfig;
    private StudyConfig studyConfig;

    /**
     * Build list of Fastq samples according to configuration
     *
     * @param rootOutdir the root directory to output
     * @return list of Fastq samples
     * @throws IOException throws {@code IOException} if configuration has none fastq list of files
     */
    public List<FastqFileSample> buildFastqSamples(final String rootOutdir) throws IOException {
        checkConfig(SampleType.FASTQ);
        final String fastqList = studyConfig.getFastqList();
        final Path filePath = checkSampleFile(fastqList);
        return new ArrayList<>(Files.readAllLines(filePath).stream()
                .skip(1)
                .filter(StringUtils::isNotBlank)
                .map(line -> parseFastqLine(line, rootOutdir))
                .collect(Collectors.toMap(FastqFileSample::getName, s -> s, FastqFileSample::merge))
                .values());
    }

    /**
     * Build list of Bam samples according to configuration
     *
     * @return list of Bam samples
     * @throws IOException throws {@code IOException} if configuration has none bam list of files
     */
    public List<BamFileSample> buildBamSamples(final String rootOutdir) throws IOException {
        checkConfig(SampleType.BAM);
        final String bamList = studyConfig.getBamList();
        final Path filePath = checkSampleFile(bamList);
        return Files.readAllLines(filePath).stream()
                .skip(1)
                .filter(StringUtils::isNotBlank)
                .map(line -> parseBamLine(line, rootOutdir))
                .collect(Collectors.toList());
    }

    /**
     * Parse fastq file with samples line by line, returning paired or single sample
     *
     * @param line       one entry of fastq file list
     * @param rootOutdir the root directory to output
     * @return fastq sample
     */
    private FastqFileSample parseFastqLine(final String line, final String rootOutdir) {
        final String[] values = line.trim().split("\\t");
        final String sampleName = values[1];
        final String file1 = values[2];

        checkInputParameters(sampleName, file1);
        final String readType = globalConfig.getPipelineInfo().getReadType();
        if (StringUtils.isBlank(readType) || !FastqReadType.contains(readType)) {
            throw new IllegalArgumentException(format(
                    "Incompatible read type: %s found in study_config file, only paired or single allowed.", readType));
        }

        final String sampleOutputDir = format(DIR_FORMAT, rootOutdir, sampleName);
        FastqFileSample.FastqFileSampleBuilder fileSampleBuilder = FastqFileSample.builder()
                .name(sampleName)
                .fastq1(Collections.singletonList(file1))
                .sampleOutputDir(sampleOutputDir)
                .tmpOutdir(format(DIR_FORMAT, sampleOutputDir, TMP))
                .fastqOutdir(format(DIR_FORMAT, sampleOutputDir, FASTQ))
                .bamOutdir(format(DIR_FORMAT, sampleOutputDir, BAM))
                .qcOutdir(format(DIR_FORMAT, sampleOutputDir, "qc"));
        if (readType.equalsIgnoreCase(FastqReadType.SINGLE.getType())) {
            final Parameters parameters = Parameters.setParameters(FASTQ, values, 3);
            return fileSampleBuilder
                    .type(FastqReadType.SINGLE)
                    .sampleType(parameters.getSampleType())
                    .matchControl(parameters.getMatchControl())
                    .controlName(parameters.getMatchControl())
                    .build();
        }
        String fastq2 = values[3];
        if (readType.equalsIgnoreCase(FastqReadType.PAIRED.getType()) && StringUtils.isAnyBlank(values[3])) {
            throw new IllegalArgumentException("Pair-end fastq files for sample don't exist.");
        }
        final Parameters parameters = Parameters.setParameters(FASTQ, values, 4);
        return fileSampleBuilder
                .fastq2(Collections.singletonList(fastq2))
                .type(FastqReadType.PAIRED)
                .sampleType(parameters.getSampleType())
                .matchControl(parameters.getMatchControl())
                .controlName(parameters.getMatchControl())
                .build();
    }

    /**
     * Parse bam file with samples line by line, returning bam sample
     *
     * @param line       one entry of bam file list
     * @param rootOutdir the root directory to output
     * @return bam sample
     */
    private BamFileSample parseBamLine(final String line, final String rootOutdir) {
        final String[] values = line.trim().split("\\t");
        final String sampleName = values[1];
        final String file1 = values[2];

        checkInputParameters(sampleName, file1);
        final Parameters parameters = Parameters.setParameters(BAM, values, 3);
        setParametersForSpecialConditionsInBam2FastqWorkflow(parameters, values);
        final String outputDir = format(DIR_FORMAT, rootOutdir, sampleName);
        return BamFileSample.builder()
                .name(sampleName)
                .bam(file1)
                .sampleType(parameters.getSampleType())
                .matchControl(parameters.getMatchControl())
                .controlName(parameters.getMatchControl())
                .controlBam(getControlBamName(file1, parameters.getMatchControl(), sampleName))
                .sampleOutputDir(outputDir)
                .tmpOutdir(format(DIR_FORMAT, outputDir, TMP))
                .build();
    }

    /**
     * @param parameters the type of {@link Parameters} which
     *                   contains values of sample
     * @param values     the array of one entry of bam file list
     */
    private void setParametersForSpecialConditionsInBam2FastqWorkflow(Parameters parameters, String[] values) {
        if (globalConfig.getPipelineInfo().getWorkflow().equalsIgnoreCase(PipelineType.BAM_2_FASTQ.getName())
                && values.length == 3) {
            parameters.setSampleType(PipelineUtils.TUMOR);
            parameters.setMatchControl(PipelineUtils.NA);
        }
    }

    /**
     * Checks control bam name according to control name.
     *
     * @param fileName    bam file name
     * @param controlName control name parameter from {@code Parameters}
     * @return bam file name If the name is not NA or empty
     */
    private String getControlBamName(String fileName, String controlName, String sampleName) {
        if (controlName.equals(PipelineUtils.NA) || controlName.equals("")) {
            return null;
        }
        return fileName.replace(sampleName, controlName);
    }

    /**
     * Method checks existence of specified file list
     *
     * @param fileList a path of the fastq file
     * @return path to the list of files
     * @throws IOException Throws {@code IOException} when file is not readable
     */
    private Path checkSampleFile(final String fileList) throws IOException {
        final Path filePath = Paths.get(fileList);
        if (!filePath.toFile().exists()) {
            throw new NoSuchFileException("No such file " + fileList);
        }
        if (!Files.isReadable(filePath)) {
            throw new IOException(format("File %s is not readable.", filePath));
        }
        return filePath;
    }

    /**
     * Method checks list of files (fastq or bam) from configuration according to file type
     * If list of files is empty (empty link), then throw {@code IllegalArgumentException}
     *
     * @param sampleType FastQ or Bam
     */
    private void checkConfig(SampleType sampleType) {
        if ((SampleType.FASTQ.equals(sampleType) && StringUtils.isBlank(studyConfig.getFastqList())) ||
                (SampleType.BAM.equals(sampleType) && StringUtils.isBlank(studyConfig.getBamList()))) {
            throw new IllegalArgumentException("File type has no list of files in configuration");
        }
    }

    private void checkInputParameters(String sampleName, String file1) {
        if (StringUtils.isAnyBlank(sampleName, file1)) {
            log.error(format("Empty input parameters found: sample name - '%s', file parameter - '%s'",
                    sampleName, file1));
            throw new IllegalArgumentException(EMPTY_INPUT_EXCEPTION);
        }
    }
}
