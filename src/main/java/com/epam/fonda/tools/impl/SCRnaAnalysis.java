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
import com.epam.fonda.tools.PostProcessTool;
import com.epam.fonda.utils.PipelineUtils;
import com.epam.fonda.workflow.TaskContainer;
import com.epam.fonda.workflow.impl.Flag;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.epam.fonda.utils.RnaAnalysisUtils.getSampleFileListReference;
import static com.epam.fonda.utils.ToolUtils.validate;

@Slf4j
@RequiredArgsConstructor
public class SCRnaAnalysis implements PostProcessTool {
    private static final String SCRNA_ANALYSIS_LOG_FILE_TEMPLATE = "analysis_logFile_template";
    private static final String SCRNA_ANALYSIS_EXPRESS_DATA_TEMPLATE = "scRna_analysis_count_tool_template";
    private static final String TOOL_STEP = "Generate gene-barcode matrix";
    private static final String COUNT_TOOL = "count";
    private static final int PERIOD = 60;

    @NonNull
    private Flag flag;
    @NonNull
    private List<String> sampleNames;

    @Data
    private class SCRnaAnalysisFields {
        private String logFile;
        private String successMessage;
        private String errorMessage;
        private String toolName;
        private String jarPath;
        private String rScript;
        private String outdir;
        private String sampleList;
        private String genomeBuild;
        private int period;
        private String successPattern;
        private String steps;
    }

    @Override
    public void generate(Configuration configuration, TemplateEngine templateEngine) {
        if (!flag.isConversion() || !flag.isCount()) {
            return;
        }
        StringBuilder command = new StringBuilder()
                .append(periodicStatusCheck(configuration, templateEngine))
                .append(expressData(configuration, templateEngine));
        configuration.setCustTask(COUNT_TOOL);
        try {
            PipelineUtils.printShell(configuration, command.toString(), null, null);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot create bash script for " + COUNT_TOOL);
        }
    }

    /**
     * Periodically check the existence of individual expression data file
     *
     * @param configuration  is the type of {@link Configuration} which contains
     *                       its fields: workflow.
     * @param templateEngine is type of {@link TemplateEngine} which set from workflow
     * @return log message about success or fail check (bash script) in String format
     */
    String periodicStatusCheck(Configuration configuration, TemplateEngine templateEngine) {
        String workflow = configuration.getGlobalConfig().getPipelineInfo().getWorkflow();
        String logDir = configuration.getCommonOutdir().getLogOutdir();
        return sampleNames.stream()
                .map(s -> logFileScanningShellScript(workflow, templateEngine, logDir, s))
                .collect(Collectors.joining());
    }

    /**
     * Method generates shell script for scanning log file
     *
     * @param workflow       workflow name from Pipeline info (GlobalConfig)
     * @param templateEngine is type of {@link TemplateEngine} which set from workflow
     * @param logDir         path to log files directory
     * @param sampleName     name of the sample
     * @return shell script in {@code String} format
     */
    private String logFileScanningShellScript(String workflow, TemplateEngine templateEngine,
                                              String logDir, String sampleName) {
        Validate.notBlank(logDir, "Logging directory is not specified");
        SCRnaAnalysis.SCRnaAnalysisFields scRnaAnalysisFields = new SCRnaAnalysis.SCRnaAnalysisFields();
        String fileName = workflow + "_alignment_for_" + sampleName + "_analysis";
        scRnaAnalysisFields.errorMessage = "Error gene expression results from " + sampleName;
        scRnaAnalysisFields.successMessage = "Confirm gene expression results from " + sampleName;
        scRnaAnalysisFields.logFile = String.format("%s/%s.log", logDir, fileName);
        scRnaAnalysisFields.period = PERIOD;
        scRnaAnalysisFields.toolName = TOOL_STEP;
        scRnaAnalysisFields.steps = String.join("|", TaskContainer.getTasks());
        scRnaAnalysisFields.successPattern = TaskContainer.getTasks().stream()
                .reduce((first, second) -> second)
                .orElse(null);
        Context context = new Context();
        context.setVariable("fields", scRnaAnalysisFields);
        return templateEngine.process(SCRNA_ANALYSIS_LOG_FILE_TEMPLATE, context);
    }

    /**
     * @param configuration  is the type of {@link Configuration} which contains
     *                       its fields: rScript, sampleList (fastq or bam), outDir and genomeBuild.
     * @param templateEngine is type of {@link TemplateEngine} which set from workflow
     * @return processed command for expressData
     */
    String expressData(Configuration configuration, TemplateEngine templateEngine) {
        checkConfiguration(configuration);
        SCRnaAnalysis.SCRnaAnalysisFields scRnaAnalysisFields = new SCRnaAnalysis.SCRnaAnalysisFields();
        scRnaAnalysisFields.jarPath = PipelineUtils.getExecutionPath();
        scRnaAnalysisFields.sampleList = getSampleFileListReference(configuration.getStudyConfig());
        scRnaAnalysisFields.rScript = validate(configuration.getGlobalConfig().getToolConfig().getRScript(),
                GlobalConfigFormat.R_SCRIPT);
        scRnaAnalysisFields.outdir = configuration.getCommonOutdir().getRootOutdir();
        scRnaAnalysisFields.genomeBuild = validate(configuration.getGlobalConfig().getDatabaseConfig().getGenomeBuild(),
                GlobalConfigFormat.GENOME_BUILD);

        Context context = new Context();
        context.setVariable("scRnaAnalysisFields", scRnaAnalysisFields);
        TaskContainer.addTasks("Merge gene expression");
        return templateEngine.process(SCRNA_ANALYSIS_EXPRESS_DATA_TEMPLATE, context);
    }

    private void checkConfiguration(Configuration configuration) {
        Validate.notBlank(configuration.getGlobalConfig().getToolConfig().getRScript(),
                "RScript is not specified");
        Validate.notBlank(configuration.getGlobalConfig().getDatabaseConfig().getGenomeBuild(),
                "Genome build configuration is not specified");
    }
}
