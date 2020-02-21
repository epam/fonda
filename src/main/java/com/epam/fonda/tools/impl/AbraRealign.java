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
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfigFormat;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.workflow.TaskContainer;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Arrays;

import static com.epam.fonda.utils.DnaUtils.isWgsWorkflow;
import static com.epam.fonda.utils.ToolUtils.validate;

@RequiredArgsConstructor
public class AbraRealign implements Tool<BamResult> {

    private static final String AMPLICON_ABRA_REALIGN_TOOL_TEMPLATE_NAME = "abra_realign_tool_template";

    @Data
    @Builder
    private static class ToolFields {
        private String java;
        private String samtools;
        private String abra2;
    }

    @Data
    @Builder
    private static class AdditionalFields {
        private String genome;
        private String bed;
        private int numThreads;
        private String readType;
        private String tmpOutdir;
        private String realignBam;
        private String bam;
        private boolean isWgs;
    }

    @NonNull
    private FastqFileSample sample;
    @NonNull
    private BamResult bamResult;

    /**
     * This method generates {@link BamResult} for PicardMergeDnaBam tool.
     *
     * @param configuration  is the type of {@link Configuration} which contains
     *                       its fields: samtools, java, abra2.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link BamResult} with bash script.
     **/
    @Override
    public BamResult generate(Configuration configuration, TemplateEngine templateEngine) {
        AdditionalFields additionalFields = initializeAdditionalFields(configuration);
        Context context = new Context();
        context.setVariable("toolFields", initializeToolFields(configuration));
        context.setVariable("additionalFields", additionalFields);
        String cmd = templateEngine.process(AMPLICON_ABRA_REALIGN_TOOL_TEMPLATE_NAME, context);
        TaskContainer.addTasks("ABRA realignment");
        BamOutput bamOutput = bamResult.getBamOutput();
        bamOutput.setBam(additionalFields.realignBam);
        bamOutput.setBamIndex(additionalFields.realignBam.concat(".bai"));
        AbstractCommand resultCommand = bamResult.getCommand();
        resultCommand.setToolCommand(resultCommand.getToolCommand() + cmd);
        if (isWgsWorkflow(configuration)) {
            resultCommand.getTempDirs().addAll(Arrays.asList(bamOutput.getBam(), bamOutput.getBamIndex()));
        }
        return bamResult;
    }

    /**
     * This method initializes fields of the ToolFields {@link ToolFields} class.
     *
     * @param configuration is the type of {@link Configuration} which contains
     *                      its fields: samtools, java, abra2.
     * @return {@link ToolFields} with its fields.
     **/
    private ToolFields initializeToolFields(Configuration configuration) {
        return ToolFields.builder()
                .java(validate(configuration.getGlobalConfig().getToolConfig().getJava(), GlobalConfigFormat.JAVA))
                .samtools(validate(configuration.getGlobalConfig().getToolConfig().getSamTools(),
                        GlobalConfigFormat.SAMTOOLS))
                .abra2(validate(configuration.getGlobalConfig().getToolConfig().getAbra2(), GlobalConfigFormat.ABRA2))
                .build();
    }

    /**
     * This method initializes fields of the AdditionalFields {@link AdditionalFields} class.
     *
     * @param configuration is the type of {@link Configuration} which contains its fields: bed,
     *                      genome, numThreads, readType.
     * @return {@link AdditionalFields} with its fields.
     **/
    private AdditionalFields initializeAdditionalFields(Configuration configuration) {
        return AdditionalFields.builder()
                .bam(bamResult.getBamOutput().getBam())
                .bed(validate(configuration.getGlobalConfig().getDatabaseConfig().getBed(), GlobalConfigFormat.BED))
                .genome(validate(configuration.getGlobalConfig().getDatabaseConfig().getGenome(),
                        GlobalConfigFormat.GENOME))
                .realignBam(bamResult.getBamOutput().getBam().replace(".bam", ".realign.bam"))
                .numThreads(configuration.getGlobalConfig().getQueueParameters().getNumThreads())
                .readType(validate(configuration.getGlobalConfig().getPipelineInfo().getReadType(),
                        GlobalConfigFormat.READ_TYPE))
                .tmpOutdir(sample.getTmpOutdir())
                .isWgs(isWgsWorkflow(configuration))
                .build();
    }
}
