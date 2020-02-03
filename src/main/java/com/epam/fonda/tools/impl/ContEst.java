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

package com.epam.fonda.tools.impl;

import com.epam.fonda.entity.command.BashCommand;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfigFormat;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.tools.results.ContEstOutput;
import com.epam.fonda.tools.results.ContEstResult;
import com.epam.fonda.utils.DnaUtils;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Collections;

import static com.epam.fonda.utils.PipelineUtils.TASK_TO_CHECK;
import static com.epam.fonda.utils.ToolUtils.validate;

@RequiredArgsConstructor
public class ContEst implements Tool<ContEstResult> {
    private static final String CONT_EST_TOOL_TEMPLATE_NAME = "contest_tool_template";

    @Data
    @Builder
    private static class ToolFields {
        private String java;
        private String gatk;
        private String controlBam;
        private String contEstOutDir;
    }

    @Data
    @Builder
    private static class AdditionalFields {
        private String tmpContEstOutDir;
        private String genome;
        private String bed;
        private String bam;
        private String contEstPopAF;
        private boolean isWgs;
    }

    @NonNull
    private String sampleName;
    @NonNull
    private String sampleOutputDir;
    @NonNull
    private BamResult bamResult;

    /**
     * Generates bash script {@link BashCommand} for ContEst tool.
     *
     * @param configuration  is the type of {@link Configuration} which contains fields:
     *                       java, gatk.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link BashCommand} with bash script.
     */
    @Override
    public ContEstResult generate(Configuration configuration, TemplateEngine templateEngine) {
        AdditionalFields additionalFields = initializeAdditionalFields(configuration);
        ToolFields toolFields = constructFieldsForContEst(configuration);
        final String contEstRes = String.format("%s/%s.contEst.result", toolFields.contEstOutDir, sampleName);
        Context context = new Context();
        context.setVariable("additionalFields", additionalFields);
        context.setVariable("toolFields", toolFields);
        context.setVariable("contEstRes", contEstRes);
        ContEstOutput contEstOutput = ContEstOutput.builder()
                .contEstOutdir(toolFields.contEstOutDir)
                .contEstOutDirTmp(additionalFields.tmpContEstOutDir)
                .contEstRes(contEstRes)
                .build();
        contEstOutput.createDirectory();
        String cmd = templateEngine.process(CONT_EST_TOOL_TEMPLATE_NAME, context);
        TASK_TO_CHECK.add("Contamination estimation");
        final BashCommand command = BashCommand.withTool(cmd);
        command.setTempDirs(Collections.singletonList(additionalFields.tmpContEstOutDir));
        return ContEstResult.builder()
                .command(command)
                .contEstOutput(contEstOutput)
                .build();
    }

    private ToolFields constructFieldsForContEst(Configuration configuration) {
        return ToolFields.builder()
                .java(configuration.getGlobalConfig().getToolConfig().getJava())
                .gatk(configuration.getGlobalConfig().getToolConfig().getGatk())
                .controlBam(bamResult.getBamOutput().getControlBam())
                .contEstOutDir(String.format("%s/contEst", sampleOutputDir))
                .build();
    }

    private AdditionalFields initializeAdditionalFields(Configuration configuration) {
        return AdditionalFields.builder()
                .tmpContEstOutDir(String.format("%s/contEst/tmp", sampleOutputDir))
                .genome(validate(
                        configuration.getGlobalConfig().getDatabaseConfig().getGenome(),
                        GlobalConfigFormat.GENOME))
                .bed(validate(configuration.getGlobalConfig().getDatabaseConfig().getBed(), GlobalConfigFormat.BED))
                .bam(bamResult.getBamOutput().getBam())
                .contEstPopAF(validate(
                        configuration.getGlobalConfig().getDatabaseConfig().getContEstPopAF(),
                        GlobalConfigFormat.CONTEST_POPAF))
                .isWgs(DnaUtils.isWgsWorkflow(configuration))
                .build();
    }
}
