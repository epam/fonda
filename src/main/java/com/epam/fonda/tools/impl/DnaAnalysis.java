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
import com.epam.fonda.samples.bam.BamFileSample;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.PostProcessTool;
import com.epam.fonda.utils.PipelineUtils;
import com.epam.fonda.utils.RnaAnalysisUtils;
import com.epam.fonda.workflow.TaskContainer;
import com.epam.fonda.workflow.impl.Flag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.epam.fonda.utils.DnaUtils.isWgsWorkflow;
import static com.epam.fonda.utils.PipelineUtils.CASE;
import static com.epam.fonda.utils.PipelineUtils.TUMOR;

@AllArgsConstructor
public class DnaAnalysis implements PostProcessTool {
    private static final String VARDICT = "vardict";
    private static final String MUTECT1 = "mutect1";
    private static final String MUTECT2 = "mutect2";
    private static final String LOFREQ = "lofreq";
    private static final String GATK_HAPLOTYPE_CALLER = "gatkHaplotypeCaller";
    private static final String STRELKA2 = "strelka2";
    private static final String SCALPEL = "scalpel";
    private static final String SNPEFF_ANNOTATION = "SnpEff annotation";
    private static final String DNA_ANALYSIS_STATUS_CHECK_TEMPLATE = "analysis_logFile_template";
    private static final int DEFAULT_VALUE = 60;

    @Data
    private static class DnaAnalysisFields {
        private Integer period;
        private String toolName;
        private String logFile;
        private String errorMessage;
        private String successMessage;
        private String steps;
    }

    private final List<FastqFileSample> fastqSamples;
    private final List<BamFileSample> bamSamples;
    @NonNull
    private Flag flag;

    /**
     * This method generates a bash script for {@link DnaAnalysis} post process tool.
     *
     * @param configuration  the {@link Configuration} that is used to generate a bash script.
     * @param templateEngine the {@link TemplateEngine}.
     **/
    @Override
    public String generate(Configuration configuration, TemplateEngine templateEngine) {
        if (!checkToolset(flag) || isWgsWorkflow(configuration)) {
            return StringUtils.EMPTY;
        }

        if (CollectionUtils.isEmpty(fastqSamples) && CollectionUtils.isEmpty(bamSamples)) {
            throw new IllegalArgumentException(
                    "Error: no sample files are properly provided, please check!");
        }

        final StringBuilder cmd = new StringBuilder();
        filterToolset(flag)
                .forEach(tool -> periodicDnaMutationStatusCheck(configuration, templateEngine, cmd, tool));
        final String mutationAnalysis = RnaAnalysisUtils.dnaRnaMutationAnalysis(configuration, templateEngine,
                String.join("+", configuration.getGlobalConfig().getPipelineInfo().getToolset()));
        cmd.append(mutationAnalysis);
        configuration.setCustTask("mergeMutation");
        try {
            return PipelineUtils.printShell(configuration, cmd.toString(), null, null);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot create bash script for DNA analysis post processing");
        }
    }

    private void periodicDnaMutationStatusCheck(final Configuration configuration,
                                                final TemplateEngine templateEngine,
                                                final StringBuilder cmd,
                                                final String tool) {
        if (CollectionUtils.isNotEmpty(fastqSamples)) {
            fastqSamples.stream()
                    .filter(sample -> checkSampleType(sample.getSampleType()))
                    .forEach(sample -> cmd.append(passSampleToConstructDnaAnalysisFields(configuration,
                            sample.getName(), templateEngine, tool)));
            return;
        }
        bamSamples.stream()
                .filter(sample -> checkSampleType(sample.getSampleType()))
                .forEach(sample -> cmd.append(passSampleToConstructDnaAnalysisFields(configuration,
                        sample.getName(), templateEngine, tool)));
    }

    /**
     * Passes fastq or bam sample name to constructDnaAnalysisFields method
     * @param configuration of type {@link Configuration}  which contains its fields: workflow, outdir, logOutdir,
     *                      rScript, fastqList, bamList.
     * @param templateEngine of type {@link TemplateEngine} contains Thymeleaf TemplateEngine variable
     * @param sampleName of type {@link String} contains name of the sample
     * @param tool the name of the tool
     * @return builds command for sample
     */
    private String passSampleToConstructDnaAnalysisFields(Configuration configuration, String sampleName,
                                                          TemplateEngine templateEngine, String tool) {
        final Context context = new Context();
        final DnaAnalysisFields dnaAnalysisFields = constructDnaAnalysisFields(configuration, sampleName, tool);
        context.setVariable("fields", dnaAnalysisFields);
        return templateEngine.process(DNA_ANALYSIS_STATUS_CHECK_TEMPLATE, context);
    }

    /**
     * This method initializes fields of the {@link DnaAnalysisFields} class.
     *
     * @param configuration is the type of {@link Configuration} which contains its fields: workflow, outdir,
     *                      logOutdir, rScript, fastqList, bamList.
     * @param sampleName the name of the sample
     * @param task the name of the tool
     * @return {@link DnaAnalysisFields} with fields.
     **/
    private DnaAnalysisFields constructDnaAnalysisFields(final Configuration configuration,
                                                         final String sampleName,
                                                         final String task) {
        final DnaAnalysisFields dnaAnalysisFields = new DnaAnalysisFields();
        final String fileName = String.format("%s_%s_for_%s_analysis",
                configuration.getGlobalConfig().getPipelineInfo().getWorkflow(), task, sampleName);
        dnaAnalysisFields.toolName = SNPEFF_ANNOTATION;
        dnaAnalysisFields.steps = String.join("|", TaskContainer.getTasks());
        dnaAnalysisFields.logFile = String.format("%s/%s.log",
                configuration.getCommonOutdir().getLogOutdir(), fileName);
        dnaAnalysisFields.period = defaultOrSpecifiedPeriod(configuration);
        dnaAnalysisFields.errorMessage = String.format("Error DNA mutation results from %s", sampleName);
        dnaAnalysisFields.successMessage = String.format("Confirm DNA mutation results from %s", sampleName);
        return dnaAnalysisFields;
    }

    /**
     * @param type contains the type of the sample.
     * @return the result of checking.
     */
    private boolean checkSampleType(String type) {
        return type.equals(TUMOR) || type.equals(CASE);
    }

    /**
     * @param configuration contains a value if specified in the global config file or null.
     * @return a value from the global config file or a default value which is equals 60.
     */
    private Integer defaultOrSpecifiedPeriod(Configuration configuration) {
        return Optional.ofNullable(configuration.getGlobalConfig().getToolConfig().getStatusCheckPeriod())
                .orElse(DnaAnalysis.DEFAULT_VALUE);
    }

    private Set<String> filterToolset(Flag flag) {
        final Set<String> toolset = new HashSet<>();
        if (flag.isVardict()) {
            toolset.add(VARDICT);
        }
        if (flag.isMutect1()) {
            toolset.add(MUTECT1);
        }
        if (flag.isMutect2()) {
            toolset.add(MUTECT2);
        }
        if (flag.isLofreq()) {
            toolset.add(LOFREQ);
        }
        if (flag.isGatkHaplotypeCaller()) {
            toolset.add(GATK_HAPLOTYPE_CALLER);
        }
        if (flag.isStrelka2()) {
            toolset.add(STRELKA2);
        }
        if (flag.isScalpel()) {
            toolset.add(SCALPEL);
        }
        return toolset;
    }

    private boolean checkToolset(final Flag flag) {
        return flag.isVardict() || flag.isMutect1() || flag.isMutect2() || flag.isLofreq() ||
                flag.isGatkHaplotypeCaller() || flag.isStrelka2() || flag.isScalpel();
    }
}
