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
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.utils.AmpliconGatkDatabaseFields;
import com.epam.fonda.utils.AmpliconGatkToolFields;
import com.epam.fonda.workflow.TaskContainer;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Collections;

@RequiredArgsConstructor
public class AmpliconGatkRecalibrate implements Tool<BamResult> {

    private static final String AMPLICON_GATK_RECALIBRATE_TOOL_TEMPLATE_NAME =
            "amplicon_gatk_recalibrate_tool_template";

    @Data
    private class AdditionalFields {
        private String recalBam;
        private String recalBamIndex;
        private String recalTable;
        private String bam;
    }

    @NonNull
    private String tmpOutdir;
    @NonNull
    private BamResult bamResult;

    /**
     * This method generates {@link BamResult} for AmpliconGatkRecalibrate tool.
     *
     * @param configuration  is the type of {@link Configuration} which contains
     *                       its fields: java, gatk, genome, knownIndelsMills, knownIndelsPhase1.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link BamResult} with bash script.
     **/
    @Override
    public BamResult generate(Configuration configuration, TemplateEngine templateEngine) {
        AdditionalFields additionalFields = initializeAdditionalFields();
        Context context = new Context();
        context.setVariable("toolFields", AmpliconGatkToolFields.init(configuration));
        context.setVariable("databaseFields", AmpliconGatkDatabaseFields.init(configuration));
        context.setVariable("additionalFields", additionalFields);
        context.setVariable("tmpOutdir", tmpOutdir);
        String cmd = templateEngine.process(AMPLICON_GATK_RECALIBRATE_TOOL_TEMPLATE_NAME, context);
        TaskContainer.addTasks("GATK recalibration");
        BamOutput bamOutput = bamResult.getBamOutput();
        bamOutput.setBam(additionalFields.recalBam);
        AbstractCommand resultCommand = bamResult.getCommand();
        resultCommand.setToolCommand(resultCommand.getToolCommand() + cmd);
        resultCommand.setTempDirs(Collections.singletonList(additionalFields.recalTable));
        return bamResult;
    }



    /**
     * This method initializes fields of the AdditionalFields {@link AmpliconGatkRecalibrate.AdditionalFields} class.
     *
     * @return {@link AmpliconGatkRecalibrate.AdditionalFields} with its fields.
     **/
    private AdditionalFields initializeAdditionalFields() {
        AdditionalFields additionalFields = new AdditionalFields();
        additionalFields.bam = bamResult.getBamOutput().getBam();
        additionalFields.recalBam = additionalFields.bam.replace(".bam", ".recal.bam");
        additionalFields.recalBamIndex = additionalFields.recalBam.replace(".bam", ".bam.bai");
        additionalFields.recalTable = additionalFields.recalBam.replace(".bam", ".table");
        return additionalFields;
    }
}
