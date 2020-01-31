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
import com.epam.fonda.samples.bam.BamFileSample;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.BamResult;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Arrays;

import static com.epam.fonda.utils.PipelineUtils.TASK_TO_CHECK;
import static com.epam.fonda.utils.ToolUtils.validate;

@Data
public class SortBamByReadName implements Tool<BamResult> {
    private static final String SORT_BAM_BY_READ_NAME_TEMPLATE = "sort_bam_by_read_name_template";

    @Data
    @Builder
    private static class ToolFields {
        private String java;
        private String picard;
        private String samtools;
    }

    @NonNull
    private String fastqSampleOutputDir;
    @NonNull
    private BamFileSample sample;

    /**
     * This method generates {@link BamResult} for SortBamByReadName tool.
     *
     * @param configuration  is the type of {@link Configuration} which contains
     *                       its fields: samtools, java, picard.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link BamResult} with bash script.
     **/
    @Override
    public BamResult generate(Configuration configuration, TemplateEngine templateEngine) {
        ToolFields toolFields = initializeToolFields(configuration);
        final String bam = sample.getBam();
        final String sortedBam = String.format("%s/%s.sortByReadname.bam", fastqSampleOutputDir, sample.getName());
        final String sortedBamIndex = String.format("%s.bai", sortedBam);
        Context context = buildContext(toolFields, bam, sortedBam, sortedBamIndex);
        final String cmd = templateEngine.process(SORT_BAM_BY_READ_NAME_TEMPLATE, context);
        TASK_TO_CHECK.addAll(Arrays.asList("Sort bam", "Index bam"));
        AbstractCommand command = BashCommand.withTool(cmd);
        command.setTempDirs(Arrays.asList(sortedBam, sortedBamIndex));
        return BamResult.builder()
                .command(command)
                .bamOutput(BamOutput.builder()
                        .bam(sortedBam)
                        .build())
                .build();
    }

    private Context buildContext(ToolFields toolFields, String bam, String sortedBam, String sortedBamIndex) {
        Context context = new Context();
        context.setVariable("bam", bam);
        context.setVariable("toolFields", toolFields);
        context.setVariable("sortedBam", sortedBam);
        context.setVariable("sortedBamIndex", sortedBamIndex);
        return context;
    }

    /**
     * This method initializes fields of the ToolFields {@link SortBamByReadName} class.
     *
     * @param configuration is the type of {@link Configuration} which contains its fields: java,
     *                      picard, samtools.
     * @return {@link SortBamByReadName} with its fields.
     **/
    private ToolFields initializeToolFields(Configuration configuration) {
        return ToolFields.builder()
                .java(validate(configuration.getGlobalConfig().getToolConfig().getJava(),
                        GlobalConfigFormat.JAVA))
                .picard(validate(configuration.getGlobalConfig().getToolConfig().getPicard(),
                        GlobalConfigFormat.PICARD))
                .samtools(validate(configuration.getGlobalConfig().getToolConfig().getSamTools(),
                        GlobalConfigFormat.SAMTOOLS))
                .build();
    }
}
