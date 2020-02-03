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
import com.epam.fonda.entity.configuration.GlobalConfigFormat;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.VariantsVcfResult;
import com.epam.fonda.tools.results.VcfScnpeffAnnonationResult;
import com.epam.fonda.tools.results.VcfScnpeffAnnotationOutput;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static com.epam.fonda.utils.PipelineUtils.TASK_TO_CHECK;
import static com.epam.fonda.utils.PipelineUtils.getExecutionPath;
import static com.epam.fonda.utils.ToolUtils.validate;

@RequiredArgsConstructor
public class VcfSnpeffAnnotation implements Tool<VcfScnpeffAnnonationResult> {

    private static final String VCF_SNPEFF_ANNOTATION_TOOL_TEMPLATE_NAME = "vcf_snpeff_annotation_tool_template";

    @Data
    private class ToolFields {
        private String transvar;
        private String python;
        private String snpsift;
    }

    @Data
    private class DatabaseFields {
        private String genomeBuild;
        private String snpsiftDb;
        private String canonicalTranscript;
    }

    @Data
    private class AdditionalFields {
        private String jarPath;
        private String variantsVcf;
        private String var2Snpsift;
        private String filteredTool;
    }

    @NonNull
    private String sampleName;
    @NonNull
    private VariantsVcfResult variantsVcfResult;

    /**
     * This method generates {@link VcfScnpeffAnnonationResult} for VcfSnpeffAnnotation tool.
     *
     * @param configuration  is the type of {@link Configuration} which contains
     *                       its fields: transvar, python, snpsift, snpsiftDb, genomeBuild, canonicalTranscript,
     *                       vcfSnpeffAnnotation.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link VcfScnpeffAnnonationResult} with bash script.
     **/
    @Override
    public VcfScnpeffAnnonationResult generate(Configuration configuration, TemplateEngine templateEngine) {
        AdditionalFields additionalFields = initializeAdditionalFields();
        String cmd = templateEngine.process(VCF_SNPEFF_ANNOTATION_TOOL_TEMPLATE_NAME,
                buildContext(configuration, additionalFields));
        TASK_TO_CHECK.add("SnpEff annotation");
        VcfScnpeffAnnotationOutput vcfScnpeffAnnotationOutput = VcfScnpeffAnnotationOutput.builder()
                .var2Snpsift(additionalFields.var2Snpsift)
                .build();
        return VcfScnpeffAnnonationResult.builder()
                .vcfScnpeffAnnotationOutput(vcfScnpeffAnnotationOutput)
                .command(new BashCommand(cmd))
                .build();
    }

    /**
     * This method initializes fields of the ToolFields {@link ToolFields} class.
     *
     * @param configuration is the type of {@link Configuration} which contains
     *                      its fields: transvar, python, snpsift.
     * @return {@link ToolFields} with its fields.
     **/
    private ToolFields initializeToolFields(Configuration configuration) {
        ToolFields toolFields = new ToolFields();
        toolFields.transvar = validate(configuration.getGlobalConfig().getToolConfig().getTransvar(),
                GlobalConfigFormat.TRANSVAR);
        toolFields.python = validate(configuration.getGlobalConfig().getToolConfig().getPython(),
                GlobalConfigFormat.PYTHON);
        toolFields.snpsift = validate(configuration.getGlobalConfig().getToolConfig().getSnpsift(),
                GlobalConfigFormat.SNPSIFT);
        return toolFields;
    }

    /**
     * This method initializes fields of the DatabaseFields {@link DatabaseFields} class.
     *
     * @param configuration is the type of {@link Configuration} which contains
     *                      its fields: snpsiftDb, genomeBuild, canonicalTranscript.
     * @return {@link DatabaseFields} with its fields.
     **/
    private DatabaseFields initializeDatabaseFields(Configuration configuration) {
        DatabaseFields databaseFields = new DatabaseFields();
        databaseFields.genomeBuild = validate(configuration.getGlobalConfig().getDatabaseConfig().getGenomeBuild(),
                GlobalConfigFormat.GENOME_BUILD);
        databaseFields.snpsiftDb = validate(configuration.getGlobalConfig().getDatabaseConfig().getSnpsiftdb(),
                GlobalConfigFormat.SNPSIFTDB);
        databaseFields.canonicalTranscript = validate(
                configuration.getGlobalConfig().getDatabaseConfig().getCanonicalTranscript(),
                GlobalConfigFormat.CANONICAL_TRANSCRIPT);
        return databaseFields;
    }

    /**
     * This method initializes fields of the AdditionalFields {@link AdditionalFields} class.
     *
     * @return {@link AdditionalFields} with its fields.
     **/
    private AdditionalFields initializeAdditionalFields() {
        AdditionalFields additionalFields = new AdditionalFields();
        additionalFields.jarPath = getExecutionPath();
        additionalFields.variantsVcf = variantsVcfResult.getVariantsVcfOutput().getVariantsVcf();
        additionalFields.var2Snpsift = additionalFields.variantsVcf
                .replace(".vcf", ".pass.annotation.tsv");
        additionalFields.filteredTool = variantsVcfResult.getFilteredTool();
        return additionalFields;
    }

    /**
     * This method build Tmymeleaf context for vcf_snpeff_annotation_tool_template.txt template
     *
     * @param configuration is the type of {@link Configuration} which contains
     *                      its fields: transvar, python, snpsift, snpsiftDb, genomeBuild, canonicalTranscript,
     *                      vcfSnpeffAnnotation.
     * @return {@link Context}
     */
    private Context buildContext(Configuration configuration, AdditionalFields additionalFields) {
        Context context = new Context();
        context.setVariable("toolFields", initializeToolFields(configuration));
        context.setVariable("databaseFields", initializeDatabaseFields(configuration));
        context.setVariable("additionalFields", additionalFields);
        context.setVariable("sampleName", sampleName);
        return context;
    }
}
