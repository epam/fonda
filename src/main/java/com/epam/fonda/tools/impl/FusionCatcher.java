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
import com.epam.fonda.tools.results.FastqResult;
import com.epam.fonda.tools.results.FusionCatcherOutput;
import com.epam.fonda.tools.results.FusionCatcherResult;
import com.epam.fonda.workflow.TaskContainer;
import lombok.Data;
import lombok.NonNull;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Collections;

import static com.epam.fonda.utils.ToolUtils.validate;

@Data
public class FusionCatcher implements Tool<FusionCatcherResult> {
    private static final String FUSION_CATCHER_TOOL_TEMPLATE_NAME = "fusionCatcher_tool_template";

    @Data
    private class FusionCatcherFields {
        private String fusionCatcher;
        private String fusionCatcherOutdir;
        private String tmpFusionCatcherOutdir;
        private int numThreads;
        private String mergedFastq1;
        private String mergedFastq2;
        private String sampleName;
        private String fusionCatcherResult;
    }

    @NonNull
    private FastqFileSample sample;
    @NonNull
    private FastqResult result;

    /**
     * This method generates bash script {@link BashCommand} for FusionCatcher tool.
     * @param configuration  is the type of {@link Configuration} from which fusionCatcher
     *                       fields are used by FusionCatcher class.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link BashCommand} with bash script.
     **/
    @Override
    public FusionCatcherResult generate(Configuration configuration, TemplateEngine templateEngine) {
        if(result.getOut().getMergedFastq1() == null) {
            throw new IllegalArgumentException(
                    "Error Step: In Fusion_Catcher: not fastq files are properly provided, please check!");
        }
        FusionCatcherFields fusionCatcherFields = constructFieldsByIndex(configuration);
        FusionCatcherOutput fusionCatcherOutput = FusionCatcherOutput.builder()
                .fusionCatcherOutdir(fusionCatcherFields.fusionCatcherOutdir)
                .fusionCatcherResult(fusionCatcherFields.fusionCatcherResult)
                .tmpFusionCatcherOutdir(fusionCatcherFields.tmpFusionCatcherOutdir)
                .build();
        fusionCatcherOutput.createDirectory();
        Context context = new Context();
        context.setVariable("fusionCatcherFields", fusionCatcherFields);
        final String cmd = templateEngine.process(FUSION_CATCHER_TOOL_TEMPLATE_NAME, context);
        TaskContainer.addTasks("FusionCatcher");
        AbstractCommand command = BashCommand.withTool(cmd);
        command.setTempDirs(Collections.singletonList(fusionCatcherFields.tmpFusionCatcherOutdir));
        return FusionCatcherResult.builder()
                .fusionCatcherOutput(fusionCatcherOutput)
                .command(command)
                .fastqResult(result)
                .build();
    }

    /**
     * This method initializes fields of the FusionCatcher {@link FusionCatcher} class.
     * @param configuration is the type of {@link Configuration} which contains
     *                      its fields: fusionCatcher.
     * @return {@link FusionCatcherFields} with fields.
     **/
    private FusionCatcherFields constructFieldsByIndex(Configuration configuration) {
        FusionCatcherFields fusionCatcherFields = new FusionCatcherFields();
        fusionCatcherFields.fusionCatcher = validate(configuration.getGlobalConfig().getToolConfig().getFusionCatcher(),
                GlobalConfigFormat.FUSION_CATCHER);
        fusionCatcherFields.fusionCatcherOutdir = String.format("%s/fusionCatcher", sample.getSampleOutputDir());
        fusionCatcherFields.tmpFusionCatcherOutdir = String.format("%s/fusionCatcher/tmp", sample.getSampleOutputDir());
        fusionCatcherFields.numThreads = configuration.getGlobalConfig().getQueueParameters().getNumThreads();
        fusionCatcherFields.mergedFastq1 = result.getOut().getMergedFastq1();
        fusionCatcherFields.mergedFastq2 = result.getOut().getMergedFastq2();
        fusionCatcherFields.sampleName = sample.getName();
        fusionCatcherFields.fusionCatcherResult = String.format("%s/%s.fusionCatcher.fusion.results",
                fusionCatcherFields.fusionCatcherOutdir, fusionCatcherFields.sampleName);
        return fusionCatcherFields;
    }
}
