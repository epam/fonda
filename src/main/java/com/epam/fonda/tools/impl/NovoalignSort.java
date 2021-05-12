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

import com.epam.fonda.entity.command.AbstractCommand;
import com.epam.fonda.entity.command.BashCommand;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.entity.configuration.GlobalConfigFormat;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.utils.PipelineUtils;
import com.epam.fonda.workflow.PipelineType;
import com.epam.fonda.workflow.TaskContainer;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Arrays;

import static com.epam.fonda.utils.ToolUtils.validate;

@Data
@AllArgsConstructor
public class NovoalignSort implements Tool<BamResult> {
    private static final String NOVOALIGN_SORT_TOOL_TEMPLATE_NAME = "novoalign_sort_tool_template";

    private FastqFileSample sample;
    private String fastq1;
    private String fastq2;
    private int index;

    /**
     * This method generates bamResult {@link BamResult} for NovoalignSort expression.
     *
     * @param configuration  is the type of {@link Configuration}.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link BamResult} with bash script.
     */
    @Override
    public BamResult generate(Configuration configuration, TemplateEngine templateEngine) {
        NovoalignSortFields novoalignSortFields = constructFieldsForNovoalignSort(configuration);
        if (StringUtils.isBlank(fastq1)) {
            throw new IllegalArgumentException(
                    "Error Step: In novoalignSort: no fastq files are properly provided, please check!");
        }
        Context context = new Context();
        context.setVariable("novoalignSortFields", novoalignSortFields);
        context.setVariable("fastq1", fastq1);
        context.setVariable("fastq2", fastq2);
        final String cmd = templateEngine.process(NOVOALIGN_SORT_TOOL_TEMPLATE_NAME, context);
        TaskContainer.addTasks("Novoalign alignment", "Index bam");
        BamOutput bamOutput = BamOutput.builder()
                .sortedBam(novoalignSortFields.sortedBam)
                .sortedBamIndex(novoalignSortFields.sortedBamIndex)
                .build();
        bamOutput.setBam(StringUtils.isBlank(bamOutput.getBam())
                ? novoalignSortFields.sortedBam
                : String.format("%s,%s", bamOutput.getBam(), novoalignSortFields.sortedBam));
        AbstractCommand command = BashCommand.withTool(cmd);
        command.setTempDirs(Arrays.asList(novoalignSortFields.sortedBam, novoalignSortFields.sortedBamIndex));
        return BamResult.builder()
                .bamOutput(bamOutput)
                .command(command)
                .build();
    }

    private NovoalignSortFields constructFieldsForNovoalignSort(Configuration configuration) {
        NovoalignSortFields novoalignSortFields = new NovoalignSortFields();
        GlobalConfig.ToolConfig toolConfig = configuration.getGlobalConfig().getToolConfig();
        novoalignSortFields.novoalign = validate(toolConfig.getNovoalign(),
                GlobalConfigFormat.NOVOALIGN);
        novoalignSortFields.novoindex = validate(configuration.getGlobalConfig().getDatabaseConfig().getNovoIndex(),
                GlobalConfigFormat.NOVOINDEX);
        novoalignSortFields.samtools = validate(toolConfig.getSamTools(),
                GlobalConfigFormat.SAMTOOLS);
        novoalignSortFields.bamOutdir = sample.getBamOutdir();
        novoalignSortFields.sampleName = sample.getName();
        novoalignSortFields.numThreads = configuration.getGlobalConfig().getQueueParameters().getNumThreads();
        novoalignSortFields.index = index;
        novoalignSortFields.tmpBam = String.format("%s/%s_%s.novoalign.sorted",
                novoalignSortFields.getBamOutdir(),
                novoalignSortFields.getSampleName(),
                novoalignSortFields.getIndex());
        novoalignSortFields.sortedBam = String.format("%s.bam",
                novoalignSortFields.getTmpBam());
        novoalignSortFields.sortedBamIndex = String.format("%s.bai",
                novoalignSortFields.getSortedBam());
        novoalignSortFields.rg = constructFieldRG(configuration, sample.getName());
        novoalignSortFields.tune = PipelineUtils.NA.equals(toolConfig.getNovoalignTune())
                ? null
                : toolConfig.getNovoalignTune();
        return novoalignSortFields;
    }

    private String constructFieldRG(Configuration configuration, String sampleName) {
        return isDnaAmpliconWorkflow(configuration)
                ? String.format("\'@RG\\tID:%s\\tSM:%s\\tLB:%s\\tPL:Illumina\'", sampleName, sampleName, sampleName)
                : String.format("\'@RG\\tID:%s\\tSM:%s\\tLB:DNA\\tPL:Illumina\'", sampleName, sampleName);
    }

    private boolean isDnaAmpliconWorkflow(final Configuration configuration) {
        return PipelineType.DNA_AMPLICON_VAR_FASTQ.getName()
                .equals(configuration.getGlobalConfig().getPipelineInfo().getWorkflow());
    }

    @Data
    private class NovoalignSortFields {
        private String novoalign;
        private String novoindex;
        private String samtools;
        private String bamOutdir;
        private String sampleName;
        private int numThreads;
        private int index;
        private String tmpBam;
        private String rg;
        private String sortedBam;
        private String sortedBamIndex;
        private String tune;
    }
}
