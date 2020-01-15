package com.epam.fonda.workflow.impl;

import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.samples.bam.BamFileSample;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.impl.SamToFastq;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.BamResult;
import com.epam.fonda.tools.results.FastqResult;
import com.epam.fonda.utils.TemplateEngineUtils;
import com.epam.fonda.workflow.BamWorkflow;
import com.epam.fonda.workflow.stage.impl.PostAlignment;
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
        sample.createDirectory();
        configuration.setCustTask("convert");
        BamResult bamResult = BamResult.builder()
                .bamOutput(BamOutput.builder()
                        .bam(sample.getBam())
                        .build())
                .build();
        FastqFileSample fastqFileSample = FastqFileSample.builder()
                .name(sample.getName())
                .build();
        bamResult = new PostAlignment(bamResult).process(flag, fastqFileSample, configuration, TEMPLATE_ENGINE);
        FastqResult fastqResult = new SamToFastq(sample.getName(), sample.getSampleOutputDir(), bamResult)
                .generate(configuration, TEMPLATE_ENGINE);
        final String cmd = bamResult.getCommand().getToolCommand() + fastqResult.getCommand().getToolCommand() +
                cleanUpTmpDir(Collections.singletonList(sample.getTmpOutdir()));
        printShell(configuration, cmd, sample.getName(), null);
        log.debug(String.format("Successful step: the %s sample was processed.", sample.getName()));
    }

    @Override
    public void postProcess(Configuration configuration, List<BamFileSample> samples) throws IOException {
        List<String> sampleNames = samples.stream().map(BamFileSample::getName).collect(Collectors.toList());
        new FastqListAnalysis(flag, sampleNames).generate(configuration, TEMPLATE_ENGINE);
    }
}
