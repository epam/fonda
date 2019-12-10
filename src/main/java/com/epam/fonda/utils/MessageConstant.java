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

public final class MessageConstant {
    public static final String HELP_DESCRIPTION = "Show this utility message";
    public static final String DETAIL_DESCRIPTION = "Show the details of the fonda framework";
    public static final String TEST_DESCRIPTION = "Default: no. Test the commands without actually running the job";
    public static final String LOCAL_DESCRIPTION = "Default: no. Running the job on local machine";
    public static final String STUDY_CONFIG_DESCRIPTION = "Configuration file for the specific study (Required)";
    public static final String GLOBAL_CONFIG_DESCRIPTION = "Configuration file for the particular pipeline (Required)";
    public static final String SYNC_DESCRIPTION = "Run fonda in sync mode, waiting for all tasks to complete";
    public static final String HEADER = "\nfonda (Framework Of NGS Data Analysis)";

    private MessageConstant() {}
}
