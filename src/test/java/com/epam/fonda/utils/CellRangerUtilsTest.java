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

package com.epam.fonda.utils;

import com.epam.fonda.samples.fastq.FastqFileSample;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.epam.fonda.utils.CellRangerUtils.extractFastqDir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CellRangerUtilsTest {

    private static final String TEST_FASTQ_FILE = "/ngs/data/demo/test/fastq_data/pbmc4k_S1_L001_R1_001.fastq.gz";

    @Test
    void shouldThrowExceptionIfFastq1IsNull() {
        FastqFileSample expectedSample = FastqFileSample.builder()
                .build();
        NullPointerException thrownException = assertThrows(NullPointerException.class,
            () -> extractFastqDir(expectedSample));

        assertTrue(thrownException.getMessage()
                .contains("Error Step: in extractFastqDir - fastq1 is not provided, please check!"));
    }

    @Test
    void shouldExtractFastqDir() {
        List<String> expectedFastq1List = new ArrayList<>();
        expectedFastq1List.add("FileInTheCurrentDir");
        FastqFileSample expectedSample = FastqFileSample.builder()
                .fastq1(expectedFastq1List)
                .build();
        extractFastqDir(expectedSample);
        assertEquals(1, expectedSample.getFastqDirs().size());
    }

    @Test
    void shouldCheckExtractedFastqDirs() {
        List<String> expectedFastq1List = new ArrayList<>();
        expectedFastq1List.add(TEST_FASTQ_FILE);
        FastqFileSample expectedSample = FastqFileSample.builder()
                .fastq1(expectedFastq1List)
                .build();
        List<String> expectedDirsList = new ArrayList<>();
        expectedDirsList.add(Paths.get(TEST_FASTQ_FILE).toAbsolutePath().getParent().toString());
        extractFastqDir(expectedSample);
        assertEquals(expectedDirsList, expectedSample.getFastqDirs());
    }
}
