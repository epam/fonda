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

import com.epam.fonda.entity.command.BashCommand;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfigFormat;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.RsemOutput;
import com.epam.fonda.tools.results.RsemResult;
import com.epam.fonda.workflow.TaskContainer;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static com.epam.fonda.utils.ToolUtils.validate;

@AllArgsConstructor
public class RsemExpression implements Tool<RsemResult> {

    private String sampleName;
    private String sampleOutputDir;
    private BamOutput bamOutput;

    @Data
    private class RsemExpressionFields {
        private int nthreads;
        private String index;
        private String rsem;
        private String sampleName;
        private String srsemOutdir;
        private String bam;
        private String rsemGeneResult;
        private String rsemIsoformResult;
    }

    private static final String RSEM_EXPRESSION_TEMPLATE_NAME = "rsem_expression_template";

    /**
     * This method generates bash script {@link BashCommand} for RSEM expression.
     *
     * @param configuration  is the type of {@link Configuration}.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link BashCommand} with bash script.
     */
    @Override
    public RsemResult generate(Configuration configuration, TemplateEngine templateEngine) {
        final String rsemOutdir = String.format("%s/rsem", sampleOutputDir);
        RsemOutput rsemOutput = RsemOutput.builder()
                .rsemOutdir(rsemOutdir)
                .build();
        rsemOutput.createDirectory();
        RsemExpressionFields rsemExpressionFields = constructFields(configuration, rsemOutdir);
        Context context = new Context();
        context.setVariable("rsemExpressionFields", rsemExpressionFields);
        final String cmd = templateEngine.process(RSEM_EXPRESSION_TEMPLATE_NAME, context);
        TaskContainer.addTasks("rsem");
        rsemOutput.setRsemGeneResult(rsemExpressionFields.rsemGeneResult);
        rsemOutput.setRsemIsoformResult(rsemExpressionFields.rsemIsoformResult);
        return RsemResult.builder()
                .bamOutput(bamOutput)
                .rsemOutput(rsemOutput)
                .command(BashCommand.withTool(cmd))
                .build();
    }

    /**
     * This method initializes fields of the RSEM Expression {@link RsemExpression} class.
     *
     * @param configuration
     * @param rsemOutdir
     * @return {@link RsemExpressionFields} with fields
     */
    private RsemExpressionFields constructFields(final Configuration configuration, final String rsemOutdir) {
        RsemExpressionFields rsemExpressionFields = new RsemExpressionFields();
        rsemExpressionFields.nthreads = configuration.getGlobalConfig().getQueueParameters().getNumThreads();
        rsemExpressionFields.index = validate(configuration.getGlobalConfig().getToolConfig().getRsemIndex(),
                GlobalConfigFormat.RSEMINDEX);
        rsemExpressionFields.rsem = validate(configuration.getGlobalConfig().getToolConfig().getRsem(),
                GlobalConfigFormat.RSEM);
        rsemExpressionFields.sampleName = sampleName;
        rsemExpressionFields.srsemOutdir = rsemOutdir;
        rsemExpressionFields.bam = bamOutput.getBam();
        rsemExpressionFields.rsemGeneResult = String.format("%s/%s.rsem.gene.expression.results",
                rsemExpressionFields.srsemOutdir, rsemExpressionFields.sampleName);
        rsemExpressionFields.rsemIsoformResult = String.format("%s/%s.rsem.isoform.expression.results",
                rsemExpressionFields.srsemOutdir, rsemExpressionFields.sampleName);
        return rsemExpressionFields;
    }
}
