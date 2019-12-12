package com.epam.fonda.tools.impl;

import com.epam.fonda.entity.command.AbstractCommand;
import com.epam.fonda.entity.command.BashCommand;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.samples.bam.BamFileSample;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.BamResult;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Arrays;

@Data
public class SortBamByReadName implements Tool<BamResult> {
    private static final String SORT_BAM_BY_READ_NAME_TEMPLATE = "sort_bam_by_read_name_template";

    @Data
    @Builder
    private static class ToolFields {
        private String java;
        private String picard;
        private String samtools;
        private String outDir;
    }

    @NonNull
    private String sampleName;
    @NonNull
    private String fastqSampleOutputDir;
    @NonNull
    private BamFileSample sample;

    @Override
    public BamResult generate(Configuration configuration, TemplateEngine templateEngine) {
        ToolFields toolFields = initializeToolFields(configuration);
        final String bam = sample.getBam();
        final String sortedBam = String.format("%s/%s.sortByReadname.bam", fastqSampleOutputDir, sampleName);
        final String sortedBamIndex = String.format("%s.bai", sortedBam);
        Context context = buildContext(toolFields, bam, sortedBam, sortedBamIndex);
        final String cmd = templateEngine.process(SORT_BAM_BY_READ_NAME_TEMPLATE, context);
        AbstractCommand command = BashCommand.withTool(cmd);
        command.setTempDirs(Arrays.asList(sortedBam, sortedBamIndex));
        return BamResult.builder()
                .command(command)
                .bamOutput(BamOutput.builder()
                        .sortedBam(sortedBam)
                        .build())
                .build();
    }

    private Context buildContext(ToolFields toolFields, String bam, String sortedBam, String sortedBamIndex) {
        Context context = new Context();
        context.setVariable("bam", bam);
        context.setVariable("toolFields", toolFields);
        context.setVariable("sortedBam", sortedBam);
        context.setVariable("sortedBamIndex", sortedBamIndex);
        return context;
    }

    private ToolFields initializeToolFields(Configuration configuration) {
        return ToolFields.builder()
                .java(configuration.getGlobalConfig().getToolConfig().getJava())
                .picard(configuration.getGlobalConfig().getToolConfig().getPicard())
                .samtools(configuration.getGlobalConfig().getToolConfig().getSamTools())
                .build();
    }
}
