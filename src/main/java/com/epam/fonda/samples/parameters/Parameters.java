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
package com.epam.fonda.samples.parameters;

import com.epam.fonda.samples.SampleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import static com.epam.fonda.utils.PipelineUtils.CASE;
import static com.epam.fonda.utils.PipelineUtils.NA;
import static com.epam.fonda.utils.PipelineUtils.TUMOR;

/**
 * The <tt>Parameters</tt> class represents the sample parameters from fastqs file
 */
@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Parameters {
    private String sampleType;
    private String matchControl;

    /**
     * Set sample type and match control parameters and return object with these parameters
     *
     * @param fileType    Fastq or BAM
     * @param values      values from source text file (one line)
     * @param shortLength shortest valid number of parameters
     * @return Instance of Parameters
     */
    public static Parameters setParameters(String fileType, String[] values, int shortLength) {
        checkInputParameters(fileType, values, shortLength);
        Parameters parameters = new Parameters();
        int longLength = shortLength + 2;
        int length = values.length;

        if (length == shortLength) {
            if (fileType.equalsIgnoreCase(SampleType.FASTQ.getType())) {
                parameters.setSampleType(TUMOR);
            } else if (fileType.equalsIgnoreCase(SampleType.BAM.getType())) {
                parameters.setSampleType(CASE);
            } else {
                log.error(String.format("Incorrect fileType: '%s'", fileType));
                throw new IllegalArgumentException("Incorrect fileType");
            }
            parameters.setMatchControl(NA);
        } else if (length == longLength) {
            parameters.setSampleType(values[longLength - 2]);
            parameters.setMatchControl(values[longLength - 1]);
        } else {
            log.error(String.format("Error Step: Please check the number of columns in fastq or bam list file. " +
                    "It should be either %d or %d!", shortLength, longLength));
            throw new IllegalArgumentException(" Incompatible number of columns");
        }
        return parameters;
    }

    /**
     * Checks validity of all input parameters
     *
     * @param fileType    Fastq or BAM
     * @param values      values from source text file (one line)
     * @param shortLength shortest valid number of parameters
     */
    public static void checkInputParameters(String fileType, String[] values, int shortLength) {
        if (StringUtils.isBlank(fileType) || values == null || shortLength <= 0) {
            log.error("Error Step: null input parameters: specified minimum is " + shortLength +
                    ", file type: '" + fileType + "'");
            throw new IllegalArgumentException("Null input parameters");
        }
    }
}
