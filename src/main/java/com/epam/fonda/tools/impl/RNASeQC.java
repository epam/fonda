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
import com.epam.fonda.entity.configuration.StudyConfigFormat;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.MetricsOutput;
import com.epam.fonda.tools.results.MetricsResult;
import com.epam.fonda.workflow.TaskContainer;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Arrays;

import static com.epam.fonda.utils.PipelineUtils.getExecutionPath;
import static com.epam.fonda.utils.ToolUtils.validate;

@RequiredArgsConstructor
@Data
public class RNASeQC implements Tool<MetricsResult> {

    private static final String BAM_EXTENSION = ".bam";
    private static final String RNASEQC_TOOL_TEMPLATE_NAME = "RNASeQC_tool_template";

    @NonNull
    private FastqFileSample sample;
    @NonNull
    private BamOutput bamOutput;

    @Data
    private class ToolFields {
        private String rnaSeqcJava;
        private String rnaSeqc;
        private String picard;
        private String java;
        private String python;
    }

    @Data
    private class DatabaseFields {
        private String genome;
        private String annotgene;
        private String rRnaBed;
    }

    @Data
    private class AdditionalQcFields {
        private String sampleName;
        private String sqcOutdir;
        private String sbamOutdir;
        private String readType;
        private String stmpOutdir;
        private String jarPath;
        private String project;
        private String run;
        private String date;
    }

    @Data
    private class MetricFields {
        private String gcbiasMetrics;
        private String gcbiasChart;
        private String gcsumMetrics;
        private String rnaMetrics;
        private String mkdupMetrics;
        private String mergedQcMetrics;
    }

    /**
     * This method generates bash script {@link BashCommand} for RNASeQC tool.
     *
     * @param configuration  is the type of {@link Configuration} which contains
     *                       its fields: annotgene, genome, rRnaBed, rnaSeqcJava, rnaSeqc, python, picard, readType,
     *                       project, run, date.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link BashCommand} with bash script.
     **/
    @Override
    public MetricsResult generate(Configuration configuration, TemplateEngine templateEngine) {
        AdditionalQcFields additionalQcFields = initializeAdditionalFields(configuration);
        MetricFields metricFields = initializeMetricsFields(additionalQcFields);
        Context context = new Context();
        context.setVariable("additionalQcFields", additionalQcFields);
        context.setVariable("toolFields", initializeToolFields(configuration));
        context.setVariable("databaseFields", initializeDatabaseFields(configuration));
        context.setVariable("metricFields", metricFields);
        context.setVariable("mkdupBam", bamOutput.getMkdupBam());
        final String cmd = templateEngine.process(RNASEQC_TOOL_TEMPLATE_NAME, context);
        TaskContainer.addTasks("RNA QC metrics", "Merge RNA QC");
        final MetricsOutput metricsOutput = MetricsOutput.builder().build();
        metricsOutput.setRnaMetrics(metricFields.rnaMetrics);
        metricsOutput.setMergedQcMetrics(metricFields.mergedQcMetrics);
        metricsOutput.setGcbiasMetrics(metricFields.gcbiasMetrics);
        metricsOutput.setGcsumMetrics(metricFields.gcsumMetrics);
        metricsOutput.setGcbiasChart(metricFields.gcbiasChart);
        AbstractCommand resultCommand = BashCommand.withTool(cmd);
        resultCommand.setTempDirs(Arrays.asList(metricsOutput.getGcbiasMetrics(), metricsOutput.getGcsumMetrics()));
        return MetricsResult.builder()
                .bamOutput(bamOutput)
                .metricsOutput(metricsOutput)
                .command(resultCommand)
                .build();
    }

    /**
     * This method initializes fields of the ToolFields {@link ToolFields} class.
     *
     * @param configuration is the type of {@link Configuration} which contains
     *                      its fields: rnaSeqcJava, rnaSeqc, python, picard, java.
     * @return {@link ToolFields} with its fields.
     **/
    private ToolFields initializeToolFields(Configuration configuration) {
        ToolFields toolFields = new ToolFields();
        toolFields.java = validate(configuration.getGlobalConfig().getToolConfig().getJava(), GlobalConfigFormat.JAVA);
        toolFields.picard = validate(configuration.getGlobalConfig().getToolConfig().getPicard(),
                GlobalConfigFormat.PICARD);
        toolFields.python = validate(configuration.getGlobalConfig().getToolConfig().getPython(),
                GlobalConfigFormat.PYTHON);
        toolFields.rnaSeqc = validate(configuration.getGlobalConfig().getToolConfig().getRnaseqc(),
                GlobalConfigFormat.RNA_SEQC);
        toolFields.rnaSeqcJava = validate(configuration.getGlobalConfig().getToolConfig().getRnaseqcJava(),
                GlobalConfigFormat.RNA_SEQC_JAVA);
        return toolFields;
    }

    /**
     * This method initializes fields of the DatabaseFields {@link DatabaseFields} class.
     *
     * @param configuration is the type of {@link Configuration} which contains
     *                      its fields: annotgene, genome, rRnaBed.
     * @return {@link DatabaseFields} with its fields.
     **/
    private DatabaseFields initializeDatabaseFields(Configuration configuration) {
        DatabaseFields databaseFields = new DatabaseFields();
        databaseFields.annotgene = validate(configuration.getGlobalConfig().getDatabaseConfig().getAnnotgene(),
                GlobalConfigFormat.ANNOTGENE);
        databaseFields.genome = validate(configuration.getGlobalConfig().getDatabaseConfig().getGenome(),
                GlobalConfigFormat.GENOME);
        databaseFields.rRnaBed = configuration.getGlobalConfig().getDatabaseConfig().getRRNABED();
        return databaseFields;
    }

    /**
     * This method initializes fields of the AdditionalQcFields {@link AdditionalQcFields} class.
     *
     * @param configuration is the type of {@link Configuration} which contains
     *                      its fields: readType, project, run, date.
     * @return {@link AdditionalQcFields} with its fields.
     **/
    private AdditionalQcFields initializeAdditionalFields(Configuration configuration) {
        AdditionalQcFields additionalQcFields = new AdditionalQcFields();
        additionalQcFields.sbamOutdir = sample.getBamOutdir();
        additionalQcFields.sqcOutdir = sample.getQcOutdir();
        additionalQcFields.stmpOutdir = sample.getTmpOutdir();
        additionalQcFields.sampleName = sample.getName();
        additionalQcFields.jarPath = getExecutionPath(configuration);
        additionalQcFields.readType = validate(configuration.getGlobalConfig().getPipelineInfo().getReadType(),
                GlobalConfigFormat.READ_TYPE);
        additionalQcFields.project = validate(configuration.getStudyConfig().getProject(), StudyConfigFormat.PROJECT);
        additionalQcFields.run = validate(configuration.getStudyConfig().getRun(), StudyConfigFormat.RUN);
        additionalQcFields.date = validate(configuration.getStudyConfig().getDate(), StudyConfigFormat.DATE);
        return additionalQcFields;
    }

    /**
     * This method initializes fields of the MetricFields {@link MetricFields} class.
     *
     * @param additionalQcFields is the type of {@link AdditionalQcFields} which contains
     *                             its fields: bam, mkdupBam, sbamOutdir, sqcOutdir, sampleName.
     * @return {@link MetricFields} with its fields.
     **/
    private MetricFields initializeMetricsFields(AdditionalQcFields additionalQcFields) {
        final String mkdupBam = bamOutput.getMkdupBam();
        MetricFields metricFields = new MetricFields();
        metricFields.mkdupMetrics = bamOutput.getMkdupMetric()
                .replace(additionalQcFields.sbamOutdir, additionalQcFields.sqcOutdir);
        metricFields.gcbiasMetrics = mkdupBam.replace(BAM_EXTENSION, ".gcbias.metrics")
                .replace(additionalQcFields.sbamOutdir, additionalQcFields.sqcOutdir);
        metricFields.gcsumMetrics = mkdupBam.replace(BAM_EXTENSION, ".gc.summary.metrics")
                .replace(additionalQcFields.sbamOutdir, additionalQcFields.sqcOutdir);
        metricFields.gcbiasChart = mkdupBam.replace(BAM_EXTENSION, ".gcbias.pdf")
                .replace(additionalQcFields.sbamOutdir, additionalQcFields.sqcOutdir);
        metricFields.rnaMetrics = String.format("%s/metrics.tsv", additionalQcFields.sqcOutdir);
        metricFields.mergedQcMetrics = String.format("%s/%s.alignment.merged.QC.metric.txt",
                additionalQcFields.sqcOutdir, additionalQcFields.sampleName);
        return metricFields;
    }
}
