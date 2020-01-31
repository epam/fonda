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

package com.epam.fonda.tools.impl;

import com.epam.fonda.entity.command.BashCommand;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.entity.configuration.GlobalConfigFormat;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.PileupOutput;
import com.epam.fonda.tools.results.SequenzaOutput;
import com.epam.fonda.tools.results.SequenzaResult;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Collections;

import static com.epam.fonda.utils.PipelineUtils.TASK_TO_CHECK;
import static com.epam.fonda.utils.ToolUtils.validate;

@RequiredArgsConstructor
public class Sequenza implements Tool<SequenzaResult> {
    private static final String SEQUENZA_TOOL_TEMPLATE = "sequenza_template";

    private final String sampleName;
    private final String sampleOutDir;
    private final PileupOutput pileupOutput;

    @Data
    @Builder
    private static class ToolFields {
        private final String sequenza;
        private final String python;
        private final String rScript;
        private final String sequenzaGc50;
        private final String sequenzaSeqz;
        private final String sequenzaSeqzReduce;
        private final String sequenzaSeg;
        private final String sequenzaInfor;
        private final String pileup;
        private final String controlPileup;
        private final String outDir;
    }

    /**
     * Generates bash script {@link BashCommand} for Sequenza tool.
     * @param configuration is the type of {@link Configuration} which contains fields:
     *                      sequenza, python, Rscript and sequenzaGc50.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link BashCommand} with bash script.
     */
    @Override
    public SequenzaResult generate(final Configuration configuration, final TemplateEngine templateEngine) {
        final ToolFields toolFields = initToolFields(configuration);
        final Context context = new Context();
        context.setVariable("toolFields", toolFields);
        final String cmd = templateEngine.process(SEQUENZA_TOOL_TEMPLATE, context);
        TASK_TO_CHECK.add("Sequenza detection");
        final SequenzaOutput output = SequenzaOutput.builder()
                .sequenzaSegOutput(toolFields.getSequenzaSeg())
                .sequenzaInforOutput(toolFields.getSequenzaInfor())
                .sequenzaSeqzOutput(toolFields.getSequenzaSeqz())
                .sequenzaSeqzReduceOutput(toolFields.getSequenzaSeqzReduce())
                .outDir(toolFields.getOutDir())
                .build();
        output.createDirectory();
        final BashCommand command = BashCommand.withTool(cmd);
        command.setTempDirs(Collections.singletonList(toolFields.getSequenzaSeqz()));
        return SequenzaResult.builder()
                .command(command)
                .toolName("sequenza")
                .sequenzaOutput(output)
                .build();
    }

    private ToolFields initToolFields(final Configuration configuration) {
        final String outputDir = String.format("%s/sequenza", sampleOutDir);
        final GlobalConfig.ToolConfig toolConfig = configuration.getGlobalConfig().getToolConfig();
        final GlobalConfig.DatabaseConfig databaseConfig = configuration.getGlobalConfig().getDatabaseConfig();
        return ToolFields.builder()
                .python(validate(toolConfig.getPython(), GlobalConfigFormat.PYTHON))
                .rScript(validate(toolConfig.getRScript(), GlobalConfigFormat.R_SCRIPT))
                .sequenza(validate(toolConfig.getSequenza(), GlobalConfigFormat.SEQUENZA))
                .sequenzaGc50(validate(databaseConfig.getSequenzaGc50(), GlobalConfigFormat.SEQUENZA_GC50))
                .sequenzaSeqz(format(outputDir, ".seqz.gz"))
                .sequenzaSeqzReduce(format(outputDir, "_small.seqz.gz"))
                .sequenzaSeg(format(outputDir, "_sequenza_segment.txt"))
                .sequenzaInfor(format(outputDir, "_tumor_infor.txt"))
                .pileup(validate(pileupOutput.getPileup(), "pileup_output"))
                .controlPileup(validate(pileupOutput.getControlPileup(), "control_pileup_output"))
                .outDir(outputDir)
                .build();
    }

    private String format(final String outputDir, final String suffix) {
        return String.format("%s/%s%s", outputDir, sampleName, suffix);
    }
}
