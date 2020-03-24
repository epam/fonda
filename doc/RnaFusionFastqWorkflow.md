# Fonda workflows

## RnaFusion_Fastq workflow

The following documentation describes the Fonda **RnaFusion_Fastq** workflow launching.  
This document contains a description of the installation requirements, the steps of Fonda building and the launch of the **RnaFusion_Fastq** workflow.

### Overall workflow description

**RnaFusion_Fastq** is RNA sequencing data for gene fusion detection using fastq data.

The workflow provides the following available tools for each analytic step:
 
- mouse sequence detection: **xenome**  
- sequence trimming: **trimmomatic**, **seqpurge**
- fusion detection: **starFusion**, **fusionCatcher**

A workflow toolset could contain the following popular options:

- `toolset=seqpurge+starFusion`  
- `toolset=trimmomatic+starFusion`

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
                   zlib1g \
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

-  Install **Perl** modules:

``` bash
perl -MCPAN -e shell
install DB_File
install URI::Escape
install Set::IntervalTree
install Carp::Assert
install JSON::XS
install PerlIO::gzip
```

-  Install **STAR-Fusion**:

``` bash
git clone --recursive https://github.com/STAR-Fusion/STAR-Fusion.git  && \
cd STAR-Fusion  && \
make
```

-  Install **FusionCatcher**:

``` bash
# Install 3rd party dependencies
apt-get install gawk automake parallel build-essential libc6-dev libtbb-dev libtbb2 python-dev python-numpy \
                python-biopython python-xlrd python-openpyxl default-jdk
git clone https://github.com/ndaniel/fusioncatcher && \
cd fusioncatcher/tools/ && \
./install_tools.sh && \
cd ../data && \
./download-human-db.sh
```

-  Install **picard**:

``` bash
cd /opt  && \
wget -q "https://github.com/broadinstitute/picard/releases/download/2.10.3/picard.jar"
```

-  Install **xenome**:

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

If the `src_scripts` option in global config is not set, please make sure `src` folder and `.jar` file are put in the same parental directory for proper usages. Fonda needs to call some external scripts from the `src` folder (`python` and `R` subfolders) in some pipeline usages.

### Workflow launching

-   Set required input parameters:

Prepare **global_config** file that represents a configuration file for a particular pipeline version.  
Example template of the **RnaFusion_Fastq** workflow **global\_config** file:

``` bash
[Queue_Parameters]
NUMTHREADS = 4
MAXMEM = 24g
QUEUE = all.q
PE = -pe threaded

[Databases]
SPECIES = human
GENOME_BUILD = GRCh38
ANNOTGENE = /ngs/data/reference_genome/GRCh38/Annotation/Gencode_v26/gencode.v26.annotation.gtf
GENOME = /ngs/data/reference_genome/GRCh38/Sequence/GRCh38.genome.fa
STARINDEX = /ngs/data/reference_genome/GRCh38/Index/STAR_gc26
STARFUSIONLIB = /ngs/data/reference_genome/GRCh38/Index/CTAT_lib_gc24
ADAPTER_FWD = AGATCGGAAGAGCACACGTCTGAACTCCAGTCAC
ADAPTER_REV = AGATCGGAAGAGCGTCGTGTAGGGAAAGAGTGTAGATCTCGGTGGTCGCCGTATCATT

[all_tools]
star = /ngs/data/tools/STAR/v2.4.0h1/bin/Linux_x86_64/STAR
seqpurge = /ngs/data/app/ngs-bits/v1.0/bin/SeqPurge
starFusion = /ngs/data/tools/STAR-Fusion/STAR-Fusion/STAR-Fusion
python = /ngs/data/app/python/v2.7.2/bin/python
Rscript = /ngs/data/app/R/v3.5.0/bin/Rscript
doubletdetection_python = /ngs/data/py/versions/3.5.2/bin/python
java = /ngs/data/app/java/v1.8.0u121/bin/java
picard = /ngs/data/app/picard/v2.10.3/picard.jar
samtools = /ngs/data/app/samtools/v0.1.19/samtools
trimmomatic = /ngs/data/tools/Trimmomatic/v0.36/trimmomatic-0.36.jar

[Pipeline_Info]
workflow = RnaFusion_Fastq
toolset = seqpurge+starFusion
flag_xenome = no
read_type = paired
```

Prepare **study_config** file that represents a configuration file for a particular study for a specific the NGS data analysis.  
Example template of the **RnaFusion_Fastq** workflow **study\_config** file:

``` bash
[Series_Info]
job_name = pe_job
dir_out = /ngs/data/demo/test/RnaFusion_test
fastq_list = /ngs/data/demo/test/example/RnaFusion_SampleFastqPaths.txt
LibraryType = RNASeq_Paired
DataGenerationSource = Internal
Date = 20200324
Project = Example_project
Run = run1234
Cufflinks.library_type = fr-unstranded
```

- Run **RnaFusion_Fastq** workflow in the **_local machine mode_**:

``` bash
java -jar fonda-<VERSION>.jar -global_config global_config_RnaFusion_Fastq.txt -study_config config_RnaFusion_Fastq_test.txt -local
```
