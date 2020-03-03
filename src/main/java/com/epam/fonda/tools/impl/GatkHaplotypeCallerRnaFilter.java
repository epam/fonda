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
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.VariantsVcfResult;
import com.epam.fonda.workflow.TaskContainer;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static com.epam.fonda.utils.ToolUtils.validate;

@Data
public class GatkHaplotypeCallerRnaFilter implements Tool<VariantsVcfResult> {
    private static final String GATK_HAPLOTYPE_CALLER_RNA_FILTER_TOOL_TEMPLATE_NAME =
            "gatk_haplotype_caller_rna_filter_tool_template";

    @NonNull
    private String sampleName;
    @NonNull
    private VariantsVcfResult variantsVcfResult;

    /**
     * This method generates bash script for GatkHaplotypeCallerRnaFilter tool
     *
     * @param configuration  is of type {@link Configuration} and contains its fields.
     * @param templateEngine is of type {@link TemplateEngine} and contains thymeleaf engine
     * @return is of type {@link VariantsVcfResult} and contains bash script
     */
    @Override
    public VariantsVcfResult generate(Configuration configuration, TemplateEngine templateEngine) {
        ToolFields toolFields = initializeToolFields(configuration);
        AdditionalFields additionalFields = initializeAdditionalFields();
        final Context context = buildContext(toolFields, additionalFields);
        final String cmd = templateEngine.process(GATK_HAPLOTYPE_CALLER_RNA_FILTER_TOOL_TEMPLATE_NAME, context);
        TaskContainer.addTasks("GATK haplotypecaller filtration");
        AbstractCommand command = BashCommand.withTool(variantsVcfResult.getAbstractCommand().getToolCommand() + cmd);
        variantsVcfResult.setAbstractCommand(command);
        variantsVcfResult.getVariantsVcfOutput().setVariantsVcf(additionalFields.variantsVcfFiltered);
        return variantsVcfResult;
    }

    private Context buildContext(ToolFields toolFields, AdditionalFields additionalFields) {
        final Context context = new Context();
        context.setVariable("toolFields", toolFields);
        context.setVariable("additionalFields", additionalFields);
        context.setVariable("gatkHapRaw", variantsVcfResult.getVariantsVcfOutput().getVariantsVcf());
        return context;
    }

    private ToolFields initializeToolFields(Configuration configuration) {
        return ToolFields.builder()
                .genome(validate(configuration.getGlobalConfig().getDatabaseConfig().getGenome(),
                        GlobalConfigFormat.GENOME))
                .java(validate(configuration.getGlobalConfig().getToolConfig().getJava(), GlobalConfigFormat.JAVA))
                .gatk(validate(configuration.getGlobalConfig().getToolConfig().getGatk(), GlobalConfigFormat.GATK))
                .build();
    }

    private AdditionalFields initializeAdditionalFields() {
        String gatkHapOutdir = variantsVcfResult.getVariantsVcfOutput().getVariantsOutputDir();
        return AdditionalFields.builder()
                .gatkHapOutdir(gatkHapOutdir)
                .tmpGatkHapOutdir(variantsVcfResult.getVariantsVcfOutput().getVariantsTmpOutputDir())
                .variantsVcfFiltered(String.format("%s/%s.gatkHaplotypeCaller.variants.vcf", gatkHapOutdir, sampleName))
                .build();
    }

    @Data
    @Builder
    private static class ToolFields {
        private String genome;
        private String java;
        private String gatk;
    }

    @Data
    @Builder
    private static class AdditionalFields {
        private String gatkHapOutdir;
        private String tmpGatkHapOutdir;
        private String variantsVcfFiltered;
    }
}
