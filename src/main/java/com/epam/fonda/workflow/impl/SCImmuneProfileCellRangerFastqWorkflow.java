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

import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.impl.QcSummary;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.utils.TemplateEngineUtils;
import com.epam.fonda.workflow.FastqWorkflow;
import com.epam.fonda.workflow.stage.impl.Alignment;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.epam.fonda.utils.PipelineUtils.cleanUpTmpDir;
import static com.epam.fonda.utils.PipelineUtils.printShell;

@Slf4j
@RequiredArgsConstructor
public class SCImmuneProfileCellRangerFastqWorkflow implements FastqWorkflow {
    private static final TemplateEngine TEMPLATE_ENGINE = TemplateEngineUtils.init();

    @NonNull
    final Flag flag;

    /**
     * this method runs SCImmuneProfileCellRangerFastqWorkflow which launches vdj tool
     * @param configuration of type {@link Configuration} consists config which is needed for this workflow
     * @param sample of type {@link FastqFileSample} consists samples
     * @throws IOException in case if printShell method doesn't work properly
     */
    @Override
    public void run(Configuration configuration, FastqFileSample sample) throws IOException {
        configuration.setCustTask("alignment");
        BamResult bamResult = new Alignment().estimating(flag, sample, configuration, TEMPLATE_ENGINE);

        final String cmd = bamResult.getCommand().getToolCommand() +
                cleanUpTmpDir(bamResult.getCommand().getTempDirs());

        printShell(configuration, cmd, sample.getName(), null);
        log.debug(String.format("Successful step: the %s sample was processed.", sample.getName()));
    }

    /**
     * this method launches QcSummary and RnaAnalusis tools
     * @param configuration of type {@link Configuration} consists config which is needed
     * @param samples consists samples
     * @throws IOException in case if QcSummary doesn't work properly
     */
    @Override
    public void postProcess(Configuration configuration, List<FastqFileSample> samples) throws IOException {
        List<String> sampleNames = samples.stream().map(FastqFileSample::getName).collect(Collectors.toList());
        new QcSummary(flag, sampleNames).generate(configuration, TEMPLATE_ENGINE);
    }
}
