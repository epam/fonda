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
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static com.epam.fonda.utils.PipelineUtils.TASK_TO_CHECK;
import static com.epam.fonda.utils.ToolUtils.validate;
import static java.lang.String.format;

@RequiredArgsConstructor
public class Trimmomatic implements Tool<FastqResult> {

    @Data
    private class TrimmomaticFields {
        private String trimmomatic;
        private String java;
        private String adapterSEQ;
        private String sfqOutdir;
        private String sampleName;
        private String prefix;
        private String trimmedFastq1;
        private String trimmedFastq2;
        private String trimmedUnpairedFastq1;
        private String trimmedUnpairedFastq2;
        private String fastq1;
        private String fastq2;
        private String index;
        private int numThreads;
    }

    private static final String TRIMMOMATIC_TOOL_TEMPLATE_NAME = "trimmomatic_tool_template";

    @NonNull
    private FastqFileSample sample;
    @NonNull
    private FastqResult result;
    private final Integer index;

    /**
     * This method generates bash script {@link BashCommand} for Trimmomatic tool.
     *
     * @param configuration  is the type of {@link Configuration} which contains
     *                       its fields: trimmomatic, java, seqAdapter, numThreads.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link BashCommand} with bash script.
     **/
    @Override
    public FastqResult generate(Configuration configuration, TemplateEngine templateEngine) {
        TrimmomaticFields trimmomaticFields = constructFieldsByIndex(configuration);
        Context context = new Context();
        context.setVariable("trimmomaticFields", trimmomaticFields);
        final String cmd = templateEngine.process(TRIMMOMATIC_TOOL_TEMPLATE_NAME, context);
        TASK_TO_CHECK.add("Trimmomatic trimming");
        final FastqOutput fastqOutput = result.getOut();
        if (StringUtils.isNoneBlank(trimmomaticFields.adapterSEQ)) {
            fastqOutput.setMergedFastq1(trimmomaticFields.trimmedFastq1);
            fastqOutput.setMergedFastq2(trimmomaticFields.trimmedFastq2);
        }
        AbstractCommand command = result.getCommand();
        command.setToolCommand(command.getToolCommand() + cmd);
        return FastqResult.builder()
                .out(fastqOutput)
                .command(command)
                .build();
    }

    /**
     * This method initializes fields of the Trimmomatic {@link Trimmomatic} class.
     *
     * @param configuration is the type of {@link Configuration} which contains
     *                      its fields: trimmomatic, java, seqAdapter, numThreads.
     * @return {@link TrimmomaticFields} with fields.
     **/
    private TrimmomaticFields constructFieldsByIndex(Configuration configuration) {
        TrimmomaticFields trimmomaticFields = new TrimmomaticFields();
        if (index != null) {
            trimmomaticFields.index = String.valueOf(index);
        }
        trimmomaticFields.adapterSEQ = configuration.getGlobalConfig().getDatabaseConfig().getAdapterSEQ();
        trimmomaticFields.java = validate(configuration.getGlobalConfig().getToolConfig().getJava(),
                GlobalConfigFormat.JAVA);
        trimmomaticFields.numThreads = configuration.getGlobalConfig().getQueueParameters().getNumThreads();
        trimmomaticFields.trimmomatic = validate(configuration.getGlobalConfig().getToolConfig().getTrimmomatic(),
                GlobalConfigFormat.TRIMMOMATIC);
        trimmomaticFields.sfqOutdir = sample.getFastqOutdir();
        trimmomaticFields.sampleName = sample.getName();
        trimmomaticFields.fastq1 = result.getOut().getMergedFastq1();
        trimmomaticFields.fastq2 = result.getOut().getMergedFastq2();
        if (trimmomaticFields.index != null) {
            trimmomaticFields.prefix = format("%s/%s_%s", trimmomaticFields.sfqOutdir,
                    sample.getName(), trimmomaticFields.index);
        } else {
            trimmomaticFields.prefix = format("%s/%s", trimmomaticFields.sfqOutdir, sample.getName());
        }
        trimmomaticFields.trimmedFastq1 = format("%s.trimmed.R1.fastq.gz", trimmomaticFields.prefix);
        trimmomaticFields.trimmedFastq2 = format("%s.trimmed.R2.fastq.gz", trimmomaticFields.prefix);
        trimmomaticFields.trimmedUnpairedFastq1 = format("%s.trimmed_unpaired.R1.fq.gz",
                trimmomaticFields.prefix);
        trimmomaticFields.trimmedUnpairedFastq2 = format("%s.trimmed_unpaired.R2.fq.gz",
                trimmomaticFields.prefix);
        return trimmomaticFields;
    }
}
