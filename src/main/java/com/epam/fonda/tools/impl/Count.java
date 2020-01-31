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
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.utils.CellRangerUtils;
import com.epam.fonda.utils.PipelineUtils;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Arrays;

import static com.epam.fonda.utils.PipelineUtils.TASK_TO_CHECK;

@Slf4j
@RequiredArgsConstructor
public class Count implements Tool<BamResult> {
    private static final String COUNT_TEMPLATE = "count_template";
    private static final String CELLRANGER_OUTPUT_FOLDER = "outs";

    @NonNull
    private FastqFileSample sample;
    @NonNull
    private BamResult bamResult;

    @Data
    private class CountFields {
        private String jarPath;
        private String cellRanger;
        private String rScript;
        private String sampleName;
        private String fastqDir;
        private String transcriptome;
        private String countOutdir;
        private String genomeBuild;
        private String expectedCells;
        private String forcedCells;
        private String nosecondary;
        private String chemistry;
        private String r1Length;
        private String r2Length;
        private String lanes;
        private String indices;
        private String bam;
        private String matrixInfo;
        private int numThreads;
    }

    @Override
    public BamResult generate(Configuration configuration, TemplateEngine templateEngine) {
        CountFields countFields = constructFields(configuration);
        Context context = new Context();
        context.setVariable("countFields", countFields);
        final String cmd = templateEngine.process(COUNT_TEMPLATE, context);
        TASK_TO_CHECK.addAll(Arrays.asList("Cellranger count", "Generate gene-barcode matrix"));
        if (countFields.genomeBuild.split("\\s*,\\s*").length == 2) {
            TASK_TO_CHECK.add("Merge gene-barcode matrix");
        }
        bamResult.setCommand(BashCommand.withTool(cmd));
        bamResult.setBamOutput(BamOutput.builder().bam(countFields.bam).build());
        return bamResult;
    }

    /**
     * Method sets all needed fields in {@link CountFields} for next processing.
     *
     * @param configuration is the type of {@link Configuration} which contains its fields: workflow, outdir,
     *                      logOutdir, rScript, fastqList, bamList.
     * @return type of {@link CountFields} with all set fields
     */
    private CountFields constructFields(Configuration configuration) {
        checkValues(configuration);
        CountFields countFields = new CountFields();
        countFields.jarPath = PipelineUtils.getExecutionPath();
        countFields.cellRanger = configuration.getGlobalConfig().getToolConfig().getCellranger();
        countFields.rScript = configuration.getGlobalConfig().getToolConfig().getRScript();
        countFields.sampleName = sample.getName();
        countFields.fastqDir = String.join(",", CellRangerUtils.extractFastqDir(sample).getFastqDirs());
        countFields.transcriptome = configuration.getGlobalConfig().getDatabaseConfig().getTranscriptome();
        countFields.countOutdir = String.format("%s/%s", configuration.getCommonOutdir().getRootOutdir(), "count");
        PipelineUtils.createDir(countFields.countOutdir);
        countFields.genomeBuild = configuration.getGlobalConfig().getDatabaseConfig().getGenomeBuild();
        countFields.expectedCells = configuration.getGlobalConfig().getCellrangerConfig().getCellrangerExpectedCells();
        countFields.forcedCells = configuration.getGlobalConfig().getCellrangerConfig().getCellrangerForcedCells();
        countFields.nosecondary = configuration.getGlobalConfig().getCellrangerConfig().getCellrangerNosecondary();
        countFields.chemistry = configuration.getGlobalConfig().getCellrangerConfig().getCellrangerChemistry();
        countFields.r1Length = configuration.getGlobalConfig().getCellrangerConfig().getCellrangerR1Length();
        countFields.r2Length = configuration.getGlobalConfig().getCellrangerConfig().getCellrangerR2Length();
        countFields.lanes = configuration.getGlobalConfig().getCellrangerConfig().getCellrangerLanes();
        countFields.indices = configuration.getGlobalConfig().getCellrangerConfig().getCellrangerIndices();
        String samplePath = String.format("%s/%s", countFields.countOutdir, countFields.sampleName);
        countFields.bam = String.format("%s/%s/possorted_genome_bam.bam", samplePath, CELLRANGER_OUTPUT_FOLDER);
        countFields.matrixInfo = String.format("%s/%s/filtered_feature_bc_matrix", samplePath,
                CELLRANGER_OUTPUT_FOLDER);
        countFields.numThreads = configuration.getGlobalConfig().getQueueParameters().getNumThreads();
        return countFields;
    }

    /**
     * Method checks all needed fields of configuration for null/empty values
     *
     * @param configuration is the type of {@link Configuration} which contains its fields: workflow, outdir,
     *      *                           logOutdir, rScript, fastqList, bamList.
     */
    private void checkValues(Configuration configuration) {
        GlobalConfig.CellrangerConfig cellrangerConfig = configuration.getGlobalConfig().getCellrangerConfig();
        Validate.notBlank(configuration.getGlobalConfig().getToolConfig().getCellranger(),
                "CellRanger is not specified");
        Validate.notBlank(configuration.getGlobalConfig().getToolConfig().getRScript(),
                "RScript is not specified");
        Validate.notBlank(configuration.getGlobalConfig().getDatabaseConfig().getTranscriptome(),
                "Transcriptome is not specified");
        Validate.notBlank(configuration.getGlobalConfig().getDatabaseConfig().getGenomeBuild(),
                "Genome build configuration is not specified");
        Validate.notBlank(cellrangerConfig.getCellrangerChemistry(), "CellRanger chemistry is not specified");
        Validate.notBlank(cellrangerConfig.getCellrangerExpectedCells(),
                "CellRanger expected is not specified");
        Validate.notBlank(cellrangerConfig.getCellrangerForcedCells(),
                "CellRanger forced cells is not specified");
        Validate.notBlank(cellrangerConfig.getCellrangerIndices(), "CellRanger indices is not specified");
        Validate.notBlank(cellrangerConfig.getCellrangerLanes(), "CellRanger lanes is not specified");
        Validate.notBlank(cellrangerConfig.getCellrangerNosecondary(),
                "CellRanger nosecondary field is not specified");
        Validate.notBlank(cellrangerConfig.getCellrangerR1Length(), "CellRanger r1 length is not specified");
        Validate.notBlank(cellrangerConfig.getCellrangerR2Length(), "CellRanger r2 length is not specified");
    }
}
