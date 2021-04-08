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

package com.epam.fonda.tools.impl;

import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.tools.PostProcessTool;
import com.epam.fonda.utils.PipelineUtils;
import com.epam.fonda.utils.RnaAnalysisUtils;
import com.epam.fonda.workflow.impl.Flag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class RnaMutationAnalysis implements PostProcessTool {
    private static final String GATK_HAPLOTYPE_CALLER_TOOL = "gatkHaplotypeCaller";
    private static final String GATK_STEP_NAME = "SnpEff annotation";

    @NonNull
    private final Flag flag;
    @NonNull
    private final List<String> sampleNames;

    @Override
    public String generate(Configuration configuration, TemplateEngine templateEngine) {
        if (!checkToolset(flag)) {
            return StringUtils.EMPTY;
        }
        StringBuilder command = new StringBuilder();
        if (flag.isGatkHaplotypeCaller()) {
            command.append(RnaAnalysisUtils.periodicStatusCheck(configuration, templateEngine, "RNA mutation",
                    GATK_STEP_NAME, sampleNames));
        }
        final String mutationAnalysis = RnaAnalysisUtils.dnaRnaMutationAnalysis(configuration, templateEngine,
                String.join("+", configuration.getGlobalConfig().getPipelineInfo().getToolset()));
        command.append(mutationAnalysis);
        configuration.setCustTask("mergeMutation");
        try {
            return PipelineUtils.printShell(configuration, command.toString(), null, null);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot create bash script for " + GATK_HAPLOTYPE_CALLER_TOOL);
        }
    }

    private boolean checkToolset(Flag flag) {
        return flag.isVardict() || flag.isMutect1() || flag.isMutect2() || flag.isLofreq() ||
                flag.isGatkHaplotypeCaller() || flag.isStrelka2() || flag.isScalpel();
    }
}
