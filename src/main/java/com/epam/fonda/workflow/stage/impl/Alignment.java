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

package com.epam.fonda.workflow.stage.impl;

import com.epam.fonda.entity.command.AbstractCommand;
import com.epam.fonda.entity.command.BashCommand;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.impl.BwaSort;
import com.epam.fonda.tools.impl.Count;
import com.epam.fonda.tools.impl.Hisat2;
import com.epam.fonda.tools.impl.NovoalignSort;
import com.epam.fonda.tools.impl.PicardMarkDuplicate;
import com.epam.fonda.tools.impl.PicardRemoveDuplicate;
import com.epam.fonda.tools.impl.RNASeQC;
import com.epam.fonda.tools.impl.SCRNASeqDoubletDetection;
import com.epam.fonda.tools.impl.Salmon;
import com.epam.fonda.tools.impl.Star;
import com.epam.fonda.tools.impl.StarFusion;
import com.epam.fonda.tools.impl.Vdj;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.tools.results.FastqResult;
import com.epam.fonda.tools.results.MetricsOutput;
import com.epam.fonda.tools.results.MetricsResult;
import com.epam.fonda.tools.results.SalmonResult;
import com.epam.fonda.tools.results.StarFusionResult;
import com.epam.fonda.workflow.impl.Flag;
import com.epam.fonda.workflow.stage.Stage;
import org.thymeleaf.TemplateEngine;

/**
 * The second stage of workflow.
 * Consists of list of tools that require {@link FastqResult} and
 * reproduce {@link BamResult} and {@link MetricsResult} as a result.
 */
public class Alignment implements Stage {

    private FastqResult fastqResult;
    private int index;
    private BamResult bamResult;
    private MetricsResult metricsResult;

    public Alignment() {
        this.bamResult = BamResult.builder()
                .bamOutput(BamOutput.builder().build())
                .command(BashCommand.withTool(""))
                .build();
    }

    public Alignment(final FastqResult fastqResult) {
        this(fastqResult, 0);
    }

    public Alignment(final FastqResult fastqResult, final int index) {
        this.fastqResult = fastqResult;
        this.index = index;
        this.bamResult = BamResult.builder()
                .bamOutput(BamOutput.builder().build())
                .command(BashCommand.withTool(""))
                .build();
        this.metricsResult = MetricsResult.builder()
                .bamOutput(bamResult.getBamOutput())
                .metricsOutput(MetricsOutput.builder().build())
                .command(BashCommand.withTool(""))
                .build();
    }

    /**
     * Method consists of list of tools that can be invoked on mapping reads process.
     * @param flag is the type of {@link Flag} that indicates whether tool was set in configuration
     * @param sample is the type of {@link FastqFileSample} which contains fastq lists.
     * @param configuration is the type of {@link Configuration} which contains its fields: workflow, outdir,
     *                      logOutdir, rScript, fastqList, bamList.
     * @param templateEngine an instance of {@link TemplateEngine} to process multiple template
     * @return {@link BamResult} which presents paths to bam, index of bam, sorted bam, etc
     */
    public BamResult mapping(final Flag flag, final FastqFileSample sample, final Configuration configuration,
                             final TemplateEngine templateEngine) {
        if (flag.isStar()) {
            bamResult = new Star(flag, sample, fastqResult.getOut()).generate(configuration, templateEngine);
            if (!flag.isRsem()) {
                bamResult = markDuplicate(flag, sample, configuration, templateEngine);
                qcCheck(flag, sample, configuration, templateEngine);
            }
        } else if (flag.isHisat2()) {
            bamResult = new Hisat2(sample, fastqResult.getOut()).generate(configuration, templateEngine);
            bamResult = markDuplicate(flag, sample, configuration, templateEngine);
            qcCheck(flag, sample, configuration, templateEngine);
        } else if (flag.isSalmon()) {
            SalmonResult salmonResult = new Salmon(sample, fastqResult).generate(configuration, templateEngine);
            bamResult.setCommand(mergeCommands(salmonResult.getCommand()));
            return bamResult;
        } else if (flag.isStarFusion()) {
            StarFusionResult starFusionResult = new StarFusion(sample, fastqResult.getOut())
                    .generate(configuration, templateEngine);
            bamResult.setCommand(mergeCommands(starFusionResult.getCommand()));
            return bamResult;
        } else if (flag.isBwa()) {
            bamResult = new BwaSort(sample, fastqResult.getOut().getMergedFastq1(),
                    fastqResult.getOut().getMergedFastq2(), index).generate(configuration, templateEngine);
        } else if (flag.isNovoalign()) {
            bamResult = new NovoalignSort(sample, fastqResult.getOut().getMergedFastq1(),
                    fastqResult.getOut().getMergedFastq2(), index).generate(configuration, templateEngine);
        }

        mergeCommands(bamResult.getCommand());
        return bamResult;
    }

    /**
     * Method consists of list of tools that can be invoked on identifying doublets in single-cell RNA-seq data process.
     * @param flag is the type of {@link Flag} that indicates whether tool was set in configuration
     * @param sample is the type of {@link FastqFileSample} which contains fastq lists.
     * @param configuration is the type of {@link Configuration} which contains its fields: workflow, outdir,
     *                      logOutdir, rScript, fastqList, bamList.
     * @param templateEngine an instance of {@link TemplateEngine} to process multiple template
     * @return {@link BamResult} which presents paths to bam, index of bam, sorted bam, etc
     */
    public BamResult estimating(final Flag flag, final FastqFileSample sample, final Configuration configuration,
                                final TemplateEngine templateEngine) {
        String result = "";
        if (flag.isCount()) {
            bamResult = new Count(sample, bamResult).generate(configuration, templateEngine);
        }
        if (flag.isDoubletDetection() || flag.isScrublet()) {
            result = new SCRNASeqDoubletDetection(sample).generate(configuration, templateEngine);
        }
        if (flag.isVdj()) {
            bamResult = new Vdj(sample, bamResult).generate(configuration, templateEngine);
        }

        AbstractCommand command = bamResult.getCommand();
        command.setToolCommand(command.getToolCommand() + result);
        return bamResult;
    }

    private AbstractCommand mergeCommands(final AbstractCommand toolCommand) {
        final AbstractCommand fastqCommand = fastqResult.getCommand();
        final AbstractCommand metricsCommand = metricsResult.getCommand();
        toolCommand.setToolCommand(fastqCommand.getToolCommand() + toolCommand.getToolCommand()
                + metricsCommand.getToolCommand());
        toolCommand.getTempDirs().addAll(fastqCommand.getTempDirs());
        toolCommand.getTempDirs().addAll(metricsCommand.getTempDirs());
        return toolCommand;
    }

    private BamResult markDuplicate(final Flag flag, final FastqFileSample sample, final Configuration configuration,
                                    final TemplateEngine templateEngine) {
        bamResult = new PicardMarkDuplicate(sample, bamResult).generate(configuration, templateEngine);
        if (flag.isRmdup()) {
            bamResult = new PicardRemoveDuplicate(bamResult).generate(configuration, templateEngine);
        }
        return bamResult;
    }

    private void qcCheck(final Flag flag, final FastqFileSample sample, final Configuration configuration,
                         final TemplateEngine templateEngine) {
        if (flag.isRnaSeQC()) {
            metricsResult = new RNASeQC(sample, bamResult.getBamOutput()).generate(configuration, templateEngine);
        }
    }
}
