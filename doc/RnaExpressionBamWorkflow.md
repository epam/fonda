# Fonda workflows

## RnaExpression_Bam workflow

The following documentation describes the Fonda **RnaExpression_Bam** workflow launching.  
This document contains a description of the installation requirements, the steps of Fonda building and the launch of the **RnaExpression_Bam** workflow.

### Overall workflow description

**RnaExpression_Bam** workflow is responsible for RNA sequencing data for gene expression analysis using bam data

The workflow provides the following available tools for each analytic step:
- expression estimation: **cufflinks**, **stringtie**, **rsem**
- read count: **feature_count**
- expression data combination: **conversion**

A workflow toolset could contain the following popular options:

- `toolset=featureCount+conversion`
- `toolset=rsem+conversion`
- `toolset=stringtie+conversion` ('conversion' tool is starts RnaAnalysis)

### Software requirements

Before the Fonda launch, it is necessary to prepare execution environment to successful workflow launch. 

-  Install common:

``` bash
sudo su && \ 
apt-get update -y && \ 
apt-get install -y wget \
                   curl \
                   openjdk-8-jdk \
                   openjdk-7-jdk \
                   unzip \
                   git \
                   cmake \
                   libigraph0-dev \
                   gcc \
                   libssl-dev \
                   libcrypto++-dev \
                   libxml2-dev \
                   libgmp-dev \
                   zlib1g-dev \
                   make \
                   g++ \
                   qt5-default \
                   libqt5xmlpatterns5-dev \
                   libqt5sql5-mysql \
                   libbz2-dev \
                   liblzma-dev \
                   libncurses5-dev \
                   libncursesw5-dev \
                   python \
                   python-pip \
                   software-properties-common \
                   python-software-properties \
                   libdb-dev \
                   pkg-config \
                   libjsoncpp-dev
```

-  Install **R** package:

``` bash
echo "deb http://cran.rstudio.com/bin/linux/ubuntu xenial/" | tee -a /etc/apt/sources.list && \ 
gpg --keyserver keyserver.ubuntu.com --recv-key E084DAB9 && \ 
gpg -a --export E084DAB9 | apt-key add - && \ 
apt-get update && \ 
apt-get install r-base r-base-dev 

#[install R packages] 
#[enter to R environment and run]
install.packages("plyr", repos="http://cran.r-project.org") 
```

-  Install **cufflinks**:

``` bash
cd /opt  && \
wget -q "http://cole-trapnell-lab.github.io/cufflinks/assets/downloads/cufflinks-2.2.1.Linux_x86_64.tar.gz" && \
tar -xzf cufflinks-2.2.1.Linux_x86_64.tar.gz
```

-  Install **stringtie**:

``` bash
cd /opt  && \
wget -q "http://ccb.jhu.edu/software/stringtie/dl/stringtie-2.0.6.tar.gz" && \
tar -xzf stringtie-2.0.6.tar.gz && \
make release
```

-  Install **rsem**:

``` bash
cd /opt  && \
git clone https://github.com/deweylab/RSEM.git && \
cd RSEM && \
make install
```

-  Install **Subread** (_feature_count_):

``` bash
cd /opt  && \
wget -q "https://ayera.dl.sourceforge.net/project/subread/subread-1.4.5-p1/subread-1.4.5-p1-Linux-x86_64.tar.gz" && \
tar -xzf subread-1.4.5-p1-Linux-x86_64.tar.gz
```

### Building Fonda 

Fonda package contains two major components:

- Fonda `.jar` file
- `src` folder

The following command will generate a build folder in the current directory. In the build folder, the user can find a `libs` folder, which contains the Fonda `.jar` file and the `src` directory:

``` bash
./gradlew build zip
```

Please make sure `src` folder and `.jar` file are put in the same parental directory for proper usages. Fonda needs to call some external scripts from the `src` folder (`python` and `R` subfolders) in some pipeline usages.

### Workflow launching

-   Set required input parameters:

Prepare **global_config** file that represents a configuration file for a particular pipeline version.  
Example template of the **RnaExpression_Bam** workflow **global\_config** file:

``` bash
[Queue_Parameters]
NUMTHREADS = 4
MAXMEM = 8g
QUEUE = main.q
PE = PE

[Databases]
SPECIES = human
GENOME_BUILD = GRCh38
ANNOTGENE =  /ngs/data/Annotation/Gencode_v26/gencode.v26.annotation.gtf
GENOME = /ngs/data/Sequence/GRCh38.genome.fa
TRANSCRIPTOME = /ngs/data/Sequence/GRCh38.gencode.v26.pc_transcripts.fa
STARINDEX = /ngs/data/Index/STAR_gc26
ANNOTGENESAF = /ngs/data/Annotation/Gencode_v26/gencode.v26.annotation.knowntrx.exon.level1-2.trxlevel1-3.saf
ADAPTER_FWD = AGATCGGAAGAGCACACGTCTGAACTCCAGTCAC
ADAPTER_REV = AGATCGGAAGAGCGTCGTGTAGGGAAAGAGTGTAGATCTCGGTGGTCGCCGTATCATT
ADAPTER_SEQ = /ngs/data/trim_adapters
RSEMINDEX = /ngs/data/Index/RSEM_gc26/GRCh38.genome

[all_tools]
star = /ngs/data/tools/STAR/STAR-STAR_2.4.0h1/bin/Linux_x86_64/STAR
seqpurge = /ngs/data/tools/ngs_bits/ngs-bits/bin/SeqPurge
cufflinks = /ngs/data/tools/cufflinks/cufflinks-2.2.1.Linux_x86_64/cufflinks
feature_count = /ngs/data/tools/subread/subread-1.4.5-p1-Linux-x86_64/bin/featureCounts
java = /usr/lib/jvm/java-8-openjdk-amd64/bin/java
rnaseqc_java = /usr/lib/jvm/java-7-openjdk-amd64/bin/java
samtools = /ngs/data/tools/samtools/samtools-0.1.19/samtools
picard = /ngs/data/tools/picard/picard.jar
rnaseqc = /ngs/data/tools/rnaseqc/RNA-SeQC_v1.1.8.jar
python = /ngs/data/tools/python
Rscript = /ngs/data/tools/Rscript
rsem = /ngs/data/tools/RSEM/
stringtie = /ngs/data/tools/stringtie-2.0.6/stringtie

[Pipeline_Info]
workflow = RnaExpression_Bam
toolset = featureCount+conversion
flag_xenome = no
read_type = paired
```

Prepare **study_config** file that represents a configuration file for a particular study for a specific the NGS data analysis.  
Example template of the **RnaExpression_Bam** workflow **study\_config** file:

``` bash
[Series_Info]
job_name = pe_job
dir_out = /ngs/data/demo/test/RnaExpressionBam_test
bam_list = /ngs/data/demo/test/example/DnaCaptureVar_WES_SampleBamPaths.txt
LibraryType = DNA
DataGenerationSource = Internal
Date = 20200326
Project = Example_project
Run = run1234
Cufflinks.library_type = fr-unstranded
```

- Run **RnaExpression_Bam** workflow in the **_local machine mode_**:

``` bash
java -jar fonda-<VERSION>.jar -global_config gFeatureCountRsemCufflinksStringtie.txt -study_config sRnaExpressionBam.txt -local
```
