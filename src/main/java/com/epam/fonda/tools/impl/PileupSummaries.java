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
import com.epam.fonda.tools.results.PileupSummariesResult;
import com.epam.fonda.utils.ToolUtils;
import com.epam.fonda.workflow.TaskContainer;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static com.epam.fonda.utils.ToolUtils.validate;
import static java.lang.String.format;

@RequiredArgsConstructor
public class PileupSummaries implements Tool<PileupSummariesResult> {
    private static final String PILEUP_SUMMARIES_TOOL_TEMPLATE_NAME = "pileup_summaries_template";

    private final String sampleName;
    private final BamOutput bam;
    private final String outputDir;

    @Data
    @Builder
    private static class ToolFields {
        private final String bam;
        private final String genome;
        private final String bed;
        private final String gatk;
        private final String contamVcf;
        private final String seqDict;
        private final String pileupTable;
    }

    @Override
    public PileupSummariesResult generate(Configuration configuration, TemplateEngine templateEngine) {
        final ToolFields toolFields = initToolFields(configuration);
        final Context context = new Context();
        context.setVariable("toolFields", toolFields);
        final String cmd = templateEngine.process(PILEUP_SUMMARIES_TOOL_TEMPLATE_NAME, context);
        TaskContainer.addTasks("PileupSummaries");
        return PileupSummariesResult.builder()
                .command(BashCommand.withTool(cmd))
                .pileupTable(toolFields.pileupTable)
                .build();
    }

    private ToolFields initToolFields(final Configuration configuration) {
        final GlobalConfig.DatabaseConfig databaseConfig = configuration.getGlobalConfig().getDatabaseConfig();
        final GlobalConfig.ToolConfig toolConfig = configuration.getGlobalConfig().getToolConfig();
        return ToolFields.builder()
                .gatk(validate(toolConfig.getGatk(), GlobalConfigFormat.GATK))
                .genome(validate(databaseConfig.getGenome(), GlobalConfigFormat.GENOME))
                .bam(validate(bam.getBam(), ToolUtils.BAM))
                .bed(databaseConfig.getBed())
                .contamVcf(databaseConfig.getContaminationVCF())
                .seqDict(databaseConfig.getSequenceDictionary())
                .pileupTable(format("%s/%s.tumor-pileups.table", outputDir, sampleName))
                .build();
    }
}
