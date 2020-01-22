/*
 * Copyright 2017-2020 Sanofi and EPAM Systems, Inc. (https://www.epam.com/)
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

import static com.epam.fonda.utils.PipelineUtils.cleanUpTmpDir;
import static com.epam.fonda.utils.PipelineUtils.printShell;

@Slf4j
@RequiredArgsConstructor
public class Bam2FastqWorkflow implements BamWorkflow {
    private static final TemplateEngine TEMPLATE_ENGINE = TemplateEngineUtils.init();

    @NonNull
    final Flag flag;

    @Override
    public void run(Configuration configuration, BamFileSample sample) throws IOException {
        if (!PipelineUtils.checkSampleType(sample.getSampleType())) {
            return;
        }
        sample.createDirectory();
        PipelineUtils.createDir(sample.getSampleOutputDir() + "/fastq");
        configuration.setCustTask("convert");
        BamResult bamResult = new SortBamByReadName(sample.getSampleOutputDir(), sample)
                .generate(configuration, TEMPLATE_ENGINE);
        final StringBuilder cmd = new StringBuilder(bamResult.getCommand().getToolCommand());
        if (flag.isPicard()) {
            FastqResult fastqResult = new SamToFastq(sample.getName(), sample.getSampleOutputDir(), bamResult)
                    .generate(configuration, TEMPLATE_ENGINE);
            cmd.append(fastqResult.getCommand().getToolCommand());
        }
        cmd.append(cleanUpTmpDir(Collections.singletonList(sample.getTmpOutdir())));
        printShell(configuration, cmd.toString(), sample.getName(), null);
        log.debug(String.format("Successful step: the %s sample was processed.", sample.getName()));
    }

    @Override
    public void postProcess(Configuration configuration, List<BamFileSample> samples) {
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
