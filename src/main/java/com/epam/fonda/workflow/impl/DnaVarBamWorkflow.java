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
package com.epam.fonda.workflow.impl;

import com.epam.fonda.entity.command.BashCommand;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.samples.bam.BamFileSample;
import com.epam.fonda.tools.impl.DnaAnalysis;
import com.epam.fonda.tools.impl.QcSummary;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.utils.PipelineUtils;
import com.epam.fonda.utils.TemplateEngineUtils;
import com.epam.fonda.workflow.BamWorkflow;
import com.epam.fonda.workflow.stage.impl.SecondaryAnalysis;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.epam.fonda.utils.PipelineUtils.cleanUpTmpDir;
import static com.epam.fonda.utils.PipelineUtils.printShell;

@Slf4j
@RequiredArgsConstructor
public class DnaVarBamWorkflow implements BamWorkflow {

    private static final TemplateEngine TEMPLATE_ENGINE = TemplateEngineUtils.init();

    @NonNull
    final Flag flag;

    @Override
    public void run(final Configuration configuration, final BamFileSample sample) throws IOException {
        if (isNotCaseOrTumor(sample.getSampleType())) {
            return;
        }
        sample.createDirectory();
        configuration.setCustTask("variantDetection");
        final boolean isPaired = StringUtils.isNoneBlank(sample.getControlName())
                && !PipelineUtils.NA.equals(sample.getControlName());
        final BamResult bamResult = buildBamResult(sample);

        final String resultCmd = new SecondaryAnalysis(bamResult, sample.getName(), sample.getSampleOutputDir(),
                sample.getControlName(), isPaired).process(flag, configuration, TEMPLATE_ENGINE) +
                cleanUpTmpDir(bamResult.getCommand().getTempDirs());
        printShell(configuration, resultCmd, sample.getName(), null);

        log.debug(String.format("Successful step: the %s sample was processed.", sample.getName()));
    }

    @Override
    public void postProcess(final Configuration configuration, final List<BamFileSample> samples) throws IOException {
        List<String> sampleNames = samples.stream()
                .filter(s -> !isNotCaseOrTumor(s.getSampleType()))
                .map(BamFileSample::getName)
                .collect(Collectors.toList());
        new QcSummary(flag, sampleNames).generate(configuration, TEMPLATE_ENGINE);
        final List<BamFileSample> samplesWithCheckedTypes = samples.stream()
                .filter(s -> !isNotCaseOrTumor(s.getSampleType())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(samplesWithCheckedTypes)) {
            return;
        }
        new DnaAnalysis(null, samplesWithCheckedTypes, flag).generate(configuration, TEMPLATE_ENGINE);
    }

    private BamResult buildBamResult(final BamFileSample sample) {
        final BamOutput bamOutput = BamOutput.builder()
                .bam(sample.getBam())
                .controlBam(sample.getControlBam())
                .build();
        final BashCommand command = BashCommand.withTool("");
        command.getTempDirs().add(sample.getTmpOutdir());
        return BamResult.builder()
                .bamOutput(bamOutput)
                .command(command)
                .build();
    }

    private boolean isNotCaseOrTumor(final String sampleType) {
        return !sampleType.equals(PipelineUtils.CASE) && !sampleType.equals(PipelineUtils.TUMOR);
    }
}
