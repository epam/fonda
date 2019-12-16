package com.epam.fonda.tools.impl;

import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.samples.fastq.FastqReadType;
import com.epam.fonda.tools.PostProcessTool;
import lombok.Builder;
import lombok.Data;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FastqListAnalysis implements PostProcessTool {

    @Override
    public void generate(Configuration configuration, TemplateEngine templateEngine) throws IOException {
        FastqListAnalysisFields toolFields = initFastqListAnalysisFields(configuration);
        String bamList = configuration.getStudyConfig().getBamList();
        String fastqPath = String.format("%s/%s-%s-%s-FastqPaths.txt", toolFields.outdir, toolFields.project,
                toolFields.runID, toolFields.date);
        StringBuilder resultStr = new StringBuilder();
        Files.newBufferedReader(Paths.get(bamList)).lines().skip(1).forEach(line -> {
            String[] lineOfWordsFromBamList = line.trim().split("\\t");
            if (lineOfWordsFromBamList.length != 5 && lineOfWordsFromBamList.length != 3) {
                System.out.println("Error Step: Please check the number of columns in bam_list file." +
                        " It should be either 3 or 5!");
                return;
            }
            String sampleName = lineOfWordsFromBamList[1];
            String sampleType = lineOfWordsFromBamList.length == 5 ? lineOfWordsFromBamList[3] : "tumor";
            String matchControl = lineOfWordsFromBamList.length == 5 ? lineOfWordsFromBamList[4] : "NA";
            if (FastqReadType.PAIRED.name().equals(toolFields.readType)) {
                resultStr.append("parameterType\tshortName\tParameter1\tParameter2\tsample_type\tmatch_control\n");
                String fq1 = String.format("%s/%s/fastq/%s.R1.fastq.gz", toolFields.getOutdir(), sampleName,
                        sampleName);
                String fq2 = String.format("%s/%s/fastq/%s.R2.fastq.gz", toolFields.getOutdir(), sampleName,
                        sampleName);
                resultStr.append(String.format("fastqFile\t%s\t%s\t%s\t%s\t%s\n", sampleName, fq1, fq2, sampleType,
                        matchControl));
            } else if (FastqReadType.SINGLE.name().equals(toolFields.readType)) {
                resultStr.append("parameterType\\tshortName\\tParameter\\tsample_type\\tmatch_control\\n");
                String fq = String.format("%s/%s/fastq/%s.fastq.gz", toolFields.getOutdir(), sampleName,
                        sampleName);
                resultStr.append(String.format("fastqFile\t%s\t%s\t%s\t%s\n", sampleName, fq, sampleType,
                        matchControl));
            }
            try {
                Files.newBufferedWriter(Paths.get(fastqPath)).write(resultStr.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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
