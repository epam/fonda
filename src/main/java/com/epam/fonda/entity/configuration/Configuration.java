/*
 * Copyright 2017-2021 Sanofi and EPAM Systems, Inc. (https://www.epam.com/)
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

import lombok.Data;

/**
 * The <tt>Configuration</tt> class represents the user specified parameters from command line
 */
@Data
public class Configuration {
    private GlobalConfig globalConfig;
    private StudyConfig studyConfig;
    private CommonOutdir commonOutdir;
    private String custTask;
    private boolean syncMode;
    private boolean testMode;
    private boolean localMode;
    private boolean masterMode;
}
