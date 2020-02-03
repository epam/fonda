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

import com.epam.fonda.entity.command.BashCommand;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfigFormat;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.tools.results.VdjOutput;
import com.epam.fonda.utils.CellRangerUtils;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static com.epam.fonda.utils.PipelineUtils.TASK_TO_CHECK;
import static com.epam.fonda.utils.ToolUtils.validate;

@RequiredArgsConstructor
public class Vdj implements Tool<BamResult> {

    @Data
    private class VdjFields {
        private String genome;
        private String cellRanger;
        private String forcedCells;
        private String denovo;
        private String lanes;
        private String indices;
        private String bam;
        private int numThreads;
    }

    @Data
    private class SampleFields {
        private String vdjOutdir;
        private String sampleName;
        private String fastqOutdir;
    }

    private static final String VDJ_TOOL_TEMPLATE_NAME = "vdj_tool_template";

    private VdjFields vdjFields;
    private SampleFields sampleFields;

    @NonNull
    private FastqFileSample sample;
    @NonNull
    private BamResult bamResult;

    /**
     * This method generates bash script {@link BashCommand} for Vdj tool.
     *
     * @param configuration  is the type of {@link Configuration} which contains
     *                       its fields: cellranger, cellrangerLanes, cellrangerIndices, cellrangerChain,
     *                       cellrangerDenovo, cellrangerForcedCells, genome.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link BashCommand} with bash script.
     **/
    @Override
    public BamResult generate(Configuration configuration, TemplateEngine templateEngine) {
        initializeVdjFields(configuration);
        Context context = new Context();
        context.setVariable("vdjFields", vdjFields);
        context.setVariable("sampleFields", sampleFields);
        final String cmd = templateEngine.process(VDJ_TOOL_TEMPLATE_NAME, context);
        TASK_TO_CHECK.add("Cellranger vdj analysis");
        VdjOutput vdjOutput = VdjOutput.builder()
                .vdjBamResult(vdjFields.bam)
                .vdjOutdir(sampleFields.vdjOutdir)
                .build();
        vdjOutput.createDirectory();
        bamResult.setCommand(BashCommand.withTool(cmd));
        bamResult.setBamOutput(BamOutput.builder().bam(vdjFields.bam).build());
        return bamResult;
    }

    /**
     * This method initializes fields of the {@link SampleFields} class.
     *
     * @return {@link SampleFields} with its fields.
     **/
    private SampleFields initializeSampleFields(Configuration configuration) {
        sampleFields = new SampleFields();
        sampleFields.vdjOutdir = String.format("%s/vdj", configuration.getCommonOutdir().getRootOutdir());
        sampleFields.sampleName = sample.getName();
        sampleFields.fastqOutdir = String.join(",", CellRangerUtils.extractFastqDir(sample).getFastqDirs());
        return sampleFields;
    }

    /**
     * This method initializes fields of the {@link VdjFields} class.
     *
     * @param configuration is the type of {@link Configuration} which contains
     *                      its fields: cellranger, cellrangerLanes, cellrangerIndices, cellrangerChain,
     *                      cellrangerDenovo, cellrangerForcedCells, genome.
     **/
    private void initializeVdjFields(Configuration configuration) {
        vdjFields = new VdjFields();
        sampleFields = initializeSampleFields(configuration);
        vdjFields.bam = String.format("%s/%s/outs/all_contig.bam", sampleFields.getVdjOutdir(),
                sampleFields.getSampleName());
        vdjFields.cellRanger = validate(configuration.getGlobalConfig().getToolConfig().getCellranger(),
                GlobalConfigFormat.CELLRANGER);
        vdjFields.denovo = configuration.getGlobalConfig().getCellrangerConfig().getCellrangerDenovo();
        vdjFields.forcedCells = validate(
                configuration.getGlobalConfig().getCellrangerConfig().getCellrangerForcedCells(),
                GlobalConfigFormat.CELLRANGER_FORCED_CELLS);
        vdjFields.indices = configuration.getGlobalConfig().getCellrangerConfig().getCellrangerIndices();
        vdjFields.lanes = configuration.getGlobalConfig().getCellrangerConfig().getCellrangerLanes();
        vdjFields.genome = validate(configuration.getGlobalConfig().getDatabaseConfig().getGenome(),
                GlobalConfigFormat.GENOME);
        vdjFields.numThreads = configuration.getGlobalConfig().getQueueParameters().getNumThreads();
    }
}
