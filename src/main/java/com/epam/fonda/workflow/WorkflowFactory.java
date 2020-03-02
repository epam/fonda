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

package com.epam.fonda.workflow;

import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.workflow.impl.Bam2FastqWorkflow;
import com.epam.fonda.workflow.impl.DnaVarBamWorkflow;
import com.epam.fonda.workflow.impl.DnaVarFastqWorkflow;
import com.epam.fonda.workflow.impl.Flag;
import com.epam.fonda.workflow.impl.HlaTypingFastqWorkflow;
import com.epam.fonda.workflow.impl.RnaExpressionBamWorkflow;
import com.epam.fonda.workflow.impl.RnaExpressionFastqWorkflow;
import com.epam.fonda.workflow.impl.RnaFusionFastqWorkflow;
import com.epam.fonda.workflow.impl.SCImmuneProfileCellRangerFastqWorkflow;
import com.epam.fonda.workflow.impl.SCRnaExpressionCellRangerFastqWorkflow;
import com.epam.fonda.workflow.impl.TcrRepertoireFastqWorkflow;

/**
 * The <tt>WorkflowFactory</tt> class provides workflow to launch
 */
public class WorkflowFactory {

    /**
     * Method consists of definition {@link Workflow} type for processing data from configuration.
     * @param workflowName is type of workflow for processing from user specified configuration
     * @param configuration is the type of {@link Configuration} which contains global and study configuration
     * @return {@link Workflow} type for processing
     */
    public Workflow getWorkflow(final String workflowName, final Configuration configuration) {
        Flag flag = Flag.buildFlags(configuration);
        switch (PipelineType.getByName(workflowName)) {
            case RNA_EXPRESSION_FASTQ: return new RnaExpressionFastqWorkflow(flag);
            case SC_RNA_EXPRESSION_FASTQ: return new RnaExpressionFastqWorkflow(flag);
            case SC_RNA_EXPRESSION_CELLRANGER_FASTQ: return new SCRnaExpressionCellRangerFastqWorkflow(flag);
            case RNA_EXPRESSION_BAM: return new RnaExpressionBamWorkflow(flag);
            case SC_IMMUNE_PROFILE_CELL_RANGER_FASTQ: return new SCImmuneProfileCellRangerFastqWorkflow(flag);
            case RNA_FUSION_FASTQ: return new RnaFusionFastqWorkflow(flag);
            case DNA_AMPLICON_VAR_FASTQ: return new DnaVarFastqWorkflow(flag, "Index mkdup bam");
            case DNA_CAPTURE_VAR_FASTQ: return new DnaVarFastqWorkflow(flag, "Index rmdup bam");
            case DNA_AMPLICON_VAR_BAM: return new DnaVarBamWorkflow(flag);
            case DNA_CAPTURE_VAR_BAM: return new DnaVarBamWorkflow(flag);
            case DNA_WGS_VAR_BAM: return new DnaVarBamWorkflow(flag);
            case DNA_WGS_VAR_FASTQ: return new DnaVarFastqWorkflow(flag, "Index rmdup bam");
            case BAM_2_FASTQ: return new Bam2FastqWorkflow(flag);
            case TCR_REPERTOIRE_FASTQ: return new TcrRepertoireFastqWorkflow(flag);
            case HLA_TYPING_FASTQ: return new HlaTypingFastqWorkflow(flag);
            case SC_RNA_EXPRESSION_BAM: return new RnaExpressionBamWorkflow(flag);
            default: throw new IllegalArgumentException(String.format("Requested workflow %s is not supported yet",
                    workflowName));
        }
    }
}
