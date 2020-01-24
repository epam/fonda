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

import com.epam.fonda.entity.command.BashCommand;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.*;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static java.lang.String.format;

@RequiredArgsConstructor
public class OptiType implements Tool<OptiTypeResult> {

    private static final String OPTY_TIPE_TOOL_TEMPLATE_NAME = "optiType_tool_template";

    @Data
    private class OptiTypeFields {
        private String optiType;
        private String sOptiTypeOutDir;
        private String sampleName;
        private String libraryType;
        private String python;
        private String fastq1;
        private String fastq2;
        private String mhc1hlaTypeRes;
        private String mhc1hlaCoverage;
    }

    @NonNull
    private FastqFileSample sample;
    @NonNull
    private FastqResult fastqResult;

    /**
     * This method generates bash script {@link BashCommand} for OptiType tool.
     *
     * @param configuration  is the type of {@link Configuration} which contains
     *                       its fields: optiType, libraryType, python.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link BashCommand} with bash script.
     **/
    @Override
    public OptiTypeResult generate(Configuration configuration, TemplateEngine templateEngine) {
        final String optiTypeOutDir = format("%s/optiType", sample.getSampleOutputDir());
        OptiTypeOutput optiTypeOutput = OptiTypeOutput.builder()
                .optiTypeOutdir(optiTypeOutDir)
                .build();
        optiTypeOutput.createDirectory();
        OptiType.OptiTypeFields optiTypeFields = constructFieldsForOptiType(configuration, optiTypeOutDir);
        if (optiTypeFields.fastq1 == null) {
            throw new IllegalArgumentException(
                    "Error Step: In optiType: not fastq files are properly provided, please check!");
        }
        Context context = new Context();
        context.setVariable("optiTypeFields", optiTypeFields);
        final String cmd = templateEngine.process(OPTY_TIPE_TOOL_TEMPLATE_NAME, context);
        optiTypeOutput.setMhc1hlaTypeRes(optiTypeFields.mhc1hlaTypeRes);
        optiTypeOutput.setMhc1hlaCoverage(optiTypeFields.mhc1hlaCoverage);
        return OptiTypeResult.builder()
                .fastqResult(fastqResult)
                .optiTypeOutput(optiTypeOutput)
                .command(BashCommand.withTool(cmd))
                .build();
    }

    /**
     * This method initializes fields of the OptiType {@link OptiType} class.
     *
     * @param configuration  is the type of {@link Configuration} which contains
     *                       its fields: optiType, libraryType, python.
     * @param optiTypeOutDir its a root to directory with output files.
     * @return {@link OptiType.OptiTypeFields} with fields.
     **/
    private OptiTypeFields constructFieldsForOptiType(final Configuration configuration, final String optiTypeOutDir) {
        OptiTypeFields optiTypeFields = new OptiTypeFields();
        optiTypeFields.optiType = configuration.getGlobalConfig().getToolConfig().getOptitype();
        optiTypeFields.sOptiTypeOutDir = optiTypeOutDir;
        optiTypeFields.sampleName = sample.getName();
        optiTypeFields.libraryType = configuration.getStudyConfig().getLibraryType();
        optiTypeFields.python = configuration.getGlobalConfig().getToolConfig().getPython();
        optiTypeFields.fastq1 = fastqResult.getOut().getMergedFastq1();
        optiTypeFields.fastq2 = fastqResult.getOut().getMergedFastq2();
        optiTypeFields.mhc1hlaTypeRes = format("%s/%s.mhc1hla.type.results", optiTypeFields.sOptiTypeOutDir,
                optiTypeFields.sampleName);
        optiTypeFields.mhc1hlaCoverage = format("%s/%s.mhc1hla.coverage", optiTypeFields.sOptiTypeOutDir,
                optiTypeFields.sampleName);
        return optiTypeFields;
    }
}
