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

package com.epam.fonda.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static java.lang.String.format;

@Slf4j
public final class MessageConstant {
    public static final String HELP_DESCRIPTION = "Show this utility message";
    public static final String DETAIL_DESCRIPTION = "Show the details of the fonda framework";
    public static final String TEST_DESCRIPTION = "Default: no. Test the commands without actually running the job";
    public static final String LOCAL_DESCRIPTION = "Default: no. Running the job on local machine";
    public static final String STUDY_CONFIG_DESCRIPTION = "Configuration file for the specific study (Required)";
    public static final String GLOBAL_CONFIG_DESCRIPTION = "Configuration file for the particular pipeline (Required)";
    public static final String SYNC_DESCRIPTION = "Run fonda in sync mode, waiting for all tasks to complete";
    public static final String HEADER = "\nFonda (Framework Of NGS Data Analysis)";

    static {
        final String propertiesFile = "version.properties";

        String version = "";
        try(InputStream stream = MessageConstant.class.getClassLoader().getResourceAsStream(propertiesFile)) {
            if (stream != null) {
                Properties properties = new Properties();
                properties.load(stream);
                version = properties.getProperty("version");
            }
        } catch (IOException e) {
            log.error("Failed to read the fonda version: " + e);
        }
        VERSION_MESSAGE = format("The current build version: %s.", version);
    }

    public static final String VERSION_MESSAGE;

    private MessageConstant() {}
}
