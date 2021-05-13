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
import com.epam.fonda.entity.configuration.GlobalConfigFormat;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.workflow.TaskContainer;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static com.epam.fonda.utils.ToolUtils.validate;
import static com.epam.fonda.utils.ToolUtils.validateOldPicardVersion;

@RequiredArgsConstructor
@Data
public class PicardMarkDuplicate implements Tool<BamResult> {

    private static final String MKDUP_TOOL_TEMPLATE_NAME = "picard_mark_duplicates_tool_template";
    private static final double THE_LAST_OLD_PICARD_VERSION = 1.123;
    @NonNull
    private FastqFileSample sample;
    @NonNull
    private BamResult bamResult;

    @Data
    private class ToolFields {
        private boolean oldPicardVersion;
        private String picard;
        private String java;
        private String samtools;
    }

    @Data
    private class AdditionalMkDupFields {
        private String sbamOutdir;
        private String sqcOutdir;
        private String stmpOutdir;
        private String mkdupBam;
        private String mkdupBamIndex;
        private String mkdupMetric;
    }

    /**
     * This method generates bash script {@link BashCommand} for PicardMarkDuplicate tool.
     *
     * @param configuration  is the type of {@link Configuration} which contains
     *                       its fields: samtools, picard, java.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link BashCommand} with bash script.
     **/
    @Override
    public BamResult generate(Configuration configuration, TemplateEngine templateEngine) {
        AdditionalMkDupFields additionalPicardMarkDuplicateFields = initializeAdditionalFields();
        Context context = new Context();
        context.setVariable("additionalPicardMarkDuplicateFields", additionalPicardMarkDuplicateFields);
        context.setVariable("toolFields", initializeToolFields(configuration));
        context.setVariable("bam", bamResult.getBamOutput().getBam());
        final String cmd = templateEngine.process(MKDUP_TOOL_TEMPLATE_NAME, context);
        TaskContainer.addTasks("Mark duplicates", "Index mkdup bam");
        final boolean amplicon = isAmplicon(configuration);
        BamOutput bamOutput = bamResult.getBamOutput();
        bamOutput.setBam(amplicon ? bamOutput.getBam() : additionalPicardMarkDuplicateFields.mkdupBam);
        bamOutput.setMkdupBam(additionalPicardMarkDuplicateFields.mkdupBam);
        bamOutput.setMkdupBamIndex(additionalPicardMarkDuplicateFields.mkdupBamIndex);
        bamOutput.setMkdupMetric(additionalPicardMarkDuplicateFields.mkdupMetric);
        AbstractCommand resultCommand = bamResult.getCommand();
        resultCommand.setToolCommand(resultCommand.getToolCommand() + cmd);
        return BamResult.builder()
                .bamOutput(bamOutput)
                .command(resultCommand)
                .build();
    }

    /**
     * This method initializes fields of the AdditionalMkDupFields {@link AdditionalMkDupFields} class.
     *
     * @return {@link AdditionalMkDupFields} with its fields.
     **/
    private AdditionalMkDupFields initializeAdditionalFields() {
        final String bam = bamResult.getBamOutput().getBam();
        AdditionalMkDupFields additionalMkDupFields = new AdditionalMkDupFields();
        additionalMkDupFields.sbamOutdir = sample.getBamOutdir();
        additionalMkDupFields.sqcOutdir = sample.getQcOutdir();
        additionalMkDupFields.stmpOutdir = sample.getTmpOutdir();
        additionalMkDupFields.mkdupBam = bam.replace(".bam", ".mkdup.bam");
        additionalMkDupFields.mkdupBamIndex = bam.replace(".bam", ".mkdup.bam.bai");
        additionalMkDupFields.mkdupMetric = bam.replace(".bam", ".mkdup.metrics")
                .replace(additionalMkDupFields.sbamOutdir, additionalMkDupFields.sqcOutdir);
        return additionalMkDupFields;
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
        toolFields.oldPicardVersion = validateOldPicardVersion(configuration);
        toolFields.picard = validate(configuration.getGlobalConfig().getToolConfig().getPicard(),
                GlobalConfigFormat.PICARD);
        toolFields.samtools = validate(configuration.getGlobalConfig().getToolConfig().getSamTools(),
                GlobalConfigFormat.SAMTOOLS);
        return toolFields;
    }

    private boolean isAmplicon(final Configuration configuration) {
        return configuration.getGlobalConfig().getPipelineInfo().getWorkflow().toLowerCase().contains("amplicon");
    }
}
