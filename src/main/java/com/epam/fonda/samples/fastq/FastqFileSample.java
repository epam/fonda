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

package com.epam.fonda.samples.fastq;

import com.epam.fonda.entity.configuration.DirectoryManager;
import com.epam.fonda.samples.Sample;
import com.epam.fonda.utils.CellRangerUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The <tt>FastqFileSample</tt> class represents fastqs set from the user specified fastqs file and folders,
 * which are created in building sample stage
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FastqFileSample implements Sample, DirectoryManager {
    private String name;
    private String sampleType;
    private String matchControl;
    private String controlName;
    private List<String> fastq1;
    private List<String> fastq2;
    private List<String> fastqDirs;
    private FastqReadType type;
    private String sampleOutputDir;
    private String tmpOutdir;
    private String fastqOutdir;
    private String bamOutdir;
    private String qcOutdir;

    // This field is only used for single-cell workflow
    private List<LibraryCsv> library;

    /**
     * Add all fastq1 and fastq2 list of files to the source
     *
     * @param file file to be merged
     * @return source file merged with received file
     */
    public FastqFileSample merge(final FastqFileSample file) {
        checkParameters(file);
        this.setFastq1(Stream.of(this.getFastq1(), file.getFastq1())
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));
        if (FastqReadType.PAIRED.equals(this.type)) {
            this.setFastq2(Stream.of(this.getFastq2(), file.getFastq2())
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList()));
        }
        return this;
    }

    /**
     * Method checks for compatibility of sample type and match control in given and received files
     *
     * @param file received file to merge
     */
    private void checkParameters(final FastqFileSample file) {
        if ((!Objects.equals(this.getMatchControl(), file.getMatchControl()) ||
                !this.getSampleType().equals(file.getSampleType()))
                && !StringUtils.containsIgnoreCase(file.getName(), "ADT")) {
            log.error(String.format("Error Step: %s has multiple sample types or matched controls!" +
                    " Please check!", file.getName()));
            throw new IllegalArgumentException("Multiple sample types or matched controls in a sample");
        }
    }

    @Override
    public List<String> getDirs() {
        return Arrays.asList(sampleOutputDir, tmpOutdir, fastqOutdir, bamOutdir, qcOutdir);
    }

    /**
     * Create library samples for single-cell workflow
     * @param files primary built samples from a sample manifest
     * @return library sample with {@link List<LibraryCsv>}
     */
    public static FastqFileSample mergeLibrarySamples(final List<FastqFileSample> files) {
        final FastqFileSample sample = files.get(0);
        List<LibraryCsv> libraries = new ArrayList<>();
        final List<String> filesDirs = files.stream()
                .map(f -> {
                    List<String> dirs = CellRangerUtils.extractFastqDir(f).getFastqDirs();
                    libraries.addAll(dirs.stream()
                            .map(d -> LibraryCsv.builder()
                                    .fastqDir(d)
                                    .libraryType(mapLibraryType(f.getSampleType()))
                                    .sampleName(f.getName())
                                    .build())
                            .collect(Collectors.toList()));
                    return dirs;
                })
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
        FastqFileSampleBuilder sampleBuilder = FastqFileSample.builder()
                .name(sample.getControlName())
                .sampleType("feature")
                .fastqDirs(filesDirs)
                .library(libraries)
                .sampleOutputDir(sample.getSampleOutputDir())
                .fastqOutdir(sample.getFastqOutdir())
                .type(sample.getType())
                .tmpOutdir(sample.getTmpOutdir())
                .qcOutdir(sample.getQcOutdir())
                .fastq1(files.stream()
                        .map(FastqFileSample::getFastq1)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()));
        if (FastqReadType.PAIRED.equals(sample.getType())) {
            sampleBuilder.fastq2(files.stream()
                    .map(FastqFileSample::getFastq2)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList()));
        }
        return sampleBuilder.build();
    }

    private static String mapLibraryType(final String sampleType) {
        switch (sampleType) {
            case "custom": return "Custom";
            case "Antibody": return "Antibody Capture";
            case "CRISPR Guide Capture": return "CRISPR Guide Capture";
            case "GEX": return "Gene Expression";
            default: throw new IllegalArgumentException(String.format("Requested %s library type is not allowed",
                    sampleType));
        }
    }
}
