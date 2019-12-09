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
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.ExomecnvOutput;
import com.epam.fonda.tools.results.ExomecnvResult;
import com.epam.fonda.utils.ToolUtils;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Arrays;

import static com.epam.fonda.utils.ToolUtils.validate;

@RequiredArgsConstructor
public class Exomecnv implements Tool<ExomecnvResult> {
    private static final String EXOMECNV_TEMPLATE = "exomecnv_template";

    private final String sampleName;
    private final String controlSampleName;
    private final BamOutput bamOutput;
    private final String sampleOutDir;

    @Data
    @Builder
    private static class ToolFields {
        private final String exomecnv;
        private final String java;
        private final String rScript;
        private final String gatk;
        private final String genome;
        private final String bed;
        private final String bam;
        private final String controlBam;
        private final String sampleName;
        private final String controlSampleName;
        private final String outDir;
        private final String readDepthSummary;
        private final String controlReadDepthSummary;
    }

    /**
     * Generates bash script {@link BashCommand} for ExomeCNV tool.
     * @param configuration is the type of {@link Configuration} which contains fields:
     *                      exomecnv, java, gatk, Rscript, bed, genome.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link BashCommand} with bash script.
     */
    @Override
    public ExomecnvResult generate(final Configuration configuration, final TemplateEngine templateEngine) {
        final ToolFields toolFields = initToolFields(configuration);
        final Context context = new Context();
        context.setVariable("toolFields", toolFields);
        final String cmd = templateEngine.process(EXOMECNV_TEMPLATE, context);
        final ExomecnvOutput output = ExomecnvOutput.builder()
                .readDepthSummary(toolFields.getReadDepthSummary())
                .controlReadDepthSummary(toolFields.getControlReadDepthSummary())
                .outDir(toolFields.getOutDir())
                .build();
        output.createDirectory();
        final BashCommand command = BashCommand.withTool(cmd);
        command.setTempDirs(Arrays.asList(toolFields.getControlReadDepthSummary(), toolFields.getReadDepthSummary()));
        return ExomecnvResult.builder()
                .command(command)
                .toolName("exomecnv")
                .output(output)
                .build();
    }

    private ToolFields initToolFields(final Configuration configuration) {
        final String outputDir = String.format("%s/exomecnv", sampleOutDir);
        final GlobalConfig.ToolConfig toolConfig = configuration.getGlobalConfig().getToolConfig();
        final GlobalConfig.DatabaseConfig databaseConfig = configuration.getGlobalConfig().getDatabaseConfig();
        return ToolFields.builder()
                .exomecnv(validate(toolConfig.getExomecnv(), GlobalConfigFormat.EXOMECNV))
                .java(validate(toolConfig.getJava(), GlobalConfigFormat.JAVA))
                .rScript(validate(toolConfig.getRScript(), GlobalConfigFormat.R_SCRIPT))
                .gatk(validate(toolConfig.getGatk(), GlobalConfigFormat.GATK))
                .genome(validate(databaseConfig.getGenome(), GlobalConfigFormat.GENOME))
                .bed(validate(databaseConfig.getBed(), GlobalConfigFormat.BED))
                .bam(validate(bamOutput.getBam(), ToolUtils.BAM))
                .controlBam(validate(bamOutput.getControlBam(), ToolUtils.CONTROL_BAM))
                .sampleName(validate(sampleName, ToolUtils.SAMPLE_NAME))
                .controlSampleName(validate(controlSampleName, ToolUtils.CONTROL_SAMPLE_NAME))
                .outDir(outputDir)
                .readDepthSummary(format(outputDir, sampleName))
                .controlReadDepthSummary(format(outputDir, controlSampleName))
                .build();
    }

    private String format(final String outputDir, final String sampleName) {
        return String.format("%s/%s.sample_interval_summary", outputDir, sampleName);
    }
}
