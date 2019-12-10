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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public final class CellRangerUtils {

    private CellRangerUtils() {}

    /**
     * this method extracts fastq1 and fastq2 directories from fastq lists
     * @param sample contains fastq lists
     */
    public static FastqFileSample extractFastqDir(FastqFileSample sample) {
        List<String> fastq1List = sample.getFastq1();
        List<String> fastq2List = sample.getFastq2();
        if (CollectionUtils.isEmpty(sample.getFastqDirs())) {
            sample.setFastqDirs(new ArrayList<>());
        }
        Validate.notEmpty(fastq1List, "Error Step: in extractFastqDir - fastq1 is not provided, please check!");
        extract(sample.getFastqDirs(), fastq1List);
        extract(sample.getFastqDirs(), fastq2List);
        return sample;
    }

    /**
     * this method adds fastq directories to fastq directory list
     * @param fastqDirs is a fastq directory list to be filled
     * @param fastqList is a list of fastq1 or fastq2
     */
    private static void extract(List<String> fastqDirs, List<String> fastqList) {
        if (CollectionUtils.isEmpty(fastqList)) {
            return;
        }
        fastqList.forEach(fastq -> {
            String parentDir = Paths.get(fastq).toAbsolutePath().getParent().toString();
            if (fastqDirs.contains(parentDir)) {
                return;
            }
            fastqDirs.add(parentDir);
        });
    }
}
