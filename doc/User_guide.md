# Additional sections

- [The format of the input manifest file](#the-format-of-the-input-manifest-file-for-batch-processing)
- [Available parameter options in **global_config** files](#available-parameter-options-in-globalconfig-for-major-workflows)
- [Popular toolsets in different workflows](#popular-toolsets-in-different-workflows)

## The format of the input manifest file for batch processing

The input manifest file path defines the path to the file that contains the file list for the parameter `fastq_list` (Fastq pipelines) or `bam_list` (Bam pipelines) in the **study_config** file.

### DNA sequencing data from FASTQ

| parameterType | shortName | Parameter1 | Parameter2 (optional column) | sample_type (optional column) | match_control (optional column) |
| --- | --- | --- | --- | --- | --- |
| _fastqFile_ | _sample1_ | _sample1.R1.fastq1.gz_ | _sample1.R2.fastq2.gz_ (if paired reads) | _tumor_ / _normal_ | _sample1\_N_ |

**shortName** can be shared among different technical replicates for one sample ID, while cannot be redundant for different biological samples.

### DNA sequencing data from BAM

| parameterType | shortName | Parameter1 | sample_type (optional column) | match_control (optional column) |
| --- | --- | --- | --- | --- |
| _bamFile_ | _sample1_ | _sample1.bam_ | _tumor_ / _normal_ | _sample1\_N_ |

**shortName** shall be unique.

### RNA sequencing data from FASTQ

| parameterType | shortName | Parameter1 | Parameter2 (optional column) |
| --- | --- | --- | --- |
| _fastqFile_ | _sample1_ | _sample1.R1.fastq1.gz_ | _sample1.R2.fastq2.gz_ (if paired reads) |

**shortName** can be shared among different technical replicates for one sample ID, while cannot be redundant for different biological samples.

### RNA sequencing data from BAM

| parameterType | shortName | Parameter1 |
| --- | --- | --- |
| _bamFile_ | _sample1_ | _sample1.bam_ |

**shortName** shall be unique.

Examples of input manifest files you can see [here](../../example/sample_manifest/).

## Available parameter options in **global_config** for major workflows

Users should choose to set the tools and databases as their specific pipeline needs, do not need to install all tools. In addition, when set the parameter names in the **global_config** file, make sure they exactly match the names in the table, the names are case sensitive.

### RnaCaptureVar_Fastq

| Section | Parameters |
| --- | --- |
| **\[Queue_Parameters\]** | NUMTHREADS (4)<br/>MAXMEM (8g)<br/>QUEUE (all.q/c32.q)<br/>PE (-pe threaded) |
| **\[all\_tools\]**<br/>need to install properly before running Fonda pipeline | Star, hisat2, seqpurge, trimmomatic, java, python, Rscript, gatk, abra2, vardict, mutect1, lofreq, strelka2, freebayes, sequenza, exomecnv, samtools, picard, transvar, snpsift, xenome, contest, src_scripts |
| **\[Databases\]**<br/>need to download/prepare properly before running Fonda pipeline | SPECIES (human/mouse)<br/>BED<br/>BED_WITH_HEADER<br/>BED_FOR_COVERAGE<br/>SNPSIFTDB (for snpsift)<br/>MOUSEXENOMEINDEX (for xenome)<br/>CONTEST_POPAF (for contest)<br/>CANONICAL_TRANSCRIPT<br/>GENOME<br/>GENOME_BUILD (hg19/GRCh38/mm10)<br/>KNOWN_INDELS_MILLS (for gatk_realign)<br/>KNOWN_INDELS_PHASE1 (for gatk_realign)<br/>DBSNP (for gatk_realign)<br/>COSMIC (for gatk_realign)<br/>NOVOINDEX (for novoalign)<br/>ADAPTER_SEQ (for seqpurge)<br/>ADAPTER_FWD (for trimmomatic)<br/>ADAPTER_REV (for trimmomatic) |
| **\[Pipeline_Info\]** | workflow<br/>toolset<br/>flag_xenome (yes/no)<br/>read_type (paired/single) |

### RnaExpression_Fastq

| Section | Parameters |
| --- | --- |
| **\[Queue_Parameters\]** | NUMTHREADS (4)<br/>MAXMEM (8g)<br/>QUEUE (all.q/c32.q)<br/>PE (-pe threaded) |
| **\[all\_tools\]**<br/>need to install properly before running Fonda pipeline | star, hisat2, seqpurge, trimmomatic, java, rnaseqc_java, python, Rscript, cufflinks, rsem, stringtie, feature_count, samtools, picard, rnaseqc, xenome, src_scripts |
| **\[Databases\]**<br/>need to download/prepare properly before running Fonda pipeline | SPECIES (human/mouse)<br/>ANNOTGENE<br/>GENOME<br/>GENOME_BUILD (hg19/GRCh38)<br/>TRANSCRIPTOME<br/>ANNOTGENESAF<br/>STARINDEX (for star)<br/>MOUSEXENOMEINDEX (for xenome)<br/>ADAPTER_SEQ (for seqpurge)<br/>ADAPTER_FWD (for trimmomatic)<br/>ADAPTER_REV (for trimmomatic) |
| **\[Pipeline_Info\]** | workflow<br/>toolset<br/>flag_xenome (yes/no)<br/>read_type (paired/single) |

### DnaCaptureVar_Fastq

| Section | Parameters |
| --- | --- |
| **\[Queue_Parameters\]** | NUMTHREADS (4)<br/>MAXMEM (8g)<br/>QUEUE (all.q/c32.q)<br/>PE (-pe threaded) |
| **\[all\_tools\]**<br/>need to install properly before running Fonda pipeline | bwa, novoalign, seqpurge, trimmomatic, java, python, Rscript, gatk, abra2, vardict, mutect1, mutect2, lofreq, strelka2, freebayes, sequenza, exomecnv, samtools, picard, transvar, snpsift, xenome, contest, src_scripts |
| **\[Databases\]**<br/>need to download/prepare properly before running Fonda pipeline | SPECIES (human/mouse)<br/>BED<br/>BED_WITH_HEADER<br/>BED_FOR_COVERAGE<br/>SNPSIFTDB (for snpsift)<br/>MOUSEXENOMEINDEX (for xenome)<br/>CONTEST_POPAF (for contest)<br/>CANONICAL_TRANSCRIPT<br/>GENOME<br/>GENOME_BUILD (hg19/GRCh38/mm10)<br/>KNOWN_INDELS_MILLS (for gatk_realign)<br/>KNOWN_INDELS_PHASE1 (for gatk_realign)<br/>DBSNP (for gatk_realign)<br/>COSMIC (for gatk_realign)<br/>NOVOINDEX (for novoalign)<br/>ADAPTER_SEQ (for seqpurge)<br/>ADAPTER_FWD (for trimmomatic)<br/>ADAPTER_REV (for trimmomatic) |
| **\[Pipeline_Info\]** | workflow<br/>toolset<br/>flag_xenome (yes/no)<br/>read_type (paired/single) |

### DnaAmpliconVar_Fastq

| Section | Parameters |
| --- | --- |
| **\[Queue_Parameters\]** | NUMTHREADS (4)<br/>MAXMEM (8g)<br/>QUEUE (all.q/c32.q)<br/>PE (-pe threaded) |
| **\[all\_tools\]**<br/>need to install properly before running Fonda pipeline | bwa, novoalign, seqpurge, trimmomatic, java, python, Rscript, gatk, abra2, vardict, mutect1, mutect2, lofreq, strelka2, freebayes, samtools, picard, transvar, snpsift, xenome, src_scripts |
| **\[Databases\]**<br/>need to download/prepare properly before running Fonda pipeline | SPECIES (human/mouse)<br/>BED<br/>BED_WITH_HEADER<br/>BED_FOR_COVERAGE<br/>SNPSIFTDB (for snpsift)<br/>MOUSEXENOMEINDEX (for xenome)<br/>CONTEST_POPAF (for contest)<br/>CANONICAL_TRANSCRIPT<br/>GENOME<br/>GENOME_BUILD (hg19/GRCh38/mm10)<br/>KNOWN_INDELS_MILLS (for gatk_realign)<br/>KNOWN_INDELS_PHASE1 (for gatk_realign)<br/>DBSNP (for gatk_realign)<br/>COSMIC (for gatk_realign)<br/>NOVOINDEX (for novoalign)<br/>ADAPTER_SEQ (for seqpurge)<br/>ADAPTER_FWD (for trimmomatic)<br/>ADAPTER_REV (for trimmomatic) |
| **\[Pipeline_Info\]** | workflow<br/>toolset<br/>flag_xenome (yes/no)<br/>read_type (paired/single) |

### scRnaExpression_CellRanger_Fastq

| Section | Parameters |
| --- | --- |
| **\[Queue_Parameters\]** | NUMTHREADS (4)<br/>MAXMEM (8g)<br/>QUEUE (all.q/c32.q)<br/>PE (-pe threaded) |
| **\[all\_tools\]**<br/>need to install properly before running Fonda pipeline | cellranger, java, python, Rscript, samtools, picard, src_scripts |
| **\[Databases\]**<br/>need to download/prepare properly before running Fonda pipeline | SPECIES (human/mouse)<br/>GENOME_BUILD (hg19/GRCh38/mm10)<br/>GENOME<br/>TRANSCRIPTOME |
| **\[cellranger\]** | cellranger_EXPECTED_CELLS<br/>cellranger_FORCED_CELLS<br/>cellranger_NOSECONDARY<br/>cellranger_CHEMISTRY<br/>cellranger_R1-LENGTH<br/>cellranger_R2-LENGTH<br/>cellranger_LANES<br/>cellranger_INDICES |
| **\[Pipeline_Info\]** | workflow<br/>toolset<br/>flag_xenome (yes/no)<br/>read_type (paired/single) |

## Popular toolsets in different workflows

A toolset contains a number of tools users want to run in a specific pipeline version. The combination of tools represent the components that users want Fonda to execute for a particular study dataset.  
As we mentioned previously, any change in the **global_config** would generate a new pipeline version. Therefore, toolsets that contain different software step combinations will result to different pipeline versions.  
Below there are a few popular toolsets for different workflows.  
**_Note_**: make sure each individual tool executes properly before you use it in the Fonda context.

### RnaExpression\_Fastq

**Available tools for each analytic step**:  
mouse sequence detection: `xenome`  
sequence trimming: `trimmomatic`, `seqpurge`  
sequence alignment: `star`, `hisat2`  
expression estimation: `cufflinks`, `stringtie`, `rsem`  
read count: `feature_count`  
qc: `qc`, `rnaseqc`  
data processing: `samtools`, `picard`  
expression data combination: `conversion`

**Popular toolset options**:  
`toolset=star+qc+featureCount+cufflinks+conversion`  
`toolset=star+qc+featureCount+rsem+conversion`  
`toolset=hisat2+qc+featureCount+stringtie`  
`toolset=star+qc` (specific for bam reads QC examination)

### DnaCaptureVar\_Fastq

**Available tools for each analytic step**:  
mouse sequence detection: `xenome`  
sequence trimming: `trimmomatic`, `seqpurge`  
sequence alignment: `bwa`, `novoalign`  
sequence realignment: `abra2`, `gatk`  
variant detection: `gatk`, `mutect`, `mutect2`, `vardict`, `lofreq`, `strelka2`, `freebayes`, `scalpel`  
CNV detection: `sequenza`, `exomecnv`  
variant annotation: `snpsift` (associate with `transvar`), `oncotation`  
qc: `qc`  
data processing: `samtools`, `picard`

**Popular toolset options**:  
`toolset=bwa+abra_realign+picard+vardict+mutect2+qc`  
`toolset=novoalign+gatk_realign+picard+strelka2+snpsift+qc`  
`toolset=bwa+abra_realign+picard+qc` (specific for bam reads QC examination)

### DnaAmpliconVar\_Fastq

**Available tools for each analytic step**:  
mouse sequence detection: `xenome`  
sequence trimming: `trimmomatic`, `seqpurge`  
sequence alignment: `bwa`, `novoalign`  
sequence realignment: `abra2`, `gatk`  
variant detection: `gatk`, `mutect`, `mutect2`, `vardict`, `lofreq`, `strelka2`, `freebayes`, `scalpel`  
CNV detection: `sequenza`, `exomecnv`  
variant annotation: `snpsift` (associate with `transvar`), `oncotation`  
qc: `qc`  
data processing: `samtools`, `picard`

**Popular toolset options**:  
`toolset= novoalign+abra_realign+picard+vardict+mutect1+snpsift`  
`toolset= bwa+abra_realign+picard+vardict+strelka2+snpsift`  
`toolset=bwa+abra_realign+picard+qc` (specific for bam reads QC examination)

### RnaCaptureVar\_Fastq

**Available tools for each analytic step**:  
mouse sequence detection: `xenome`  
sequence trimming: `trimmomatic`, `seqpurge`  
sequence alignment: `star`, `hisat2`  
sequence realignment: `abra2`, `gatk`  
variant detection: `gatk`, `mutect`, `vardict`, `lofreq`, `strelka2`, `freebayes`, `scalpel`  
variant annotation: snpsift (associate with `transvar`), `oncotation`  
qc: `qc`  
data processing: `samtools`, `picard`

**Popular toolset options**:
`toolset=star+abra_realign+picard+vardict+qc`  
`toolset=star+gatk_realign+picard+strelka2+snpsift+qc`  
`toolset=star+abra_realign+picard+qc` (specific for bam reads QC examination)

### scRnaExpresson\_Fastq

**Available tools for each analytic step**:  
sequence alignment and analysis: `count`  
doublelet detection: `doubletdetection`, `scrublet`  
qc: `qc`  
data processing: `samtools`, `picard`, `python`, `Rscript`  

**Popular toolset options**:  
`toolset=count`  
`toolset=count+qc`  
`toolset=count+doubletdetection`  
`toolset=count+scrublet`  
`toolset=count+doubletdetection+scrublet`  
