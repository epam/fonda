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

package com.epam.fonda.workflow;

import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.entity.configuration.StudyConfig;
import com.epam.fonda.workflow.impl.RnaExpressionFastqWorkflow;
import com.epam.fonda.workflow.impl.RnaFusionFastqWorkflow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkflowFactoryTest {
    private Configuration expectedConfiguration;

    @BeforeEach
    void init() {
        expectedConfiguration = new Configuration();
        expectedConfiguration.setGlobalConfig(new GlobalConfig());
        expectedConfiguration.setStudyConfig(new StudyConfig());
        expectedConfiguration.getGlobalConfig().getPipelineInfo()
                .setToolset(new LinkedHashSet<>(Collections.singleton("task")));
    }

    @Test
    void shouldGetWorkflow() {
        final Workflow workflow = new WorkflowFactory().getWorkflow("RnaExpression_Fastq",
                expectedConfiguration);
        assertEquals(workflow.getClass(), RnaExpressionFastqWorkflow.class);
    }

    @Test
    void shouldGetRnaFusionWorkflow() {
        final Workflow workflow = new WorkflowFactory().getWorkflow("RnaFusion_Fastq",
                expectedConfiguration);
        assertEquals(workflow.getClass(), RnaFusionFastqWorkflow.class);
    }

    @Test
    void buildUnsupportedWorkflow() {
        assertThrows(NullPointerException.class, () ->
                new WorkflowFactory().getWorkflow("RnaExpression_F", expectedConfiguration));
    }
}
