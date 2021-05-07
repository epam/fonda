/*
 * Copyright 2017-2021 Sanofi and EPAM Systems, Inc. (https://www.epam.com/)
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
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.utils.DnaUtils;
import com.epam.fonda.workflow.TaskContainer;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Arrays;

import static com.epam.fonda.utils.ToolUtils.validate;
import static java.lang.String.format;

@Data
@AllArgsConstructor
public class BwaSort implements Tool<BamResult> {
    private static final String BWA_SORT_TOOL_TEMPLATE_NAME = "bwaSortFields_tool_template";

    private FastqFileSample sample;
    private String fastq1;
    private String fastq2;
    private int index;

    /**
     * This method generates bamResult {@link BamResult} for BwaSort expression.
     *
     * @param configuration  is the type of {@link Configuration}.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link BamResult} with bash script.
     */
    @Override
    public BamResult generate(Configuration configuration, TemplateEngine templateEngine) {
        if (fastq1 == null) {
            throw new IllegalArgumentException(
                    "Error Step: In bwaSort: no fastq files are properly provided, please check!");
        }
        BwaSortFields bwaSortFields = constructFieldsForBwaSort(configuration);
        Context context = new Context();
        context.setVariable("bwaSortFields", bwaSortFields);
        context.setVariable("fastq1", fastq1);
        context.setVariable("fastq2", fastq2);
        final String cmd = templateEngine.process(BWA_SORT_TOOL_TEMPLATE_NAME, context);
        BamOutput bamOutput = BamOutput.builder()
                .sortedBam(bwaSortFields.sortedBam)
                .sortedBamIndex(bwaSortFields.sortedBamIndex)
                .build();
        bamOutput.setBam(StringUtils.isBlank(bamOutput.getBam())
                ? bwaSortFields.sortedBam
                : format("%s,%s", bamOutput.getBam(), bwaSortFields.sortedBam));
        AbstractCommand command = BashCommand.withTool(cmd);
        command.setTempDirs(Arrays.asList(bwaSortFields.sortedBam, bwaSortFields.sortedBamIndex));
        TaskContainer.addTasks("BWA alignment", "Index bam");
        return BamResult.builder()
                .bamOutput(bamOutput)
                .command(command)
                .build();
    }

    private BwaSortFields constructFieldsForBwaSort(Configuration configuration) {
        BwaSortFields bwaSortFields = new BwaSortFields();

        bwaSortFields.bwa = validate(configuration.getGlobalConfig().getToolConfig().getBwa(), GlobalConfigFormat.BWA);
        bwaSortFields.genome = validate(configuration.getGlobalConfig().getDatabaseConfig().getGenome(),
                GlobalConfigFormat.GENOME);
        bwaSortFields.samtools = validate(configuration.getGlobalConfig().getToolConfig().getSamTools(),
                GlobalConfigFormat.SAMTOOLS);
        bwaSortFields.sbamOutDir = sample.getBamOutdir();
        bwaSortFields.numThreads = configuration.getGlobalConfig().getQueueParameters().getNumThreads();
        bwaSortFields.index = index;
        bwaSortFields.fastq1 = fastq1;
        bwaSortFields.fastq2 = fastq2;
        bwaSortFields.sampleName = sample.getName();
        bwaSortFields.tmpBam = format("%s/%s_%d.bwa.sorted",
                bwaSortFields.sbamOutDir, bwaSortFields.sampleName, index);
        bwaSortFields.sortedBam = format("%s.bam", bwaSortFields.tmpBam);
        bwaSortFields.sortedBamIndex = format("%s.bai", bwaSortFields.sortedBam);
        bwaSortFields.rg = DnaUtils.buildRGIdTag(sample.getName(), index);
        return bwaSortFields;
    }

    @Data
    private class BwaSortFields {
        private String bwa;
        private String genome;
        private String samtools;
        private String sbamOutDir;
        private String sampleName;
        private String fastq1;
        private String fastq2;
        private String tmpBam;
        private int index;
        private String sortedBam;
        private String sortedBamIndex;
        private String rg;
        private int numThreads;
    }
}
