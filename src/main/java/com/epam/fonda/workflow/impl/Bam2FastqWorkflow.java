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

import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.orchestrator.ScriptManager;
import com.epam.fonda.samples.bam.BamFileSample;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.impl.FastqListAnalysis;
import com.epam.fonda.tools.impl.SamToFastq;
import com.epam.fonda.tools.impl.SortBamByReadName;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.tools.results.FastqResult;
import com.epam.fonda.utils.PipelineUtils;
import com.epam.fonda.utils.TemplateEngineUtils;
import com.epam.fonda.workflow.BamWorkflow;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.epam.fonda.entity.configuration.orchestrator.ScriptType.ALIGNMENT;
import static com.epam.fonda.entity.configuration.orchestrator.ScriptType.TEMP;
import static com.epam.fonda.utils.PipelineUtils.cleanUpTmpDir;
import static com.epam.fonda.utils.PipelineUtils.printShell;

@Slf4j
@RequiredArgsConstructor
public class Bam2FastqWorkflow implements BamWorkflow {
    private static final TemplateEngine TEMPLATE_ENGINE = TemplateEngineUtils.init();

    @NonNull
    final Flag flag;
    final ScriptManager scriptManager;

    @Override
    public void run(Configuration configuration, BamFileSample sample) throws IOException {
        if (!PipelineUtils.checkSampleType(sample.getSampleType())) {
            return;
        }
        String fastqOutdir = sample.getSampleOutputDir() + "/fastq";
        sample.createDirectory();
        PipelineUtils.createDir(fastqOutdir);
        configuration.setCustTask("convert");
        BamResult bamResult = new SortBamByReadName(fastqOutdir, sample)
                .generate(configuration, TEMPLATE_ENGINE);
        final StringBuilder cmd = new StringBuilder(bamResult.getCommand().getToolCommand());
        if (flag.isPicard()) {
            FastqResult fastqResult = new SamToFastq(sample.getName(), fastqOutdir, bamResult)
                    .generate(configuration, TEMPLATE_ENGINE);
            cmd.append(fastqResult.getCommand().getToolCommand());
        }
        Collections.addAll(bamResult.getCommand().getTempDirs(), sample.getTmpOutdir());

        final String command = configuration.isMasterMode()
                ? cmd.toString()
                : cmd.append(cleanUpTmpDir(bamResult.getCommand().getTempDirs())).toString();
        final String custScript = printShell(configuration, command, sample.getName(), null);
        if (scriptManager != null) {
            scriptManager.addScript(sample.getName(), ALIGNMENT, custScript);
            bamResult.getCommand().getTempDirs().forEach(t -> scriptManager.addScript(sample.getName(), TEMP, t));
        }
        log.debug(String.format("Successful Step: the %s sample was processed.", sample.getName()));
    }

    @Override
    public void postProcess(Configuration configuration, List<BamFileSample> samples) throws IOException {
        List<FastqFileSample> fastqFileSamples = samples.stream()
                .map(s -> FastqFileSample.builder()
                            .name(s.getName())
                            .sampleType(s.getSampleType())
                            .matchControl(s.getMatchControl())
                            .build())
                .collect(Collectors.toList());
        new FastqListAnalysis(fastqFileSamples).generate(configuration, TEMPLATE_ENGINE);
    }
}
