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

package com.epam.fonda.tools.impl;

import com.epam.fonda.entity.command.BashCommand;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.tools.results.StarFusionOutput;
import com.epam.fonda.tools.results.StarFusionResult;
import lombok.Data;
import lombok.NonNull;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Data
public class StarFusion implements Tool<StarFusionResult> {
    private static final String STAR_FUSION_TOOL_TEMPLATE_NAME = "star_fusion_tool_template";

    @Data
    private class ToolFields {
        private String star;
        private String java;
        private String picard;
        private String samtools;
    }

    @Data
    private class AdditionalStarFusionFields {
        private String starIndex;
        private String sampleName;
        private int numThreads;
        private String mergedFastq1;
        private String mergedFastq2;
        private String unSortedBam;
        private String unSortedBamIndex;
        private String bamIndex;
        private String juncFile;
        private String starFusion;
        private String starFusionLib;
        private String starFusionOutdir;
        private String bamOutdir;
    }

    @NonNull
    private FastqFileSample sample;
    @NonNull
    private BamResult bamResult;

    /**
     * This method generates bash script {@link BashCommand} for StarFusion tool.
     *
     * @param configuration  is the type of {@link Configuration} from which starFusion and
     *                       starFusionIndex fields are used by StarFusion class.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link BashCommand} with bash script.
     **/
    @Override
    public StarFusionResult generate(Configuration configuration, TemplateEngine templateEngine) {
        if(bamResult.getFastqOutput().getMergedFastq1() == null) {
            throw new IllegalArgumentException(
                    "Error Step: In star: no fastq files are properly provided, please check!");
        }
        AdditionalStarFusionFields additionalStarFusionFields = constructFieldsByIndex(configuration);
        final String star4fusionBam = String.format("%s/%s.star.sorted.bam", sample.getBamOutdir(),
                sample.getName());
        final String starFusionResult = String.format("%s/%s.starFusion.fusion.final.abridged",
                additionalStarFusionFields.starFusionOutdir, sample.getName());
        Context context = new Context();
        context.setVariable("starFusionFields", additionalStarFusionFields);
        context.setVariable("toolFields", initializeToolFields(configuration));
        context.setVariable("bam", star4fusionBam);
        context.setVariable("starFusionResult", starFusionResult);
        String cmd = templateEngine.process(STAR_FUSION_TOOL_TEMPLATE_NAME, context);
        StarFusionOutput starFusionOutput = StarFusionOutput.builder()
                .starFusionResult(starFusionResult)
                .starFusionOutdir(additionalStarFusionFields.starFusionOutdir)
                .build();
        starFusionOutput.createDirectory();
        return StarFusionResult.builder()
                .bamResult(bamResult)
                .command(BashCommand.withTool(cmd))
                .starFusionOutput(starFusionOutput)
                .build();
    }

    /**
     * This method initializes fields of the StarFusion {@link StarFusion} class.
     *
     * @param configuration is the type of {@link Configuration} which contains
     *                      its fields: starFusion, starFusionIndex.
     * @return {@link AdditionalStarFusionFields} with fields.
     **/
    private AdditionalStarFusionFields constructFieldsByIndex(Configuration configuration) {
        AdditionalStarFusionFields starFusionFields = new AdditionalStarFusionFields();
        starFusionFields.starIndex = configuration.getGlobalConfig().getDatabaseConfig().getStarIndex();
        starFusionFields.sampleName = sample.getName();
        starFusionFields.numThreads = configuration.getGlobalConfig().getQueueParameters().getNumThreads();
        starFusionFields.mergedFastq1 = bamResult.getFastqOutput().getMergedFastq1();
        starFusionFields.mergedFastq2 = bamResult.getFastqOutput().getMergedFastq2();
        starFusionFields.bamOutdir = sample.getBamOutdir();
        starFusionFields.unSortedBam = String.format("%s/%s.Aligned.out.bam", starFusionFields.bamOutdir,
                starFusionFields.sampleName);
        starFusionFields.unSortedBamIndex = String.format("%s/%s.Aligned.out.bam.bai", starFusionFields.bamOutdir,
                starFusionFields.sampleName);
        starFusionFields.bamIndex = String.format("%s/%s.star.sorted.bam.bai", starFusionFields.bamOutdir,
                starFusionFields.sampleName);
        starFusionFields.juncFile = String.format("%s/%s.Chimeric.out.junction", starFusionFields.bamOutdir,
                starFusionFields.sampleName);
        starFusionFields.starFusion = configuration.getGlobalConfig().getToolConfig().getStarFusion();
        starFusionFields.starFusionLib = configuration.getGlobalConfig().getDatabaseConfig().getStarFusionLib();
        starFusionFields.starFusionOutdir = String.format("%s/starFusion", sample.getSampleOutputDir());
        return starFusionFields;
    }

    private ToolFields initializeToolFields(Configuration configuration) {
        ToolFields toolFields = new ToolFields();
        toolFields.java = configuration.getGlobalConfig().getToolConfig().getJava();
        toolFields.picard = configuration.getGlobalConfig().getToolConfig().getPicard();
        toolFields.samtools = configuration.getGlobalConfig().getToolConfig().getSamTools();
        toolFields.star = configuration.getGlobalConfig().getToolConfig().getStar();
        return toolFields;
    }
}
