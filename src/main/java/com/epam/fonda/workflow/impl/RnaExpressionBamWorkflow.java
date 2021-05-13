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
import com.epam.fonda.tools.impl.RnaAnalysis;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.utils.TemplateEngineUtils;
import com.epam.fonda.workflow.BamWorkflow;
import com.epam.fonda.workflow.stage.impl.SecondaryAnalysis;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.epam.fonda.entity.configuration.orchestrator.ScriptType.ALIGNMENT;
import static com.epam.fonda.entity.configuration.orchestrator.ScriptType.POST_PROCESS;
import static com.epam.fonda.entity.configuration.orchestrator.ScriptType.TEMP;
import static com.epam.fonda.utils.PipelineUtils.printShell;

@Slf4j
@RequiredArgsConstructor
public class RnaExpressionBamWorkflow implements BamWorkflow {
    private static final TemplateEngine TEMPLATE_ENGINE = TemplateEngineUtils.init();

    @NonNull
    final Flag flag;
    final ScriptManager scriptManager;

    @Override
    public void run(final Configuration configuration, final BamFileSample sample) throws IOException {
        sample.createDirectory();
        configuration.setCustTask("ExpressionEstimation");
        final BamOutput bamOutput = BamOutput.builder().bam(sample.getBam()).build();
        final BamResult bamResult = BamResult.builder()
                .bamOutput(bamOutput)
                .command(BashCommand.withTool(""))
                .build();

        final String secondaryAnalysis = new SecondaryAnalysis(bamResult, sample.getName(), sample.getSampleOutputDir())
                .process(flag, configuration, TEMPLATE_ENGINE);
        final String custScript = printShell(configuration, secondaryAnalysis, sample.getName(), null);
        if (scriptManager != null) {
            scriptManager.addScript(sample.getName(), ALIGNMENT, custScript);
            scriptManager.addScript(sample.getName(), TEMP, sample.getTmpOutdir());
        }
        log.debug(String.format("Successful Step: the %s sample was processed.", sample.getName()));
    }

    @Override
    public void postProcess(final Configuration configuration, final List<BamFileSample> samples) {
        List<String> sampleNames = samples.stream().map(BamFileSample::getName).collect(Collectors.toList());
        final String rnaScriptsString = new RnaAnalysis(flag, sampleNames).generate(configuration, TEMPLATE_ENGINE);
        if (scriptManager != null) {
            final String[] rnaScripts = rnaScriptsString.split(StringUtils.SPACE);
            Arrays.stream(rnaScripts).forEach(s -> scriptManager.addScript(StringUtils.EMPTY, POST_PROCESS, s));
        }
    }
}
