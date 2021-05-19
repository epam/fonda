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
import com.epam.fonda.samples.bam.BamFileSample;
import com.epam.fonda.tools.impl.DnaAnalysis;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.utils.DnaUtils;
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

import static com.epam.fonda.entity.configuration.orchestrator.ScriptType.ALIGNMENT;
import static com.epam.fonda.entity.configuration.orchestrator.ScriptType.POST_PROCESS;
import static com.epam.fonda.entity.configuration.orchestrator.ScriptType.TEMP;
import static com.epam.fonda.utils.PipelineUtils.cleanUpTmpDir;
import static com.epam.fonda.utils.PipelineUtils.isPaired;
import static com.epam.fonda.utils.PipelineUtils.printShell;

@Slf4j
@RequiredArgsConstructor
public class DnaVarBamWorkflow implements BamWorkflow {

    private static final TemplateEngine TEMPLATE_ENGINE = TemplateEngineUtils.init();

    @NonNull
    final Flag flag;
    final ScriptManager scriptManager;

    @Override
    public void run(final Configuration configuration, final BamFileSample sample) throws IOException {
        if (DnaUtils.isNotCaseOrTumor(sample.getSampleType())) {
            return;
        }
        sample.createDirectory();
        configuration.setCustTask("variantDetection");
        final BamResult bamResult = buildBamResult(sample);

        final String cmd = new SecondaryAnalysis(bamResult, sample.getName(), sample.getSampleOutputDir(),
                sample.getControlName(), isPaired(sample.getControlName()), scriptManager)
                .process(flag, configuration, TEMPLATE_ENGINE);
        final String resultCmd = configuration.isMasterMode()
                ? cmd
                : cmd + cleanUpTmpDir(bamResult.getCommand().getTempDirs());
        final String custScript = configuration.isMasterMode() && StringUtils.isBlank(cmd)
                ? StringUtils.EMPTY
                : printShell(configuration, resultCmd, sample.getName(), null);
        if (scriptManager != null) {
            scriptManager.addScript(sample.getName(), ALIGNMENT, custScript);
            bamResult.getCommand().getTempDirs().forEach(t -> scriptManager.addScript(sample.getName(), TEMP, t));
        }

        log.debug(String.format("Successful Step: the %s sample was processed.", sample.getName()));
    }

    @Override
    public void postProcess(final Configuration configuration, final List<BamFileSample> samples) {
        final List<BamFileSample> samplesWithCheckedTypes = samples.stream()
                .filter(s -> !DnaUtils.isNotCaseOrTumor(s.getSampleType())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(samplesWithCheckedTypes)) {
            return;
        }
        final String dnaScript = new DnaAnalysis(null, samplesWithCheckedTypes, flag)
                .generate(configuration, TEMPLATE_ENGINE);
        if (scriptManager != null) {
            scriptManager.addScript(StringUtils.EMPTY, POST_PROCESS, dnaScript);
        }
    }

    private BamResult buildBamResult(final BamFileSample sample) {
        final BamOutput bamOutput = BamOutput.builder()
                .bam(sample.getBam())
                .controlBam(sample.getControlBam())
                .build();
        return BamResult.builder()
                .bamOutput(bamOutput)
                .command(BashCommand.withTool(StringUtils.EMPTY))
                .build();
    }
}
