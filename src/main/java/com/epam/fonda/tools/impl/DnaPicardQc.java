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
import com.epam.fonda.entity.configuration.GlobalConfigFormat;
import com.epam.fonda.entity.configuration.StudyConfigFormat;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.MetricsOutput;
import com.epam.fonda.tools.results.MetricsResult;
import com.epam.fonda.workflow.PipelineType;
import com.epam.fonda.workflow.TaskContainer;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.epam.fonda.utils.PipelineUtils.getExecutionPath;
import static com.epam.fonda.utils.ToolUtils.validate;

@RequiredArgsConstructor
public class DnaPicardQc implements Tool<MetricsResult> {

    private static final String DNA_PICARD_QC_TOOL_TEMPLATE_NAME = "dna_picard_qc_tool_template";
    private static final String DNA_AMPLICON_PICARD_QC_TOOL_TEMPLATE_NAME = "dna_amplicon_picard_qc_tool_template";
    private static final String DNA_CAPTURE_PICARD_QC_TOOL_TEMPLATE_NAME = "dna_capture_picard_qc_tool_template";
    private static final String DNA_WGS_PICARD_QC_TOOL_TEMPLATE_NAME = "dna_wgs_picard_qc_tool_template";
    private static final String HS_METRICS = ".hs.metrics";
    private static final String BAM_EXTENSION = ".bam";

    @Data
    private class ToolFields {
        private String python;
        private String java;
        private String picard;
        private String samtools;
        private String bedtools;
    }

    @Data
    private class DatabaseFields {
        private String bed;
        private String bedWithHeader;
        private String bedForCoverage;
        private String genome;
    }

    @Data
    private class OutputDirFields {
        private String bamOutdir;
        private String qcOutdir;
        private String tmpOutdir;
    }

    @Data
    private class MetricsFields {
        private String mkdupMetric;
        private String alignMetrics;
        private String mkdupHsMetrics;
        private String rmdupHsMetrics;
        private String qualityMetrics;
        private String pileup;
        private String bedCoverage;
        private String insertMetrics;
        private String insertChart;
        private String gcbiasMetrics;
        private String gcsumMetrics;
        private String gcbiasChart;
        private String mergedQcMetrics;
    }

    @Data
    private class AdditionalFields {
        private String jarPath;
        private String sampleName;
        private String readType;
        private String project;
        private String run;
        private String date;
        private String bam;
        private String mkdupBam;
        private String analysis;
    }

    @NonNull
    private FastqFileSample sample;
    @NonNull
    private MetricsResult metricsResult;
    private String workflowType;
    private String libraryType;

    /**
     * This method generates {@link MetricsResult} for DnaAmpliconPicardQc tool.
     *
     * @param configuration  is the type of {@link Configuration} which contains
     *                       its fields: picard, java, bedtools, samtools, python, bed, bedFotCoverage,
     *                       bedWithHeader, genome, project, readType, run, date.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link MetricsResult} with bash script.
     **/
    @Override
    public MetricsResult generate(Configuration configuration, TemplateEngine templateEngine) {
        workflowType = configuration.getGlobalConfig().getPipelineInfo().getWorkflow();
        libraryType = validate(configuration.getStudyConfig().getLibraryType(), StudyConfigFormat.LIBRARY_TYPE);
        if (checkWorkflow()) {
            return metricsResult;
        }
        StringBuilder cmd = new StringBuilder();
        AdditionalFields additionalFields = initializeAdditionalFields(configuration);
        MetricsFields metricsFields = initializeMetricsFields();
        Context context = buildContext(configuration, additionalFields, metricsFields);
        AbstractCommand resultCommand = metricsResult.getCommand();
        List<String> metricsResultsList = buildCommonMetricsResult(metricsFields);

        if (isWorkflowDnaAmplicon()) {
            cmd.append(templateEngine.process(DNA_AMPLICON_PICARD_QC_TOOL_TEMPLATE_NAME, context));
            metricsResultsList.add(metricsFields.getBedCoverage());
        } else if (isCaptureWorkflowTargetType(libraryType)) {
            cmd.append(templateEngine.process(DNA_CAPTURE_PICARD_QC_TOOL_TEMPLATE_NAME, context));
            metricsResultsList.addAll(new ArrayList<>(Arrays.asList(metricsFields.getBedCoverage(),
                    metricsFields.getRmdupHsMetrics(), additionalFields.mkdupBam)));
        } else if (isWorkflowDnaCapture() && matchesExomeLibraryTypeCondition(libraryType) || isWorkflowRna()) {
            cmd.append(templateEngine.process(DNA_PICARD_QC_TOOL_TEMPLATE_NAME, context));
            metricsResultsList.add(metricsFields.getRmdupHsMetrics());
        } else if (isWorkflowDnaWgs()) {
            cmd.append(templateEngine.process(DNA_WGS_PICARD_QC_TOOL_TEMPLATE_NAME, context));
            metricsResultsList.add(metricsFields.getRmdupHsMetrics());
        }
        if (isWorkflowRna()) {
            TaskContainer.addTasks("RNA QC metrics", "Merge RNA QC");
        } else {
            TaskContainer.addTasks("DNA QC metrics", "Merge DNA QC");
        }
        resultCommand.setToolCommand(resultCommand.getToolCommand() + cmd);
        resultCommand.getTempDirs().addAll(metricsResultsList);

        return MetricsResult.builder()
                .bamOutput(metricsResult.getBamOutput())
                .metricsOutput(buildMetricsOutput())
                .command(resultCommand)
                .build();
    }

    /**
     * This method constructs the thymeleaf context for bash script template.
     *
     * @param configuration consists all configuration fields from ToolFields, DatabaseFields,
     *                      outputDirFields, MetricsFields and AdditionalFields classes.
     * @return {@link Context} with thymeleaf context.
     */
    private Context buildContext(Configuration configuration,
                                 AdditionalFields additionalFields,
                                 MetricsFields metricsFields) {
        Context context = new Context();
        context.setVariable("toolFields", initializeToolFields(configuration));
        context.setVariable("databaseFields", initializeDatabaseFields(configuration));
        context.setVariable("outputDirFields", initializeOutputDirFields());
        context.setVariable("metricsFields", metricsFields);
        context.setVariable("additionalFields", additionalFields);
        return context;
    }

    /**
     * This method builds metrics outputs.
     *
     * @return {@link MetricsOutput} with outputs
     */
    private MetricsOutput buildMetricsOutput() {
        MetricsFields metricsFields = initializeMetricsFields();
        final MetricsOutput metricsOutput = metricsResult.getMetricsOutput();
        metricsOutput.setAlignMetrics(metricsFields.alignMetrics);
        if (isWorkflowDnaAmplicon() || isCaptureWorkflowTargetType(libraryType)) {
            metricsOutput.setBedCoverage(metricsFields.bedCoverage);
        }
        metricsOutput.setGcbiasChart(metricsFields.gcbiasChart);
        metricsOutput.setGcbiasMetrics(metricsFields.gcbiasMetrics);
        metricsOutput.setGcsumMetrics(metricsFields.gcsumMetrics);
        metricsOutput.setInsertChart(metricsFields.insertChart);
        metricsOutput.setInsertMetrics(metricsFields.insertMetrics);
        metricsOutput.setMergedQcMetrics(metricsFields.mergedQcMetrics);
        metricsOutput.setMkdupHsMetrics(metricsFields.mkdupHsMetrics);
        metricsOutput.setQualityMetrics(metricsFields.qualityMetrics);
        metricsOutput.setPileup(metricsFields.pileup);
        return metricsOutput;
    }

    private List<String> buildCommonMetricsResult(final MetricsFields metricsFields) {
        return new ArrayList<>(Arrays.asList(
                metricsFields.getAlignMetrics(),
                metricsFields.getMkdupHsMetrics(),
                metricsFields.getQualityMetrics(),
                metricsFields.getPileup(),
                metricsFields.getInsertMetrics(),
                metricsFields.getGcbiasMetrics(),
                metricsFields.getGcsumMetrics()));
    }

    /**
     * This method initializes fields of the ToolFields {@link ToolFields} class.
     *
     * @param configuration is the type of {@link Configuration} which contains
     *                      its fields: picard, java, bedtools, samtools, python.
     * @return {@link ToolFields} with its fields.
     **/
    private ToolFields initializeToolFields(Configuration configuration) {
        ToolFields toolFields = new ToolFields();
        if (isWorkflowDnaAmplicon() || isCaptureWorkflowTargetType(libraryType)) {
            toolFields.bedtools = validate(configuration.getGlobalConfig().getToolConfig().getBedTools(),
                    GlobalConfigFormat.BEDTOOLS);
        }
        toolFields.java = validate(configuration.getGlobalConfig().getToolConfig().getJava(), GlobalConfigFormat.JAVA);
        toolFields.picard = validate(configuration.getGlobalConfig().getToolConfig().getPicard(),
                GlobalConfigFormat.PICARD);
        toolFields.python = validate(configuration.getGlobalConfig().getToolConfig().getPython(),
                GlobalConfigFormat.PYTHON);
        toolFields.samtools = validate(configuration.getGlobalConfig().getToolConfig().getSamTools(),
                GlobalConfigFormat.SAMTOOLS);
        return toolFields;
    }

    /**
     * This method initializes fields of the DatabaseFields {@link DatabaseFields} class.
     *
     * @param configuration is the type of {@link Configuration} which contains
     *                      its fields: bed, bedFotCoverage, bedWithHeader, genome.
     * @return {@link DatabaseFields} with its fields.
     **/
    private DatabaseFields initializeDatabaseFields(Configuration configuration) {
        DatabaseFields databaseFields = new DatabaseFields();
        databaseFields.bed = validate(configuration.getGlobalConfig().getDatabaseConfig().getBed(),
                GlobalConfigFormat.BED);
        databaseFields.bedForCoverage = validate(
                configuration.getGlobalConfig().getDatabaseConfig().getBedForCoverage(),
                GlobalConfigFormat.BED_FOR_COVERAGE);
        databaseFields.bedWithHeader = validate(configuration.getGlobalConfig().getDatabaseConfig().getBedWithHeader(),
                GlobalConfigFormat.BED_WITH_HEADER);
        databaseFields.genome = validate(configuration.getGlobalConfig().getDatabaseConfig().getGenome(),
                GlobalConfigFormat.GENOME);
        return databaseFields;
    }

    /**
     * @return OutputDirFields with output directories
     */
    private OutputDirFields initializeOutputDirFields() {
        OutputDirFields outputDirFields = new OutputDirFields();
        outputDirFields.bamOutdir = sample.getBamOutdir();
        outputDirFields.qcOutdir = sample.getQcOutdir();
        outputDirFields.tmpOutdir = sample.getTmpOutdir();
        return outputDirFields;
    }

    /**
     * This method initializes fields of the MetricsFields {@link MetricsFields} class.
     *
     * @return {@link MetricsFields} with its fields.
     **/
    private MetricsFields initializeMetricsFields() {
        final String mkdupBam = metricsResult.getBamOutput().getMkdupBam();
        final String bam = metricsResult.getBamOutput().getBam();
        final String bamOutdir = sample.getBamOutdir();
        final String qcOutdir = sample.getQcOutdir();
        final MetricsFields metricsFields;
        metricsFields = isWorkflowDnaAmplicon()
                ? constructMetricsForDnaAmpliconPicardQc(bam, bamOutdir, qcOutdir)
                : constructMetricsForDnaCapturePicardQcAndDnaPicardQc(mkdupBam, bam, bamOutdir, qcOutdir);
        if (isWorkflowDnaAmplicon() || isCaptureWorkflowTargetType(libraryType)) {
            metricsFields.bedCoverage = bam.replace(BAM_EXTENSION, ".coverage.per.base.txt")
                    .replace(bamOutdir, qcOutdir);
        }
        metricsFields.mkdupMetric = metricsResult.getBamOutput().getMkdupMetric();
        return metricsFields;
    }

    private MetricsFields constructMetricsForDnaAmpliconPicardQc(String bam, String bamOutdir, String qcOutdir) {
        MetricsFields metricsFields = new MetricsFields();
        metricsFields.alignMetrics = replaceBamOutdirWithQcOutdir(bam
                .replace(BAM_EXTENSION, ".align.metrics"), bamOutdir, qcOutdir);
        metricsFields.gcbiasChart = replaceBamOutdirWithQcOutdir(bam
                .replace(BAM_EXTENSION, ".gcbias.pdf"), bamOutdir, qcOutdir);
        metricsFields.gcbiasMetrics = replaceBamOutdirWithQcOutdir(bam
                .replace(BAM_EXTENSION, ".gcbias.metrics"), bamOutdir, qcOutdir);
        metricsFields.gcsumMetrics = replaceBamOutdirWithQcOutdir(bam
                .replace(BAM_EXTENSION, ".gc.summary.metrics"), bamOutdir, qcOutdir);
        metricsFields.insertChart = replaceBamOutdirWithQcOutdir(bam
                .replace(BAM_EXTENSION, ".insertsize.pdf"), bamOutdir, qcOutdir);
        metricsFields.insertMetrics = replaceBamOutdirWithQcOutdir(bam
                .replace(BAM_EXTENSION, ".insertsize.metrics"), bamOutdir, qcOutdir);
        metricsFields.mergedQcMetrics = String.format("%s/%s.alignment.merged.QC.metric.txt", qcOutdir,
                sample.getName());
        metricsFields.mkdupHsMetrics = replaceBamOutdirWithQcOutdir(bam
                .replace(BAM_EXTENSION, HS_METRICS), bamOutdir, qcOutdir);
        metricsFields.qualityMetrics = replaceBamOutdirWithQcOutdir(bam
                .replace(BAM_EXTENSION, ".quality.metrics"), bamOutdir, qcOutdir);
        metricsFields.pileup = replaceBamOutdirWithQcOutdir(bam
                .replace(BAM_EXTENSION, ".pileup"), bamOutdir, qcOutdir);
        return metricsFields;
    }

    private MetricsFields constructMetricsForDnaCapturePicardQcAndDnaPicardQc(String mkdupBam, String bam,
                                                                              String bamOutdir, String qcOutdir) {
        MetricsFields metricsFields = new MetricsFields();
        metricsFields.alignMetrics = replaceBamOutdirWithQcOutdir(mkdupBam
                .replace(BAM_EXTENSION, ".align.metrics"), bamOutdir, qcOutdir);
        metricsFields.gcbiasChart = replaceBamOutdirWithQcOutdir(mkdupBam
                .replace(BAM_EXTENSION, ".gcbias.pdf"), bamOutdir, qcOutdir);
        metricsFields.gcbiasMetrics = replaceBamOutdirWithQcOutdir(mkdupBam
                .replace(BAM_EXTENSION, ".gcbias.metrics"), bamOutdir, qcOutdir);
        metricsFields.gcsumMetrics = replaceBamOutdirWithQcOutdir(mkdupBam
                .replace(BAM_EXTENSION, ".gc.summary.metrics"), bamOutdir, qcOutdir);
        metricsFields.insertChart = replaceBamOutdirWithQcOutdir(mkdupBam
                .replace(BAM_EXTENSION, ".insertsize.pdf"), bamOutdir, qcOutdir);
        metricsFields.insertMetrics = replaceBamOutdirWithQcOutdir(mkdupBam
                .replace(BAM_EXTENSION, ".insertsize.metrics"), bamOutdir, qcOutdir);
        metricsFields.mergedQcMetrics = String.format("%s/%s.alignment.merged.QC.metric.txt", qcOutdir,
                sample.getName());
        metricsFields.mkdupHsMetrics = replaceBamOutdirWithQcOutdir(mkdupBam
                .replace(BAM_EXTENSION, HS_METRICS), bamOutdir, qcOutdir);
        metricsFields.qualityMetrics = replaceBamOutdirWithQcOutdir(mkdupBam
                .replace(BAM_EXTENSION, ".quality.metrics"), bamOutdir, qcOutdir);
        metricsFields.rmdupHsMetrics = replaceBamOutdirWithQcOutdir(bam
                .replace(BAM_EXTENSION, HS_METRICS), bamOutdir, qcOutdir);
        metricsFields.pileup = replaceBamOutdirWithQcOutdir(bam
                .replace(BAM_EXTENSION, ".pileup"), bamOutdir, qcOutdir);
        return metricsFields;
    }

    /**
     * This method initializes fields of the AdditionalFields {@link AdditionalFields} class.
     *
     * @param configuration is the type of {@link Configuration} which contains
     *                      its fields: project, readType, run, date.
     * @return {@link AdditionalFields} with its fields.
     **/
    private AdditionalFields initializeAdditionalFields(Configuration configuration) {
        AdditionalFields additionalFields = new AdditionalFields();
        additionalFields.bam = metricsResult.getBamOutput().getBam();
        if (!isWorkflowDnaAmplicon()) {
            additionalFields.mkdupBam = metricsResult.getBamOutput().getMkdupBam();
        }
        additionalFields.date = validate(configuration.getStudyConfig().getDate(), StudyConfigFormat.DATE);
        additionalFields.jarPath = getExecutionPath();
        additionalFields.project = validate(configuration.getStudyConfig().getProject(), StudyConfigFormat.PROJECT);
        additionalFields.readType = validate(configuration.getGlobalConfig().getPipelineInfo().getReadType(),
                GlobalConfigFormat.READ_TYPE);
        additionalFields.run = validate(configuration.getStudyConfig().getRun(), StudyConfigFormat.RUN);
        additionalFields.sampleName = sample.getName();
        additionalFields.analysis = isWorkflowRna() ? "RNA" : "DNA";
        return additionalFields;
    }

    private boolean matchesCaptureLibraryTypeCondition(String libraryType) {
        return libraryType.contains("target") || libraryType.contains("Target") || libraryType.contains("TARGET") ||
                libraryType.contains("IDT_17genesPanel") || libraryType.contains("IDT_56_Panel") ||
                libraryType.contains("IDT_SEADpanel") || libraryType.contains("IDT_Tp53panel") ||
                libraryType.contains("IDT_Tp53_SERD") || libraryType.contains("JAK2V617F");
    }

    private boolean matchesExomeLibraryTypeCondition(String libraryType) {
        return libraryType.contains("exome") || libraryType.contains("Exome") || libraryType.contains("EXOME")
                || libraryType.contains("WEX") || libraryType.contains("WES");
    }

    private boolean isWorkflowDnaCapture() {
        return PipelineType.DNA_CAPTURE_VAR_FASTQ.getName().equals(workflowType);
    }

    private boolean isWorkflowDnaAmplicon() {
        return PipelineType.DNA_AMPLICON_VAR_FASTQ.getName().equals(workflowType);
    }

    private boolean isWorkflowDnaWgs() {
        return PipelineType.DNA_WGS_VAR_FASTQ.getName().equals(workflowType);
    }

    private boolean isWorkflowRna() {
        return workflowType.contains("Rna");
    }

    private boolean isCaptureWorkflowTargetType(final String libraryType) {
        return isWorkflowDnaCapture() && matchesCaptureLibraryTypeCondition(libraryType);
    }

    private String replaceBamOutdirWithQcOutdir(String stringToReplace, String bamOutdir, String qcOutdir) {
        return stringToReplace.replace(bamOutdir, qcOutdir);
    }

    private boolean checkWorkflow() {
        return !isWorkflowDnaAmplicon()
                && (!isWorkflowDnaCapture()
                && !(matchesExomeLibraryTypeCondition(libraryType) || matchesCaptureLibraryTypeCondition(libraryType)))
                && !isWorkflowDnaWgs()
                && !isWorkflowRna();
    }
}
