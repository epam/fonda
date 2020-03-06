# Fonda workflows

## Bam2Fastq workflow

The following documentation describes the Fonda **Bam2Fastq** workflow launching.  
This document contains a description of the installation requirements, the steps of Fonda building and the launch of the **Bam2Fastq** workflow.

### Overall workflow description

**Bam2Fastq** workflow responsible for converting bam file to fastq files.
The workflow provides the following available tools for each analysis step:

- data processing: **picard**, **samtools**

A workflow toolset could contain the following popular option:

- `toolset=picard`

### Software requirements

Before the Fonda launch it is necessary to prepare execution environment for successful workflow launch.

- Install common:

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

- Install **R** package:

``` bash
echo "deb http://cran.rstudio.com/bin/linux/ubuntu xenial/" | tee -a /etc/apt/sources.list && \
gpg --keyserver keyserver.ubuntu.com --recv-key E084DAB9 && \
gpg -a --export E084DAB9 | apt-key add - && \
apt-get update && \
apt-get install r-base r-base-dev

#[install R packages]
#[enter to R environment and run]
install.packages("getopt", repos="http://cran.r-project.org")
install.packages("plyr", repos="http://cran.r-project.org")
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

- Set required input parameters:

Prepare **global_config** file that represents a configuration file for a particular pipeline version.  
Example template of the **Bam2Fastq** workflow **global\_config** file:

``` bash
[Queue_Parameters]
NUMTHREADS = 2
MAXMEM = 24g
QUEUE = all.q
PE = -pe threaded

[Databases]
SPECIES = human

[all_tools]
java = /ngs/data/app/java/v1.8.0u121/bin/java
samtools = /ngs/data/tools/samtools/v0.1.19/samtools
picard = /ngs/data/tools/picard/v2.10.3/picard.jar
python = /ngs/data/app/python/v2.7.2/bin/python
Rscript = /ngs/data/app/R/v3.5.0/bin/Rscript
doubletdetection_python = /ngs/data/py/versions/3.5.2/bin/python

[Pipeline_Info]
workflow = Bam2Fastq
toolset = picard
flag_xenome = no
read_type = paired
```

Prepare **study_config** file that represents a configuration file for a particular study for a specific the NGS data analysis.  
Example template of the **Bam2Fastq** workflow **study\_config** file:

``` bash
[Series_Info]
job_name = pe_job
dir_out = /ngs/data/demo/test/Bam2Fastq_test
bam_list = /ngs/data/demo/test/example/HlaTyping_WES_SampleBamPaths.txt
LibraryType = DNASeq_Paired
DataGenerationSource = Internal
Date = 20140318
Project = Example_project
Run = run1234
Cufflinks.library_type = fr-unstranded
```

- Run **Bam2Fastq** workflow in the **_local machine mode_**:

``` bash
java -jar fonda-<VERSION>.jar -global_config global_config_Bam2Fastq_v1.1.txt -study_config config_Bam2Fastq_test.txt -local
```
