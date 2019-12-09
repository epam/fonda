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

package com.epam.fonda.entity.configuration;

import com.beust.jcommander.Parameter;
import lombok.Data;

/**
 * The <tt>StudyConfig</tt> class represents the user specified workflow parameters from study config file
 */
@Data
public class StudyConfig {
    @Parameter(names = StudyConfigFormat.JOB_NAME)
    private String jobName;
    @Parameter(names = StudyConfigFormat.DIR_OUT)
    private String dirOut;
    @Parameter(names = StudyConfigFormat.BAM_LIST)
    private String bamList;
    @Parameter(names = StudyConfigFormat.FASTQ_LIST)
    private String fastqList;
    @Parameter(names = StudyConfigFormat.LIBRARY_TYPE)
    private String libraryType;
    @Parameter(names = StudyConfigFormat.DATA_GENERATION_SOURCE)
    private String dataGenerationSource;
    @Parameter(names = StudyConfigFormat.DATE)
    private String date;
    @Parameter(names = StudyConfigFormat.PROJECT)
    private String project;
    @Parameter(names = StudyConfigFormat.RUN)
    private String run;
    @Parameter(names = StudyConfigFormat.CUFFLINKS_LIBRARY_TYPE)
    private String cufflinksLibraryType;
    @Parameter(names = StudyConfigFormat.RNA_SEQ_CONFIGURATION)
    private String rnaSeqConfiguration;
}
