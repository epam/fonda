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

package com.epam.fonda.samples.bam;

import com.epam.fonda.entity.configuration.DirectoryManager;
import com.epam.fonda.samples.Sample;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * The <tt>BamFileSample</tt> class represents bam set from the user specified bam file and folders,
 * which are created in building sample stage
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BamFileSample implements Sample, DirectoryManager {
    private String name;
    private String sampleType;
    private String matchControl;
    private String controlName;
    private String controlBam;
    private String bam;
    private String sampleOutputDir;
    private String tmpOutdir;

    @Override
    public List<String> getDirs() {
        return Arrays.asList(sampleOutputDir, tmpOutdir);
    }
}
