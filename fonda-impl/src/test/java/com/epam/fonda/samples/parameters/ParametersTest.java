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

package com.epam.fonda.samples.parameters;

import com.epam.fonda.tools.impl.AbstractTest;
import org.junit.jupiter.api.Test;

import static com.epam.fonda.utils.PipelineUtils.CASE;
import static com.epam.fonda.utils.PipelineUtils.NA;
import static com.epam.fonda.utils.PipelineUtils.TUMOR;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ParametersTest extends AbstractTest {
    private static final String FASTQ = "fastq";
    private static final String BAM = "bam";
    private static final String WRONG_TYPE = "Wrong type";
    private static final String[] VALUES_FROM_LINE =
            "fastqFile\tUM-UC-3\tfile1.fastq.gz\t/file2.fastq.gz".trim().split("\\t");
    private static final String SAMPLE_TYPE_TEST = "test";
    private static final String FILE1 = "/file1.fastq.gz";
    private static final String FILE2 = "/file2.fastq.gz";
    private static final String SHORT_NAME = "UM-UC-3";
    private static final String PARAMETER_TYPE = "fastqFile";
    private static final int SHORT_LENGTH_SINGLE = 3;
    private static final int SHORT_LENGTH_PAIRED = 4;

    @Test
    void checkInputParameters() {
        assertDoesNotThrow(() ->
                Parameters.checkInputParameters(FASTQ, VALUES_FROM_LINE, SHORT_LENGTH_SINGLE));
    }

    @Test
    void checkInputParametersNegativeShortLength() {
        assertThrows(IllegalArgumentException.class, () ->
                Parameters.checkInputParameters(FASTQ, VALUES_FROM_LINE, -SHORT_LENGTH_SINGLE));
    }

    @Test
    void checkInputParametersZeroShortLength() {
        assertThrows(IllegalArgumentException.class, () ->
                Parameters.checkInputParameters(FASTQ, VALUES_FROM_LINE, 0));
    }

    @Test
    void checkInputParametersNullValueList() {
        assertThrows(IllegalArgumentException.class, () ->
                Parameters.checkInputParameters(FASTQ, null, SHORT_LENGTH_SINGLE));
    }

    @Test
    void checkInputParametersNullFileType() {
        assertThrows(IllegalArgumentException.class, () ->
                Parameters.checkInputParameters(null, VALUES_FROM_LINE, SHORT_LENGTH_SINGLE));
    }

    @Test
    void setParametersFastqPairedMin() {
        String[] values = new String[]{PARAMETER_TYPE, SHORT_NAME, FILE1, FILE2};
        Parameters parameters = Parameters.setParameters(FASTQ, values, SHORT_LENGTH_PAIRED);

        assertEquals(TUMOR, parameters.getSampleType());
    }

    @Test
    void setParametersFastqPairedMax() {
        String[] values = new String[]{PARAMETER_TYPE, SHORT_NAME, FILE1, FILE2, SAMPLE_TYPE_TEST, NA};
        Parameters parameters = Parameters.setParameters(FASTQ, values, SHORT_LENGTH_PAIRED);

        assertEquals(SAMPLE_TYPE_TEST, parameters.getSampleType());
    }

    @Test
    void setParametersFastqSingleMin() {
        String[] values = new String[]{PARAMETER_TYPE, SHORT_NAME, FILE1};
        Parameters parameters = Parameters.setParameters(FASTQ, values, SHORT_LENGTH_SINGLE);

        assertEquals(TUMOR, parameters.getSampleType());
    }

    @Test
    void setParametersFastSingleMax() {
        String[] values = new String[]{PARAMETER_TYPE, SHORT_NAME, FILE1, SAMPLE_TYPE_TEST, NA};
        Parameters parameters = Parameters.setParameters(FASTQ, values, SHORT_LENGTH_SINGLE);

        assertEquals(SAMPLE_TYPE_TEST, parameters.getSampleType());
    }

    @Test
    void setParametersBamMin() {
        String[] values = new String[]{PARAMETER_TYPE, SHORT_NAME, FILE1};
        Parameters parameters = Parameters.setParameters(BAM, values, SHORT_LENGTH_SINGLE);

        assertEquals(CASE, parameters.getSampleType());
    }

    @Test
    void setParametersBamMax() {
        String[] values = new String[]{PARAMETER_TYPE, SHORT_NAME, FILE1, SAMPLE_TYPE_TEST, NA};
        Parameters parameters = Parameters.setParameters(BAM, values, SHORT_LENGTH_SINGLE);

        assertEquals(SAMPLE_TYPE_TEST, parameters.getSampleType());
    }

    @Test
    void setParametersWrongFileType() {
        String[] values = new String[]{PARAMETER_TYPE, SHORT_NAME, FILE1};

        assertThrows(IllegalArgumentException.class, () ->
                Parameters.setParameters(WRONG_TYPE, values, SHORT_LENGTH_SINGLE));
    }

    @Test
    void setParametersWrongShortLength() {
        String[] values = new String[]{PARAMETER_TYPE, SHORT_NAME, FILE1};

        assertThrows(IllegalArgumentException.class, () ->
                Parameters.setParameters(FASTQ, values, 7));
    }

    @Test
    void builderTest() {
        Parameters parameters = Parameters.builder()
                .sampleType(SAMPLE_TYPE_TEST)
                .matchControl(NA)
                .build();

        assertEquals(SAMPLE_TYPE_TEST, parameters.getSampleType());
    }
}
