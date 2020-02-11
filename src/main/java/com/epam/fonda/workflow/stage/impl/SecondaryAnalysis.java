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
package com.epam.fonda.workflow.stage.impl;

import com.epam.fonda.entity.command.BashCommand;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.impl.ContEst;
import com.epam.fonda.tools.impl.Cufflinks;
import com.epam.fonda.tools.impl.Exomecnv;
import com.epam.fonda.tools.impl.FeatureCount;
import com.epam.fonda.tools.impl.Freebayes;
import com.epam.fonda.tools.impl.GatkHaplotypeCaller;
import com.epam.fonda.tools.impl.Lofreq;
import com.epam.fonda.tools.impl.Mutect1;
import com.epam.fonda.tools.impl.Mutect2;
import com.epam.fonda.tools.impl.Pileup;
import com.epam.fonda.tools.impl.RsemAnnotation;
import com.epam.fonda.tools.impl.RsemExpression;
import com.epam.fonda.tools.impl.Scalpel;
import com.epam.fonda.tools.impl.Sequenza;
import com.epam.fonda.tools.impl.Strelka2;
import com.epam.fonda.tools.impl.Stringtie;
import com.epam.fonda.tools.impl.Vardict;
import com.epam.fonda.tools.impl.VcfSnpeffAnnotation;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.tools.results.ContEstResult;
import com.epam.fonda.tools.results.CufflinksResult;
import com.epam.fonda.tools.results.ExomecnvResult;
import com.epam.fonda.tools.results.FeatureCountResult;
import com.epam.fonda.tools.results.PileupResult;
import com.epam.fonda.tools.results.RsemResult;
import com.epam.fonda.tools.results.SequenzaResult;
import com.epam.fonda.tools.results.StringtieResult;
import com.epam.fonda.tools.results.VariantsVcfResult;
import com.epam.fonda.tools.results.VcfScnpeffAnnonationResult;
import com.epam.fonda.workflow.impl.Flag;
import com.epam.fonda.workflow.stage.Stage;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.SetUtils;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.util.Set;

import static com.epam.fonda.utils.PipelineUtils.addTask;
import static com.epam.fonda.utils.PipelineUtils.cleanUpTmpDir;
import static com.epam.fonda.utils.PipelineUtils.createStaticShell;

@AllArgsConstructor
@RequiredArgsConstructor
public class SecondaryAnalysis implements Stage {

    @NonNull
    private BamResult bamResult;
    @NonNull
    private String sampleName;
    @NonNull
    private String sampleOutputDir;
    private String controlSampleName;
    private boolean isPaired;

    /**
     * Method consists of list of tools that can be invoked on re-analysis of either qualitative or quantitative data.
     * @param flag is the type of {@link Flag} that indicates whether tool was set in configuration
     * @param configuration is the type of {@link Configuration} which contains its fields: workflow, outdir,
     *                      logOutdir, rScript, fastqList, bamList.
     * @param templateEngine an instance of {@link TemplateEngine} to process multiple template
     * @return {@link String} represents a cmd lines
     * @throws IOException if an I/O error has occurred
     */
    public String process(final Flag flag, final Configuration configuration,
                          final TemplateEngine templateEngine) throws IOException {
        if (bamResult == null) {
            BamOutput bamOutput = BamOutput.builder().build();
            bamResult = BamResult.builder()
                    .bamOutput(bamOutput)
                    .command(BashCommand.withTool(""))
                    .build();
        }
        final StringBuilder alignCmd = new StringBuilder();
        final Set<String> tempDirs = bamResult.getCommand().getTempDirs();
        featureCount(flag, configuration, templateEngine, alignCmd);
        rsem(flag, configuration, templateEngine, alignCmd, tempDirs);
        cufflinks(flag, configuration, templateEngine, alignCmd, tempDirs);
        stringtie(flag, configuration, templateEngine, alignCmd, tempDirs);
        vardict(flag, configuration, templateEngine, alignCmd);
        gatkHaplotypeCaller(flag, configuration, templateEngine, alignCmd);
        contEst(flag, configuration, templateEngine, alignCmd);
        strelka2(flag, configuration, templateEngine, alignCmd);
        mutect1(flag, configuration, templateEngine, alignCmd);
        mutect2(flag, configuration, templateEngine, alignCmd);
        scalpel(flag, configuration, templateEngine, alignCmd);
        lofreq(flag, configuration, templateEngine, alignCmd);
        sequenza(flag, configuration, templateEngine, alignCmd);
        exomecnv(flag, configuration, templateEngine, alignCmd);
        freebayes(flag, configuration, templateEngine, alignCmd);
        return alignCmd.toString();
    }

    private void contEst(final Flag flag, final Configuration configuration, final TemplateEngine templateEngine,
                         final StringBuilder alignCmd) throws IOException {
        if (!(isPaired && flag.isContEst())) {
            return;
        }
        final ContEstResult result = new ContEst(sampleName, sampleOutputDir, bamResult)
                .generate(configuration, templateEngine);
        createCustomToolShell(configuration, alignCmd, result.getCommand().getToolCommand() +
                cleanUpTmpDir(result.getCommand().getTempDirs()), "contEst");
    }

    private void freebayes(final Flag flag, final Configuration configuration, final TemplateEngine templateEngine,
                           final StringBuilder alignCmd) throws IOException {
        if (isPaired || !flag.isFreebayes()) {
            return;
        }
        final Freebayes freebayes = new Freebayes(sampleName, bamResult.getBamOutput(), sampleOutputDir);
        processVcfTool(configuration, templateEngine, alignCmd, freebayes);
    }

    private void gatkHaplotypeCaller(final Flag flag, final Configuration configuration,
                                     final TemplateEngine templateEngine, final StringBuilder alignCmd)
            throws IOException {
        if (isPaired || !flag.isGatkHaplotypeCaller()) {
            return;
        }
        final GatkHaplotypeCaller gatkHaplotypeCaller = new GatkHaplotypeCaller(sampleName,
                bamResult.getBamOutput().getBam(), sampleOutputDir);
        processVcfTool(configuration, templateEngine, alignCmd, gatkHaplotypeCaller);
    }

    private void exomecnv(final Flag flag, final Configuration configuration, final TemplateEngine templateEngine,
                          final StringBuilder alignCmd) throws IOException {
        if (!(isPaired && flag.isExomecnv())) {
            return;
        }
        final ExomecnvResult result = new Exomecnv(sampleName, controlSampleName, bamResult.getBamOutput(),
                sampleOutputDir).generate(configuration, templateEngine);
        createCustomToolShell(configuration, alignCmd, result.getCommand().getToolCommand() +
                cleanUpTmpDir(result.getCommand().getTempDirs()), result.getToolName());
    }

    private void sequenza(final Flag flag, final Configuration configuration, final TemplateEngine templateEngine,
                          final StringBuilder alignCmd) throws IOException {
        if (!(isPaired && flag.isSequenza())) {
            return;
        }
        final PileupResult pileupResult = new Pileup(sampleName, sampleOutputDir, controlSampleName, bamResult)
                .generate(configuration, templateEngine);
        final SequenzaResult result = new Sequenza(sampleName, sampleOutputDir, pileupResult.getPileupOutput())
                .generate(configuration, templateEngine);
        createCustomToolShell(configuration, alignCmd, pileupResult.getCommand().getToolCommand()
                        + result.getCommand().getToolCommand() + cleanUpTmpDir(result.getCommand().getTempDirs()),
                result.getToolName());
    }

    private void lofreq(final Flag flag, final Configuration configuration, final TemplateEngine templateEngine,
                        final StringBuilder alignCmd) throws IOException {
        if (!flag.isLofreq()) {
            return;
        }
        final Lofreq lofreq = new Lofreq(sampleName, bamResult.getBamOutput(), sampleOutputDir, isPaired);
        processVcfTool(configuration, templateEngine, alignCmd, lofreq);
    }

    private void scalpel(final Flag flag, final Configuration configuration, final TemplateEngine templateEngine,
                         final StringBuilder alignCmd) throws IOException {
        if (!flag.isScalpel()) {
            return;
        }
        final Scalpel scalpel = new Scalpel(sampleName, bamResult.getBamOutput(), sampleOutputDir, isPaired);
        processVcfTool(configuration, templateEngine, alignCmd, scalpel);
    }

    private void mutect2(final Flag flag, final Configuration configuration, final TemplateEngine templateEngine,
                         final StringBuilder alignCmd) throws IOException {
        if (!(isPaired && flag.isMutect2())) {
            return;
        }
        final Mutect2 mutect2 = new Mutect2(sampleName, bamResult.getBamOutput(), sampleOutputDir);
        processVcfTool(configuration, templateEngine, alignCmd, mutect2);
    }

    private void mutect1(final Flag flag, final Configuration configuration, final TemplateEngine templateEngine,
                         final StringBuilder alignCmd) throws IOException {
        if (isPaired || !flag.isMutect1()) {
            return;
        }
        final Mutect1 mutect1 = new Mutect1(sampleName, bamResult.getBamOutput(), sampleOutputDir);
        processVcfTool(configuration, templateEngine, alignCmd, mutect1);
    }

    private void strelka2(final Flag flag, final Configuration configuration, final TemplateEngine templateEngine,
                          final StringBuilder alignCmd) throws IOException {
        if (!flag.isStrelka2()) {
            return;
        }
        final Strelka2 strelka2 = new Strelka2(sampleName, bamResult.getBamOutput(), sampleOutputDir, isPaired);
        processVcfTool(configuration, templateEngine, alignCmd, strelka2);
    }

    private void vardict(final Flag flag, final Configuration configuration, final TemplateEngine templateEngine,
                         final StringBuilder alignCmd) throws IOException {
        if (!flag.isVardict()) {
            return;
        }
        final Vardict vardict = new Vardict(sampleName, controlSampleName, bamResult.getBamOutput(),
                sampleOutputDir, isPaired);
        processVcfTool(configuration, templateEngine, alignCmd, vardict);
    }

    private void stringtie(final Flag flag, final Configuration configuration, final TemplateEngine templateEngine,
                           final StringBuilder alignCmd, final Set<String> tempDirs) throws IOException {
        if (!flag.isStringtie()) {
            return;
        }
        StringtieResult stringtieResult = new Stringtie(sampleName, sampleOutputDir, bamResult.getBamOutput())
                .generate(configuration, templateEngine);
        createCustomToolShell(configuration, alignCmd,
                stringtieResult.getCommand().getToolCommand() + cleanUpTmpDir(tempDirs), "stringtie");
    }

    private void cufflinks(final Flag flag, final Configuration configuration, final TemplateEngine templateEngine,
                           final StringBuilder alignCmd, final Set<String> tempDirs) throws IOException {
        if (!flag.isCufflinks()) {
            return;
        }
        CufflinksResult cufflinksResult = new Cufflinks(sampleName, sampleOutputDir, bamResult.getBamOutput())
                .generate(configuration, templateEngine);
        createCustomToolShell(configuration, alignCmd,
                cufflinksResult.getCommand().getToolCommand() + cleanUpTmpDir(tempDirs), "cufflinks");
    }

    private void rsem(final Flag flag, final Configuration configuration, final TemplateEngine templateEngine,
                      final StringBuilder alignCmd, final Set<String> tempDirs) throws IOException {
        if (!flag.isRsem()) {
            return;
        }
        if (flag.isHisat2()) {
            throw new IllegalArgumentException(
                    "Error Step: Unfortunately, HISAT2 is not compatible with RSEM, please change either one!");
        }
        RsemResult rsemResult = new RsemExpression(sampleName, sampleOutputDir, bamResult.getBamOutput())
                .generate(configuration, templateEngine);
        rsemResult = new RsemAnnotation(rsemResult).generate(configuration, templateEngine);
        createCustomToolShell(configuration, alignCmd,
                rsemResult.getCommand().getToolCommand() + cleanUpTmpDir(tempDirs), "rsem");
    }

    private void featureCount(final Flag flag, final Configuration configuration, final TemplateEngine templateEngine,
                              final StringBuilder alignCmd) throws IOException {
        if (!flag.isFeatureCount()) {
            return;
        }
        FeatureCountResult featureCountResult = new FeatureCount(sampleName, sampleOutputDir, bamResult)
                .generate(configuration, templateEngine);
        createCustomToolShell(configuration, alignCmd, featureCountResult.getCommand().getToolCommand(),
                "featureCount");
    }

    private void processVcfTool(final Configuration configuration, final TemplateEngine templateEngine,
                                final StringBuilder alignCmd, final Tool<VariantsVcfResult> tool) throws IOException {
        final VariantsVcfResult toolResult = tool.generate(configuration, templateEngine);
        final VcfScnpeffAnnonationResult vcfSnpeffAnnotationResult = new VcfSnpeffAnnotation(sampleName, toolResult)
                .generate(configuration, templateEngine);
        createVcfToolShell(configuration, alignCmd, toolResult, vcfSnpeffAnnotationResult);
    }

    private void createVcfToolShell(final Configuration configuration, final StringBuilder alignCmd,
                                    final VariantsVcfResult vcfToolResult,
                                    final VcfScnpeffAnnonationResult vcfScnpeffAnnonationResult) throws IOException {
        createCustomToolShell(configuration, alignCmd, vcfToolResult.getAbstractCommand().getToolCommand()
                        + vcfScnpeffAnnonationResult.getCommand().getToolCommand()
                        + cleanUpTmpDir(SetUtils.emptyIfNull(vcfToolResult.getAbstractCommand().getTempDirs())),
                vcfToolResult.getFilteredTool());
    }

    private void createCustomToolShell(final Configuration configuration,
                                       final StringBuilder alignCmd,
                                       final String cmd,
                                       final String task) throws IOException {
        createStaticShell(configuration, task, cmd, sampleName);
        final String addTaskCmd = addTask(configuration, task, sampleName);
        alignCmd.append(addTaskCmd);
    }
}
