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
import com.epam.fonda.tools.results.VariantsVcfOutput;
import com.epam.fonda.tools.results.VariantsVcfResult;
import com.epam.fonda.workflow.TaskContainer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static com.epam.fonda.utils.ToolUtils.validate;
import static java.lang.String.format;

@RequiredArgsConstructor
@AllArgsConstructor
public class FilterMutectCalls implements Tool<VariantsVcfResult> {

    private static final String FILTER_MUTECT_CALLS_TOOL_TEMPLATE = "filter_mutect_calls_tool_template";

    @NonNull
    private final String sampleName;
    @NonNull
    private final String outputDir;
    @NonNull
    private final String vcf;
    private String contamTable;
    private String segments;
    private String artifactsPriors;

    @Data
    @Builder
    private static class ToolFields {
        private final String genome;
        private final String gatk;
        private final String contamTable;
        private final String segments;
        private final String artifactsPriors;
        private final String vcfStats;
        private final String filteringStats;
        private final String vcf;
        private final boolean includeOptions;
        private final String filteredVcf;
    }

    @Override
    public VariantsVcfResult generate(Configuration configuration, TemplateEngine templateEngine) {
        final ToolFields toolFields = initToolFields(configuration);
        final Context context = new Context();
        context.setVariable("toolFields", toolFields);
        final String cmd = templateEngine.process(FILTER_MUTECT_CALLS_TOOL_TEMPLATE, context);
        TaskContainer.addTasks("FilterMutectCalls");
        final VariantsVcfOutput output = VariantsVcfOutput.builder()
                .variantsVcfFiltered(toolFields.getFilteredVcf())
                .build();
        return VariantsVcfResult.builder()
                .abstractCommand(BashCommand.withTool(cmd))
                .filteredTool("mutect2")
                .variantsVcfOutput(output)
                .build();
    }

    private ToolFields initToolFields(final Configuration configuration) {
        final GlobalConfig.DatabaseConfig databaseConfig = configuration.getGlobalConfig().getDatabaseConfig();
        final GlobalConfig.ToolConfig toolConfig = configuration.getGlobalConfig().getToolConfig();
        return ToolFields.builder()
                .genome(validate(databaseConfig.getGenome(), GlobalConfigFormat.GENOME))
                .gatk(validate(toolConfig.getGatk(), GlobalConfigFormat.GATK))
                .vcf(vcf)
                .contamTable(contamTable)
                .segments(segments)
                .artifactsPriors(artifactsPriors)
                .vcfStats(format("%s/%s.vcf.stats", outputDir, sampleName))
                .filteringStats(format("%s/%s.filtering.stats", outputDir, sampleName))
                .includeOptions(!"mm10".equals(configuration.getGlobalConfig().getDatabaseConfig().getGenomeBuild()))
                .filteredVcf(format("%s/%s.filtered.vcf", outputDir, sampleName))
                .build();
    }
}
