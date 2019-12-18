package com.epam.fonda.tools.impl;

import com.epam.fonda.entity.command.BashCommand;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.PostProcessTool;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.BamResult;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class FastqListAnalysis implements Tool<BamResult> {
    private static final String FASTQ_LIST_ANALYSIS_TEMPLATE = "fastq_list_analysis_template";

    @NonNull
    private List<FastqFileSample> samples;

    @Override
    public BamResult generate(Configuration configuration, TemplateEngine templateEngine) {
        FastqListAnalysisFields toolFields = initFastqListAnalysisFields(configuration);
        String fastqPath = String.format("%s/%s-%s-%s-FastqPaths.txt", toolFields.outdir, toolFields.project,
                toolFields.runID, toolFields.date);
        List<AdditionalToolFields> additionalToolFieldsList = buildToolFieldsList();
        Context context = new Context();
        context.setVariable("listObjects", additionalToolFieldsList);
        context.setVariable("outDir", toolFields.outdir);
        context.setVariable("readType", toolFields.readType);
        String defineOfFileWithFastq = templateEngine.process(FASTQ_LIST_ANALYSIS_TEMPLATE, context);
        try {
            Files.newBufferedWriter(Paths.get(fastqPath)).write(defineOfFileWithFastq);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return BamResult.builder()
                .command(BashCommand.withTool(defineOfFileWithFastq))
                .build();
    }

    private List<AdditionalToolFields> buildToolFieldsList() {
        return samples.stream().map(sample -> AdditionalToolFields.builder()
                .sampleName(sample.getName())
                .sampleType(sample.getSampleType())
                .matchControl(sample.getMatchControl())
                .build()).collect(Collectors.toList());
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

    private FastqListAnalysisFields initFastqListAnalysisFields(Configuration configuration) {
        return FastqListAnalysisFields.builder()
                .outdir(configuration.getStudyConfig().getDirOut())
                .runID(configuration.getStudyConfig().getRun())
                .date(configuration.getStudyConfig().getDate())
                .project(configuration.getStudyConfig().getProject())
                .workflow(configuration.getGlobalConfig().getPipelineInfo().getWorkflow())
                .readType(configuration.getGlobalConfig().getPipelineInfo().getReadType())
                .build();
    }
}
