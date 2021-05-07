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
import com.epam.fonda.tools.results.LearnReadOrientationModelOutput;
import com.epam.fonda.tools.results.LearnReadOrientationModelResult;
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
public class LearnReadOrientationModel implements Tool<LearnReadOrientationModelResult> {

    private static final String LEARN_READ_ORIENTATION_MODEL_TEMPLATE = "learnReadOrientationModel_tool_template";

    @NonNull
    private final String sampleName;
    @NonNull
    private final String f1r2TarGz;
    @NonNull
    private final String outputDir;

    @Data
    @Builder
    private static class ToolFields {
        private final String gatk;
        private final String f1r2TarGz;
        private final String artifactsPriors;
    }

    @Override
    public LearnReadOrientationModelResult generate(Configuration configuration, TemplateEngine templateEngine) {
        final ToolFields toolFields = initToolFields(configuration);
        final Context context = new Context();
        context.setVariable("toolFields", toolFields);
        final String cmd = templateEngine.process(LEARN_READ_ORIENTATION_MODEL_TEMPLATE, context);
        TaskContainer.addTasks("LearnReadOrientationModel");
        LearnReadOrientationModelOutput modelOutput = LearnReadOrientationModelOutput.builder()
                .artifactPriorTables(toolFields.getArtifactsPriors())
                .build();
        return LearnReadOrientationModelResult.builder()
                .command(BashCommand.withTool(cmd))
                .learnReadOrientationModelOutput(modelOutput)
                .build();
    }

    private ToolFields initToolFields(final Configuration configuration) {
        final GlobalConfig.ToolConfig toolConfig = configuration.getGlobalConfig().getToolConfig();
        return ToolFields.builder()
                .gatk(validate(toolConfig.getGatk(), GlobalConfigFormat.GATK))
                .f1r2TarGz(f1r2TarGz)
                .artifactsPriors(format("%s/%s.artifacts-priors.tar.gz", outputDir, sampleName))
                .build();
    }
}
