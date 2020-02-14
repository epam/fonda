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
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.entity.configuration.GlobalConfigFormat;
import com.epam.fonda.entity.configuration.StudyConfig;
import com.epam.fonda.entity.configuration.StudyConfigFormat;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.OptiTypeResult;
import com.epam.fonda.tools.results.FastqResult;
import com.epam.fonda.tools.results.OptiTypeOutput;
import com.epam.fonda.utils.ToolUtils;
import com.epam.fonda.workflow.TaskContainer;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static com.epam.fonda.utils.ToolUtils.validate;
import static java.lang.String.format;

@RequiredArgsConstructor
public class OptiType implements Tool<OptiTypeResult> {

    private static final String OPTY_TIPE_TOOL_TEMPLATE_NAME = "optiType_tool_template";

    @NonNull
    private FastqFileSample sample;
    @NonNull
    private FastqResult fastqResult;

    @Data
    @Builder
    private static class OptiTypeFields {
        private String optiType;
        private String optiTypeOutDir;
        private String sampleName;
        private String libraryType;
        private String python;
        private String fastq1;
        private String fastq2;
        private String mhc1hlaTypeRes;
        private String mhc1hlaCoverage;
    }

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
        final OptiTypeFields optiTypeFields = constructFieldsForOptiType(configuration);
        if (optiTypeFields.fastq1 == null) {
            throw new IllegalArgumentException(
                    "Error Step: In optiType: not fastq files are properly provided, please check!");
        }
        OptiTypeOutput optiTypeOutput = OptiTypeOutput.builder()
                .optiTypeOutdir(optiTypeFields.getOptiTypeOutDir())
                .mhc1hlaTypeRes(optiTypeFields.mhc1hlaTypeRes)
                .mhc1hlaCoverage(optiTypeFields.mhc1hlaCoverage)
                .build();
        optiTypeOutput.createDirectory();
        Context context = new Context();
        context.setVariable("optiTypeFields", optiTypeFields);
        context.setVariable("isLibraryTypeRna", optiTypeFields.getLibraryType().contains("RNA"));
        context.setVariable("isLibraryTypeDna", optiTypeFields.getLibraryType().contains("DNA"));
        final String cmd = templateEngine.process(OPTY_TIPE_TOOL_TEMPLATE_NAME, context);
        TaskContainer.addTasks("OptiType HLA typing");
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
     * @return {@link OptiType.OptiTypeFields} with fields.
     **/
    private OptiTypeFields constructFieldsForOptiType(final Configuration configuration) {
        final String optiTypeOutDir = format("%s/optitype", sample.getSampleOutputDir());
        final GlobalConfig.ToolConfig toolConfig = configuration.getGlobalConfig().getToolConfig();
        final StudyConfig studyConfig = configuration.getStudyConfig();
        return OptiTypeFields.builder()
                .optiType(validate(toolConfig.getOptitype(), GlobalConfigFormat.OPTITYPE))
                .optiTypeOutDir(optiTypeOutDir)
                .sampleName(validate(sample.getName(), ToolUtils.SAMPLE_NAME))
                .libraryType(validate(studyConfig.getLibraryType(), StudyConfigFormat.LIBRARY_TYPE))
                .python(validate(toolConfig.getPython(), GlobalConfigFormat.PYTHON))
                .fastq1(fastqResult.getOut().getMergedFastq1())
                .fastq2(fastqResult.getOut().getMergedFastq2())
                .mhc1hlaTypeRes(format("%s/%s_hla_result.tsv", optiTypeOutDir, sample.getName()))
                .mhc1hlaCoverage(format("%s/%s_hla_coverage_plot.pdf", optiTypeOutDir, sample.getName()))
                .build();
    }
}
