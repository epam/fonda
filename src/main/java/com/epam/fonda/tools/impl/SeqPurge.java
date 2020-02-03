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
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.FastqOutput;
import com.epam.fonda.tools.results.FastqResult;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static com.epam.fonda.utils.PipelineUtils.TASK_TO_CHECK;
import static com.epam.fonda.utils.ToolUtils.validate;
import static java.lang.String.format;

@RequiredArgsConstructor
public class SeqPurge implements Tool<FastqResult> {

    @Data
    private class SeqPurgeFields {
        private String seqPurge;
        private String adapterFWD;
        private String adapterREV;
        private String sfqOutdir;
        private String sampleName;
        private String prefix;
        private String trimmedFastq1;
        private String trimmedFastq2;
        private String fastq1;
        private String fastq2;
        private String index;
        private int numThreads;
    }

    private static final String SEQPURGE_TOOL_TEMPLATE_NAME = "seqpurge_tool_template";

    @NonNull
    private FastqFileSample sample;
    @NonNull
    private FastqResult result;
    private final Integer index;

    /**
     * This method generates bash script {@link BashCommand} for SeqPurge tool.
     *
     * @param configuration  is the type of {@link Configuration} which contains
     *                       its fields: seqPurge, fwdAdapter, revAdapter, numThreads.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link BashCommand} with bash script.
     **/
    @Override
    public FastqResult generate(Configuration configuration, TemplateEngine templateEngine) {
        SeqPurgeFields seqPurgeFields = constructFieldsByIndex(configuration);
        if (seqPurgeFields.fastq1 == null || seqPurgeFields.fastq2 == null) {
            throw new IllegalArgumentException(
            "Error Step: In seqpurge: SeqPurge can only be applied to trim paired-end reads.");
        }
        Context context = new Context();
        context.setVariable("seqPurgeFields", seqPurgeFields);
        final String cmd = templateEngine.process(SEQPURGE_TOOL_TEMPLATE_NAME, context);
        TASK_TO_CHECK.add("Seqpurge trimming");
        final FastqOutput fastqOutput = result.getOut();
        fastqOutput.setMergedFastq1(seqPurgeFields.trimmedFastq1);
        fastqOutput.setMergedFastq2(seqPurgeFields.trimmedFastq2);
        AbstractCommand command = result.getCommand();
        command.setToolCommand(command.getToolCommand() + cmd);
        return FastqResult.builder()
                .out(fastqOutput)
                .command(command)
                .build();
    }

    /**
     * This method initializes fields of the SeqPurge {@link SeqPurge} class.
     *
     * @param configuration is the type of {@link Configuration} which contains
     *                      its fields: seqPurge, fwdAdapter, revAdapter, numThreads.
     * @return {@link SeqPurgeFields} with fields.
     **/
    private SeqPurgeFields constructFieldsByIndex(Configuration configuration) {
        SeqPurgeFields seqPurgeFields = new SeqPurgeFields();
        if (index != null) {
            seqPurgeFields.index = String.valueOf(index);
        }
        seqPurgeFields.adapterFWD = configuration.getGlobalConfig().getDatabaseConfig().getAdapterFWD();
        seqPurgeFields.adapterREV = configuration.getGlobalConfig().getDatabaseConfig().getAdapterREV();
        seqPurgeFields.numThreads = configuration.getGlobalConfig().getQueueParameters().getNumThreads();
        seqPurgeFields.seqPurge = validate(configuration.getGlobalConfig().getToolConfig().getSeqpurge(),
                GlobalConfigFormat.SEQPURGE);
        seqPurgeFields.sfqOutdir = sample.getFastqOutdir();
        seqPurgeFields.sampleName = sample.getName();
        seqPurgeFields.fastq1 = result.getOut().getMergedFastq1();
        seqPurgeFields.fastq2 = result.getOut().getMergedFastq2();
        if (seqPurgeFields.index != null) {
            seqPurgeFields.prefix = format("%s/%s_%s", seqPurgeFields.sfqOutdir, sample.getName(),
                    seqPurgeFields.index);
        } else {
            seqPurgeFields.prefix = format("%s/%s", seqPurgeFields.sfqOutdir, sample.getName());
        }
        seqPurgeFields.trimmedFastq1 = format("%s.trimmed.R1.fastq.gz", seqPurgeFields.prefix);
        seqPurgeFields.trimmedFastq2 = format("%s.trimmed.R2.fastq.gz", seqPurgeFields.prefix);
        return seqPurgeFields;
    }
}
