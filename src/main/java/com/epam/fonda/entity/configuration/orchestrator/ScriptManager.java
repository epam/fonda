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
package com.epam.fonda.entity.configuration.orchestrator;

import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.utils.PipelineUtils;

/**
 * The <tt>ScriptManager</tt> interface provides methods to work with master script.
 */
public interface ScriptManager {

    /**
     * This method executes a built script.
     * @param configuration is the type of {@link Configuration} which contains
     *                      its fields: workflow, local, numThreads, pe, queue.
     */
    default void launchScript(final Configuration configuration) {
        final String script = buildScript(configuration);
        if (configuration.isTestMode()) {
            return;
        }
        PipelineUtils.executeScript(configuration, script);
    }

    /**
     * This method builds a main script.
     * @param configuration is the type of {@link Configuration}
     * @return path to the generated script
     */
    String buildScript(final Configuration configuration);

    /**
     * This method adds script that will be launched from master script.
     * @param sampleName a name of a sample to which a script belongs
     * @param type is the type of {@link ScriptType}
     * @param script a script path
     */
    void addScript(final String sampleName, final ScriptType type, final String script);

    /**
     * This method restores all defaults within the class.
     */
    void resetScript();
}
