package com.epam.fonda.workflow.impl;

import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.impl.OptiType;
import com.epam.fonda.tools.impl.QcSummary;
import com.epam.fonda.tools.results.FastqResult;
import com.epam.fonda.tools.results.OptiTypeResult;
import com.epam.fonda.utils.PipelineUtils;
import com.epam.fonda.utils.TemplateEngineUtils;
import com.epam.fonda.workflow.FastqWorkflow;
import com.epam.fonda.workflow.stage.impl.PreAlignment;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.fonda.utils.PipelineUtils.cleanUpTmpDir;
import static com.epam.fonda.utils.PipelineUtils.printShell;

@Slf4j
@RequiredArgsConstructor
public class HlaTypingFastqWorkflow implements FastqWorkflow {

    private static final TemplateEngine TEMPLATE_ENGINE = TemplateEngineUtils.init();

    @NonNull
    final Flag flag;

    @Override
    public void run(Configuration configuration, FastqFileSample sample) throws IOException {
        sample.createDirectory();
        configuration.setCustTask("hlaTyping");
        FastqResult fastqResult = PipelineUtils.mergeFastq(sample);
        fastqResult = new PreAlignment(fastqResult).process(flag, sample, configuration, TEMPLATE_ENGINE);
        Set<String> tmpDir = fastqResult.getCommand().getTempDirs();
        StringBuilder cmd = new StringBuilder(fastqResult.getCommand().getToolCommand());
        if (flag.isOptiType()) {
            OptiTypeResult optiTypeResult = new OptiType(sample, fastqResult).generate(configuration, TEMPLATE_ENGINE);
            cmd.append(optiTypeResult.getCommand().getToolCommand());
        }
        cmd.append(cleanUpTmpDir(tmpDir));
        printShell(configuration, cmd.toString(), sample.getName(), null);
        log.debug(String.format("Successful step: the %s sample was processed.", sample.getName()));
    }

    @Override
    public void postProcess(Configuration configuration, List<FastqFileSample> samples) throws IOException {
        List<String> sampleNames = samples.stream().map(FastqFileSample::getName).collect(Collectors.toList());
        new QcSummary(flag, sampleNames).generate(configuration, TEMPLATE_ENGINE);
    }
}
