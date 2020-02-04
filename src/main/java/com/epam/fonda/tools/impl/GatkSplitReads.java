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

import com.epam.fonda.entity.command.AbstractCommand;
import com.epam.fonda.entity.command.BashCommand;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfigFormat;
import com.epam.fonda.workflow.TaskContainer;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.utils.PipelineUtils;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Arrays;

import static com.epam.fonda.utils.ToolUtils.validate;

@Data
public class GatkSplitReads implements Tool<BamResult> {
    private static final String GATK_SPLIT_READS_TOOL_TEMPLATE_NAME = "gatk_split_reads_tool_template";

    @NonNull
    private String sampleOutdir;
    @NonNull
    private BamResult bamResult;

    /**
     * This method generates bash script {@link BashCommand} for GatkSplitReads tool.
     *
     * @param configuration  is the type of {@link Configuration} and contains its fields.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link BamResult} with bash script.
     **/
    @Override
    public BamResult generate(Configuration configuration, TemplateEngine templateEngine) {
        ToolFields toolFields = initializeToolFields(configuration);
        PipelineUtils.createDir(String.format("%s/gatkSplit", sampleOutdir));
        String tmpGatkSplitOutdir = String.format("%s/gatkSplit/tmp", sampleOutdir);
        PipelineUtils.createDir(tmpGatkSplitOutdir);
        String splitBam = bamResult.getBamOutput().getBam().replace(".bam", ".splitRead.bam");
        String splitBamIndex = bamResult.getBamOutput().getBam().replace(".bam", ".bam.bai");
        Context context = new Context();
        context.setVariable("toolFields", toolFields);
        context.setVariable("tmpGatkSplitOutdir", tmpGatkSplitOutdir);
        context.setVariable("splitBam", splitBam);
        context.setVariable("pathToBam", bamResult.getBamOutput().getBam());
        String cmd = templateEngine.process(GATK_SPLIT_READS_TOOL_TEMPLATE_NAME, context);
        TaskContainer.addTasks("GATK SplitNCigarReads");
        AbstractCommand resultCommand = bamResult.getCommand();
        resultCommand.setToolCommand(resultCommand.getToolCommand() + cmd);
        resultCommand.getTempDirs().addAll(Arrays.asList(splitBam, splitBamIndex));
        bamResult.getBamOutput().setBam(splitBam);
        return bamResult;
    }

    @Data
    @Builder
    private static class ToolFields {
        private String genome;
        private String java;
        private String gatk;
    }

    private ToolFields initializeToolFields(Configuration configuration) {
        return ToolFields.builder()
                .genome(validate(configuration.getGlobalConfig().getDatabaseConfig().getGenome(),
                        GlobalConfigFormat.GENOME))
                .java(validate(configuration.getGlobalConfig().getToolConfig().getJava(), GlobalConfigFormat.JAVA))
                .gatk(validate(configuration.getGlobalConfig().getToolConfig().getGatk(), GlobalConfigFormat.GATK))
                .build();
    }
}
