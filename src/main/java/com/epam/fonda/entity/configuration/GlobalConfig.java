/*
 * Copyright 2017-2021 Sanofi and EPAM Systems, Inc. (https://www.epam.com/)
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

package com.epam.fonda.entity.configuration;

import com.beust.jcommander.Parameter;
import lombok.Data;

import java.util.LinkedHashSet;

import static com.epam.fonda.entity.configuration.EOLMarker.LF;

/**
 * The <tt>GlobalConfig</tt> class represents the user specified workflow parameters from global config file
 */
@Data
public class GlobalConfig {
    private QueueParameters queueParameters = new QueueParameters();
    private DatabaseConfig databaseConfig = new DatabaseConfig();
    private ToolConfig toolConfig = new ToolConfig();
    private CellrangerConfig cellrangerConfig = new CellrangerConfig();
    private PipelineInfo pipelineInfo = new PipelineInfo();

    @Data
    public static class QueueParameters {
        @Parameter(names = GlobalConfigFormat.NUMTHREADS)
        private int numThreads = 1;
        @Parameter(names = GlobalConfigFormat.MAXMEM)
        private String maxMem;
        @Parameter(names = GlobalConfigFormat.QUEUE)
        private String queue;
        @Parameter(names = GlobalConfigFormat.PE)
        private String pe;
    }

    @Data
    public static class DatabaseConfig {
        @Parameter(names = GlobalConfigFormat.SPECIES)
        private String species;
        @Parameter(names = GlobalConfigFormat.GENOME_BUILD)
        private String genomeBuild;
        @Parameter(names = GlobalConfigFormat.GENOME)
        private String genome;
        @Parameter(names = GlobalConfigFormat.VDJ_GENOME)
        private String vdjGenome;
        @Parameter(names = GlobalConfigFormat.STARINDEX)
        private String starIndex;
        @Parameter(names = GlobalConfigFormat.SALMONINDEX)
        private String salmonIndex;
        @Parameter(names = GlobalConfigFormat.HISAT2INDEX)
        private String hisat2Index;
        @Parameter(names = GlobalConfigFormat.NOVOINDEX)
        private String novoIndex;
        @Parameter(names = GlobalConfigFormat.BED)
        private String bed;
        @Parameter(names = GlobalConfigFormat.BED_WITH_HEADER)
        private String bedWithHeader;
        @Parameter(names = GlobalConfigFormat.BED_FOR_COVERAGE)
        private String bedForCoverage;
        @Parameter(names = GlobalConfigFormat.KNOWN_INDELS_MILLS)
        private String knownIndelsMills;
        @Parameter(names = GlobalConfigFormat.KNOWN_INDELS_PHASE1)
        private String knownIndelsPhase1;
        @Parameter(names = GlobalConfigFormat.DBSNP)
        private String dbsnp;
        @Parameter(names = GlobalConfigFormat.ADAPTER_FWD)
        private String adapterFWD;
        @Parameter(names = GlobalConfigFormat.ADAPTER_REV)
        private String adapterREV;
        @Parameter(names = GlobalConfigFormat.ADAPTER_SEQ)
        private String adapterSEQ;
        @Parameter(names = GlobalConfigFormat.SNPSIFTDB)
        private String snpsiftdb;
        @Parameter(names = GlobalConfigFormat.CANONICAL_TRANSCRIPT)
        private String canonicalTranscript;
        @Parameter(names = GlobalConfigFormat.ANNOTGENE)
        private String annotgene;
        @Parameter(names = GlobalConfigFormat.ANNOTGENESAF)
        private String annotgenesaf;
        @Parameter(names = GlobalConfigFormat.TRANSCRIPTOME)
        private String transcriptome;
        @Parameter(names = GlobalConfigFormat.FEATURE_REFERENCE)
        private String featureRef;
        @Parameter(names = GlobalConfigFormat.COSMIC)
        private String cosmic;
        @Parameter(names = GlobalConfigFormat.MUTECT_NORMAL_PANEL)
        private String mutectNormalPanel;
        @Parameter(names = GlobalConfigFormat.BED_PRIMER)
        private String bedPrimer;
        @Parameter(names = GlobalConfigFormat.R_RNABED)
        private String rRNABED;
        @Parameter(names = GlobalConfigFormat.STAR_FUSION_LIB)
        private String starFusionLib;
        @Parameter(names = GlobalConfigFormat.BOWTIE_INDEX)
        private String bowtieIndex;
        @Parameter(names = GlobalConfigFormat.MOUSE_XENOME_INDEX)
        private String mouseXenomeIndex;
        @Parameter(names = GlobalConfigFormat.SEQUENZA_GC50)
        private String sequenzaGc50;
        @Parameter(names = GlobalConfigFormat.CONTEST_POPAF)
        private String contEstPopAF;
        @Parameter(names = GlobalConfigFormat.GENOME_LOAD)
        private String genomeLoad;
        @Parameter(names = GlobalConfigFormat.COUNT_TARGET_PANEL)
        private String cellrangerCountTargetPanel;
        @Parameter(names = GlobalConfigFormat.GERMLINE_RESOURCE)
        private String germlineResource;
    }

    @Data
    public static class ToolConfig {
        @Parameter(names = GlobalConfigFormat.BEDTOOLS)
        private String bedTools;
        @Parameter(names = GlobalConfigFormat.STAR)
        private String star;
        @Parameter(names = GlobalConfigFormat.SEQPURGE)
        private String seqpurge;
        @Parameter(names = GlobalConfigFormat.TRIMMOMATIC)
        private String trimmomatic;
        @Parameter(names = GlobalConfigFormat.XENOME)
        private String xenome;
        @Parameter(names = GlobalConfigFormat.SALMON)
        private String salmon;
        @Parameter(names = GlobalConfigFormat.HISAT2)
        private String hisat2;
        @Parameter(names = GlobalConfigFormat.NOVOALIGN)
        private String novoalign;
        @Parameter(names = GlobalConfigFormat.NOVOALIGN_TUNE)
        private String novoalignTune;
        @Parameter(names = GlobalConfigFormat.JAVA)
        private String java;
        @Parameter(names = GlobalConfigFormat.SAMTOOLS)
        private String samTools;
        @Parameter(names = GlobalConfigFormat.PICARD_VERSION)
        private String picardVersion;
        @Parameter(names = GlobalConfigFormat.PICARD)
        private String picard;
        @Parameter(names = GlobalConfigFormat.SNPSIFT)
        private String snpsift;
        @Parameter(names = GlobalConfigFormat.TRANSVAR)
        private String transvar;
        @Parameter(names = GlobalConfigFormat.GATK)
        private String gatk;
        @Parameter(names = GlobalConfigFormat.PYTHON)
        private String python;
        @Parameter(names = GlobalConfigFormat.R_SCRIPT)
        private String rScript;
        @Parameter(names = GlobalConfigFormat.VARDICT)
        private String vardict;
        @Parameter(names = GlobalConfigFormat.ABRA2)
        private String abra2;
        @Parameter(names = GlobalConfigFormat.DOUBLE_DETECTION_PYTHON)
        private String doubleDetectionPython;
        @Parameter(names = GlobalConfigFormat.BWA)
        private String bwa;
        @Parameter(names = GlobalConfigFormat.RNA_SEQC_JAVA)
        private String rnaseqcJava;
        @Parameter(names = GlobalConfigFormat.MUTECT_JAVA)
        private String mutectJava;
        @Parameter(names = GlobalConfigFormat.RNA_SEQC)
        private String rnaseqc;
        @Parameter(names = GlobalConfigFormat.MUTECT)
        private String mutect;
        @Parameter(names = GlobalConfigFormat.SCALPEL)
        private String scalpel;
        @Parameter(names = GlobalConfigFormat.OPTITYPE)
        private String optitype;
        @Parameter(names = GlobalConfigFormat.CUFFLINKS)
        private String cufflinks;
        @Parameter(names = GlobalConfigFormat.FEATURE_COUNT)
        private String featureCount;
        @Parameter(names = GlobalConfigFormat.STAR_FUSION)
        private String starFusion;
        @Parameter(names = GlobalConfigFormat.CELLRANGER)
        private String cellranger;
        @Parameter(names = GlobalConfigFormat.BOWTIE2)
        private String bowtie2;
        @Parameter(names = GlobalConfigFormat.MIXCR)
        private String mixcr;
        @Parameter(names = GlobalConfigFormat.RSEM)
        private String rsem;
        @Parameter(names = GlobalConfigFormat.RSEMINDEX)
        private String rsemIndex;
        @Parameter(names = GlobalConfigFormat.STRINGTIE)
        private String stringtie;
        @Parameter(names = GlobalConfigFormat.STATUS_CHECK_PERIOD)
        private Integer statusCheckPeriod;
        @Parameter(names = GlobalConfigFormat.FUSION_CATCHER)
        private String fusionCatcher;
        @Parameter(names = GlobalConfigFormat.STRELKA2)
        private String strelka2;
        @Parameter(names = GlobalConfigFormat.LOFREQ)
        private String lofreq;
        @Parameter(names = GlobalConfigFormat.FREEBAYES)
        private String freebayes;
        @Parameter(names = GlobalConfigFormat.SEQUENZA)
        private String sequenza;
        @Parameter(names = GlobalConfigFormat.EXOMECNV)
        private String exomecnv;
        @Parameter(names = GlobalConfigFormat.SRC_SCRIPTS_FOLDER_PATH)
        private String srcPath;
    }

    @Data
    public static class CellrangerConfig {
        @Parameter(names = GlobalConfigFormat.CELLRANGER_EXPECTED_CELLS)
        private String cellrangerExpectedCells;
        @Parameter(names = GlobalConfigFormat.CELLRANGER_FORCED_CELLS)
        private String cellrangerForcedCells;
        @Parameter(names = GlobalConfigFormat.CELLRANGER_NOSECONDARY)
        private String cellrangerNosecondary;
        @Parameter(names = GlobalConfigFormat.CELLRANGER_CHEMISTRY)
        private String cellrangerChemistry;
        @Parameter(names = GlobalConfigFormat.CELLRANGER_R1_LENGTH)
        private String cellrangerR1Length;
        @Parameter(names = GlobalConfigFormat.CELLRANGER_R2_LENGTH)
        private String cellrangerR2Length;
        @Parameter(names = GlobalConfigFormat.CELLRANGER_LANES)
        private String cellrangerLanes;
        @Parameter(names = GlobalConfigFormat.CELLRANGER_INDICES)
        private String cellrangerIndices;
        @Parameter(names = GlobalConfigFormat.CELLRANGER_CHAIN)
        private String cellrangerChain;
        @Parameter(names = GlobalConfigFormat.CELLRANGER_DENOVO)
        private String cellrangerDenovo;
    }

    @Data
    public static class PipelineInfo {
        @Parameter(names = GlobalConfigFormat.WORKFLOW)
        private String workflow;
        @Parameter(names = GlobalConfigFormat.TOOLSET, converter = ToolSetConverter.class)
        private LinkedHashSet<String> toolset;
        @Parameter(names = GlobalConfigFormat.FLAG_XENOME, converter = FlagXenomeConverter.class, arity = 1)
        private boolean flagXenome;
        @Parameter(names = GlobalConfigFormat.READ_TYPE)
        private String readType;
        @Parameter(names = GlobalConfigFormat.LINE_ENDING, converter = EOLConverter.class, arity = 1)
        private EOLMarker lineEnding = LF;
    }
}
