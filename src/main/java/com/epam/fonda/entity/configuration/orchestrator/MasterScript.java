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
package com.epam.fonda.entity.configuration.orchestrator;

import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.utils.TemplateEngineUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.fonda.entity.configuration.orchestrator.ScriptType.ALIGNMENT;
import static com.epam.fonda.entity.configuration.orchestrator.ScriptType.POST_ALIGNMENT;
import static com.epam.fonda.entity.configuration.orchestrator.ScriptType.SECONDARY;
import static com.epam.fonda.utils.PipelineUtils.cleanUpTmpDir;
import static com.epam.fonda.utils.PipelineUtils.writeToFile;
import static java.lang.String.format;

/**
 * The <tt>MasterFile</tt> class represents the
 */
public final class MasterScript implements ScriptManager {
    public static final TemplateEngine TEMPLATE_ENGINE = TemplateEngineUtils.init();
    private static final String MASTER_TEMPLATE = "master_template";

    private List<SampleScripts> alignmentScripts;
    private Set<String> postProcessScripts;
    private Set<String> cleanupTempFiles;
    private Map<String, Map<ScriptType, List<String>>> scriptsBySample;
    @NonNull
    private static Configuration configuration;

    private MasterScript() {
        alignmentScripts = new LinkedList<>();
        postProcessScripts = new LinkedHashSet<>();
        cleanupTempFiles = new LinkedHashSet<>();
        scriptsBySample = new HashMap<>();
    }

    public static MasterScript getInstance(final Configuration config) {
        configuration = config;
        return MasterFileHolder.INSTANCE;
    }

    public String buildScript() {
        Map<String, String> variablesMap = initializeVariablesMap(configuration);
        Context context = new Context();
        context.setVariable("variablesMap", variablesMap);
        scriptsBySample.forEach((key, typeMap) -> alignmentScripts.add(new SampleScripts(Stream.concat(
                Stream.concat(
                        typeMap.getOrDefault(ALIGNMENT, Collections.emptyList()).stream(),
                        typeMap.getOrDefault(POST_ALIGNMENT, Collections.emptyList()).stream()),
                        typeMap.getOrDefault(SECONDARY, Collections.emptyList()).stream())
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList()))));
        final List<SampleScripts> processScript = alignmentScripts
                .stream()
                .filter(ObjectUtils::isNotEmpty)
                .collect(Collectors.toList());

        context.setVariable("samplesProcessScripts", processScript);
        context.setVariable("postProcessScripts", postProcessScripts);
        String masterShell = TEMPLATE_ENGINE.process(MASTER_TEMPLATE, context);
        final String shellToSubmit = String.valueOf(variablesMap.get("shellToSubmit"));
        try {
            writeToFile(shellToSubmit, masterShell + cleanUpTmpDir(cleanupTempFiles),
                    configuration.getGlobalConfig().getPipelineInfo().getLineEnding());
        } catch (IOException e) {
            throw new IllegalArgumentException(format("Cannot create master %s file", shellToSubmit), e);
        }
        return shellToSubmit;
    }

    @Override
    public void addScript(final String sampleName, final ScriptType type, final String script) {
        if (StringUtils.isBlank(script)) {
            return;
        }
        if (!scriptsBySample.containsKey(sampleName)) {
            final Map<ScriptType, List<String>> scriptsByProcess = new HashMap<>();
            scriptsBySample.put(sampleName, scriptsByProcess);
        }
        switch (type) {
            case ALIGNMENT:
                putScript(sampleName, ALIGNMENT, script);
                break;
            case SECONDARY:
                putScript(sampleName, SECONDARY, script);
                break;
            case POST_ALIGNMENT:
                putScript(sampleName, POST_ALIGNMENT, script);
                break;
            case POST_PROCESS:
                postProcessScripts.add(script);
                break;
            case TEMP:
                cleanupTempFiles.add(script);
                break;
            default: throw new IllegalArgumentException(String.format("Requested %s script type is not allowed", type));
        }
    }

    private void putScript(final String sampleName, final ScriptType type, final String script) {
        final Map<ScriptType, List<String>> scriptsByProcess = scriptsBySample.get(sampleName);
        if (!scriptsByProcess.containsKey(type)) {
            scriptsByProcess.put(type, Collections.singletonList(script));
            return;
        }
        if (scriptsByProcess.get(type).contains(script)) {
            return;
        }
        scriptsByProcess.get(type).add(script);
    }

    @Data
    @AllArgsConstructor
    public static class SampleScripts {
        private List<String> scripts;
    }

    @Setter
    private static class MasterFileHolder {
        private static final MasterScript INSTANCE = new MasterScript();
    }

    /**
     * This method initializes map with values to be passed to the Thymeleaf context.
     *
     * @param configuration is the type of {@link Configuration} which contains
     *                      its fields: workflow, local, numThreads, pe, queue.
     * @return resulting map of type {@link Map}
     **/
    private static Map<String, String> initializeVariablesMap(final Configuration configuration) {
        final String fileName = "master";
        Map<String, String> variablesMap = new HashMap<>();
        variablesMap.put("shellToSubmit", format("%s/%s.sh", configuration.getCommonOutdir().getShOutdir(), fileName));
        variablesMap.put("local", String.valueOf(configuration.isLocalMode()));
        variablesMap.put("fileName", fileName);
        variablesMap.put("numThreads", String.valueOf(configuration.getGlobalConfig().getQueueParameters()
                .getNumThreads()));
        variablesMap.put("queue", configuration.getGlobalConfig().getQueueParameters().getQueue());
        variablesMap.put("pe", configuration.getGlobalConfig().getQueueParameters().getPe());
        variablesMap.put("outdir", configuration.getCommonOutdir().getRootOutdir());
        variablesMap.put("sync", String.valueOf(configuration.isSyncMode()));
        return variablesMap;
    }
}
