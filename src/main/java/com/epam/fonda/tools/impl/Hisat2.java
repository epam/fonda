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
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.command.BashCommand;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.tools.results.FastqOutput;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Arrays;

@RequiredArgsConstructor
@Data
public class Hisat2 implements Tool<BamResult> {

    @Data
    private class ToolFields {
        private String hisat2;
        private String java;
        private String picard;
        private String samtools;
    }

    @Data
    private class AdditionalHisat2Fields {
        private String sampleName;
        private String fastq1;
        private String fastq2;
        private String tmpBam;
        private String rg;
        private String index;
        private String bamIndex;
        private int numThreads;
    }

    private static final String HISAT2_TOOL_TEMPLATE_NAME = "hisat2_tool_template";

    @NonNull
    private FastqFileSample sample;
    @NonNull
    private FastqOutput fastqOutput;

    /**
     * This method generates bash script {@link BashCommand} for Hisat2 tool.
     *
     * @param configuration  is the type of {@link Configuration} which contains
     *                       its fields: bam, hisat2, samtools, picard, java, numThreads, index.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link BashCommand} with bash script.
     **/
    @Override
    public BamResult generate(Configuration configuration, TemplateEngine templateEngine) {
        AdditionalHisat2Fields additionalHisat2Fields = initializeAdditionalFields(configuration);
        final String bam = String.format("%s/%s.hisat2.sorted.bam", sample.getBamOutdir(), sample.getName());
        if (additionalHisat2Fields.getFastq1() == null) {
            throw new IllegalArgumentException(
                    "Error Step: In hisat2: no fastq files are properly provided, please check!");
        }
        Context context = new Context();
        context.setVariable("additionalHisat2Fields", additionalHisat2Fields);
        context.setVariable("toolFields", initializeToolFields(configuration));
        context.setVariable("bam", bam);
        String cmd = templateEngine.process(HISAT2_TOOL_TEMPLATE_NAME, context);
        BamOutput bamOutput = BamOutput.builder().build();
        bamOutput.setBam(bam);
        bamOutput.setBamIndex(additionalHisat2Fields.bamIndex);
        bamOutput.setSortedBam(bam);
        bamOutput.setSortedBamIndex(additionalHisat2Fields.bamIndex);
        AbstractCommand resultCommand = BashCommand.withTool(cmd);
        resultCommand.setTempDirs(Arrays.asList(bamOutput.getSortedBam(), bamOutput.getSortedBamIndex()));
        return BamResult.builder()
                .bamOutput(bamOutput)
                .command(resultCommand)
                .build();
    }

    /**
     * This method initializes fields of the AdditionalHisat2Fields {@link AdditionalHisat2Fields} class.
     *
     * @param configuration is the type of {@link Configuration} which contains
     *                      its fields: numThreads, index.
     * @return {@link AdditionalHisat2Fields} with its fields.
     **/
    private AdditionalHisat2Fields initializeAdditionalFields(Configuration configuration) {
        AdditionalHisat2Fields additionalHisat2Fields = new AdditionalHisat2Fields();
        additionalHisat2Fields.numThreads = configuration.getGlobalConfig().getQueueParameters().getNumThreads();
        additionalHisat2Fields.sampleName = sample.getName();
        additionalHisat2Fields.tmpBam = String.format("%s/%s.hisat2.sorted", sample.getBamOutdir(),
                sample.getName());
        additionalHisat2Fields.fastq1 = fastqOutput.getMergedFastq1();
        additionalHisat2Fields.fastq2 = fastqOutput.getMergedFastq2();
        additionalHisat2Fields.rg = String.format("\"SM:%s\\tLB:%s\\tPL:Illumina\"", sample.getName(), sample
                .getName());
        additionalHisat2Fields.index = configuration.getGlobalConfig().getDatabaseConfig().getHisat2Index();
        additionalHisat2Fields.bamIndex = String.format("%s/%s.hisat2.sorted.bam.bai", sample.getBamOutdir(),
                sample.getName());
        return additionalHisat2Fields;
    }

    /**
     * This method initializes fields of the ToolFields {@link ToolFields} class.
     *
     * @param configuration is the type of {@link Configuration} which contains
     *                      its fields: picard, samtools, java, hisat2.
     * @return {@link ToolFields} with its fields.
     **/
    private ToolFields initializeToolFields(Configuration configuration) {
        ToolFields toolFields = new ToolFields();
        toolFields.java = configuration.getGlobalConfig().getToolConfig().getJava();
        toolFields.picard = configuration.getGlobalConfig().getToolConfig().getPicard();
        toolFields.samtools = configuration.getGlobalConfig().getToolConfig().getSamTools();
        toolFields.hisat2 = configuration.getGlobalConfig().getToolConfig().getHisat2();
        return toolFields;
    }
}
