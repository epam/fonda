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

import com.epam.fonda.entity.command.AbstractCommand;
import com.epam.fonda.entity.command.BashCommand;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfigFormat;
import com.epam.fonda.samples.fastq.FastqFileSample;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.FastqOutput;
import com.epam.fonda.tools.results.FastqResult;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static com.epam.fonda.utils.ToolUtils.validate;
import static java.lang.String.format;

@RequiredArgsConstructor
public class Xenome implements Tool<FastqResult> {

    @Data
    private class XenomeFields {
        private String stmpOutDir;
        private String prefix;
        private String humanFastq1;
        private String humanFastq2;
        private String bothFastq1;
        private String bothFastq2;
        private String ambiguousFastq1;
        private String ambiguousFastq2;
        private String convertHumanFastq1;
        private String convertHumanFastq2;
        private String convertBothFastq1;
        private String convertBothFastq2;
        private String convertAmbiguousFastq1;
        private String convertAmbiguousFastq2;
        private String humanMergedFastq1;
        private String humanMergedFastq2;
        private String fastq1;
        private String fastq2;
        private String xenome;
        private String mouseXenomeIndex;
        private String index;
    }

    private static final String XENOME_TOOL_TEMPLATE_NAME = "xenome_tool_template";

    @NonNull
    private FastqFileSample sample;
    @NonNull
    private FastqResult result;
    private final Integer index;

    /**
     * This method generates bash script {@link BashCommand} for Xenome tool.
     *
     * @param configuration  is the type of {@link Configuration} from which xenome and
     *                       mouseXenomeIndex fields are used by Xenome class.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link BashCommand} with bash script.
     **/
    @Override
    public FastqResult generate(final Configuration configuration,
                                final TemplateEngine templateEngine) {
        XenomeFields xenomeFields = constructFieldsByIndex(configuration);
        Context context = new Context();
        context.setVariable("xenomeFields", xenomeFields);
        final String cmd = templateEngine.process(XENOME_TOOL_TEMPLATE_NAME, context);
        final FastqOutput fastqOutput = result.getOut();
        fastqOutput.setMergedFastq1(xenomeFields.humanMergedFastq1);
        fastqOutput.setMergedFastq2(xenomeFields.humanMergedFastq2);
        AbstractCommand command = result.getCommand();
        command.setToolCommand(command.getToolCommand() + cmd);
        return FastqResult.builder()
                .out(fastqOutput)
                .command(command)
                .build();
    }

    /**
     * This method initializes fields of the Xenome {@link Xenome} class.
     *
     * @param configuration  is the type of {@link Configuration} which contains
     *                       its fields: xenome, mouseXenomeIndex.
     * @return {@link XenomeFields} with fields.
     **/
    private XenomeFields constructFieldsByIndex(Configuration configuration) {
        XenomeFields xenomeFields = new XenomeFields();
        if (index != null) {
            xenomeFields.index = String.valueOf(index);
        }
        xenomeFields.fastq1 = result.getOut().getMergedFastq1();
        xenomeFields.fastq2 = result.getOut().getMergedFastq2();
        xenomeFields.xenome = validate(configuration.getGlobalConfig().getToolConfig().getXenome(),
                GlobalConfigFormat.XENOME);
        xenomeFields.mouseXenomeIndex = validate(
                configuration.getGlobalConfig().getDatabaseConfig().getMouseXenomeIndex(),
                GlobalConfigFormat.MOUSE_XENOME_INDEX);
        xenomeFields.stmpOutDir = sample.getTmpOutdir();
        if (xenomeFields.index != null) {
            xenomeFields.prefix = format("%s/%s_%s", xenomeFields.stmpOutDir, sample
                    .getName(), xenomeFields.index);
            xenomeFields.humanMergedFastq1 = format("%s/%s_%s_classified_R1.fq.gz",
                    sample.getFastqOutdir(), sample.getName(), xenomeFields.index);
            xenomeFields.humanMergedFastq2 = format("%s/%s_%s_classified_R2.fq.gz",
                    sample.getFastqOutdir(), sample.getName(), xenomeFields.index);
        } else {
            xenomeFields.prefix = format("%s/%s", xenomeFields.stmpOutDir, sample.getName());
            xenomeFields.humanMergedFastq1 = format("%s/%s_classified_R1.fq.gz", sample.getFastqOutdir(),
                    sample.getName());
            xenomeFields.humanMergedFastq2 = format("%s/%s_classified_R2.fq.gz", sample.getFastqOutdir(),
                    sample.getName());
        }
        xenomeFields.humanFastq1 = format("%s_human_1.fastq", xenomeFields.prefix);
        xenomeFields.humanFastq2 = format("%s_human_2.fastq", xenomeFields.prefix);
        xenomeFields.bothFastq1 = format("%s_both_1.fastq", xenomeFields.prefix);
        xenomeFields.bothFastq2 = format("%s_both_2.fastq", xenomeFields.prefix);
        xenomeFields.ambiguousFastq1 = format("%s_ambiguous_1.fastq", xenomeFields.prefix);
        xenomeFields.ambiguousFastq2 = format("%s_ambiguous_2.fastq", xenomeFields.prefix);
        xenomeFields.convertHumanFastq1 = format("%s_convert_human_1.fastq", xenomeFields.prefix);
        xenomeFields.convertHumanFastq2 = format("%s_convert_human_2.fastq", xenomeFields.prefix);
        xenomeFields.convertBothFastq1 = format("%s_convert_both_1.fastq", xenomeFields.prefix);
        xenomeFields.convertBothFastq2 = format("%s_convert_both_2.fastq", xenomeFields.prefix);
        xenomeFields.convertAmbiguousFastq1 = format("%s_convert_ambiguous_1.fastq", xenomeFields.prefix);
        xenomeFields.convertAmbiguousFastq2 = format("%s_convert_ambiguous_2.fastq", xenomeFields.prefix);

        return xenomeFields;
    }
}
