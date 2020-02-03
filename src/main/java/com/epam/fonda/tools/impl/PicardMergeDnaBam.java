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
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.BamResult;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.epam.fonda.utils.PipelineUtils.TASK_TO_CHECK;
import static com.epam.fonda.utils.ToolUtils.validate;

@RequiredArgsConstructor
public class PicardMergeDnaBam implements Tool<BamResult> {

    private static final String PICARD_MERGE_DNA_BAM_TOOL_TEMPLATE_NAME = "picard_merge_dna_bam_tool_template";

    @Data
    private class ToolFields {
        private String java;
        private String picard;
        private String samtools;
    }

    @Data
    private class AdditionalFields {
        private String sampleName;
        private String bamList;
        private String bamOutdir;
        private String mergedBam;
    }

    @NonNull
    private FastqFileSample sample;
    @NonNull
    private List<String> bamList;

    /**
     * This method generates {@link BamResult} for PicardMergeDnaBam tool.
     *
     * @param configuration  is the type of {@link Configuration} which contains
     *                       its fields: samtools, picard, java.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link BamResult} with bash script.
     **/
    @Override
    public BamResult generate(Configuration configuration, TemplateEngine templateEngine) {
        Context context = new Context();
        AdditionalFields additionalFields = initializeAdditionalFields();
        context.setVariable("toolFields", initializeToolFields(configuration));
        context.setVariable("additionalFields", additionalFields);
        String cmd = templateEngine.process(PICARD_MERGE_DNA_BAM_TOOL_TEMPLATE_NAME, context);
        TASK_TO_CHECK.addAll(Arrays.asList("Merge DNA bams", "Index bam"));
        final BamOutput bamOutput = BamOutput.builder().bam(additionalFields.mergedBam).build();
        final BashCommand command = BashCommand.withTool(cmd);
        final List<String> filesToDelete = Arrays.asList(
                String.join(" ", bamList),
                bamList.stream().map(this::buildBamIndex).collect(Collectors.joining(" ")),
                additionalFields.mergedBam,
                buildBamIndex(additionalFields.mergedBam));
        command.setTempDirs(filesToDelete);
        return BamResult.builder()
                .bamOutput(bamOutput)
                .command(command)
                .build();
    }

    /**
     * This method initializes fields of the ToolFields {@link ToolFields} class.
     *
     * @param configuration is the type of {@link Configuration} which contains
     *                      its fields: picard, samtools, java.
     * @return {@link ToolFields} with its fields.
     **/
    private ToolFields initializeToolFields(Configuration configuration) {
        ToolFields toolFields = new ToolFields();
        toolFields.java = validate(configuration.getGlobalConfig().getToolConfig().getJava(), GlobalConfigFormat.JAVA);
        toolFields.picard = validate(configuration.getGlobalConfig().getToolConfig().getPicard(),
                GlobalConfigFormat.PICARD);
        toolFields.samtools = validate(configuration.getGlobalConfig().getToolConfig().getSamTools(),
                GlobalConfigFormat.SAMTOOLS);
        return toolFields;
    }

    /**
     * This method initializes fields of the AdditionalFields {@link AdditionalFields} class.
     * @return {@link AdditionalFields} with its fields.
     **/
    private AdditionalFields initializeAdditionalFields() {
        AdditionalFields additionalFields = new AdditionalFields();
        additionalFields.bamList = String.join(" I=", bamList);
        additionalFields.sampleName = sample.getName();
        additionalFields.bamOutdir = sample.getBamOutdir();
        additionalFields.mergedBam = String.format("%s/%s.merged.sorted.bam", additionalFields.bamOutdir,
                additionalFields.sampleName);
        return additionalFields;
    }

    private String buildBamIndex(final String bam) {
        return bam + ".bai";
    }
}
