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

package com.epam.fonda.workflow;

import lombok.Getter;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The <tt>PipelineType</tt> enum provides type of {@link Workflow} to process
 */
@Getter
public enum PipelineType {

    DNA_WGS_VAR_FASTQ("DnaWgsVar_Fastq"),
    DNA_WGS_VAR_BAM("DnaWgsVar_Bam"),
    DNA_AMPLICON_VAR_FASTQ("DnaAmpliconVar_Fastq"),
    DNA_AMPLICON_VAR_BAM("DnaAmpliconVar_Bam"),
    DNA_CAPTURE_VAR_FASTQ("DnaCaptureVar_Fastq"),
    DNA_CAPTURE_VAR_BAM("DnaCaptureVar_Bam"),
    SC_RNA_EXPRESSION_FASTQ("scRnaExpression_Fastq"),
    RNA_EXPRESSION_FASTQ("RnaExpression_Fastq"),
    RNA_EXPRESSION_BAM("RnaExpression_Bam"),
    RNA_CAPTURE_VAR_FASTQ("RnaCaptureVar_Fastq"),
    SC_RNA_EXPRESSION_CELLRANGER_FASTQ("scRnaExpression_CellRanger_Fastq"),
    SC_IMMUNE_PROFILE_CELL_RANGER_FASTQ("scImmuneProfile_CellRanger_Fastq"),
    RNA_FUSION_FASTQ("RnaFusion_Fastq"),
    BAM_2_FASTQ("Bam2Fastq"),
    TCR_REPERTOIRE_FASTQ("TcrRepertoire_Fastq"),
    HLA_TYPING_FASTQ("HlaTyping_Fastq");

    private final String name;
    private static Map<String, PipelineType> idMap;

    static {
        idMap = Arrays.stream(values()).collect(Collectors.toMap(v -> v.name, v -> v));
    }

    PipelineType(final String name) {
        this.name = name;
    }

    /**
     * Return {@link PipelineType} by name
     *
     * @param name is type of workflow for processing from user specified configuration
     * @return {@link PipelineType} of workflow
     */
    public static PipelineType getByName(final String name) {
        Validate.notBlank(name, "Workflow is not defined.");
        return Validate.notNull(idMap.get(name), "Unsupported workflow: " + name);
    }
}
