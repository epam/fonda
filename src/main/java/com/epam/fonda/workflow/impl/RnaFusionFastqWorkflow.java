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
import com.epam.fonda.entity.configuration.orchestrator.ScriptManager;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.impl.FusionCatcher;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.tools.results.FastqResult;
import com.epam.fonda.tools.results.FusionCatcherResult;
import com.epam.fonda.utils.PipelineUtils;
import com.epam.fonda.utils.TemplateEngineUtils;
import com.epam.fonda.workflow.FastqWorkflow;
import com.epam.fonda.workflow.stage.impl.Alignment;
import com.epam.fonda.workflow.stage.impl.PreAlignment;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static com.epam.fonda.entity.configuration.orchestrator.ScriptType.ALIGNMENT;
import static com.epam.fonda.entity.configuration.orchestrator.ScriptType.TEMP;
import static com.epam.fonda.utils.PipelineUtils.cleanUpTmpDir;
import static com.epam.fonda.utils.PipelineUtils.printShell;

@Slf4j
@RequiredArgsConstructor
public class RnaFusionFastqWorkflow implements FastqWorkflow {
    private static final TemplateEngine TEMPLATE_ENGINE = TemplateEngineUtils.init();

    @NonNull
    final Flag flag;
    final ScriptManager scriptManager;

    /**
     * this method runs RnaFusionFastqWorkflow which launches xenom, seqpurge, trimmomatic,
     * starFusion and fusionCatcher tools
     * @param configuration of type {@link Configuration} consists config which is needed for this workflow
     * @param sample        of type {@link FastqFileSample} consists samples
     * @throws IOException in case if printShell method doesn't work properly
     */
    @Override
    public void run(Configuration configuration, FastqFileSample sample) throws IOException {
        sample.createDirectory();
        configuration.setCustTask("fusion");
        FastqResult fastqResult = PipelineUtils.mergeFastq(sample);
        fastqResult = new PreAlignment(fastqResult).process(flag, sample, configuration, TEMPLATE_ENGINE);
        BamResult bamResult = new Alignment(fastqResult).mapping(flag, sample, configuration, TEMPLATE_ENGINE);
        Set<String> tmpDir = bamResult.getCommand().getTempDirs();
        StringBuilder cmd = new StringBuilder(bamResult.getCommand().getToolCommand());
        if (flag.isFusionCatcher()) {
            FusionCatcherResult fusionCatcherResult = new FusionCatcher(sample, fastqResult)
                    .generate(configuration, TEMPLATE_ENGINE);
            tmpDir.addAll(fusionCatcherResult.getCommand().getTempDirs());
            cmd.append(fusionCatcherResult.getCommand().getToolCommand());
        }

        final String command = configuration.isMasterMode()
                ? cmd.toString()
                : cmd.append(cleanUpTmpDir(tmpDir)).toString();
        final String custTaskScript = printShell(configuration, command, sample.getName(), null);
        if (scriptManager != null) {
            scriptManager.addScript(sample.getName(), ALIGNMENT, custTaskScript);
            tmpDir.forEach(t -> scriptManager.addScript(sample.getName(), TEMP, t));
        }
        log.debug(String.format("Successful Step: the %s sample was processed.", sample.getName()));
    }

    /**
     * this method launches QcSummary tool
     * @param configuration of type {@link Configuration} consists config which is needed
     * @param samples       consists samples
     */
    @Override
    public void postProcess(Configuration configuration, List<FastqFileSample> samples) {
        //no op
    }
}
