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

import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.samples.fastq.FastqReadType;
import com.epam.fonda.tools.impl.SeqPurge;
import com.epam.fonda.tools.impl.Trimmomatic;
import com.epam.fonda.tools.impl.Xenome;
import com.epam.fonda.tools.results.FastqResult;
import com.epam.fonda.workflow.impl.Flag;
import com.epam.fonda.workflow.stage.Stage;
import lombok.AllArgsConstructor;
import org.thymeleaf.TemplateEngine;

/**
 * The first stage of workflow.
 * Consists of list of tools that split fastqs by read groups and filter its.
 * Reproduces {@link FastqResult} as a result.
 */
@AllArgsConstructor
public class PreAlignment implements Stage {

    private FastqResult fastqResult;
    private Integer index;

    public PreAlignment(final FastqResult fastqResult) {
        this(fastqResult, null);
    }

    /**
     * Method consists of list of tools that can be invoked on pre-alignment stage.
     * @param flag is the type of {@link Flag} that indicates whether tool was set in configuration
     * @param sample is the type of {@link FastqFileSample} which contains fastq lists.
     * @param configuration is the type of {@link Configuration} which contains its fields: workflow, outdir,
     *                      logOutdir, rScript, fastqList, bamList.
     * @param templateEngine an instance of {@link TemplateEngine} to process multiple template
     * @return {@link FastqResult} which presents fastqs paths
     */
    public FastqResult process(final Flag flag, final FastqFileSample sample, final Configuration configuration,
                               final TemplateEngine templateEngine) {
        if (flag.isXenome()) {
            fastqResult = new Xenome(sample, fastqResult, index).generate(configuration, templateEngine);
        }
        if (flag.isSeqpurge() && !skipTool(configuration)) {
            fastqResult = new SeqPurge(sample, fastqResult, index).generate(configuration, templateEngine);
        } else if (flag.isTrimmomatic()) {
            fastqResult = new Trimmomatic(sample, fastqResult, index).generate(configuration, templateEngine);
        }
        return fastqResult;
    }

    private boolean skipTool(final Configuration configuration) {
        return FastqReadType.SINGLE.getType().equals(configuration.getGlobalConfig().getPipelineInfo().getReadType())
                && index != null;
    }
}
