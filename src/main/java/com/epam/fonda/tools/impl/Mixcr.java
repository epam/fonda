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
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.FastqResult;
import com.epam.fonda.tools.results.MixcrOutput;
import com.epam.fonda.tools.results.MixcrResult;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RequiredArgsConstructor
public class Mixcr implements Tool<MixcrResult> {

    private static final String MIXCR_TOOL_TEMPLATE_NAME = "mixcr_tool_template";

    @NonNull
    private FastqFileSample sample;
    @NonNull
    private FastqResult fastqResult;

    @Data
    private class MixcrFields {
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
        MixcrOutput mixcrOutput = MixcrOutput.builder()
                .mixcrOutdir(mixcrOutdir)
                .mixcrAlignVdjca(mixcrFields.mixcrAlignVdjca)
                .mixcrAssembly(mixcrFields.mixcrAssembly)
                .mixcrClones(mixcrFields.mixcrClones)
                .mixcrContigVdjca(mixcrFields.mixcrContigVdjca)
                .build();
        mixcrOutput.createDirectory();
        if (mixcrFields.fastq1 == null) {
            throw new IllegalArgumentException(
                    "Error Step: In mixcr: not fastq files are properly provided, please check!");
        }
        Context context = new Context();
        context.setVariable("mixcrFields", mixcrFields);
        final String cmd = templateEngine.process(MIXCR_TOOL_TEMPLATE_NAME, context);
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
     * @param mixcrOutdir
     * @return {@link Mixcr.MixcrFields} with fields.
     **/
    private MixcrFields constructMixcrFields(Configuration configuration, String mixcrOutdir) {
        MixcrFields mixcrFields = new MixcrFields();
        mixcrFields.mixcr = configuration.getGlobalConfig().getToolConfig().getMixcr();
        mixcrFields.mixcrOutdir = mixcrOutdir;
        mixcrFields.sampleName = sample.getName();
        mixcrFields.libraryType = configuration.getStudyConfig().getLibraryType();
        mixcrFields.species = configuration.getGlobalConfig().getDatabaseConfig().getSpecies();
        mixcrFields.fastq1 = fastqResult.getOut().getMergedFastq1();
        mixcrFields.fastq2 = fastqResult.getOut().getMergedFastq2();
        mixcrFields.mixcrAlignVdjca = String.format("%s/%s.mixcr.alignment.vdjca", mixcrOutdir, mixcrFields.sampleName);
        mixcrFields.mixcrContigVdjca = String.format("%s/%s.mixcr.alignment.contig.vdjca",
                mixcrOutdir, mixcrFields.sampleName);
        mixcrFields.mixcrAssembly = String.format("%s/%s.mixcr.clones.clns", mixcrOutdir, mixcrFields.sampleName);
        mixcrFields.mixcrClones = String.format("%s/%s.mixcr.clones.txt", mixcrOutdir, mixcrFields.sampleName);
        if (mixcrFields.species.equals("human")) {
            mixcrFields.spe = "hsa";
        } else if (mixcrFields.species.equals("mouse")) {
            mixcrFields.spe = "mmu";
        }
        mixcrFields.nThreads = configuration.getGlobalConfig().getQueueParameters().getNumThreads();
        return mixcrFields;
    }
}
