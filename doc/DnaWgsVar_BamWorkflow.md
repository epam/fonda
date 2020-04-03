# Fonda workflows

## DnaWgsVar_Bam workflow

The following documentation describes the Fonda **DnaWgsVar_Bam** workflow launching.  
This document contains a description of the installation requirements, the steps of Fonda building and the launch of the **DnaWgsVar_Bam** workflow.

### Overall workflow description

**DnaWgsVar_Bam** workflow is responsible for DNA whole genome sequencing data for genomic variant detection using bam data.
The workflow provides the following available tools for each analysis step:

- variant detection: **gatk**, **mutect2**, **lofreq**, **strelka2**, **freebayes**
- contamination estimation: **contEst**
- data processing: **samtools**, **picard**

A workflow toolset could contain the following popular option:

- `toolset=strelka2`
- `toolset=lofreq`
- `toolset=gatkHaplotypeCaller+freebayes`

### Software requirements

Before the Fonda launch, it is necessary to prepare execution environment for successful workflow launch.

- Install common:

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

- Install **strelka2**

```bash
wget https://github.com/Illumina/strelka/releases/download/v2.9.2/strelka-2.9.2.centos6_x86_64.tar.bz2  && \
tar xvjf strelka-2.9.2.centos6_x86_64.tar.bz2
```

- Install **lofreq**

```bash
cd /opt  && \
git clone https://github.com/CSB5/lofreq.git && \
cd lofreq && \
./configure && \
make && \
make install
```
**Note:** You can find lofreq in /usr/local/bin/lofreq

- Install **freebayes**

```bash
cd /opt  && \
git clone --recursive git://github.com/ekg/freebayes.git && \
cd freebayes && \
make
```

- Install **gatk**

```bash
cd /opt  && \
wget https://storage.cloud.google.com/gatk-software/package-archive/gatk/GenomeAnalysisTK-3.7-0-gcfedb67.tar.bz2 && \
tar -xf package-archive_gatk_GenomeAnalysisTK-3.7-0-gcfedb67.tar
```

### Building Fonda

Fonda package contains two major components:

- Fonda `.jar` file
- `src` folder

The following command will generate a build folder in the current directory. In the build folder, the user can find a `libs` folder, which contains the Fonda `.jar` file and the `src` directory:

``` bash
./gradlew build zip
```

If the `src_scripts` option in global config is not set, please make sure `src` folder and `.jar` file are put 
in the same parental directory for proper usages. Fonda needs to call some external scripts from the `src` folder 
(`python` and `R` subfolders) in some pipeline usages.

### Workflow launching

- Set required input parameters:

Prepare **global_config** file that represents a configuration file for a particular pipeline version.  
Example template of the **DnaWgsVar_Bam** workflow **global\_config** file:

``` bash
[Queue_Parameters]
NUMTHREADS = 4
MAXMEM = 8g
QUEUE = main.q
PE = -pe threaded

[all_tools]
java = /usr/lib/jvm/java-8-openjdk-amd64/bin/java
rnaseqc_java = /usr/lib/jvm/java-7-openjdk-amd64/bin/java
samtools = /opt/samtools/samtools-0.1.19/samtools
picard = /opt/picard/picard.jar
rnaseqc = /opt/rnaseqc/RNA-SeQC_v1.1.8.jar
python = /usr/bin/python
Rscript = /usr/bin/Rscript
bwa = /opt/bwa/bwa
transvar = /usr/local/bin/transvar
gatk = /usr/bin/gatk
freebayes = /usr/bin/freebayes
lofreq = /usr/bin/lofreq

[Databases]
SPECIES = human
GENOME_BUILD = genome.build
ADAPTER_FWD = AGATCGGAAGAGCACACGTCTGAACTCCAGTCAC
ADAPTER_REV = AGATCGGAAGAGCGTCGTGTAGGGAAAGAGTGTAGATCTCGGTGGTCGCCGTATCATT
ADAPTER_SEQ = AGATCGGAAGAT
CONTEST_POPAF = /ngs/data/hg19_population_stratified_af_hapmap_3.3.vcf

[Pipeline_Info]
workflow = DnaWgsVar_Bam
toolset = lofreq
read_type = single
flag_xenome = no
```

Prepare **study_config** file that represents a configuration file for a particular study for a specific the NGS data analysis.  
Example template of the **DnaWgsVar_Bam** workflow **study\_config** file:

``` bash
[Series_Info]
job_name = DnaWgsVar_Bam
dir_out = build/resources/integrationTest/output
bam_list = build/resources/integrationTest/bamlist.txt
LibraryType = DNAWholeExomeSeq_Single
DataGenerationSource = Internal
Date = 200403
Project = Example_project
Run = run1234
```

- Run **DnaWgsVar_Bam** workflow in the **_local machine mode_**:

``` bash
java -jar fonda-<VERSION>.jar -global_config global_config_DnaWgsVar_Bam_v1.1.txt -study_config config_DnaWgsVar_Bam_test.txt -local
```
