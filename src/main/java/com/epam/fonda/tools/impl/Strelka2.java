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
import org.apache.commons.lang3.Validate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static com.epam.fonda.utils.PipelineUtils.TASK_TO_CHECK;
import static com.epam.fonda.utils.ToolUtils.validate;

@RequiredArgsConstructor
public class Strelka2 implements Tool<VariantsVcfResult> {

    private static final String STRELKA2_TOOL_TEMPLATE_NAME = "strelka2_template";

    private final String sampleName;
    private final BamOutput bam;
    private final String sampleOutDir;
    private final boolean isPaired;

    @Data
    @Builder
    private static class ToolFields {
        private final String strelka2;
        private final String python;
        private final String genome;
        private final String bed;
        private final String bam;
        private final String controlBam;
        private final String vcf;
        private final String sampleName;
        private final String controlSampleName;
        private final String outDir;
        private final boolean isPaired;
        private final boolean isWgs;
        private final String memMb;
    }

    /**
     * Generates bash script {@link BashCommand} for Strelka2 tool.
     * @param configuration is the type of {@link Configuration} which contains fields:
     *                      strelka2, python, bed, genome.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link BashCommand} with bash script.
     */
    @Override
    public VariantsVcfResult generate(final Configuration configuration, final TemplateEngine templateEngine) {
        final ToolFields toolFields = initToolFields(configuration);
        final Context context = new Context();
        context.setVariable("toolFields", toolFields);
        final String cmd = templateEngine.process(STRELKA2_TOOL_TEMPLATE_NAME, context);
        TASK_TO_CHECK.add("Stelka2 detection");
        final VariantsVcfOutput output = VariantsVcfOutput.builder()
                .variantsVcf(toolFields.getVcf())
                .variantsOutputDir(toolFields.getOutDir())
                .build();
        output.createDirectory();
        return VariantsVcfResult.builder()
                .abstractCommand(BashCommand.withTool(cmd))
                .filteredTool("strelka2")
                .variantsVcfOutput(output)
                .build();
    }

    private ToolFields initToolFields(final Configuration configuration) {
        Validate.notNull(bam, "Bam file should be specified");
        final String outputDir = String.format("%s/strelka2", sampleOutDir);
        final GlobalConfig.DatabaseConfig databaseConfig = configuration.getGlobalConfig().getDatabaseConfig();
        final GlobalConfig.ToolConfig toolConfig = configuration.getGlobalConfig().getToolConfig();
        final boolean isWgs = DnaUtils.isWgsWorkflow(configuration);
        return ToolFields.builder()
                .genome(validate(databaseConfig.getGenome(), GlobalConfigFormat.GENOME))
                .bed(validate(databaseConfig.getBed(), GlobalConfigFormat.BED))
                .python(validate(toolConfig.getPython(), GlobalConfigFormat.PYTHON))
                .strelka2(validate(toolConfig.getStrelka2(), GlobalConfigFormat.STRELKA2))
                .bam(validate(bam.getBam(), ToolUtils.BAM))
                .controlBam(isPaired ? validate(bam.getControlBam(), ToolUtils.CONTROL_BAM) : null)
                .sampleName(validate(sampleName, ToolUtils.SAMPLE_NAME))
                .outDir(outputDir)
                .vcf(outputFile(outputDir, sampleName))
                .isPaired(isPaired)
                .isWgs(isWgs)
                .memMb(isWgs ? "10240" : "2048")
                .build();
    }

    private String outputFile(final String outputDir, final String sampleName) {
        final String vcfName = isPaired ? "strelka2.somatic.variants.vcf" : "strelka2.variants.vcf";
        return String.format("%s/%s.%s", outputDir, sampleName, vcfName);
    }
}
