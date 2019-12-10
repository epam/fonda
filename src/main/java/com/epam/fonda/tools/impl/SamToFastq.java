package com.epam.fonda.tools.impl;

import com.epam.fonda.entity.command.BashCommand;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.samples.fastq.FastqReadType;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.tools.results.FastqOutput;
import com.epam.fonda.tools.results.FastqResult;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Data
public class SamToFastq implements Tool<FastqResult> {
    private static final String SAM_TO_FASTQ_TOOL_TEMPLATE_NAME = "sam_to_fastq_tool_template";

    @Data
    @Builder
    private static class ToolFields {
        private String java;
        private String picard;
        private String outDir;
        private String readType;
        private String bam;
    }

    @NonNull
    private String sampleName;
    @NonNull
    private String sampleOutDir;
    @NonNull
    private BamResult bamResult;

    @Override
    public FastqResult generate(Configuration configuration, TemplateEngine templateEngine) {
        ToolFields toolFields = initializeToolFields(configuration);
        String fastq1 = null;
        String fastq2 = null;
        String unpairFastq = null;
        if (configuration.getGlobalConfig().getPipelineInfo().getReadType()
                .equals(FastqReadType.PAIRED.name())) {
            fastq1 = String.format("%s/%s.R1.fastq", toolFields.getOutDir(), sampleName);
            fastq2 = String.format("%s/%s.R2.fastq", toolFields.getOutDir(), sampleName);
            unpairFastq = String.format("%s/%s.unpaired.fastq", toolFields.getOutDir(), sampleName);
        } else if (configuration.getGlobalConfig().getPipelineInfo().getReadType()
                .equals(FastqReadType.SINGLE.name())) {
            fastq1 = String.format("%s/%s.fastq", toolFields.getOutDir(), sampleName);
        }
        Context context = new Context();
        context.setVariable("toolFields", toolFields);
        context.setVariable("fastq1", fastq1);
        context.setVariable("fastq2", fastq2);
        context.setVariable("unpairFastq", unpairFastq);
        final String cmd = templateEngine.process(SAM_TO_FASTQ_TOOL_TEMPLATE_NAME, context);
        return FastqResult.builder()
                .command(BashCommand.withTool(cmd))
                .out(buildFastqOutput(fastq1, fastq2, configuration))
                .build();
    }

    private FastqOutput buildFastqOutput(String fastq1, String fastq2, Configuration configuration) {
        return configuration.getGlobalConfig().getPipelineInfo().getReadType().equals(FastqReadType.PAIRED.name())
                ? FastqOutput.builder()
                .mergedFastq1(String.format("%s.gz", fastq1))
                .mergedFastq1(String.format("%s.gz", fastq2))
                .build()
                : FastqOutput.builder()
                .mergedFastq1(fastq1)
                .build();
    }

    private ToolFields initializeToolFields(Configuration configuration) {
        return ToolFields.builder()
                .java(configuration.getGlobalConfig().getToolConfig().getJava())
                .picard(configuration.getGlobalConfig().getToolConfig().getPicard())
                .readType(configuration.getGlobalConfig().getPipelineInfo().getReadType())
                .outDir(String.format("%s/samToFastq", sampleOutDir))
                .bam(bamResult.getBamOutput().getBam())
                .build();
    }
}
