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

package com.epam.fonda;

import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.utils.PipelineUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * The class provides entry point to launch
 */
@Slf4j
public final class Main {

    private Main() {}

    /**
     * The method provides entry point to launch app with arguments
     * @param args the user specified arguments from command line
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public static void main(String[] args) {
        try {
            Configuration configuration = new CmdParser().parseArgs(args);
            new FondaLauncher(configuration).launch();
            if (configuration.isTestMode()) {
                return;
            }
            final Optional<Integer> result = Executor.getExecutorResult();
            System.exit(result.orElseGet(() -> processResult(configuration)));
        } catch (Exception e) {
            log.error("Error step in main: " + e);
            e.printStackTrace();
            System.exit(PipelineUtils.ERROR_STATUS);
        }
    }

    /**
     * Method is used to get executor result
     * @param configuration is the type of {@link Configuration} which contains global and study configuration
     */
    private static int processResult(final Configuration configuration) {
        if (configuration.isSyncMode() || configuration.isTestMode()) {
            log.debug("No task were executed");
        } else {
            log.debug("Tasks executed in asynchronous mode");
        }
        return 0;
    }
}
