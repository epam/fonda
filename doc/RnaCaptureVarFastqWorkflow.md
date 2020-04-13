# Fonda workflows

## RnaCaptureVar_Fastq workflow

The following documentation describes the Fonda **RnaCaptureVar_Fastq** workflow launching.  
This document contains a description of the installation requirements, the steps of Fonda building and the launch of the **RnaCaptureVar_Fastq** workflow.

### Overall workflow description

**RnaCaptureVar_Fastq** workflow works with RNA Captured sequencing data for genomic variant detection using fastq data.

The workflow provides the following available tools for each analytic step:
 
- mouse sequence detection: **xenome**  
- sequence trimming: **trimmomatic**, **seqpurge**
- sequence alignment: **star**
- sequence realignment: **abra2**, **gatk**
- variant detection: **gatk**
- variant annotation: **snpsift** (associate with **transvar**)
- qc: **qc**
- data processing: **samtools**, **picard**

A workflow toolset could contain the following popular options:

- `toolset=star+abra_realign+picard+qc`  
- `toolset=star+gatk_realign+picard+gatkHaplotypeCaller+qc`  
- `toolset=star+gatk_realign+qc` (specific for bam reads QC examination)

### Software requirements

Before the Fonda launch it is necessary to prepare execution environment to successful workflow launch. 

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

-  Install **trimmomatic**:

``` bash
cd /opt  && \
wget -q "http://www.usadellab.org/cms/uploads/supplementary/Trimmomatic/Trimmomatic-0.38.zip" && \
unzip Trimmomatic-0.38.zip
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

-  Install **star**:

``` bash
cd /opt  && \
wget -q "https://github.com/alexdobin/STAR/archive/STAR_2.4.0h1.tar.gz" && \
tar -xzf STAR_2.4.0h1.tar.gz
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

-  Install **transvar**:

``` bash
# Install 3rd party dependencies
apt-get install -y libevent-dev && \
pip install transvar && \
transvar config --download_anno --refversion [reference name] && \
transvar config --download_ref --refversion [reference name]
```

-  Install **gatk**:

``` bash
cd /opt  && \
wget -q "https://console.cloud.google.com/storage/browser/_details/gatk-software/package-archive/gatk/GenomeAnalysisTK-3.7-0-gcfedb67.tar.bz2" && \
tar -xf GenomeAnalysisTK-3.7-0-gcfedb67.tar.bz2
```

-  Install **abra2**:

``` bash
cd /opt  && \
git clone --recursive https://github.com/mozack/abra.git && \
cd abra && \
make
```

### Building Fonda 

Fonda package contains two major components:

- Fonda `.jar` file
- `src` folder

The following command will generate a build folder in the current directory. In the build folder, the user can find a `libs` folder, which contains the Fonda `.jar` file and the `src` directory:

``` bash
./gradlew build zip
```

If the `src_scripts` option in global config is not set, please make sure `src` folder and `.jar` file are put in the same parental directory for proper usages. Fonda needs to call some external scripts from the `src` folder (`python` and `R` subfolders) in some pipeline usages.

### Workflow launching

-   Set required input parameters:

Prepare **global_config** file that represents a configuration file for a particular pipeline version.  
Example template of the **RnaCaptureVar_Fastq** workflow **global\_config** file:

``` bash
[Queue_Parameters]
NUMTHREADS = 4
MAXMEM = 24g
QUEUE = all.q
PE = -pe threaded

[Databases]
SPECIES = human
GENOME_BUILD = hg19
GENOME = /ngs/data/reference_genome/hg19/hg19_decoy/hg19.decoy.fa
STARINDEX = /ngs/data/reference_genome/hg19/hg19_decoy/STAR_Index
BED = /ngs/data/data_padded.bed
BED_WITH_HEADER = /ngs/data/data_padded.txt
BED_FOR_COVERAGE = /ngs/data/data_padded.txt
KNOWN_INDELS_MILLS = /ngs/data/public_data/gatk_known_indels/Mills_and_1000G_gold_standard.indels.hg19_decoy.vcf
KNOWN_INDELS_PHASE1 = /ngs/data/public_data/gatk_known_indels/1000G_phase1.indels.hg19_decoy.vcf
DBSNP = /ngs/data/public_data/dbSNP/dbsnp_138.hg19_decoy.vcf
SNPSIFTDB = /ngs/data/tools/SnpEff/snpEff_v4.3p/snpEff/db
CANONICAL_TRANSCRIPT = /ngs/data/reference_genome/GRCh37/Annotation/prefer_ensembl_transcript.txt

[all_tools]
star = /ngs/data/tools/STAR/v2.4.0h1/bin/Linux_x86_64/STAR
java = /ngs/data/app/java/v1.8.0u121/bin/java
samtools = /ngs/data/tools/samtools/v0.1.19/samtools
picard = /ngs/data/tools/picard/v2.10.3/picard.jar
snpsift = /ngs/data/tools/SnpEff/snpEff_v4.3p/snpEff/SnpSift.jar
transvar = /ngs/data/app/python/v2.7.2/bin/transvar
gatk = /ngs/data/tools/GATK/v3.7/GenomeAnalysisTK.jar
python = /ngs/data/app/python/v2.7.2/bin/python
Rscript = /ngs/data/app/R/v3.5.0/bin/Rscript

[Pipeline_Info]
workflow = RnaCaptureVar_Fastq
toolset = star+gatk_realign+picard+qc+gatkHaplotypeCaller
flag_xenome = no
read_type = paired
```

Prepare **study_config** file that represents a configuration file for a particular study for a specific the NGS data analysis.  
Example template of the **RnaCaptureVar_Fastq** workflow **study\_config** file:

``` bash
[Series_Info]
job_name = pe_job
dir_out = /home/fonda/RnaCaptureVar_Fastq_test
fastq_list = /home/fonda/RnaCapture_SampleFastqPaths.txt
LibraryType = RNA_Paired
DataGenerationSource = Internal
Date = 20200331
Project = Example_project
Run = run1234
Cufflinks.library_type = fr-unstranded  
```

- Run **RnaCaptureVar_Fastq** workflow in the **_local machine mode_**:

``` bash
java -jar fonda-<VERSION>.jar -global_config global_config_RnaCaptureVar_Fastq.txt -study_config config_RnaCaptureVar_Fastq_test.txt -local
```
