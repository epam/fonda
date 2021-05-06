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
import com.epam.fonda.tools.results.VariantsVcfOutput;
import com.epam.fonda.tools.results.VariantsVcfResult;
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
public class Mutect2 implements Tool<VariantsVcfResult> {
    private static final String MUTECT2_TEMPLATE = "mutect2_template";

    private final String sampleName;
    private final BamOutput bam;
    private final String sampleOutDir;
    private final String controlSampleName;

    @Data
    @Builder
    private static class ToolFields {
        private final String genome;
        private final String bed;
        private final String gatk;
        private final String sampleName;
        private final String controlSampleName;
        private final String germlineResource;
        private final String panelOfNormal;
        private final String bamOut;
        private final String f1r2TarGz;
        private final String bam;
        private final String controlBam;
        private final String vcf;
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
        final String outputDir = format("%s/mutect2", sampleOutDir);
        final ToolFields toolFields = initToolFields(configuration, outputDir);
        final Context context = new Context();
        context.setVariable("toolFields", toolFields);
        final String cmd = templateEngine.process(MUTECT2_TEMPLATE, context);
        TaskContainer.addTasks("Mutect2 detection");
        final VariantsVcfOutput output = VariantsVcfOutput.builder()
                .variantsVcf(toolFields.getVcf())
                .variantsOutputDir(outputDir)
                .build();
        output.createDirectory();
        return VariantsVcfResult.builder()
                .abstractCommand(BashCommand.withTool(cmd))
                .filteredTool("mutect2")
                .variantsVcfOutput(output)
                .build();
    }

    private ToolFields initToolFields(final Configuration configuration, final String outputDir) {
        final GlobalConfig.DatabaseConfig databaseConfig = configuration.getGlobalConfig().getDatabaseConfig();
        final GlobalConfig.ToolConfig toolConfig = configuration.getGlobalConfig().getToolConfig();
        return ToolFields.builder()
                .genome(validate(databaseConfig.getGenome(), GlobalConfigFormat.GENOME))
                .bed(databaseConfig.getBed())
                .gatk(validate(toolConfig.getGatk(), GlobalConfigFormat.GATK))
                .sampleName(validate(sampleName, ToolUtils.SAMPLE_NAME))
                .controlSampleName(controlSampleName)
                .bam(validate(bam.getBam(), ToolUtils.BAM))
                .controlBam(bam.getControlBam())
                .germlineResource(databaseConfig.getGermlineResource())
                .panelOfNormal(databaseConfig.getMutectNormalPanel())
                .bamOut(format("%s/%s.mutect2.bamout.bam", outputDir, sampleName))
                .f1r2TarGz(format("%s/%s.mutect2.f1r2.tar.gz", outputDir, sampleName))
                .vcf(format("%s/%s.mutect2.somatic.variants.vcf", outputDir, sampleName))
                .build();
    }
}
