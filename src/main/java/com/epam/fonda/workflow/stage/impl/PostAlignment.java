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

package com.epam.fonda.workflow.stage.impl;

import com.epam.fonda.entity.command.AbstractCommand;
import com.epam.fonda.entity.command.BashCommand;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.impl.AbraRealign;
import com.epam.fonda.tools.impl.AmpliconGatkRealign;
import com.epam.fonda.tools.impl.AmpliconGatkRecalibrate;
import com.epam.fonda.tools.impl.DnaPicardQc;
import com.epam.fonda.tools.impl.GatkSplitReads;
import com.epam.fonda.tools.impl.PicardMarkDuplicate;
import com.epam.fonda.tools.impl.PicardRemoveDuplicate;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.tools.results.MetricsOutput;
import com.epam.fonda.tools.results.MetricsResult;
import com.epam.fonda.workflow.PipelineType;
import com.epam.fonda.workflow.impl.Flag;
import com.epam.fonda.workflow.stage.Stage;
import lombok.AllArgsConstructor;
import org.thymeleaf.TemplateEngine;

import static com.epam.fonda.utils.DnaUtils.isWgsWorkflow;

/**
 * The workflow stage is followed by the Alignment stage.
 * Consists of list of tools that require {@link BamResult}.
 */
@AllArgsConstructor
public class PostAlignment implements Stage {
    private BamResult bamResult;

    /**
     * Method consists of list of tools that can be invoked on after alignment stage
     * to improve on the alignments of the reads.
     *
     * @param flag           is the type of {@link Flag} that indicates whether tool was set in configuration
     * @param configuration  is the type of {@link Configuration} which contains its fields: workflow, outdir,
     *                       logOutdir, rScript, fastqList, bamList.
     * @param templateEngine an instance of {@link TemplateEngine} to process multiple template
     * @return {@link BamResult} which presents paths to bam, index of bam, sorted bam, etc
     */
    public BamResult process(final Flag flag, final FastqFileSample sample, final Configuration configuration,
                             final TemplateEngine templateEngine) {
        if (flag.isPicard()) {
            bamResult = new PicardMarkDuplicate(sample, bamResult).generate(configuration, templateEngine);
            if (isCapture(configuration) || isWgsWorkflow(configuration)) {
                bamResult = new PicardRemoveDuplicate(bamResult).generate(configuration, templateEngine);
            }
            if (flag.isQc()) {
                MetricsResult metricsResult = MetricsResult.builder()
                        .bamOutput(bamResult.getBamOutput())
                        .metricsOutput(MetricsOutput.builder().build())
                        .command(BashCommand.withTool(""))
                        .build();
                metricsResult = new DnaPicardQc(sample, metricsResult).generate(configuration, templateEngine);
                final AbstractCommand command = bamResult.getCommand();
                final AbstractCommand metricsCommand = metricsResult.getCommand();
                command.setToolCommand(bamResult.getCommand().getToolCommand()
                        + metricsCommand.getToolCommand());
                command.getTempDirs().addAll(metricsCommand.getTempDirs());
                bamResult.setCommand(command);
            }
        }
        if (PipelineType.RNA_CAPTURE_VAR_FASTQ.getName()
                .equalsIgnoreCase(configuration.getGlobalConfig().getPipelineInfo().getWorkflow())) {
            bamResult = new GatkSplitReads(sample.getSampleOutputDir(),
                    bamResult).generate(configuration, templateEngine);
        }
        if (flag.isAbraRealign() && !flag.isMutect2()) {
            bamResult = new AbraRealign(sample, bamResult).generate(configuration, templateEngine);
        }
        if (flag.isGatkRealign() && !flag.isMutect2()) {
            bamResult = new AmpliconGatkRealign(sample, bamResult).generate(configuration, templateEngine);
        }
        bamResult = new AmpliconGatkRecalibrate(sample.getTmpOutdir(), bamResult)
                .generate(configuration, templateEngine);
        return bamResult;
    }

    private boolean isCapture(final Configuration configuration) {
        return configuration.getGlobalConfig().getPipelineInfo().getWorkflow().toLowerCase().contains("capture");
    }
}
