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
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.workflow.TaskContainer;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static com.epam.fonda.utils.ToolUtils.validate;
import static java.lang.String.format;

@RequiredArgsConstructor
public class GatkSortSam implements Tool<BamResult> {
    private static final String GATK_SORT_BAM_TEMPLATE = "gatk_sort_bam_template";

    @Data
    @Builder
    private static class ToolFields {
        private String gatk;
        private String samtools;
        private String bam;
        private String sortedBam;
        private String javaOptions;
    }

    private final String sampleName;
    private final String bam;
    private final String outputDir;

    @Override
    public BamResult generate(Configuration configuration, TemplateEngine templateEngine) {
        final GlobalConfig.ToolConfig toolConfig = configuration.getGlobalConfig().getToolConfig();
        final ToolFields toolFields = ToolFields.builder()
                .bam(bam)
                .gatk(validate(toolConfig.getGatk(), GlobalConfigFormat.GATK))
                .samtools(validate(toolConfig.getSamTools(), GlobalConfigFormat.SAMTOOLS))
                .sortedBam(format("%s/%s.bam.sorted", outputDir, sampleName))
                .javaOptions(toolConfig.getGatkJavaOptions())
                .build();
        final Context context = new Context();
        context.setVariable("toolFields", toolFields);
        final String cmd = templateEngine.process(GATK_SORT_BAM_TEMPLATE, context);
        TaskContainer.addTasks("GatkSortSam");
        return BamResult.builder()
                .command(BashCommand.withTool(cmd))
                .bamOutput(BamOutput.builder().bam(bam).sortedBam(toolFields.getSortedBam()).build())
                .build();
    }
}
