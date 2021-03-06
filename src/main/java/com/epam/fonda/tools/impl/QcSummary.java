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

import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfigFormat;
import com.epam.fonda.entity.configuration.StudyConfigFormat;
import com.epam.fonda.tools.PostProcessTool;
import com.epam.fonda.utils.PipelineUtils;
import com.epam.fonda.workflow.PipelineType;
import com.epam.fonda.workflow.TaskContainer;
import com.epam.fonda.workflow.impl.Flag;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.epam.fonda.utils.ToolUtils.matchesCaptureLibraryTypeCondition;
import static com.epam.fonda.utils.ToolUtils.matchesExomeLibraryTypeCondition;
import static com.epam.fonda.utils.ToolUtils.validate;

@RequiredArgsConstructor
public class QcSummary implements PostProcessTool {
    private static final String QC_SUMMARY_STATUS_CHECK_TEMPLATE = "qc_summary_status_check_template";
    private static final String QC_SUMMARY_ANALYSIS_TEMPLATE = "qc_summary_analysis_template";
    private static final int DEFAULT_VALUE = 60;

    @NonNull
    private final Flag flag;
    @NonNull
    private final List<String> sampleNames;

    /**
     * This method generates a bash script for {@link QcSummary} post process tool.
     *
     * @param configuration  the {@link Configuration} that is used to generate a bash script.
     * @param templateEngine the {@link TemplateEngine}.
     * if flag doesn't contains qc.
     **/
    @Override
    public String generate(final Configuration configuration, final TemplateEngine templateEngine) throws IOException {
        if (!flag.isQc()) {
            return StringUtils.EMPTY;
        }
        configuration.setCustTask("qcsummary");
        final Context context = new Context();
        final StringBuilder cmd = new StringBuilder(90);
        QcSummaryFields qcSummaryFields;
        for (final String sample : sampleNames) {
            qcSummaryFields = constructFields(configuration, sample);
            context.setVariable("QcSummaryFields", qcSummaryFields);
            if (!configuration.isMasterMode()) {
                cmd.append(templateEngine.process(QC_SUMMARY_STATUS_CHECK_TEMPLATE, context));
            }
        }
        cmd.append(templateEngine.process(QC_SUMMARY_ANALYSIS_TEMPLATE, context));
        TaskContainer.addTasks("QC summary analysis");
        return PipelineUtils.printShell(configuration, cmd.toString(), "", null);
    }

    private QcSummaryFields constructFields(final Configuration configuration, String sample) {
        final String workflowName = configuration.getGlobalConfig().getPipelineInfo().getWorkflow();
        final String tag = getValueForSpecificVar(workflowName, Variable.TAG,
                configuration.getStudyConfig().getLibraryType());
        if (StringUtils.isNotBlank(tag)) {
            TaskContainer.addTasks(tag);
        }
        final Set<String> tasks = TaskContainer.getTasks();
        final QcSummaryFields qcSummaryFields = QcSummaryFields.builder()
                .workflow(workflowName)
                .outDir(validate(configuration.getStudyConfig().getDirOut(), StudyConfigFormat.DIR_OUT))
                .rScript(validate(configuration.getGlobalConfig().getToolConfig().getRScript(),
                        GlobalConfigFormat.R_SCRIPT))
                .fastqList(configuration.getStudyConfig().getFastqList())
                .statusCheckPeriod(defaultOrSpecifiedPeriod(configuration))
                .errorMessage("Error QC results from " + sample)
                .successMessage("Confirm QC results from " + sample)
                .task("QC summary analysis")
                .jarPath(PipelineUtils.getExecutionPath(configuration))
                .steps(String.join("|", tasks))
                .successPattern(StringUtils.isNotBlank(tag) ? tag : tasks.toArray()[tasks.size() - 1].toString())
                .build();
        final String task = getValueForSpecificVar(workflowName, Variable.TASK,
                configuration.getStudyConfig().getLibraryType());
        final String fileName = qcSummaryFields.getWorkflow() + "_" + task + "_for_" + sample + "_analysis";
        final String logOutDir = qcSummaryFields.getOutDir() + "/log_files";
        final String logFile = logOutDir + "/" + fileName + ".log";

        qcSummaryFields.setTag(tag);
        qcSummaryFields.setLogFile(logFile);

        return qcSummaryFields;
    }

    /**
     * @param configuration contains a value if specified in the global config file or null.
     * @return a value from the global config file or a default value which is equals 60.
     */
    private Integer defaultOrSpecifiedPeriod(Configuration configuration) {
        Integer statusCheckPeriod = configuration.getGlobalConfig().getToolConfig().getStatusCheckPeriod();

        if (statusCheckPeriod != null) {
            return statusCheckPeriod;
        }

        return QcSummary.DEFAULT_VALUE;
    }

    /**
     * @param workflow defines the proper value of variable for passed workflow.
     * @param variable {@link Variable} for which the value should be obtained.
     * @param libraryType the sequencing library type: DNATargetSeq_Paired, DNATargetSeq_Single, etc
     * @return Depending on the workflow, returns the correct value for the {@link Variable}.
     */
    private String getValueForSpecificVar(String workflow, Variable variable, String libraryType) {
        final PipelineType pipelineType = PipelineType.getByName(workflow);
        Map<Enum, String> map = new HashMap<>();
        map.put(Variable.TASK, "alignment");

        switch (pipelineType) {
            case DNA_CAPTURE_VAR_FASTQ:
            case DNA_WGS_VAR_FASTQ:
            case DNA_AMPLICON_VAR_FASTQ:
                map.put(Variable.TASK, "postalignment");
                if (!matchesExomeLibraryTypeCondition(libraryType) &&
                        !matchesCaptureLibraryTypeCondition(libraryType)) {
                    break;
                }
                map.put(Variable.TAG, "Merge DNA QC");
                break;
            case RNA_EXPRESSION_FASTQ:
            case RNA_CAPTURE_VAR_FASTQ:
            case SC_RNA_EXPRESSION_FASTQ:
                map.put(Variable.TAG, "Merge RNA QC");
                break;
            case SC_RNA_EXPRESSION_CELLRANGER_FASTQ:
                map.put(Variable.TAG, "Cellranger count");
                break;
            default:
                break;
        }

        return map.get(variable);
    }

    private enum Variable {
        TASK, TAG
    }

    @Data
    @Builder
    private static class QcSummaryFields {
        private int statusCheckPeriod;
        private String logFile;
        private String tag;
        private String errorMessage;
        private String successMessage;
        private String rScript;
        private String workflow;
        private String outDir;
        private String fastqList;
        private String jarPath;
        private String task;
        private String steps;
        private String successPattern;
    }
}
