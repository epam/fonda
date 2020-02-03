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
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.entity.configuration.GlobalConfigFormat;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.VariantsVcfOutput;
import com.epam.fonda.tools.results.VariantsVcfResult;
import com.epam.fonda.utils.DnaUtils;
import com.epam.fonda.utils.ToolUtils;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Collections;

import static com.epam.fonda.utils.PipelineUtils.TASK_TO_CHECK;
import static com.epam.fonda.utils.ToolUtils.validate;

@RequiredArgsConstructor
public class Mutect2 implements Tool<VariantsVcfResult> {
    private static final String MUTECT2_TEMPLATE = "mutect2_template";

    private final String sampleName;
    private final BamOutput bam;
    private final String sampleOutDir;

    @Data
    @Builder
    private static class ToolFields {
        private final String genome;
        private final String bed;
        private final String java;
        private final String gatk;
        private final String sampleName;
        private final String outDir;
        private final String outTmpDir;
        private final String bam;
        private final String controlBam;
        private final String vcf;
        private boolean isWgs;
    }

    /**
     * Generates bash script {@link BashCommand} for Mutect2 tool.
     * @param configuration is the type of {@link Configuration} which contains fields:
     *                      gatk, java, bed, genome.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link BashCommand} with bash script.
     */
    @Override
    public VariantsVcfResult generate(final Configuration configuration, final TemplateEngine templateEngine) {
        final ToolFields toolFields = initToolFields(configuration);
        final Context context = new Context();
        context.setVariable("toolFields", toolFields);
        final String cmd = templateEngine.process(MUTECT2_TEMPLATE, context);
        TASK_TO_CHECK.add("Mutect2 detection");
        final VariantsVcfOutput output = VariantsVcfOutput.builder()
                .variantsVcf(toolFields.getVcf())
                .variantsOutputDir(toolFields.getOutDir())
                .variantsTmpOutputDir(toolFields.getOutTmpDir())
                .build();
        output.createDirectory();
        final BashCommand abstractCommand = BashCommand.withTool(cmd);
        abstractCommand.setTempDirs(Collections.singletonList(toolFields.getOutTmpDir()));
        return VariantsVcfResult.builder()
                .abstractCommand(abstractCommand)
                .filteredTool("mutect2")
                .variantsVcfOutput(output)
                .build();
    }

    private ToolFields initToolFields(final Configuration configuration) {
        final String outputDir = String.format("%s/mutect2", sampleOutDir);
        final String outputTmpDir = String.format("%s/mutect2/tmp", sampleOutDir);
        final GlobalConfig.DatabaseConfig databaseConfig = configuration.getGlobalConfig().getDatabaseConfig();
        final GlobalConfig.ToolConfig toolConfig = configuration.getGlobalConfig().getToolConfig();
        return ToolFields.builder()
                .genome(validate(databaseConfig.getGenome(), GlobalConfigFormat.GENOME))
                .bed(validate(databaseConfig.getBed(), GlobalConfigFormat.BED))
                .java(validate(toolConfig.getJava(), GlobalConfigFormat.JAVA))
                .gatk(validate(toolConfig.getGatk(), GlobalConfigFormat.GATK))
                .sampleName(validate(sampleName, ToolUtils.SAMPLE_NAME))
                .bam(validate(bam.getBam(), ToolUtils.BAM))
                .controlBam(validate(bam.getControlBam(), ToolUtils.CONTROL_BAM))
                .outDir(outputDir)
                .outTmpDir(outputTmpDir)
                .vcf(String.format("%s/%s.mutect2.somatic.variants.vcf", outputDir, sampleName))
                .isWgs(DnaUtils.isWgsWorkflow(configuration))
                .build();
    }
}
