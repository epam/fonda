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
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.FastqResult;
import com.epam.fonda.tools.results.SalmonOutput;
import com.epam.fonda.tools.results.SalmonResult;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static com.epam.fonda.utils.PipelineUtils.TASK_TO_CHECK;
import static com.epam.fonda.utils.ToolUtils.validate;
import static java.lang.String.format;

@RequiredArgsConstructor
public class Salmon implements Tool<SalmonResult> {

    @Data
    private class SalmonFields {
        private String annotgene;
        private String salmon;
        private String salmonOutdir;
        private String sampleName;
        private String fastq1;
        private String fastq2;
        private String salmonGeneResult;
        private String salmonTranscriptResult;
        private String decopMergedFastq1;
        private String decopMergedFastq2;
        private String index;
        private int numThreads;
    }

    private static final String SALMON_TOOL_TEMPLATE_NAME = "salmon_tool_template";

    @NonNull
    private FastqFileSample sample;
    @NonNull
    private FastqResult fastqResult;

    /**
     * This method generates bash script {@link BashCommand} for Salmon tool.
     *
     * @param configuration  is the type of {@link Configuration} which contains
     *                       its fields: salmon, annotgene, numThreads.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link BashCommand} with bash script.
     **/
    @Override
    public SalmonResult generate(Configuration configuration, TemplateEngine templateEngine) {
        final String salmonOutdir = format("%s/salmon", sample.getSampleOutputDir());
        SalmonOutput salmonOutput = SalmonOutput.builder()
                .salmonOutdir(salmonOutdir)
                .build();
        salmonOutput.createDirectory();
        SalmonFields salmonFields = constructFieldsForSalmon(configuration, salmonOutdir);
        if (salmonFields.fastq1 == null) {
            throw new IllegalArgumentException(
                    "Error Step: In salmon: not fastq files are properly provided, please check!");
        }
        Context context = new Context();
        context.setVariable("salmonFields", salmonFields);
        final String cmd = templateEngine.process(SALMON_TOOL_TEMPLATE_NAME, context);
        TASK_TO_CHECK.add("SALMON");
        salmonOutput.setSalmonGeneResult(salmonFields.salmonGeneResult);
        salmonOutput.setSalmonTranscriptResult(salmonFields.salmonTranscriptResult);
        return SalmonResult.builder()
                .fastqResult(fastqResult)
                .salmonOutput(salmonOutput)
                .command(BashCommand.withTool(cmd))
                .build();
    }

    /**
     * This method initializes fields of the Salmon {@link Salmon} class.
     *
     * @param configuration is the type of {@link Configuration} which contains
     *                      its fields: salmon, annotgene, numThreads.
     * @param salmonOutdir
     * @return {@link SalmonFields} with fields.
     **/
    private SalmonFields constructFieldsForSalmon(final Configuration configuration, final String salmonOutdir) {
        SalmonFields salmonFields = new SalmonFields();
        salmonFields.annotgene = validate(configuration.getGlobalConfig().getDatabaseConfig().getAnnotgene(),
                GlobalConfigFormat.ANNOTGENE);
        salmonFields.salmon = validate(configuration.getGlobalConfig().getToolConfig().getSalmon(),
                GlobalConfigFormat.SALMON);
        salmonFields.salmonOutdir = salmonOutdir;
        salmonFields.sampleName = sample.getName();
        salmonFields.fastq1 = fastqResult.getOut().getMergedFastq1();
        salmonFields.fastq2 = fastqResult.getOut().getMergedFastq2();
        salmonFields.numThreads = configuration.getGlobalConfig().getQueueParameters().getNumThreads();
        salmonFields.salmonGeneResult = format("%s/%s.salmon.gene.results", salmonFields.salmonOutdir,
                salmonFields.sampleName);
        salmonFields.salmonTranscriptResult = format("%s/%s.salmon.transcript.results", salmonFields
                .salmonOutdir, salmonFields.sampleName);
        salmonFields.decopMergedFastq1 = salmonFields.fastq1.replace(".gz", "");
        salmonFields.decopMergedFastq2 = salmonFields.fastq2.replace(".gz", "");
        salmonFields.index = validate(configuration.getGlobalConfig().getDatabaseConfig().getSalmonIndex(),
                GlobalConfigFormat.SALMONINDEX);
        return salmonFields;
    }
}
