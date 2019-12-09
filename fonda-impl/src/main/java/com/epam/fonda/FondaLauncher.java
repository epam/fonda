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
import com.epam.fonda.workflow.Workflow;
import com.epam.fonda.workflow.WorkflowFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * The <tt>FondaLauncher</tt> class provides entry point to workflow launching
 */
@RequiredArgsConstructor
@Slf4j
public class FondaLauncher {

    @NonNull
    private Configuration configuration;

    /**
     * The method consists of workflow launching process
     * @throws IOException if an I/O error has occurred
     */
    public void launch() throws IOException {
        final WorkflowFactory workflowFactory = new WorkflowFactory();
        final String workflowName = configuration.getGlobalConfig().getPipelineInfo().getWorkflow();
        final Workflow workflow = workflowFactory.getWorkflow(workflowName, configuration);
        log.debug(String.format("The %s job is launched through fonda!", workflowName));
        workflow.process(configuration);
    }
}
