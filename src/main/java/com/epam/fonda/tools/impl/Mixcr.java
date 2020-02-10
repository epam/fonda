/*
 * Copyright 2017-2020 Sanofi and EPAM Systems, Inc. (https://www.epam.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import com.epam.fonda.entity.configuration.GlobalConfigFormat;
import com.epam.fonda.entity.configuration.StudyConfigFormat;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.FastqResult;
import com.epam.fonda.tools.results.MixcrOutput;
import com.epam.fonda.tools.results.MixcrResult;
import com.epam.fonda.utils.ToolUtils;
import com.epam.fonda.workflow.TaskContainer;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static com.epam.fonda.utils.ToolUtils.validate;

@RequiredArgsConstructor
public class Mixcr implements Tool<MixcrResult> {

    private static final String MIXCR_TOOL_TEMPLATE_NAME = "mixcr_tool_template";

    @NonNull
    private FastqFileSample sample;
    @NonNull
    private FastqResult fastqResult;

    @Data
    @Builder
    private static class MixcrFields {
        private String mixcr;
        private String mixcrOutdir;
        private String sampleName;
        private String libraryType;
        private String species;
        private String fastq1;
        private String fastq2;
        private String mixcrAlignVdjca;
        private String mixcrContigVdjca;
        private String mixcrAssembly;
        private String mixcrClones;
        private String spe;
        private int nThreads;
    }

    /**
     * This method generates bash script {@link BashCommand} for Mixcr tool.
     *
     * @param configuration  is the type of {@link Configuration} which contains
     *                       its fields: mixcr, species, nThreads.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link BashCommand} with bash script.
     **/
    @Override
    public MixcrResult generate(Configuration configuration, TemplateEngine templateEngine) {
        final String mixcrOutdir = String.format("%s/mixcr", sample.getSampleOutputDir());
        MixcrFields mixcrFields = constructMixcrFields(configuration, mixcrOutdir);
        if (mixcrFields.fastq1 == null) {
            throw new IllegalArgumentException(
                    "Error Step: In mixcr: not fastq files are properly provided, please check!");
        }
        MixcrOutput mixcrOutput = MixcrOutput.builder()
                .mixcrOutdir(mixcrOutdir)
                .mixcrAlignVdjca(mixcrFields.mixcrAlignVdjca)
                .mixcrAssembly(mixcrFields.mixcrAssembly)
                .mixcrClones(mixcrFields.mixcrClones)
                .mixcrContigVdjca(mixcrFields.mixcrContigVdjca)
                .build();
        mixcrOutput.createDirectory();
        Context context = new Context();
        context.setVariable("mixcrFields", mixcrFields);
        final String cmd = templateEngine.process(MIXCR_TOOL_TEMPLATE_NAME, context);
        TaskContainer.addTasks("MIXCR detection");
        return MixcrResult.builder()
                .command(BashCommand.withTool(cmd))
                .fastqResult(fastqResult)
                .mixcrOutput(mixcrOutput)
                .build();
    }

    /**
     * This method initializes fields of the Salmon {@link Mixcr} class.
     *
     * @param configuration is the type of {@link Configuration} which contains
     *                      its fields: mixcr, species, numThreads.
     * @param mixcrOutdir   is the output path
     * @return {@link Mixcr.MixcrFields} with fields.
     **/
    private MixcrFields constructMixcrFields(Configuration configuration, String mixcrOutdir) {
        String species = validate(configuration.getGlobalConfig().getDatabaseConfig().getSpecies(),
                GlobalConfigFormat.SPECIES);
        String sampleName = validate(sample.getName(), ToolUtils.SAMPLE_NAME);
        String spe = "";
        if ("human".equals(species)) {
            spe = "hsa";
        } else if ("mouse".equals(species)) {
            spe = "mmu";
        }
        return MixcrFields.builder()
                .mixcr(validate(configuration.getGlobalConfig().getToolConfig().getMixcr(), GlobalConfigFormat.MIXCR))
                .mixcrOutdir(mixcrOutdir)
                .sampleName(sampleName)
                .libraryType(validate(configuration.getStudyConfig().getLibraryType(), StudyConfigFormat.LIBRARY_TYPE))
                .species(species)
                .fastq1(fastqResult.getOut().getMergedFastq1())
                .fastq2(fastqResult.getOut().getMergedFastq2())
                .mixcrAlignVdjca(String.format("%s/%s.mixcr.alignment.vdjca", mixcrOutdir, sampleName))
                .mixcrContigVdjca(String.format("%s/%s.mixcr.alignment.contig.vdjca",
                        mixcrOutdir, sampleName))
                .mixcrAssembly(String.format("%s/%s.mixcr.clones.clns", mixcrOutdir, sampleName))
                .mixcrClones(String.format("%s/%s.mixcr.clones.txt", mixcrOutdir, sampleName))
                .spe(spe)
                .nThreads(configuration.getGlobalConfig().getQueueParameters().getNumThreads())
                .build();
    }
}
