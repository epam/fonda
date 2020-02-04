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

package com.epam.fonda.utils;

import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfigFormat;
import com.epam.fonda.entity.configuration.StudyConfig;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.stream.Collectors;

import com.epam.fonda.workflow.TaskContainer;
import static com.epam.fonda.utils.ToolUtils.validate;

public final class RnaAnalysisUtils {
    private static final String RNA_ANALYSIS_LOG_FILE_TEMPLATE = "analysis_logFile_template";
    private static final String RNA_ANALYSIS_DATA_ANALYSIS_TEMPLATE = "rna_analysis_data_analysis_template";
    private static final String MUTATION_ANALYSIS_TEMPLATE = "mutation_analysis_template";
    private static final int PERIOD = 60;

    private RnaAnalysisUtils() {}

    @Data
    private static class RnaAnalysisFields {
        private String logFile;
        private String successMessage;
        private String errorMessage;
        private String toolName;
        private String jarPath;
        private String rScript;
        private String outdir;
        private String sampleList;
        private String task;
        private int period;
    }

    /**
     * Method return fastq or bam file list reference
     *
     * @param studyConfig Study configuration with information about the study
     * @return path to file list (fastq or bam)
     */
    public static String getSampleFileListReference(StudyConfig studyConfig) {
        if (StringUtils.isNotBlank(studyConfig.getFastqList())) {
            return studyConfig.getFastqList();
        } else if (StringUtils.isNotBlank(studyConfig.getBamList())) {
            return studyConfig.getBamList();
        }
        throw new IllegalArgumentException("No fastq/bam list of files were found");
    }

    /**
     * Periodically check the existence of individual expression data file
     *
     * @param configuration  is the type of {@link Configuration} which contains
     *                       its fields: workflow.
     * @param templateEngine is type of {@link TemplateEngine} which set from workflow
     * @param toolName       name of the current tool
     * @return log message about success or fail check (bash script) in String format
     */
    public static String periodicStatusCheck(Configuration configuration, TemplateEngine templateEngine,
                                             String processName, String toolName, List<String> sampleNames) {
        String workflow = configuration.getGlobalConfig().getPipelineInfo().getWorkflow();
        String logDir = configuration.getCommonOutdir().getLogOutdir();
        return sampleNames.stream()
                .map(s -> logFileScanningShellScript(workflow, toolName, templateEngine, processName, logDir, s))
                .collect(Collectors.joining());
    }

    /**
     * Execute RNA expression data analysis based on the requirements of workflow and task
     *
     * @param configuration  is the type of {@link Configuration} which contains
     *                       its fields: rScript, jarPath, outdir.
     * @param templateEngine is type of {@link TemplateEngine} which set from workflow
     * @return bash script in String format
     */
    public static String dataAnalysis(Configuration configuration, TemplateEngine templateEngine, String toolName) {
        final Context context = buildContext(configuration, toolName);
        TaskContainer.addTasks("Merge gene expression");
        return templateEngine.process(RNA_ANALYSIS_DATA_ANALYSIS_TEMPLATE, context);
    }

    /**
     * Method generates shell script for scanning log file
     *
     * @param workflow       workflow name from Pipeline info (GlobalConfig)
     * @param toolName       name of the currently used tool
     * @param templateEngine is type of {@link TemplateEngine} which set from workflow
     * @param logDir         path to log files directory
     * @param sampleName     is a sample name
     * @return shell script in {@code String} format
     */
    private static String logFileScanningShellScript(String workflow, String toolName, TemplateEngine templateEngine,
                                                     String processName, String logDir, String sampleName) {
        Validate.notBlank(logDir, "Logging directory is not specified");
        RnaAnalysisFields rnaAnalysisFields = new RnaAnalysisFields();
        String fileName = workflow + "_" + toolName + "_for_" + sampleName + "_analysis";
        rnaAnalysisFields.errorMessage = String.format("Error %s results from %s", processName, sampleName);
        rnaAnalysisFields.successMessage = String.format("Confirm %s results from %s", processName, sampleName);
        rnaAnalysisFields.logFile = String.format("%s/%s.log", logDir, fileName);
        rnaAnalysisFields.period = PERIOD;
        rnaAnalysisFields.toolName = toolName;
        Context context = new Context();
        context.setVariable("fields", rnaAnalysisFields);
        return templateEngine.process(RNA_ANALYSIS_LOG_FILE_TEMPLATE, context);
    }

    /**
     *
     * @param configuration is the type of {@link Configuration} which contains
     *                      its fields: rScript, jarPath, outdir.
     * @param templateEngine is type of {@link TemplateEngine} which set from workflow
     * @param task tool name
     * @return bash script in String format
     */
    public static String dnaRnaMutationAnalysis(final Configuration configuration, final TemplateEngine templateEngine,
                                                final String task) {
        final Context context = buildContext(configuration, task);
        TaskContainer.addTasks("Merge mutation annotation");
        return templateEngine.process(MUTATION_ANALYSIS_TEMPLATE, context);
    }

    /**
     * Build {@link Context} for template processing
     * @param configuration is the type of {@link Configuration} which contains
     *                      its fields: rScript, jarPath, outdir.
     * @param task name of tool for analysis
     * @return {@link Context}
     */
    private static Context buildContext(final Configuration configuration, final String task) {
        RnaAnalysisFields rnaAnalysisFields = new RnaAnalysisFields();
        rnaAnalysisFields.toolName = task;
        rnaAnalysisFields.jarPath = PipelineUtils.getExecutionPath();
        rnaAnalysisFields.outdir = configuration.getCommonOutdir().getRootOutdir();
        rnaAnalysisFields.sampleList = getSampleFileListReference(configuration.getStudyConfig());
        rnaAnalysisFields.rScript = validate(configuration.getGlobalConfig().getToolConfig().getRScript(),
                GlobalConfigFormat.R_SCRIPT);
        Context context = new Context();
        context.setVariable("rnaAnalysisFields", rnaAnalysisFields);
        return context;
    }
}
