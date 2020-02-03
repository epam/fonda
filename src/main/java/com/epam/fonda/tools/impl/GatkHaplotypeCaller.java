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
import com.epam.fonda.entity.configuration.GlobalConfigFormat;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.VariantsVcfOutput;
import com.epam.fonda.tools.results.VariantsVcfResult;
import com.epam.fonda.utils.DnaUtils;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Collections;

import static com.epam.fonda.utils.ToolUtils.validate;
import static java.lang.String.format;

@RequiredArgsConstructor
public class GatkHaplotypeCaller implements Tool<VariantsVcfResult> {
    private static final String AMPLICON_GATK_HAPLOTYPE_TOOL_TEMPLATE_NAME = "amplicon_gatk_haplotype_tool_template";
    private static final String GATK_HAPLOTYPE_RNA_TOOL_TEMPLATE_NAME = "gatk_haplotype_rna_tool_template";

    @Data
    @Builder
    private static class ToolFields {
        private String java;
        private String gatk;
    }

    @Data
    @Builder
    private static class DatabaseFields {
        private String genome;
        private String bed;
    }

    @Data
    @Builder
    private static class AdditionalFields {
        private String gatkHapOutdir;
        private String gatkHapRawVcf;
        private String tmpGatkHapOutdir;
        private String variantsVcf;
        private boolean isWgs;
    }

    @NonNull
    private String sampleName;
    @NonNull
    private String bam;
    @NonNull
    private String outDir;
    @NonNull
    private Boolean isRnaCaptureRnaWorkflow;

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
        Context context = buildContext(configuration, additionalFields);
        final String cmd = isRnaCaptureRnaWorkflow
                ? templateEngine.process(GATK_HAPLOTYPE_RNA_TOOL_TEMPLATE_NAME, context)
                : templateEngine.process(AMPLICON_GATK_HAPLOTYPE_TOOL_TEMPLATE_NAME, context);
        VariantsVcfOutput variantsVcfOutput = VariantsVcfOutput.builder()
                .gatkHapRawVcf(additionalFields.gatkHapRawVcf)
                .variantsTmpOutputDir(additionalFields.tmpGatkHapOutdir)
                .variantsOutputDir(additionalFields.gatkHapOutdir)
                .variantsVcf(additionalFields.variantsVcf)
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
        return ToolFields.builder()
                .java(validate(configuration.getGlobalConfig().getToolConfig().getJava(), GlobalConfigFormat.JAVA))
                .gatk(validate(configuration.getGlobalConfig().getToolConfig().getGatk(), GlobalConfigFormat.GATK))
                .build();
    }

    private DatabaseFields initializeDatabaseFields(Configuration configuration) {
        return DatabaseFields.builder()
                .genome(validate(configuration.getGlobalConfig().getDatabaseConfig().getGenome(),
                        GlobalConfigFormat.GENOME))
                .bed(DnaUtils.isWgsWorkflow(configuration) ? null
                        : validate(configuration.getGlobalConfig().getDatabaseConfig().getBed(),
                        GlobalConfigFormat.BED))
                .build();
    }

    private AdditionalFields initializeAdditionalFields(Configuration configuration) {
        final String gatkHapOutdir = format("%s/gatkHaplotypeCaller", outDir);
        return AdditionalFields.builder()
                .gatkHapOutdir(gatkHapOutdir)
                .gatkHapRawVcf(format("%s/%s.gatkHaplotypeCaller.raw.vcf", gatkHapOutdir, sampleName))
                .tmpGatkHapOutdir(format("%s/tmp", gatkHapOutdir))
                .isWgs(DnaUtils.isWgsWorkflow(configuration))
                .variantsVcf(format("%s/%s.gatkHaplotypeCaller.variants.vcf", gatkHapOutdir, sampleName))
                .build();
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
