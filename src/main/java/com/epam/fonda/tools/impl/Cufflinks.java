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
import com.epam.fonda.entity.configuration.StudyConfigFormat;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.CufflinksOutput;
import com.epam.fonda.tools.results.CufflinksResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static com.epam.fonda.utils.ToolUtils.validate;
import static java.lang.String.format;

@AllArgsConstructor
public class Cufflinks implements Tool<CufflinksResult> {

    private static final String CUFFLINKS_TOOL_TEMPLATE_NAME = "cufflinks_tool_template";

    private String sampleName;
    private String sampleOutputDir;
    private BamOutput bamOutput;

    /**
     * This method generates a bash script for {@link Cufflinks} tool.
     *
     * @param configuration  the {@link Configuration} that is used to generate a bash script.
     * @param templateEngine the {@link TemplateEngine}.
     * @return the {@link BashCommand} with a bash script for {@link Cufflinks} tool.
     **/
    @Override
    public CufflinksResult generate(Configuration configuration, TemplateEngine templateEngine) {
        final String cufflinksOutdir = format("%s/cufflinks", sampleOutputDir);
        CufflinksOutput cufflinksOutput = CufflinksOutput.builder()
                .cufflinksOutdir(cufflinksOutdir)
                .build();
        cufflinksOutput.createDirectory();
        CufflinksFields cufflinksFields = constructFields(configuration, cufflinksOutdir);
        Context context = new Context();
        context.setVariable("cufflinksFields", cufflinksFields);
        final String cmd = templateEngine.process(CUFFLINKS_TOOL_TEMPLATE_NAME, context);
        cufflinksOutput.setCufflinksGeneResult(cufflinksFields.cufflinksGeneResult);
        cufflinksOutput.setCufflinksIsoformResult(cufflinksFields.cufflinksIsoformResult);
        return CufflinksResult.builder()
                .bamOutput(bamOutput)
                .cufflinksOutput(cufflinksOutput)
                .command(BashCommand.withTool(cmd))
                .build();
    }

    private CufflinksFields constructFields(final Configuration configuration, final String cufflinksOutdir) {
        CufflinksFields cufflinksFields = new CufflinksFields();
        cufflinksFields.genome = validate(configuration.getGlobalConfig().getDatabaseConfig().getGenome(),
                GlobalConfigFormat.GENOME);
        cufflinksFields.annotgene = validate(configuration.getGlobalConfig().getDatabaseConfig().getAnnotgene(),
                GlobalConfigFormat.ANNOTGENE);
        cufflinksFields.cufflinks = validate(configuration.getGlobalConfig().getToolConfig().getCufflinks(),
                GlobalConfigFormat.CUFFLINKS);
        cufflinksFields.nThreads = String.valueOf(configuration.getGlobalConfig().getQueueParameters().getNumThreads());
        cufflinksFields.sampleName = sampleName;
        cufflinksFields.sCufflinksOutDir = cufflinksOutdir;
        cufflinksFields.sCufflinksLibraryType = validate(configuration.getStudyConfig().getCufflinksLibraryType(),
                StudyConfigFormat.CUFFLINKS_LIBRARY_TYPE);
        cufflinksFields.cufflinksGeneResult = format("%s/%s.cufflinks.gene.expression.results",
                cufflinksFields.sCufflinksOutDir,
                cufflinksFields.sampleName);
        cufflinksFields.cufflinksIsoformResult = format("%s/%s.cufflinks.isoform.expression.results",
                cufflinksFields.sCufflinksOutDir,
                cufflinksFields.sampleName);
        cufflinksFields.bam = bamOutput.getBam();

        return cufflinksFields;
    }

    @Data
    private class CufflinksFields {
        private String genome;
        private String annotgene;
        private String cufflinks;
        private String nThreads;
        private String sampleName;
        private String sCufflinksOutDir;
        private String sCufflinksLibraryType;
        private String bam;
        private String cufflinksGeneResult;
        private String cufflinksIsoformResult;
    }
}
