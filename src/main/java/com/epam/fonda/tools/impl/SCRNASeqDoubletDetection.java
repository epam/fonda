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

package com.epam.fonda.tools.impl;

import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfigFormat;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.utils.PipelineUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Set;

import static com.epam.fonda.utils.ToolUtils.validate;

@Slf4j
@AllArgsConstructor
public class SCRNASeqDoubletDetection {
    private static final String DOUBLE_DETECTION_TEMPLATE_NAME = "doublet_detection_template";
    private static final String SCRUBLET_TEMPLATE_NAME = "scrublet_detection_template";
    private static final String DOUBLET_DETECTION = "doubletdetection";
    private static final String SCRUBLET = "scrublet";

    private FastqFileSample sample;
    @Data
    private class DoubleDetectionFields {
        private String jarPath;
        private String python;
        private String doubleDetectionPythonPath;
        private String outDir;
        private String genomeBuild;
        private String sampleName;
    }

    public String generate(Configuration configuration, TemplateEngine templateEngine) {
        DoubleDetectionFields doubleDetectionFields = constructFields(configuration);
        Context context = new Context();
        context.setVariable("doubleDetectionFields", doubleDetectionFields);
        return getCommand(configuration.getGlobalConfig().getPipelineInfo().getToolset(),
                templateEngine, context);
    }

    /**
     * Method checks toolset from {@link Configuration} for doubletdetection and scrublet tasks and
     * processes commands for them.
     *
     * @param toolNames      toolset from {@link Configuration}
     * @param templateEngine type of {@link TemplateEngine}
     * @param context        type of {@link Context} with preset variables ({@link DoubleDetectionFields})
     * @return final command
     */
    String getCommand(Set<String> toolNames, TemplateEngine templateEngine, Context context) {
        StringBuilder command = new StringBuilder();
        if (toolNames.contains(DOUBLET_DETECTION)) {
            command.append(templateEngine.process(DOUBLE_DETECTION_TEMPLATE_NAME, context));
        }
        if (toolNames.contains(SCRUBLET)) {
            command.append(templateEngine.process(SCRUBLET_TEMPLATE_NAME, context));
        }
        return command.toString();
    }

    /**
     * This method initializes fields of the RSEM Expression {@link SCRNASeqDoubletDetection} class.
     *
     * @param configuration
     * @return {@link DoubleDetectionFields} with set fields
     */
    private DoubleDetectionFields constructFields(Configuration configuration) {
        DoubleDetectionFields doubleDetectionFields = new DoubleDetectionFields();
        doubleDetectionFields.jarPath = PipelineUtils.getExecutionPath();
        doubleDetectionFields.python = validate(configuration.getGlobalConfig().getToolConfig().getPython(),
                GlobalConfigFormat.PYTHON);
        doubleDetectionFields.doubleDetectionPythonPath = validate(
                configuration.getGlobalConfig().getToolConfig().getDoubleDetectionPython(),
                GlobalConfigFormat.DOUBLE_DETECTION_PYTHON);
        doubleDetectionFields.outDir = configuration.getCommonOutdir().getRootOutdir();
        doubleDetectionFields.genomeBuild = validate(
                configuration.getGlobalConfig().getDatabaseConfig().getGenomeBuild(),
                GlobalConfigFormat.GENOME_BUILD);
        doubleDetectionFields.sampleName = sample.getName();
        return doubleDetectionFields;
    }
}
