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

/**
 *  The <tt>StudyConfigFormat</tt> class represents the user specified constants from study config file
 */
public final class StudyConfigFormat {
    private StudyConfigFormat() {}

    public static final String JOB_NAME = "job_name";
    public static final String DIR_OUT = "dir_out";
    public static final String BAM_LIST = "bam_list";
    public static final String FASTQ_LIST = "fastq_list";
    public static final String LIBRARY_TYPE = "LibraryType";
    public static final String DATA_GENERATION_SOURCE = "DataGenerationSource";
    public static final String DATE = "Date";
    public static final String PROJECT = "Project";
    public static final String RUN = "Run";
    public static final String CUFFLINKS_LIBRARY_TYPE = "Cufflinks.library_type";
    public static final String RNA_SEQ_CONFIGURATION = "RNASeqConfiguration";
}
