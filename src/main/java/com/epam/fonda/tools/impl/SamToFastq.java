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
import com.epam.fonda.samples.fastq.FastqReadType;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.tools.results.FastqOutput;
import com.epam.fonda.tools.results.FastqResult;
import com.epam.fonda.utils.ToolUtils;
import com.epam.fonda.workflow.TaskContainer;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static com.epam.fonda.utils.ToolUtils.validate;
import static com.epam.fonda.utils.ToolUtils.validateOldPicardVersion;

@Data
public class SamToFastq implements Tool<FastqResult> {
    private static final String SAM_TO_FASTQ_TOOL_TEMPLATE_NAME = "sam_to_fastq_tool_template";

    @Data
    @Builder
    private static class ToolFields {
        private String java;
        private boolean oldPicardVersion;
        private String picard;
        private String readType;
        private String bam;
    }

    @NonNull
    private String sampleName;
    @NonNull
    private String fastqSampleOutputDir;
    @NonNull
    private BamResult bamResult;

    /**
     * This method generates bash script {@link BashCommand} for SamToFastq tool.
     *
     * @param configuration  is the type of {@link Configuration} which contains
     *                       its fields: java, picard, readType.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link BashCommand} with bash script.
     **/
    @Override
    public FastqResult generate(Configuration configuration, TemplateEngine templateEngine) {
        ToolFields toolFields = initializeToolFields(configuration);
        String fastq1 = FastqReadType.PAIRED.getType()
                .equalsIgnoreCase(configuration.getGlobalConfig().getPipelineInfo().getReadType())
                ? String.format("%s/%s.R1.fastq", fastqSampleOutputDir, sampleName)
                : String.format("%s/%s.fastq", fastqSampleOutputDir, sampleName);
        String fastq2 = FastqReadType.PAIRED.getType()
                .equalsIgnoreCase(configuration.getGlobalConfig().getPipelineInfo().getReadType())
                ? String.format("%s/%s.R2.fastq", fastqSampleOutputDir, sampleName)
                : null;
        String unpairFastq = FastqReadType.PAIRED.getType()
                .equalsIgnoreCase(configuration.getGlobalConfig().getPipelineInfo().getReadType())
                ? String.format("%s/%s.unpaired.fastq", fastqSampleOutputDir, sampleName)
                : null;
        Context context = buildContext(toolFields, fastq1, fastq2, unpairFastq);
        final String cmd = templateEngine.process(SAM_TO_FASTQ_TOOL_TEMPLATE_NAME, context);
        TaskContainer.addTasks("Convert bam to fastq");
        return FastqResult.builder()
                .command(BashCommand.withTool(cmd))
                .out(buildFastqOutput(fastq1, fastq2, configuration))
                .build();
    }

    private Context buildContext(ToolFields toolFields, String fastq1, String fastq2, String unpairFastq) {
        Context context = new Context();
        context.setVariable("toolFields", toolFields);
        context.setVariable("fastq1", fastq1);
        context.setVariable("fastq2", fastq2);
        context.setVariable("unpairFastq", unpairFastq);
        return context;
    }

    private FastqOutput buildFastqOutput(String fastq1, String fastq2, Configuration configuration) {
        return FastqReadType.PAIRED.getType().equalsIgnoreCase(configuration.getGlobalConfig()
                .getPipelineInfo().getReadType())
                ? FastqOutput.builder()
                .mergedFastq1(String.format("%s.gz", fastq1))
                .mergedFastq1(String.format("%s.gz", fastq2))
                .build()
                : FastqOutput.builder()
                .mergedFastq1(fastq1)
                .build();
    }

    /**
     * This method initializes fields of the SamToFastq {@link SamToFastq} class.
     *
     * @param configuration is the type of {@link Configuration} which contains
     *                      its fields: java, picard, readType.
     * @return {@link SamToFastq} with fields.
     **/
    private ToolFields initializeToolFields(Configuration configuration) {
        return ToolFields.builder()
                .java(validate(configuration.getGlobalConfig().getToolConfig().getJava(), GlobalConfigFormat.JAVA))
                .picard(validate(configuration.getGlobalConfig().getToolConfig().getPicard(),
                        GlobalConfigFormat.PICARD))
                .oldPicardVersion(validateOldPicardVersion(configuration))
                .readType(validate(configuration.getGlobalConfig().getPipelineInfo().getReadType(),
                        GlobalConfigFormat.READ_TYPE))
                .bam(validate(bamResult.getBamOutput().getBam(), ToolUtils.BAM))
                .build();
    }
}
