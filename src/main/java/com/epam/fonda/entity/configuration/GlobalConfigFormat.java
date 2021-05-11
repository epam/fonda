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

package com.epam.fonda.entity.configuration;

/**
 * The <tt>GlobalConfigFormat</tt> class represents the user specified constants from global config file
 */
public final class GlobalConfigFormat {
    private GlobalConfigFormat() {
    }

    // [Queue_Parameters]
    public static final String NUMTHREADS = "NUMTHREADS";
    public static final String MAXMEM = "MAXMEM";
    public static final String QUEUE = "QUEUE";
    public static final String PE = "PE";

    // [Databases]
    public static final String SPECIES = "SPECIES";
    public static final String GENOME_BUILD = "GENOME_BUILD";
    public static final String GENOME = "GENOME";
    public static final String BED = "BED";
    public static final String BED_PRIMER = "BED_PRIMER";
    public static final String NOVOINDEX = "NOVOINDEX";
    public static final String BED_WITH_HEADER = "BED_WITH_HEADER";
    public static final String BED_FOR_COVERAGE = "BED_FOR_COVERAGE";
    public static final String KNOWN_INDELS_MILLS = "KNOWN_INDELS_MILLS";
    public static final String STARINDEX = "STARINDEX";
    public static final String SALMONINDEX = "SALMONINDEX";
    public static final String HISAT2INDEX = "HISAT2INDEX";
    public static final String DBSNP = "DBSNP";
    public static final String KNOWN_INDELS_PHASE1 = "KNOWN_INDELS_PHASE1";
    public static final String CANONICAL_TRANSCRIPT = "CANONICAL_TRANSCRIPT";
    public static final String ADAPTER_FWD = "ADAPTER_FWD";
    public static final String ADAPTER_REV = "ADAPTER_REV";
    public static final String ADAPTER_SEQ = "ADAPTER_SEQ";
    public static final String SNPSIFTDB = "SNPSIFTDB";
    public static final String ANNOTGENE = "ANNOTGENE";
    public static final String TRANSCRIPTOME = "TRANSCRIPTOME";
    public static final String FEATURE_REFERENCE = "FEATURE_REFERENCE";
    public static final String ANNOTGENESAF = "ANNOTGENESAF";
    public static final String COSMIC = "COSMIC";
    public static final String MUTECT_NORMAL_PANEL = "MUTECT_NORMAL_PANEL";
    public static final String R_RNABED = "rRNABED";
    public static final String STAR_FUSION_LIB = "STARFUSIONLIB";
    public static final String BOWTIE_INDEX = "BOWTIEINDEX";
    public static final String MOUSE_XENOME_INDEX = "MOUSEXENOMEINDEX";
    public static final String SEQUENZA_GC50 = "SEQUENZA_GC50";
    public static final String CONTEST_POPAF = "CONTEST_POPAF";
    public static final String VDJ_GENOME = "VDJ_GENOME";
    public static final String GENOME_LOAD = "GENOME_LOAD";
    public static final String COUNT_TARGET_PANEL = "COUNT_TARGET_PANEL";

    // [all_tools]
    public static final String BEDTOOLS = "bedtools";
    public static final String GATK = "gatk";
    public static final String STAR = "star";
    public static final String SEQPURGE = "seqpurge";
    public static final String TRIMMOMATIC = "trimmomatic";
    public static final String XENOME = "xenome";
    public static final String SALMON = "salmon";
    public static final String HISAT2 = "hisat2";
    public static final String NOVOALIGN = "novoalign";
    public static final String JAVA = "java";
    public static final String SAMTOOLS = "samtools";
    public static final String PICARD_VERSION = "picard_version";
    public static final String PICARD = "picard";
    public static final String SNPSIFT = "snpsift";
    public static final String TRANSVAR = "transvar";
    public static final String VARDICT = "vardict";
    public static final String ABRA2 = "abra2";
    public static final String PYTHON = "python";
    public static final String R_SCRIPT = "Rscript";
    public static final String DOUBLE_DETECTION_PYTHON = "doubletdetection_python";
    public static final String BWA = "bwa";
    public static final String RNA_SEQC_JAVA = "rnaseqc_java";
    public static final String RNA_SEQC = "rnaseqc";
    public static final String MUTECT = "mutect";
    public static final String MUTECT_JAVA = "mutect_java";
    public static final String SCALPEL = "scalpel";
    public static final String OPTITYPE = "optitype";
    public static final String CUFFLINKS = "cufflinks";
    public static final String FEATURE_COUNT = "feature_count";
    public static final String STAR_FUSION = "starFusion";
    public static final String CELLRANGER = "cellranger";
    public static final String BOWTIE2 = "bowtie2";
    public static final String MIXCR = "mixcr";
    public static final String RSEM = "rsem";
    public static final String RSEMINDEX = "RSEMINDEX";
    public static final String STRINGTIE = "stringtie";
    public static final String STATUS_CHECK_PERIOD = "status_check_period";
    public static final String FUSION_CATCHER = "fusionCatcher";
    public static final String STRELKA2 = "strelka2";
    public static final String LOFREQ = "lofreq";
    public static final String FREEBAYES = "freebayes";
    public static final String SEQUENZA = "sequenza";
    public static final String EXOMECNV = "exomecnv";
    public static final String SRC_SCRIPTS_FOLDER_PATH = "src_scripts";

    // [cellranger]
    public static final String CELLRANGER_EXPECTED_CELLS = "cellranger_EXPECTED_CELLS";
    public static final String CELLRANGER_FORCED_CELLS = "cellranger_FORCED_CELLS";
    public static final String CELLRANGER_NOSECONDARY = "cellranger_NOSECONDARY";
    public static final String CELLRANGER_CHEMISTRY = "cellranger_CHEMISTRY";
    public static final String CELLRANGER_R1_LENGTH = "cellranger_R1-LENGTH";
    public static final String CELLRANGER_R2_LENGTH = "cellranger_R2-LENGTH";
    public static final String CELLRANGER_LANES = "cellranger_LANES";
    public static final String CELLRANGER_INDICES = "cellranger_INDICES";
    public static final String CELLRANGER_CHAIN = "cellranger_CHAIN";
    public static final String CELLRANGER_DENOVO = "cellranger_DENOVO";

    // [Pipeline_Info]
    public static final String WORKFLOW = "workflow";
    public static final String TOOLSET = "toolset";
    public static final String FLAG_XENOME = "flag_xenome";
    public static final String READ_TYPE = "read_type";
    public static final String LINE_ENDING = "line_ending";
}
