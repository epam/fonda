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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class RnaAnalysis implements PostProcessTool {
    private static final String RSEM_TOOL = "rsem";
    private static final String CUFFLINKS_TOOL = "cufflinks";
    private static final String STRINGTIE_TOOL = "stringtie";

    @NonNull
    private Flag flag;
    @NonNull
    private List<String> sampleNames;

    @Override
    public String generate(final Configuration configuration, final TemplateEngine templateEngine) {
        return filterToolset(flag)
                .stream().map(toolName -> {
                    StringBuilder command = new StringBuilder()
                            .append(RnaAnalysisUtils.periodicStatusCheck(configuration, templateEngine,
                                    "gene expression", toolName, sampleNames))
                            .append(RnaAnalysisUtils.dataAnalysis(configuration, templateEngine, toolName));
                    configuration.setCustTask(toolName);
                    try {
                        return PipelineUtils.printShell(configuration, command.toString(), null, null);
                    } catch (IOException e) {
                        throw new IllegalArgumentException("Cannot create bash script for " + toolName);
                    }
                }).collect(Collectors.joining(StringUtils.SPACE));
    }

    /**
     * Method checks for 'conversion' task and if it is in a flag, then checks for 'rsem', 'cufflinks' and 'stringtie'
     * tasks and add them (presented in a flag) to the set of tool
     *
     * @param flag is the type of {@link Flag} with toolset
     * @return set with contained in flag tools from a set of 'rsem', 'cufflinks' and 'stringtie'
     */
    private Set<String> filterToolset(Flag flag) {
        Set<String> toolset = new HashSet<>();
        if (!flag.isConversion()) {
            return toolset;
        }
        if (flag.isRsem()) {
            toolset.add(RSEM_TOOL);
        }
        if (flag.isCufflinks()) {
            toolset.add(CUFFLINKS_TOOL);
        }
        if (flag.isStringtie()) {
            toolset.add(STRINGTIE_TOOL);
        }
        return toolset;
    }
}
