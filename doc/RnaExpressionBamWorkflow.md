# Fonda workflows

## RnaExpression_Bam workflow

The following documentation describes the Fonda **RnaExpression_Bam** workflow launching.  
This document contains a description of the installation requirements, the steps of Fonda building and the launch of the **RnaExpression_Bam** workflow.

### Overall workflow description

**RnaExpression_Bam** workflow is responsible for RNA sequencing data for gene expression analysis using bam data

The workflow provides the following available tools for each analytic step:
- sequence trimming: **seqpurge**
- expression estimation: **cufflinks**, **stringtie**, **rsem**
- read count: **feature_count**
- qc: **rnaseqc**
- data processing: **samtools**, **picard**, **python**, **Rscript**

A workflow toolset could contain the following popular options:

- `toolset=featureCount+rsem+cufflinks+stringtie`

### Software requirements

Before the Fonda launch, it is necessary to prepare execution environment to successful workflow launch. 

-  Install common:

``` bash
sudo su && \ 
apt-get update -y && \ 
apt-get install -y wget curl openjdk-8-jdk unzip git libigraph0-dev \ 
libssl-dev libcrypto++-dev libxml2-dev libgmp-dev zlib1g-dev 

#[install python3.7] 
#[first option] 
apt-get update -y && \ 
apt-get install -y software-properties-common && \ 
add-apt-repository ppa:deadsnakes/ppa && \ 
apt-get update -y && \ 
apt-get install python3.7 

#[second option] 
apt-get update -y && \ 
apt-get install -y build-essential libncurses5-dev libnss3-dev \ 
libreadline-dev libffi-dev && \ 
cd /tmp && \ 
wget https://www.python.org/ftp/python/3.7.2/Python-3.7.2.tar.xz && \ 
tar -xf Python-3.7.2.tar.xz && \ 
cd Python-3.7.2 && \ 
./configure --enable-optimizations && \ 
make -j 1 && \ 
make altinstall 

#[install additional] 
apt-get install -y python3.7-dev python3-lxml && \ 
wget https://bootstrap.pypa.io/get-pip.py -O ./get-pip.py && \ 
python3.7 ./get-pip.py
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

-  Install **seqpurge** _(ngs-bits)_:

``` bash
# Install 3rd party dependencies
apt-get install -y g++ \
                   qt5-default \
                   libqt5xmlpatterns5-dev \
                   libqt5sql5-mysql \
                   python-matplotlib
cd /opt  && \
git clone --recursive https://github.com/imgag/ngs-bits.git && \
cd ngs-bits && \
git checkout 2019_03 && \
git submodule update --recursive --init && \
make -j$(nproc) build_3rdparty && \
make -j$(nproc) build_tools_release
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

-  Install **rnaseqc**:
``` bash
cd /opt  && \
wget -q "http://www.broadinstitute.org/cancer/cga/tools/rnaseqc/RNA-SeQC_v1.1.8.jar"
```

-  Install **samtools**:

``` bash
# Install 3rd party dependencies
apt-get install -y bzip2 && \
cd /opt  && \
wget -q "https://netix.dl.sourceforge.net/project/samtools/samtools/0.1.19/samtools-0.1.19.tar.bz2" && \
tar -xf samtools-0.1.19.tar.bz2 && \
rm -f samtools-0.1.19.tar.bz2 && \
cd samtools-0.1.19 && \
make -j$(nproc)
```

-  Install **picard**:

``` bash
cd /opt  && \
wget -q "https://github.com/broadinstitute/picard/releases/download/2.10.3/picard.jar"
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
ANNOTGENE =  /cloud-data/test-fonda/Annotation/Gencode_v26/gencode.v26.annotation.gtf
GENOME = /cloud-data/test-fonda/Sequence/GRCh38.genome.fa
TRANSCRIPTOME = /cloud-data/test-fonda/Sequence/GRCh38.gencode.v26.pc_transcripts.fa
STARINDEX = /cloud-data/test-fonda/Index/STAR_gc26
ANNOTGENESAF = /cloud-data/test-fonda/Annotation/Gencode_v26/gencode.v26.annotation.knowntrx.exon.level1-2.trxlevel1-3.saf
ADAPTER_FWD = AGATCGGAAGAGCACACGTCTGAACTCCAGTCAC
ADAPTER_REV = AGATCGGAAGAGCGTCGTGTAGGGAAAGAGTGTAGATCTCGGTGGTCGCCGTATCATT
ADAPTER_SEQ = /cloud-data/test-fonda/trim_adapters
RSEMINDEX = /cloud-data/test-fonda/Index/RSEM_gc26/GRCh38.genome

[all_tools]
star = /opt/STAR/STAR-STAR_2.4.0h1/bin/Linux_x86_64/STAR
seqpurge = /opt/ngs_bits/ngs-bits/bin/SeqPurge
cufflinks = /opt/cufflinks/cufflinks-2.2.1.Linux_x86_64/cufflinks
feature_count = /opt/subread/subread-1.4.5-p1-Linux-x86_64/bin/featureCounts
java = /usr/lib/jvm/java-8-openjdk-amd64/bin/java
rnaseqc_java = /usr/lib/jvm/java-7-openjdk-amd64/bin/java
samtools = /opt/samtools/samtools-0.1.19/samtools
picard = /opt/picard/picard.jar
rnaseqc = /opt/rnaseqc/RNA-SeQC_v1.1.8.jar
python = /opt/python
Rscript = /opt/Rscript
rsem = /opt/RSEM/
stringtie = /opt/stringtie-2.0.6/stringtie

[Pipeline_Info]
workflow = RnaExpression_Bam
toolset = featureCount+rsem+cufflinks+stringtie
flag_xenome = no
read_type = paired
feature_count = feature_count 
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
Date = 031814
Project = Example_project
Run = run1234
Cufflinks.library_type = fr-unstranded
```

- Run **RnaExpression_Bam** workflow in the **_local machine mode_**:

``` bash
java -jar fonda-<VERSION>.jar -global_config gFeatureCountRsemCufflinksStringtie.txt -study_config sRnaExpressionBam.txt -local
```
