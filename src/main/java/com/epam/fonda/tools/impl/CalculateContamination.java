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
package com.epam.fonda.tools.impl;

import com.epam.fonda.entity.command.BashCommand;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.entity.configuration.GlobalConfigFormat;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.CalculateContaminationOutput;
import com.epam.fonda.tools.results.CalculateContaminationResult;
import com.epam.fonda.workflow.TaskContainer;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static com.epam.fonda.utils.ToolUtils.validate;
import static java.lang.String.format;

@RequiredArgsConstructor
public class CalculateContamination implements Tool<CalculateContaminationResult> {

    private static final String CALCULATE_CONTAMINATION_TOOL_TEMPLATE_NAME = "calculate_contamination_template";

    @NonNull
    private final String sampleName;
    @NonNull
    private final String pileupTable;
    @NonNull
    private final String outputDir;

    @Data
    @Builder
    private static class ToolFields {
        private final String gatk;
        private final String pileupTable;
        private final String segments;
        private final String contamTable;
    }

    @Override
    public CalculateContaminationResult generate(Configuration configuration, TemplateEngine templateEngine) {
        final ToolFields toolFields = initToolFields(configuration);
        final Context context = new Context();
        context.setVariable("toolFields", toolFields);
        final String cmd = templateEngine.process(CALCULATE_CONTAMINATION_TOOL_TEMPLATE_NAME, context);
        TaskContainer.addTasks("CalculateContamination");
        final CalculateContaminationOutput calculateContaminationOutput = CalculateContaminationOutput.builder()
                .contaminationTable(toolFields.pileupTable)
                .build();
        return CalculateContaminationResult.builder()
                .command(BashCommand.withTool(cmd))
                .calculateContaminationOutput(calculateContaminationOutput)
                .build();
    }

    private ToolFields initToolFields(final Configuration configuration) {
        final GlobalConfig.ToolConfig toolConfig = configuration.getGlobalConfig().getToolConfig();
        return ToolFields.builder()
                .gatk(validate(toolConfig.getGatk(), GlobalConfigFormat.GATK))
                .pileupTable(pileupTable)
                .segments(format("%s/%s.segments.table", outputDir, sampleName))
                .contamTable(format("%s/%s.contamination.table", outputDir, sampleName))
                .build();
    }
}
