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

package com.epam.fonda.utils;

import com.epam.fonda.entity.configuration.Configuration;
import lombok.Builder;
import lombok.Data;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;

public final class DnaUtils {

    private static final int DEFAULT_VALUE = 60;
    private static final String LOGFILE_SCANNING_SHELL_SCRIPT_TEMPLATE_NAME =
            "logfile_scanning_shell_script_template";
    private static final String PERIODIC_INDEX_BAM_STATUS_CHECK = "periodic_index_check_for_fastqs";
    private static final TemplateEngine TEMPLATE_ENGINE = TemplateEngineUtils.init();
    private static final String ERROR_BAM = "Error bam from ";
    private static final String CONFIRM_BAM = "Confirm bam from ";
    private static final String LOG = "%s/%s.log";
    private static final String FORMAT = "%s_%s_for_%s_analysis";
    private static final String FORMAT_INDEX = "%s_%s_for_%s_%s_analysis";
    private static final String MSG_WITH_SAMPLE_FORMAT = "%s%s";
    private static final String MSG_WITH_SAMPLE_FORMAT_INDEX = "%s%s_%s";
    private static final String MSG_WITH_CONTROL_SAMPLE_FORMAT = "%s%s.";
    private static final String ERROR_MSG_WITH_SAMPLE = "errorMsgWithSampleName";
    private static final String CONFIRM_MSG_WITH_SAMPLE = "confirmMsgWithSampleName";
    private static final String ERROR_MSG_WITH_CONTROL_SAMPLE = "errorMsgWithControlSampleName";
    private static final String CONFIRM_MSG_WITH_CONTROL_SAMPLE = "confirmMsgWithControlSampleName";
    private static final String BAM_INDEX_TAG = "Index bam.";

    @Data
    @Builder
    private static class LogFileFields {
        private String logFileWithSampleName;
        private String logFileWithSampleNameIndex;
        private String logFileWithControlSampleName;
    }

    @Data
    @Builder
    private static class AdditionalFields {
        private String period;
        private String workflow;
        private String custTask;
        private String fileNameWithSampleName;
        private String fileNameWithSampleNameIndex;
        private String fileNameWithControlSampleName;
    }

    private DnaUtils() {
    }

    /**
     * This method checks periodic bam status and returns script according to it.
     *
     * @param tag           is of type String {@link String} and contains step of the workflow.
     * @param configuration is of type Configuration {@link Configuration} and contains its fields.
     * @param index         is of type String {@link String} and contains bam index
     * @return is of type String {@link String} and contains tool command
     */
    public static String checkPeriodicBamStatus(String tag, String sampleName, String controlSampleName,
                                                Configuration configuration, String index) {
        Integer period = getDefaultOrSpecifiedPeriod(configuration);
        AdditionalFields additionalFields = initializeAdditionalFields(configuration, sampleName, controlSampleName,
                index);
        LogFileFields logFileFields = initializeLogFields(configuration, additionalFields);
        Map<String, String> msgMap = new HashMap<>();
        if (Optional.ofNullable(index).isPresent()) {
            msgMap.put(ERROR_MSG_WITH_SAMPLE, format(MSG_WITH_SAMPLE_FORMAT_INDEX, ERROR_BAM, sampleName, index));
            msgMap.put(CONFIRM_MSG_WITH_SAMPLE, format(MSG_WITH_SAMPLE_FORMAT_INDEX, CONFIRM_BAM, sampleName,
                    index));
        } else {
            msgMap.put(ERROR_MSG_WITH_SAMPLE, format(MSG_WITH_SAMPLE_FORMAT, ERROR_BAM, sampleName));
            msgMap.put(CONFIRM_MSG_WITH_SAMPLE, format(MSG_WITH_SAMPLE_FORMAT, CONFIRM_BAM, sampleName));
            if (Optional.ofNullable(controlSampleName).isPresent()) {
                msgMap.put(ERROR_MSG_WITH_CONTROL_SAMPLE, format(MSG_WITH_CONTROL_SAMPLE_FORMAT, ERROR_BAM,
                        sampleName));
                msgMap.put(CONFIRM_MSG_WITH_CONTROL_SAMPLE, format(MSG_WITH_CONTROL_SAMPLE_FORMAT, CONFIRM_BAM,
                        sampleName));
            }
        }
        return getLogFileScanningShellScript(logFileFields, tag, msgMap, period, index);
    }

    /**
     * @param configuration is of type Configuration {@link Configuration} and contains its fields.
     * @return is of type Configuration {@link Integer} and contains period.
     */
    public static Integer getDefaultOrSpecifiedPeriod(Configuration configuration) {
        return Optional.ofNullable(configuration.getGlobalConfig().getToolConfig().getStatusCheckPeriod())
                .orElse(DEFAULT_VALUE);
    }

    /**
     * Checks periodic bam status for each fastq file and returns script command according to it
     * @param fastqs1 the list of fastq files
     * @param sampleName the name of the sample
     * @param controlSampleName the name of the control sample
     * @param configuration the configuration that contains information about:
     *                      log output directory, workflow, custTask
     * @return script command
     */
    public static String periodicIndexBamStatusCheckForFastqList(final List<String> fastqs1, final String sampleName,
                                                                 final String controlSampleName,
                                                                 final Configuration configuration) {
        final StringBuilder cmd = new StringBuilder();
        for (int i = 0; i < fastqs1.size(); i++) {
            cmd.append(checkPeriodicBamStatus(BAM_INDEX_TAG, sampleName, controlSampleName, configuration,
                    String.valueOf(i + 1)));
        }
        final Context context = new Context();
        context.setVariable("cmd", cmd.toString());
        return TEMPLATE_ENGINE.process(PERIODIC_INDEX_BAM_STATUS_CHECK, context);
    }

    /**
     * Checks the sample type
     * @param sampleType
     * @return true if sample type is not "case" or "tumor" type
     */
    public static boolean isNotCaseOrTumor(final String sampleType) {
        return !sampleType.equals(PipelineUtils.CASE) && !sampleType.equals(PipelineUtils.TUMOR);
    }

    private static String getLogFileScanningShellScript(LogFileFields logFileFields, String tag,
                                                        Map<String, String> msgMap, Integer period, String index) {
        return TEMPLATE_ENGINE.process(LOGFILE_SCANNING_SHELL_SCRIPT_TEMPLATE_NAME, buildContext(logFileFields,
                tag, msgMap, period, index));
    }

    private static Context buildContext(LogFileFields logFileFields, String tag, Map<String, String> msgMap,
                                        Integer period, String index) {
        Context context = new Context();
        String ifScript1 = "if [[ $str == \"*Error Step: ";
        String ifScript2 = "if [[ -f $logFile  ]];";
        String whileScript = "while [[ $str = \"\" ]]";
        context.setVariable("logFileWithSampleName", logFileFields.logFileWithSampleName);
        context.setVariable("logFileWithControlSampleName", logFileFields.logFileWithControlSampleName);
        context.setVariable("logFileWithSampleNameIndex", logFileFields.logFileWithSampleNameIndex);
        context.setVariable("tag", tag);
        context.setVariable(ERROR_MSG_WITH_SAMPLE, msgMap.get(ERROR_MSG_WITH_SAMPLE));
        context.setVariable(CONFIRM_MSG_WITH_SAMPLE, msgMap.get(CONFIRM_MSG_WITH_SAMPLE));
        context.setVariable(ERROR_MSG_WITH_CONTROL_SAMPLE, msgMap.get(ERROR_MSG_WITH_CONTROL_SAMPLE));
        context.setVariable(CONFIRM_MSG_WITH_CONTROL_SAMPLE, msgMap.get(CONFIRM_MSG_WITH_CONTROL_SAMPLE));
        context.setVariable("period", period);
        context.setVariable("index", index);
        context.setVariable("ifScript1", ifScript1);
        context.setVariable("ifScript2", ifScript2);
        context.setVariable("whileScript", whileScript);
        return context;
    }

    private static LogFileFields initializeLogFields(Configuration configuration, AdditionalFields additionalFields) {
        final String logOutdir = configuration.getCommonOutdir().getLogOutdir();
        return LogFileFields.builder()
                .logFileWithSampleName(format(LOG, logOutdir, additionalFields.fileNameWithSampleName))
                .logFileWithSampleNameIndex(format(LOG, logOutdir, additionalFields.fileNameWithSampleNameIndex))
                .logFileWithControlSampleName(Optional.ofNullable(additionalFields.fileNameWithControlSampleName)
                        .isPresent() ? format(LOG, logOutdir,
                        additionalFields.fileNameWithControlSampleName) : null)
                .build();
    }

    private static AdditionalFields initializeAdditionalFields(Configuration configuration, String sampleName,
                                                               String controlSampleName, String index) {
        String custTask = configuration.getCustTask();
        String workflow = configuration.getGlobalConfig().getPipelineInfo().getWorkflow();
        return AdditionalFields.builder()
                .custTask(custTask)
                .workflow(workflow)
                .fileNameWithSampleName(format(FORMAT, workflow, custTask, sampleName))
                .fileNameWithSampleNameIndex(format(FORMAT_INDEX, workflow, custTask, sampleName, index))
                .fileNameWithControlSampleName(Optional.ofNullable(controlSampleName).isPresent() ? format(FORMAT,
                        workflow, custTask, controlSampleName) : null)
                .build();
    }
}
