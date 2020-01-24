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

import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfigFormat;
import com.epam.fonda.entity.configuration.StudyConfigFormat;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.PostProcessTool;
import com.epam.fonda.utils.PipelineUtils;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.stream.Collectors;

import static com.epam.fonda.utils.ToolUtils.validate;

@Data
public class FastqListAnalysis implements PostProcessTool {
    private static final String FASTQ_LIST_ANALYSIS_TEMPLATE = "fastq_list_analysis_template";

    @NonNull
    private List<FastqFileSample> samples;

    /**
     * This method generates a file for {@link FastqListAnalysis} post process tool.
     *
     * @param configuration  the {@link Configuration} that is used to generate a file.
     * @param templateEngine the {@link TemplateEngine}.
     **/
    @Override
    public void generate(Configuration configuration, TemplateEngine templateEngine) {
        FastqListAnalysisFields toolFields = initFastqListAnalysisFields(configuration);
        String fastqPath = String.format("%s/%s-%s-%s-FastqPaths.txt", toolFields.outdir, toolFields.project,
                toolFields.runID, toolFields.date);
        List<AdditionalToolFields> additionalToolFieldsList = buildToolFieldsList();
        Context context = new Context();
        context.setVariable("listObjects", additionalToolFieldsList);
        context.setVariable("outDir", toolFields.outdir);
        context.setVariable("readType", toolFields.readType);
        String defineOfFileWithFastq = templateEngine.process(FASTQ_LIST_ANALYSIS_TEMPLATE, context);
        PipelineUtils.writeToFile(fastqPath, defineOfFileWithFastq);
    }

    @Data
    @Builder
    private static class AdditionalToolFields {
        private String sampleName;
        private String sampleType;
        private String matchControl;
    }

    @Data
    @Builder
    private static class FastqListAnalysisFields {
        private String outdir;
        private String runID;
        private String date;
        private String project;
        private String workflow;
        private String readType;
    }

    /**
     * Method creates list of AdditionalToolFields in {@link FastqListAnalysisFields} for next processing.
     *
     * @return type of {@link List<AdditionalToolFields>} with all AdditionalToolFields and its fields.
     */
    private List<AdditionalToolFields> buildToolFieldsList() {
        return samples.stream().map(sample -> AdditionalToolFields.builder()
                .sampleName(sample.getName())
                .sampleType(sample.getSampleType())
                .matchControl(sample.getMatchControl())
                .build()).collect(Collectors.toList());
    }

    /**
     * Method sets all needed fields in {@link FastqListAnalysisFields} for next processing.
     *
     * @param configuration is the type of {@link Configuration} which contains its fields: workflow, outdir,
     *                      runID, date, project, readType.
     * @return type of {@link FastqListAnalysisFields} with all set fields
     */
    private FastqListAnalysisFields initFastqListAnalysisFields(Configuration configuration) {
        return FastqListAnalysisFields.builder()
                .outdir(validate(configuration.getStudyConfig().getDirOut(), StudyConfigFormat.DIR_OUT))
                .runID(validate(configuration.getStudyConfig().getRun(), StudyConfigFormat.RUN))
                .date(validate(configuration.getStudyConfig().getDate(), StudyConfigFormat.DATE))
                .project(validate(configuration.getStudyConfig().getProject(), StudyConfigFormat.PROJECT))
                .workflow(configuration.getGlobalConfig().getPipelineInfo().getWorkflow())
                .readType(validate(configuration.getGlobalConfig().getPipelineInfo().getReadType(),
                        GlobalConfigFormat.READ_TYPE))
                .build();
    }
}
