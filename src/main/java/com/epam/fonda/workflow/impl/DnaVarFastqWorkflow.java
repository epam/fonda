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
package com.epam.fonda.workflow.impl;

import com.epam.fonda.entity.command.BashCommand;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.orchestrator.ScriptManager;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.samples.fastq.FastqReadType;
import com.epam.fonda.tools.impl.DnaAnalysis;
import com.epam.fonda.tools.impl.PicardMergeDnaBam;
import com.epam.fonda.tools.impl.QcSummary;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.tools.results.FastqOutput;
import com.epam.fonda.tools.results.FastqResult;
import com.epam.fonda.utils.DnaUtils;
import com.epam.fonda.utils.PipelineUtils;
import com.epam.fonda.utils.TemplateEngineUtils;
import com.epam.fonda.workflow.FastqWorkflow;
import com.epam.fonda.workflow.stage.impl.Alignment;
import com.epam.fonda.workflow.stage.impl.PostAlignment;
import com.epam.fonda.workflow.stage.impl.PreAlignment;
import com.epam.fonda.workflow.stage.impl.SecondaryAnalysis;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.epam.fonda.entity.configuration.orchestrator.ScriptType.ALIGNMENT;
import static com.epam.fonda.entity.configuration.orchestrator.ScriptType.POST_ALIGNMENT;
import static com.epam.fonda.entity.configuration.orchestrator.ScriptType.POST_PROCESS;
import static com.epam.fonda.entity.configuration.orchestrator.ScriptType.TEMP;
import static com.epam.fonda.utils.PipelineUtils.cleanUpTmpDir;
import static com.epam.fonda.utils.PipelineUtils.printShell;

@Slf4j
@RequiredArgsConstructor
public class DnaVarFastqWorkflow implements FastqWorkflow {
    private static final TemplateEngine TEMPLATE_ENGINE = TemplateEngineUtils.init();

    @NonNull
    final Flag flag;
    @NonNull
    private final String stringTag;
    final ScriptManager scriptManager;

    @Override
    public void run(final Configuration configuration, final FastqFileSample sample) throws IOException {
        sample.createDirectory();
        final String readType = configuration.getGlobalConfig().getPipelineInfo().getReadType();
        final boolean isPairedFastq = determineReadType(readType);

        configuration.setCustTask("alignment");
        final List<String> fastqs1 = sample.getFastq1();
        final List<String> fastqs2 = sample.getFastq2();
        final List<String> bamsToMerge = new ArrayList<>();
        for (int i = 0; i < fastqs1.size(); i++) {
            alignFastq(configuration, sample, isPairedFastq, fastqs1, fastqs2, bamsToMerge, i);
        }
        processPostAlignment(configuration, sample, fastqs1, bamsToMerge);
        log.debug(String.format("Successful Step: the %s sample was processed.", sample.getName()));
    }

    @Override
    public void postProcess(final Configuration configuration, final List<FastqFileSample> samples) throws IOException {
        List<String> sampleNames = samples.stream().map(FastqFileSample::getName).collect(Collectors.toList());
        final String qcSummaryScript = new QcSummary(flag, sampleNames).generate(configuration, TEMPLATE_ENGINE);
        final String dnaAnalysisScript = new DnaAnalysis(samples, null, flag)
                .generate(configuration, TEMPLATE_ENGINE);
        if (scriptManager != null) {
            scriptManager.addScript(StringUtils.EMPTY, POST_PROCESS, qcSummaryScript);
            scriptManager.addScript(StringUtils.EMPTY, POST_PROCESS, dnaAnalysisScript);
        }
    }

    private boolean determineReadType(final String readType) {
        if (FastqReadType.PAIRED.name().equalsIgnoreCase(readType)) {
            return true;
        } else if (FastqReadType.SINGLE.name().equalsIgnoreCase(readType)) {
            return false;
        }
        throw new IllegalArgumentException("Error Step: readType and the fastq files are incompatible, please check!");
    }

    private void alignFastq(final Configuration configuration,
                            final FastqFileSample sample,
                            final boolean isPairedFastq,
                            final List<String> fastqs1,
                            final List<String> fastqs2,
                            final List<String> bamsToMerge,
                            final int i) throws IOException {
        final int index = i + 1;
        final FastqOutput fastqOutput = FastqOutput.builder()
                .mergedFastq1(fastqs1.get(i))
                .mergedFastq2(isPairedFastq ? fastqs2.get(i) : null)
                .build();
        FastqResult fastqResult = FastqResult.builder()
                .command(BashCommand.withTool(""))
                .out(fastqOutput)
                .build();
        fastqResult = new PreAlignment(fastqResult, index).process(flag, sample, configuration, TEMPLATE_ENGINE);
        final BamResult bamResult = new Alignment(fastqResult, index)
                .mapping(flag, sample, configuration, TEMPLATE_ENGINE);
        bamsToMerge.add(bamResult.getBamOutput().getBam());
        final String alignScript = printShell(configuration, bamResult.getCommand().getToolCommand(), sample.getName(),
                String.valueOf(index));
        if (scriptManager != null) {
            scriptManager.addScript(sample.getName(), ALIGNMENT, alignScript);
        }
    }

    private void processPostAlignment(final Configuration configuration,
                                      final FastqFileSample sample,
                                      final List<String> fastqs1,
                                      final List<String> bamsToMerge) throws IOException {
        final StringBuilder resultCmd = new StringBuilder();
        resultCmd.append(DnaUtils.periodicIndexBamStatusCheckForFastqList(fastqs1, sample.getName(), null,
                configuration));
        BamResult bamResult = new PicardMergeDnaBam(sample, bamsToMerge).generate(configuration, TEMPLATE_ENGINE);

        configuration.setCustTask("postalignment");
        bamResult = new PostAlignment(bamResult).process(flag, sample, configuration, TEMPLATE_ENGINE);
        resultCmd.append(bamResult.getCommand().getToolCommand());
        BamOutput bamOutput = bamResult.getBamOutput();
        bamOutput.setControlBam(bamOutput.getBam().replace(sample.getName(), sample.getControlName()));
        if (sample.getSampleType().equals(PipelineUtils.CASE) || sample.getSampleType().equals(PipelineUtils.TUMOR)) {
            resultCmd.append(DnaUtils.checkPeriodicBamStatus(stringTag, sample.getName(), sample.getControlName(),
                    configuration, null));
            final boolean isPaired = StringUtils.isNoneBlank(sample.getControlName())
                    && !PipelineUtils.NA.equals(sample.getControlName());
            resultCmd.append(new SecondaryAnalysis(bamResult, sample.getName(), sample.getSampleOutputDir(),
                    sample.getControlName(), isPaired, scriptManager)
                    .process(flag, configuration, TEMPLATE_ENGINE));
        }
        final String command = configuration.isMasterMode()
                ? resultCmd.toString()
                : resultCmd.append(cleanUpTmpDir(bamResult.getCommand().getTempDirs())).toString();
        final String postAlignScript = printShell(configuration, command, sample.getName(), null);

        if (scriptManager != null) {
            scriptManager.addScript(sample.getName(), POST_ALIGNMENT, postAlignScript);
            bamResult.getCommand().getTempDirs().forEach(t -> scriptManager.addScript(sample.getName(), TEMP, t));
        }
    }
}
