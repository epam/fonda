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
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.FastqOutput;
import com.epam.fonda.tools.results.StarFusionOutput;
import com.epam.fonda.tools.results.StarFusionResult;
import lombok.Data;
import lombok.NonNull;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Arrays;

import static com.epam.fonda.utils.PipelineUtils.TASK_TO_CHECK;
import static com.epam.fonda.utils.ToolUtils.validate;

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
    private FastqOutput fastqOutput;

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
        if (fastqOutput.getMergedFastq1() == null) {
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
        TASK_TO_CHECK.addAll(Arrays.asList("STAR4FUSION alignment", "Sort bam", "Index bam", "STAR-Fusion detection"));
        StarFusionOutput starFusionOutput = StarFusionOutput.builder()
                .starFusionResult(starFusionResult)
                .starFusionOutdir(additionalStarFusionFields.starFusionOutdir)
                .build();
        starFusionOutput.createDirectory();
        AbstractCommand command = BashCommand.withTool(cmd);
        command.setTempDirs(Arrays.asList(additionalStarFusionFields.unSortedBam,
                additionalStarFusionFields.unSortedBamIndex,
                additionalStarFusionFields.bamIndex, star4fusionBam));
        return StarFusionResult.builder()
                .command(command)
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
        starFusionFields.starIndex = validate(configuration.getGlobalConfig().getDatabaseConfig().getStarIndex(),
                GlobalConfigFormat.STARINDEX);
        starFusionFields.sampleName = sample.getName();
        starFusionFields.numThreads = configuration.getGlobalConfig().getQueueParameters().getNumThreads();
        starFusionFields.mergedFastq1 = fastqOutput.getMergedFastq1();
        starFusionFields.mergedFastq2 = fastqOutput.getMergedFastq2();
        starFusionFields.bamOutdir = sample.getBamOutdir();
        starFusionFields.unSortedBam = String.format("%s/%s.Aligned.out.bam", starFusionFields.bamOutdir,
                starFusionFields.sampleName);
        starFusionFields.unSortedBamIndex = String.format("%s/%s.Aligned.out.bam.bai", starFusionFields.bamOutdir,
                starFusionFields.sampleName);
        starFusionFields.bamIndex = String.format("%s/%s.star.sorted.bam.bai", starFusionFields.bamOutdir,
                starFusionFields.sampleName);
        starFusionFields.juncFile = String.format("%s/%s.Chimeric.out.junction", starFusionFields.bamOutdir,
                starFusionFields.sampleName);
        starFusionFields.starFusion = validate(configuration.getGlobalConfig().getToolConfig().getStarFusion(),
                GlobalConfigFormat.STAR_FUSION);
        starFusionFields.starFusionLib = validate(
                configuration.getGlobalConfig().getDatabaseConfig().getStarFusionLib(),
                GlobalConfigFormat.STAR_FUSION_LIB);
        starFusionFields.starFusionOutdir = String.format("%s/starFusion", sample.getSampleOutputDir());
        return starFusionFields;
    }

    private ToolFields initializeToolFields(Configuration configuration) {
        ToolFields toolFields = new ToolFields();
        toolFields.java = validate(configuration.getGlobalConfig().getToolConfig().getJava(), GlobalConfigFormat.JAVA);
        toolFields.picard = validate(configuration.getGlobalConfig().getToolConfig().getPicard(),
                GlobalConfigFormat.PICARD);
        toolFields.samtools = validate(configuration.getGlobalConfig().getToolConfig().getSamTools(),
                GlobalConfigFormat.SAMTOOLS);
        toolFields.star = validate(configuration.getGlobalConfig().getToolConfig().getStar(), GlobalConfigFormat.STAR);
        return toolFields;
    }
}
