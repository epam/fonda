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
package com.epam.fonda.workflow.impl;

import com.epam.fonda.entity.configuration.Configuration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public class Flag {
    private boolean rsem;
    private boolean rmdup;
    private boolean rnaSeQC;
    private boolean xenome;
    private boolean seqpurge;
    private boolean trimmomatic;
    private boolean star;
    private boolean hisat2;
    private boolean salmon;
    private boolean featureCount;
    private boolean cufflinks;
    private boolean stringtie;
    private boolean count;
    private boolean doubletDetection;
    private boolean scrublet;
    private boolean conversion;
    private boolean qc;
    private boolean vdj;
    private boolean vardict;
    private boolean mutect1;
    private boolean mutect2;
    private boolean lofreq;
    private boolean gatkHaplotypeCaller;
    private boolean strelka2;
    private boolean scalpel;
    private boolean starFusion;
    private boolean fusionCatcher;
    private boolean sequenza;
    private boolean exomecnv;
    private boolean freebayes;
    private boolean bwa;
    private boolean novoalign;
    private boolean picard;
    private boolean abraRealign;
    private boolean gatkRealign;
    private boolean contEst;
    private boolean mixcr;

    public static Flag buildFlags(final Configuration configuration) {
        final Set tasks = configuration.getGlobalConfig().getPipelineInfo().getToolset();
        return Flag.builder()
                .rmdup(tasks.contains("rmdup"))
                .rsem(tasks.contains("rsem"))
                .rnaSeQC(tasks.contains("qc"))
                .xenome(configuration.getGlobalConfig().getPipelineInfo().isFlagXenome())
                .seqpurge(tasks.contains("seqpurge"))
                .trimmomatic(tasks.contains("trimmomatic"))
                .star(tasks.contains("star"))
                .hisat2(tasks.contains("hisat2"))
                .salmon(tasks.contains("salmon"))
                .featureCount(tasks.contains("featureCount"))
                .cufflinks(tasks.contains("cufflinks"))
                .stringtie(tasks.contains("stringtie"))
                .count(tasks.contains("count"))
                .doubletDetection(tasks.contains("doubletDetection"))
                .scrublet(tasks.contains("scrublet"))
                .conversion(tasks.contains("conversion"))
                .qc(tasks.contains("qc"))
                .vdj(tasks.contains("vdj"))
                .vardict(tasks.contains("vardict"))
                .mutect1(tasks.contains("mutect1"))
                .mutect2(tasks.contains("mutect2"))
                .lofreq(tasks.contains("lofreq"))
                .gatkHaplotypeCaller(tasks.contains("gatkHaplotypeCaller"))
                .strelka2(tasks.contains("strelka2"))
                .scalpel(tasks.contains("scalpel"))
                .starFusion(tasks.contains("starFusion"))
                .fusionCatcher(tasks.contains("fusionCatcher"))
                .sequenza(tasks.contains("sequenza"))
                .exomecnv(tasks.contains("exomecnv"))
                .freebayes(tasks.contains("freebayes"))
                .bwa(tasks.contains("bwa"))
                .novoalign(tasks.contains("novoalign"))
                .picard(tasks.contains("picard"))
                .abraRealign(tasks.contains("abra_realign"))
                .gatkRealign(tasks.contains("gatk_realign"))
                .contEst(tasks.contains("contEst"))
                .mixcr(tasks.contains("mixcr"))
                .build();
    }
}
