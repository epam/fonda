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
package com.epam.fonda.entity.configuration.orchestrator;

import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.utils.PipelineUtils;

/**
 *
 */
public interface ScriptManager {

    /**
     * Launch script
     */
    default void launchScript(final Configuration configuration) {
        final String script = buildScript();
        PipelineUtils.executeScript(configuration, script);
    }

    String buildScript();

    void addScript(final String sampleName, final ScriptType type, final String script);
}
