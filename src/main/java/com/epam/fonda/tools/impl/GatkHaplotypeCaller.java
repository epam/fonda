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
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.VariantsVcfOutput;
import com.epam.fonda.tools.results.VariantsVcfResult;
import com.epam.fonda.utils.DnaUtils;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Collections;

import static java.lang.String.format;

@RequiredArgsConstructor
public class GatkHaplotypeCaller implements Tool<VariantsVcfResult> {

    private static final String AMPLICON_GATK_HAPLOTYPE_TOOL_TEMPLATE_NAME = "amplicon_gatk_haplotype_tool_template";

    @Data
    private class ToolFields {
        private String java;
        private String gatk;
    }

    @Data
    private class DatabaseFields {
        private String genome;
        private String bed;
    }

    @Data
    private class AdditionalFields {
        private String gatkHapOutdir;
        private String gatkHapVariants;
        private String tmpGatkHapOutdir;
        private boolean isWgs;
    }

    @NonNull
    private String sampleName;
    @NonNull
    private String bam;
    @NonNull
    private String outDir;

    /**
     * This method generates bash script for GatkHaplotypeCaller tool
     *
     * @param configuration  is of type {@link Configuration} and contains its fields.
     * @param templateEngine is of type {@link TemplateEngine} and contains thymeleaf engine
     * @return is of type {@link VariantsVcfResult} and contains bash script
     */
    @Override
    public VariantsVcfResult generate(Configuration configuration, TemplateEngine templateEngine) {
        final AdditionalFields additionalFields = initializeAdditionalFields(configuration);
        final String cmd = templateEngine.process(AMPLICON_GATK_HAPLOTYPE_TOOL_TEMPLATE_NAME,
                buildContext(configuration, additionalFields));
        VariantsVcfOutput variantsVcfOutput = VariantsVcfOutput.builder()
                .variantsVcf(additionalFields.gatkHapVariants)
                .variantsTmpOutputDir(additionalFields.tmpGatkHapOutdir)
                .variantsOutputDir(additionalFields.gatkHapOutdir)
                .build();
        variantsVcfOutput.createDirectory();
        VariantsVcfResult variantsVcfResult = VariantsVcfResult.builder()
                .variantsVcfOutput(variantsVcfOutput)
                .filteredTool("gatkHaplotypeCaller")
                .abstractCommand(new BashCommand(cmd))
                .build();
        variantsVcfResult.getAbstractCommand().setTempDirs(Collections.singletonList(additionalFields
                .tmpGatkHapOutdir));
        return variantsVcfResult;
    }

    private ToolFields initializeToolFields(Configuration configuration) {
        ToolFields toolFields = new ToolFields();
        toolFields.java = configuration.getGlobalConfig().getToolConfig().getJava();
        toolFields.gatk = configuration.getGlobalConfig().getToolConfig().getGatk();
        return toolFields;
    }

    private DatabaseFields initializeDatabaseFields(Configuration configuration) {
        DatabaseFields databaseFields = new DatabaseFields();
        databaseFields.genome = configuration.getGlobalConfig().getDatabaseConfig().getGenome();
        databaseFields.bed = configuration.getGlobalConfig().getDatabaseConfig().getBed();
        return databaseFields;
    }

    private AdditionalFields initializeAdditionalFields(Configuration configuration) {
        AdditionalFields additionalFields = new AdditionalFields();
        additionalFields.gatkHapOutdir = format("%s/gatkHaplotypeCaller", outDir);
        additionalFields.gatkHapVariants = format("%s/%s.gatkHaplotypeCaller.variants.vcf", additionalFields
                        .gatkHapOutdir, sampleName);
        additionalFields.tmpGatkHapOutdir = format("%s/tmp", additionalFields.gatkHapOutdir);
        additionalFields.isWgs = DnaUtils.isWgsWorkflow(configuration);
        return additionalFields;
    }

    private Context buildContext(Configuration configuration, AdditionalFields additionalFields) {
        Context context = new Context();
        context.setVariable("toolFields", initializeToolFields(configuration));
        context.setVariable("databaseFields", initializeDatabaseFields(configuration));
        context.setVariable("additionalFields", additionalFields);
        context.setVariable("bam", bam);
        return context;
    }
}
