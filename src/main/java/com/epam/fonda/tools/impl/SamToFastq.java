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
        private String readType;
        private String bam;
    }

    @NonNull
    private String sampleName;
    @NonNull
    private String fastqSampleOutputDir;
    @NonNull
    private BamResult bamResult;

    @Override
    public FastqResult generate(Configuration configuration, TemplateEngine templateEngine) {
        ToolFields toolFields = initializeToolFields(configuration);
        String fastq1 = null;
        String fastq2 = null;
        String unpairFastq = null;
        if (FastqReadType.PAIRED.name()
                .equals(configuration.getGlobalConfig().getPipelineInfo().getReadType())) {
            fastq1 = String.format("%s/%s.R1.fastq", fastqSampleOutputDir, sampleName);
            fastq2 = String.format("%s/%s.R2.fastq", fastqSampleOutputDir, sampleName);
            unpairFastq = String.format("%s/%s.unpaired.fastq", fastqSampleOutputDir, sampleName);
        } else if (FastqReadType.SINGLE.name()
                .equals(configuration.getGlobalConfig().getPipelineInfo().getReadType())) {
            fastq1 = String.format("%s/%s.fastq", fastqSampleOutputDir, sampleName);
        }
        Context context = buildContext(toolFields, fastq1, fastq2, unpairFastq);
        final String cmd = templateEngine.process(SAM_TO_FASTQ_TOOL_TEMPLATE_NAME, context);
        return FastqResult.builder()
                .command(BashCommand.withTool(cmd))
                .out(buildFastqOutput(fastq1, fastq2, configuration))
                .build();
    }

    private Context buildContext(ToolFields toolFields, String fastq1, String fastq2, String unpairFastq) {
        Context context = new Context();
        context.setVariable("toolFields", toolFields);
        context.setVariable("fastq1", fastq1);
        context.setVariable("fastq2", fastq2);
        context.setVariable("unpairFastq", unpairFastq);
        return context;
    }

    private FastqOutput buildFastqOutput(String fastq1, String fastq2, Configuration configuration) {
        return FastqReadType.PAIRED.name().equals(configuration.getGlobalConfig().getPipelineInfo().getReadType())
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
                .bam(bamResult.getBamOutput().getBam())
                .build();
    }
}
