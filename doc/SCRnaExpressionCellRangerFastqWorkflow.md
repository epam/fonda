# Fonda workflows

## SCRnaExpressionCellRangerFastq workflow

The following documentation describes the Fonda **SCRnaExpressionCellRangerFastq** workflow launching.  
This document contains a description of the installation requirements, the steps of Fonda building and the launch of the **SCRnaExpressionCellRangerFastq** workflow.

### Overall workflow description

**SCRnaExpressionCellRangerFastq** workflow is 10X single cell RNA sequencing data analysis for gene expression used fastq data.

The workflow provides the following available tools for each analysis step:

- sequence alignment and analysis: **count**
- doublelet detection: **DoubletDetection**, **Scrublet**
- qc: **qc**
- data processing: **python**, **Rscript**

A workflow toolset could contain the following popular options:

- `toolset=count`
- `toolset=count+qc`
- `toolset=count+doubletdetection+qc`
- `toolset=count+scrublet+qc`
- `toolset=count+doubletdetection+scrublet+qc`

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

- Install **cellranger**:

``` bash
apt-get install make clang-6.0 golang-1.11-go libz-dev libbz2-dev \ liblzma-dev && \
export PATH=/usr/lib/go-1.11/bin:$PATH

#[Install rustup from https://www.rustup.rs/]
rustup install 1.28.0 && \
rustup default 1.28.0 && \
make && \
cd /opt

#[download file from https://support.10xgenomics.com/single-cell-dna/software/downloads/latest]
tar -xzvf cellranger-[version].tar.gz

#[Setup Martian and binary dependencies]
source /path/to/ranger/sourceme.bash
#[Setup Cell Ranger]
source /path/to/cellranger/sourceme.bash

#[Generate Loupe Cell Browser (.cloupe) and Loupe V(D)J Browser files (.vloupe)]
cp path/to/cellranger-3.0.2/cellranger-cs/*/lib/bin/{crconverter,vlconverter} /path/to/open-source-cellranger/lib/bin/
```

- Install **DoubletDetection**:

``` bash
pip3 install cmake && \
git clone --branch v2.4.1 https://github.com/JonathanShor/DoubletDetection.git && \
cd DoubletDetection && \
pip3 install -r requirements.txt && \
pip3 install .
```

- Install **Scrublet**:

``` bash
pip3 install scrublet
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

- Set required input parameters:

Prepare **global_config** file that represents a configuration file for a particular pipeline version.  
Example template of the **SCRnaExpressionCellRangerFastq** workflow **global\_config** file:

``` bash
[Queue_Parameters]
NUMTHREADS = 8
MAXMEM = 50g
QUEUE = all.q
PE = -pe threaded

[Databases]
SPECIES = human,mouse
GENOME_BUILD = hg19,mm10
GENOME = /path/refdata-cellranger-hg19_and_mm10-1.2.0
TRANSCRIPTOME = /path/refdata-cellranger-hg19_and_mm10-1.2.0

[all_tools]
cellranger = /path/cellranger
java = /path/java
python = /path/python3
Rscript = /path/Rscript
doubletdetection_python = /path/python3

[cellranger]
cellranger_EXPECTED_CELLS = 100
cellranger_FORCED_CELLS = NA
cellranger_NOSECONDARY = FALS
cellranger_CHEMISTRY = SC3Pv2
cellranger_R1-LENGTH = NA
cellranger_R2-LENGTH = NA
cellranger_LANES = NA
cellranger_INDICES = NA

[Pipeline_Info]
workflow = scRnaExpression_CellRanger_Fastq
toolset = count+qc+scrublet
read_type = paired
```

Prepare **study_config** file that represents a configuration file for a particular study for a specific the NGS data analysis.  
Example template of the **SCRnaExpressionCellRangerFastq** workflow **study\_config** file:

``` bash
[Series_Info]
job_name = pe_job
dir_out = /path/scRNAexpression_CellRanger_Fastq_test
fastq_list = /path/scRnaExpression_CellRanger_RNASeq_SampleFastqPaths.txt
LibraryType = RNASeq_Paired
DataGenerationSource = Internal
Date = 20191022
Project = Example_project
Run = run1234
Cufflinks.library_type = fr-unstranded
```

- Run **SCRnaExpressionCellRangerFastq** workflow in the **_local machine mode_**:

``` bash
java -jar fonda-<VERSION>.jar -global_config global_config_scRnaExpression_CellRanger_Fastq_v1.1_mouse.txt -study_config config_scRnaExpression_CellRanger_Fastq_test.txt -local
```
