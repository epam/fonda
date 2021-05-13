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
import org.apache.commons.lang3.Validate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static com.epam.fonda.utils.ToolUtils.validate;

@RequiredArgsConstructor
public class Mutect1 implements Tool<VariantsVcfResult> {

    private static final String MUTECT1_TOOL_TEMPLATE_NAME = "mutect1_template";

    private final String sampleName;
    private final BamOutput bam;
    private final String sampleOutDir;

    @Data
    @Builder
    private static class ToolFields {
        private final String genome;
        private final String mutectJava;
        private final String bed;
        private final String mutect;
        private final String dbsnp;
        private final String cosmic;
        private final String mutectNormalPanel;
        private final String sampleName;
        private final String outDir;
        private final String outTmpDir;
        private final String bam;
        private final String vcf;
    }

    /**
     * Generates bash script {@link BashCommand} for Mutect1 tool.
     * @param configuration is the type of {@link Configuration} which contains fields:
     *                      mutect, mutect_java, bed, genome, cosmic, dbsnp, mutect_normal_panel.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link BashCommand} with bash script.
     */
    @Override
    public VariantsVcfResult generate(final Configuration configuration, final TemplateEngine templateEngine) {
        final ToolFields toolFields = initToolFields(configuration);
        final Context context = new Context();
        context.setVariable("toolFields", toolFields);
        final String cmd = templateEngine.process(MUTECT1_TOOL_TEMPLATE_NAME, context);
        TaskContainer.addTasks("Mutect1 detection");
        final VariantsVcfOutput output = VariantsVcfOutput.builder()
                .variantsVcf(toolFields.getVcf())
                .variantsOutputDir(toolFields.getOutDir())
                .variantsTmpOutputDir(toolFields.getOutTmpDir())
                .build();
        output.createDirectory();
        return VariantsVcfResult.builder()
                .abstractCommand(BashCommand.withTool(cmd))
                .filteredTool("mutect1")
                .variantsVcfOutput(output)
                .build();
    }

    private ToolFields initToolFields(final Configuration configuration) {
        Validate.notNull(bam, "Bam file should be specified");
        final String outputDir = String.format("%s/mutect1", sampleOutDir);
        final String outputTmpDir = String.format("%s/mutect1/tmp", sampleOutDir);
        final GlobalConfig.DatabaseConfig databaseConfig = configuration.getGlobalConfig().getDatabaseConfig();
        final GlobalConfig.ToolConfig toolConfig = configuration.getGlobalConfig().getToolConfig();
        return ToolFields.builder()
                .genome(validate(databaseConfig.getGenome(), GlobalConfigFormat.GENOME))
                .bed(validate(databaseConfig.getBed(), GlobalConfigFormat.BED))
                .bam(validate(bam.getBam(), ToolUtils.BAM))
                .mutectJava(validate(toolConfig.getMutectJava(), GlobalConfigFormat.MUTECT_JAVA))
                .mutect(validate(toolConfig.getMutect(), GlobalConfigFormat.MUTECT))
                .dbsnp(validate(databaseConfig.getDbsnp(), GlobalConfigFormat.DBSNP))
                .cosmic(validate(databaseConfig.getCosmic(), GlobalConfigFormat.COSMIC))
                .mutectNormalPanel(validate(databaseConfig.getMutectNormalPanel(),
                        GlobalConfigFormat.MUTECT_NORMAL_PANEL))
                .sampleName(validate(sampleName, ToolUtils.SAMPLE_NAME))
                .vcf(String.format("%s/%s.mutect1.variants.vcf", outputDir, sampleName))
                .outDir(outputDir)
                .outTmpDir(outputTmpDir)
                .build();
    }
}
