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

import com.epam.fonda.entity.command.BashCommand;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfigFormat;
import com.epam.fonda.entity.configuration.StudyConfigFormat;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.tools.results.FeatureCountOutput;
import com.epam.fonda.tools.results.FeatureCountResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

import static com.epam.fonda.utils.PipelineUtils.cleanUpTmpDir;
import static com.epam.fonda.utils.ToolUtils.validate;
import static java.lang.String.format;

@AllArgsConstructor
@Data
public class FeatureCount implements Tool<FeatureCountResult> {

    @Data
    private class AdditionalFeatureCountFields {
        private String annotgeneSaf;
        private String featureCount;
        private String sampleName;
        private String cufflinksLibraryType;
        private String bam;
        private String workflow;
        private Boolean local;
        private String fileName;
        private String shellToSubmit;
        private String featureCountGeneCount;
        private String featureCountByCufflinksLibraryType;
    }

    @Data
    private class QueueParametersFields {
        private int numThreads;
        private String queue;
        private String pe;
    }

    @Data
    private class DirectoryFields {
        private String featureOutdir;
        private String shOutdir;
        private String outdir;
    }

    private static final String FEATURECOUNT_TOOL_TEMPLATE_NAME = "featurecount_tool_template";
    private static final String TASK = "featureCount";

    private String sampleName;
    private String sampleOutputDir;
    private BamResult bamResult;

    /**
     * This method generates bash script {@link BashCommand} for FeatureCount tool.
     *
     * @param configuration  is the type of {@link Configuration} which contains
     *                       its fields: featureCount, annotgeneSaf, cufflinksLibraryType, workflow, local,
     *                       numThreads, pe, queue.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link BashCommand} with bash script.
     **/
    @Override
    public FeatureCountResult generate(Configuration configuration, TemplateEngine templateEngine) {
        final String featureOutdir = format("%s/%s", sampleOutputDir, "feature_count");
        FeatureCountOutput featureCountOutput = FeatureCountOutput.builder()
                .featureOutdir(featureOutdir)
                .build();
        featureCountOutput.createDirectory();
        Context context = new Context();
        final AdditionalFeatureCountFields additionalFeatureCountFields =
                initializeAdditionalFeatureCountFields(configuration, featureOutdir);
        context.setVariable("additionalFeatureCountFields", additionalFeatureCountFields);
        context.setVariable("queueParametersFields", initializeQueueParametersFields(configuration));
        context.setVariable("directoryFields", initializeDirectoryFields(configuration, featureOutdir));
        String cmd = templateEngine.process(FEATURECOUNT_TOOL_TEMPLATE_NAME, context);
        featureCountOutput.setFeatureCountGeneCount(additionalFeatureCountFields.featureCountGeneCount);
        return FeatureCountResult.builder()
                .featureCountOutput(featureCountOutput)
                .bamResult(bamResult)
                .command(BashCommand.withTool(cmd + cleanUpTmpDir(bamResult.getCommand().getTempDirs())))
                .build();
    }

    /**
     * This method initializes fields of the AdditionalFeatureCountFields {@link AdditionalFeatureCountFields} class.
     *
     * @param configuration is the type of {@link Configuration} which contains
     *                      its fields: featureCount, annotgeneSaf, cufflinksLibraryType, workflow, local.
     * @param featureOutdir
     * @return {@link AdditionalFeatureCountFields} with its fields.
     **/
    private AdditionalFeatureCountFields initializeAdditionalFeatureCountFields(final Configuration configuration,
                                                                                final String featureOutdir) {
        AdditionalFeatureCountFields additionalFeatureCountFields = new AdditionalFeatureCountFields();
        additionalFeatureCountFields.annotgeneSaf = validate(configuration.getGlobalConfig().getDatabaseConfig()
                .getAnnotgenesaf(), GlobalConfigFormat.ANNOTGENESAF);
        additionalFeatureCountFields.bam = bamResult.getBamOutput().getBam();
        additionalFeatureCountFields.cufflinksLibraryType = validate(
                configuration.getStudyConfig().getCufflinksLibraryType(), StudyConfigFormat.CUFFLINKS_LIBRARY_TYPE);
        additionalFeatureCountFields.featureCount = validate(
                configuration.getGlobalConfig().getToolConfig().getFeatureCount(), GlobalConfigFormat.FEATURE_COUNT);
        additionalFeatureCountFields.workflow = configuration.getGlobalConfig().getPipelineInfo().getWorkflow();
        additionalFeatureCountFields.sampleName = sampleName;
        additionalFeatureCountFields.fileName = format("%s_%s_for_%s_analysis", configuration
                .getGlobalConfig().getPipelineInfo().getWorkflow(), TASK, sampleName);
        additionalFeatureCountFields.local = configuration.isLocalMode();
        additionalFeatureCountFields.shellToSubmit = format("%s/%s.sh",
                configuration.getCommonOutdir().getShOutdir(), additionalFeatureCountFields.getFileName());
        additionalFeatureCountFields.featureCountGeneCount = format("%s/%s_featureCount_gene.txt",
                featureOutdir, sampleName);
        Map<String, String> cufflinksLibraryTypeTofeatureCountMap = constructCufflinksLibraryTypeTofeatureCountMap();
        additionalFeatureCountFields.featureCountByCufflinksLibraryType = cufflinksLibraryTypeTofeatureCountMap
                .get(additionalFeatureCountFields.cufflinksLibraryType);
        return additionalFeatureCountFields;
    }

    /**
     * This method initializes fields of the QueueParametersFields {@link QueueParametersFields} class.
     *
     * @param configuration  is the type of {@link Configuration} which contains its fields: numThreads, pe, queue.
     * @return {@link QueueParametersFields} with with its fields.
     **/
    private QueueParametersFields initializeQueueParametersFields(Configuration configuration) {
        QueueParametersFields queueParametersFields = new QueueParametersFields();
        queueParametersFields.numThreads = configuration.getGlobalConfig().getQueueParameters().getNumThreads();
        queueParametersFields.pe = validate(configuration.getGlobalConfig().getQueueParameters().getPe(),
                GlobalConfigFormat.PE);
        queueParametersFields.queue = validate(configuration.getGlobalConfig().getQueueParameters().getQueue(),
                GlobalConfigFormat.QUEUE);
        return queueParametersFields;
    }

    /**
     * This method initializes fields of the DirectoryFields {@link DirectoryFields} class.
     *
     * @param configuration  is the type of {@link Configuration} which contains its fields: outdir, featureOutdir,
     *                       shOutdir.
     * @param featureOutdir
     * @return {@link DirectoryFields} with its fields.
     **/
    private DirectoryFields initializeDirectoryFields(Configuration configuration, final String featureOutdir) {
        DirectoryFields directoryFields = new DirectoryFields();
        directoryFields.outdir = configuration.getCommonOutdir().getRootOutdir();
        directoryFields.featureOutdir = featureOutdir;
        directoryFields.shOutdir = configuration.getCommonOutdir().getShOutdir();
        return directoryFields;
    }

    /**
     * This method initializes constructCufflinksLibraryTypeTofeatureCountMap.
     * @return {@link Map}.
     **/
    private Map<String, String> constructCufflinksLibraryTypeTofeatureCountMap() {
        Map<String, String> cufflinksLibraryTypeTofeatureCountMap = new HashMap<>();
        cufflinksLibraryTypeTofeatureCountMap.put("fr-unstranded", "0");
        cufflinksLibraryTypeTofeatureCountMap.put("fr-firststrand", "2");
        cufflinksLibraryTypeTofeatureCountMap.put("fr-secondstrand", "1");
        return cufflinksLibraryTypeTofeatureCountMap;
    }
}
