# Fonda workflows

## SCImmuneProfileCellRangerFastq workflow

The following documentation describes the Fonda **SCImmuneProfileCellRangerFastq** workflow launching.  
This document contains a description of the installation requirements, the steps of Fonda building and the launch of the **SCImmuneProfileCellRangerFastq** workflow.

### Overall workflow description

**SCImmuneProfileCellRangerFastq** workflow is 10X single cell RNA/TCR/BCR sequencing data for immune profiling 
using fastq data.

The workflow provides the following available tools for each analytic step:
- sequence assembly and paired clonotype calling: **cellranger vdj**
- qc: **qc**
- data processing: **python**, **Rscript**

A workflow toolset could contain the following popular options:

- `toolset=vdj`
- `toolset=vdj+qc`

### Software requirements

Before the Fonda launch it is necessary to prepare execution environment to successful workflow launch. 

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

-  Install **cellranger**:

``` bash
add-apt-repository ppa:longsleep/golang-backports -y && \
apt-get update -y && \
apt-get install -y golang-1.11-go make clang-6.0 libz-dev libbz2-dev liblzma-dev && \ 
export PATH=/usr/lib/go-1.11/bin:$PATH

#[Install rustup from https://www.rustup.rs/] 
rustup install 1.28.0 && \ 
rustup default 1.28.0 

cd /opt
#[download file from https://support.10xgenomics.com/single-cell-dna/software/downloads/latest] 
tar -xzvf cellranger-[version].tar.gz && \
export PATH=/opt/cellranger-3.0.2:$PATH

#[Setup Cell Ranger] 
source /path/to/cellranger/sourceme.bash 
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
Example template of the **SCImmuneProfileCellRangerFastq** workflow **global\_config** file:

``` bash
[Queue_Parameters] 
NUMTHREADS = 8 
MAXMEM = 50g 
QUEUE = all.q 
PE = -pe threaded 

[Databases] 
SPECIES = human 
GENOME_BUILD = GRCh38 
GENOME = /path/refdata-cellranger-vdj-GRCh38-alts-ensembl-2.0.0

[all_tools] 
cellranger = /path/cellranger 
java = /path/java 
python = /path/python3 
Rscript = /path/Rscript

[cellranger] 
cellranger_FORCED_CELLS = 5000 
cellranger_DENOVO = FALSE
cellranger_CHAIN = auto
cellranger_LANES = NA
cellranger_INDICES = NA

[Pipeline_Info] 
workflow = scImmuneProfile_CellRanger_Fastq
toolset = vdj+qc 
read_type = paired 
```

Prepare **study_config** file that represents a configuration file for a particular study for a specific the NGS data analysis.  
Example template of the **SCImmuneProfileCellRangerFastq** workflow **study\_config** file:

``` bash
[Series_Info] 
job_name = pe_job 
dir_out = /path/scImmuneProfile_CellRanger_Fastq_Test 
fastq_list = /path/scImmuneProfile_CellRanger_FastqPaths.txt 
LibraryType = scRNASeq_Paired 
DataGenerationSource = Internal 
Date = 20191115 
Project = Example_project 
Run = run1234 
Cufflinks.library_type = fr-unstranded   
```

- Run **SCImmuneProfileCellRangerFastq** workflow in the **_local machine mode_**:

``` bash
java -jar fonda-<VERSION>.jar -global_config global_config_scImmuneProfile_CellRanger_Fastq.txt -study_config config_scImmuneProfile_CellRanger_Fastq_test.txt -local
```
