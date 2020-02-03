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

import com.epam.fonda.entity.command.AbstractCommand;
import com.epam.fonda.entity.command.BashCommand;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfigFormat;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.BamOutput;
import com.epam.fonda.tools.results.BamResult;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Arrays;

import static com.epam.fonda.utils.PipelineUtils.TASK_TO_CHECK;
import static com.epam.fonda.utils.ToolUtils.validate;

@RequiredArgsConstructor
@Data
public class PicardRemoveDuplicate implements Tool<BamResult> {

    private static final String PICARD_REMOVE_DUPLICATE_TOOL_TEMPLATE_NAME = "picard_remove_duplicate_tool_template";

    @NonNull
    private BamResult bamResult;

    /**
     * This method generates bash script {@link BashCommand} for PicardRemoveDuplicate tool.
     *
     * @param configuration  is the type of {@link Configuration} which contains
     *                       its field: samtools.
     * @param templateEngine is the type of {@link TemplateEngine}.
     * @return {@link BashCommand} with bash script.
     **/
    @Override
    public BamResult generate(Configuration configuration, TemplateEngine templateEngine) {
        final String mkdupBam = bamResult.getBamOutput().getMkdupBam();
        final String rmdupBam = mkdupBam.replace(".mkdup.bam", ".rmdup.bam");
        Context context = new Context();
        context.setVariable("samtools",
                validate(configuration.getGlobalConfig().getToolConfig().getSamTools(), GlobalConfigFormat.SAMTOOLS));
        context.setVariable("mkdupBam", mkdupBam);
        context.setVariable("rmdupBam", rmdupBam);
        final String cmd = templateEngine.process(PICARD_REMOVE_DUPLICATE_TOOL_TEMPLATE_NAME, context);
        TASK_TO_CHECK.addAll(Arrays.asList("Remove duplicates", "Index rmdup bam"));
        BamOutput bamOutput = bamResult.getBamOutput();
        bamOutput.setBam(rmdupBam);
        bamOutput.setRmdupBam(rmdupBam);
        bamOutput.setRmdupBamIndex(rmdupBam.replace(".bam", ".bam.bai"));
        AbstractCommand resultCommand = bamResult.getCommand();
        resultCommand.setToolCommand(resultCommand.getToolCommand() + cmd);
        resultCommand.setTempDirs(Arrays.asList(bamResult.getBamOutput().getMkdupBam(),
                bamResult.getBamOutput().getMkdupBamIndex()));
        return BamResult.builder()
                .bamOutput(bamOutput)
                .command(resultCommand)
                .build();
    }
}
