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
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.tools.results.PileupOutput;
import com.epam.fonda.tools.results.PileupResult;
import com.epam.fonda.workflow.TaskContainer;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static com.epam.fonda.utils.ToolUtils.validate;

@RequiredArgsConstructor
public class Pileup implements Tool<PileupResult> {
    private static final String PILEUP_TOOL_TEMPLATE_NAME = "pileup_tool_template";

    @Data
    @Builder
    private static class ToolFields {
        private String samtools;
        private String pileupOutdir;
        private String controlBam;
    }

    @Data
    @Builder
    private static class AdditionaPileuplFields {
        private String genome;
        private String bed;
        private String sampleName;
        private String controlSampleName;
        private String bam;
    }

    @NonNull
    private String sampleName;
    @NonNull
    private String sampleOutputDir;
    @NonNull
    private String sampleControlName;
    @NonNull
    private BamResult bamResult;

    /**
     * This method generates bash script {@link BashCommand} for Pileup tool.
     *
     * @param configuration  is the type of {@link Configuration} from which pileup
     *                       field are used by Pileup class.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link BashCommand} with bash script.
     **/
    @Override
    public PileupResult generate(Configuration configuration, TemplateEngine templateEngine) {
        ToolFields toolFields = constructFieldsForPileup(configuration);
        AdditionaPileuplFields additionalFields = initializeAdditionalFields(configuration);
        final String pileup = String.format("%s/%s.pileup.gz", toolFields.pileupOutdir, additionalFields.sampleName);
        final String controlPileup = String.format("%s/%s.pileup.gz", toolFields.pileupOutdir,
                additionalFields.controlSampleName);
        PileupOutput pileupOutput = PileupOutput.builder()
                .pileup(pileup)
                .controlPileup(controlPileup)
                .pileOutDir(toolFields.pileupOutdir)
                .build();
        pileupOutput.createDirectory();
        Context context = new Context();
        context.setVariable("toolFields", toolFields);
        context.setVariable("additionalFields", additionalFields);
        context.setVariable("pileup", pileup);
        context.setVariable("controlPileup", controlPileup);
        String cmd = templateEngine.process(PILEUP_TOOL_TEMPLATE_NAME, context);
        TaskContainer.addTasks("bam pileup");
        return PileupResult.builder()
                .command(BashCommand.withTool(cmd))
                .pileupOutput(pileupOutput)
                .build();
    }

    /**
     * This method initializes fields of the Pileup {@link Pileup} class.
     *
     * @param configuration is the type of {@link Configuration} which contains
     *                      its fields: pileup.
     * @return {@link AdditionaPileuplFields} with fields.
     **/
    private AdditionaPileuplFields initializeAdditionalFields(Configuration configuration) {
        return AdditionaPileuplFields.builder()
                .genome(validate(configuration.getGlobalConfig().getDatabaseConfig().getGenome(),
                        GlobalConfigFormat.GENOME))
                .bed(validate(configuration.getGlobalConfig().getDatabaseConfig().getBed(), GlobalConfigFormat.BED))
                .sampleName(sampleName)
                .controlSampleName(sampleControlName)
                .bam(bamResult.getBamOutput().getBam())
                .build();
    }

    private ToolFields constructFieldsForPileup(Configuration configuration) {
        return ToolFields.builder()
                .samtools(validate(configuration.getGlobalConfig().getToolConfig().getSamTools(),
                        GlobalConfigFormat.SAMTOOLS))
                .pileupOutdir(String.format("%s/pileup", sampleOutputDir))
                .controlBam(bamResult.getBamOutput().getControlBam())
                .build();
    }
}
