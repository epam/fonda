# Fonda workflows

## TcrRepertoire_Fastq workflow

The following documentation describes the Fonda **TcrRepertoire_Fastq** workflow launching.  
This document contains a description of the installation requirements, the steps of Fonda building and the launch of the **TcrRepertoire_Fastq** workflow.

### Overall workflow description

**TcrRepertoire_Fastq** workflow works with DNA or RNA sequencing data for TCR or BCR repertoire detection using fastq data.

The workflow provides the following available tools for each analytic step:

- mouse sequence detection: **xenome**  
- sequence trimming: **trimmomatic**, **seqpurge**
- T- or B- cell receptor repertoire analysis: **mixcr**

A workflow toolset could contain the following popular options:

- `toolset=trimmomatic+mixcr`
- `toolset=seqpurge+mixcr`

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

-  Install **mixcr**:

``` bash
cd /opt  && \
wget -q https://github.com/milaboratory/mixcr/releases/download/v3.0.12/mixcr-3.0.12.zip && \
unzip mixcr-3.0.12.zip
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
Example template of the **TcrRepertoire_Fastq** workflow **global\_config** file:

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
ADAPTER_FWD = AGATCGGAAGAGCACACGTCTGAACTCCAGTCAC
ADAPTER_REV = AGATCGGAAGAGCGTCGTGTAGGGAAAGAGTGTAGATCTCGGTGGTCGCCGTATCATT
ADAPTER_SEQ =  /ngs/data/tools/Trimmomatic/v0.36/adapters/TruSeq3-PE-2.fa
MOUSEXENOMEINDEX = /ngs/data/XenomeIndex/hg19.genome

[all_tools]
seqpurge = /ngs/data/app/ngs-bits/v1.0/bin/SeqPurge
java = /ngs/data/app/java/v1.8.0u121/bin/java
samtools = /ngs/data/tools/samtools/v0.1.19/samtools
mixcr = /ngs/data/tools/MiXCR/v2.1.3/mixcr
python = /ngs/data/app/python/v2.7.13/bin/python
Rscript = /ngs/data/app/R/v3.5.0/bin/Rscript
doubletdetection_python = /ngs/data/py/versions/3.5.2/bin/python
trimmomatic = /ngs/data/tools/Trimmomatic/v0.36/trimmomatic-0.36.jar
xenome = /ngs/data/tools/xenome/v1.0.1-r/xenome

[Pipeline_Info]
workflow = TcrRepertoire_Fastq
toolset = seqpurge+mixcr
flag_xenome = no
read_type = paired
```

Prepare **study_config** file that represents a configuration file for a particular study for a specific the NGS data analysis.  
Example template of the **TcrRepertoire_Fastq** workflow **study\_config** file:

``` bash
[Series_Info]
job_name = pe_job
dir_out = /ngs/data/demo/test/TcrRepertoire_test
fastq_list = /ngs/data/demo/test/example/TcrRepertoire_SampleFastqPaths.txt
LibraryType = RNASeq_Paired
DataGenerationSource = Internal
Date = 20200327
Project = Example_project
Run = run1234
Cufflinks.library_type = fr-unstranded
```

- Run **TcrRepertoire_Fastq** workflow in the **_local machine mode_**:

``` bash
java -jar fonda-<VERSION>.jar -global_config global_config_TcrRepertoire_Fastq.txt -study_config config_TcrRepertoire_Fastq_test.txt -local
```
