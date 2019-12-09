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
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.StringtieOutput;
import com.epam.fonda.tools.results.StringtieResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@AllArgsConstructor
public class Stringtie implements Tool<StringtieResult> {

    private static final String STRINGTIE_TOOL_TEMPLATE_NAME = "stringtie_tool_template";

    private String sampleName;
    private String sampleOutputDir;
    private BamOutput bamOutput;

    @Data
    public class StringTieFields {
        private String annotgene;
        private String stringtie;
        private String numThreads;
        private String sampleName;
        private String sStringtieOutdir;
        private String bam;
        private String stringtieGeneResult;
        private String stringtieAssemblyTranscript;
    }

    /**
     * This method generates bash script {@link BashCommand} for Stringtie expression.
     *
     * @param configuration  is the type of {@link Configuration}.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link BashCommand} with bash script.
     */
    @Override
    public StringtieResult generate(Configuration configuration, TemplateEngine templateEngine) {
        final String stringtieOutdir = String.format("%s/stringtie", sampleOutputDir);
        StringtieOutput stringtieOutput = StringtieOutput.builder()
                .stringtieOutdir(stringtieOutdir)
                .build();
        stringtieOutput.createDirectory();
        StringTieFields stringtieFields = constructFields(configuration, stringtieOutdir);
        Context context = new Context();
        context.setVariable("stringtieFields", stringtieFields);
        final String cmd = templateEngine.process(STRINGTIE_TOOL_TEMPLATE_NAME, context);
        stringtieOutput.setStringtieGeneResult(stringtieFields.stringtieGeneResult);
        stringtieOutput.setStringtieAssemblyTranscript(stringtieFields.stringtieAssemblyTranscript);
        return StringtieResult.builder()
                .bamOutput(bamOutput)
                .stringtieOutput(stringtieOutput)
                .command(BashCommand.withTool(cmd))
                .build();
    }

    private StringTieFields constructFields(final Configuration configuration, final String stringtieOutdir) {
        StringTieFields stringtieFields = new StringTieFields();
        stringtieFields.annotgene = configuration.getGlobalConfig().getDatabaseConfig().getAnnotgene();
        stringtieFields.stringtie = configuration.getGlobalConfig().getToolConfig().getStringtie();
        stringtieFields.numThreads = String.valueOf(
                configuration.getGlobalConfig().getQueueParameters().getNumThreads());
        stringtieFields.sampleName = sampleName;
        stringtieFields.sStringtieOutdir = stringtieOutdir;
        stringtieFields.bam = bamOutput.getBam();
        stringtieFields.stringtieGeneResult = String.format("%s/%s.stringtie.gene.expression.results",
                stringtieFields.sStringtieOutdir,
                stringtieFields.sampleName);
        stringtieFields.stringtieAssemblyTranscript = String.format("%s/%s.stringtie.assembly.transcripts.gtf",
                stringtieFields.sStringtieOutdir,
                stringtieFields.sampleName);

        return stringtieFields;
    }
}
