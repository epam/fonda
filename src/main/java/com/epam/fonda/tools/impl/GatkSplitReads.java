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
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.BamResult;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Arrays;

@Data
public class GatkSplitReads implements Tool<BamResult> {
    private static final String GATK_SPLIT_READS_TOOL_TEMPLATE_NAME = "gatk_split_reads_tool_template";

    @NonNull
    private String outDir;
    @NonNull
    private String bam;

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
        String tmpGatkSplitOutdir = String.format("%s/tmp", outDir);
        String splitBam = bam.replace(".bam", ".splitRead.bam");
        String splitBamIndex = bam.replace(".bam", ".bam.bai");
        Context context = new Context();
        context.setVariable("toolFields", toolFields);
        context.setVariable("tmpGatkSplitOutdir", tmpGatkSplitOutdir);
        context.setVariable("splitBam", splitBam);
        context.setVariable("pathToBam", bam);
        String cmd = templateEngine.process(GATK_SPLIT_READS_TOOL_TEMPLATE_NAME, context);
        AbstractCommand command = BashCommand.withTool(cmd);
        command.setTempDirs(Arrays.asList(splitBam, splitBamIndex));
        return BamResult.builder()
                .command(command)
                .bamOutput(BamOutput.builder()
                        .bam(splitBam)
                        .build())
                .build();
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
                .genome(configuration.getGlobalConfig().getDatabaseConfig().getGenome())
                .java(configuration.getGlobalConfig().getToolConfig().getJava())
                .gatk(configuration.getGlobalConfig().getToolConfig().getGatk())
                .build();
    }
}
