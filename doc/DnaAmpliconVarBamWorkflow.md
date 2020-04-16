# Fonda workflows

## DnaAmpliconVar_Bam workflow

The following documentation describes the Fonda **DnaAmpliconVar_Bam** workflow launching.  
This document contains a description of the installation requirements, the steps of Fonda building and the launch of the **DnaAmpliconVar_Bam** workflow.

### Overall workflow description

**DnaAmpliconVar_Bam** works with DNA Amplicon sequencing data for genomic variant detection using bam data.

The workflow provides the following available tools for each analytic step:
 
- variant detection: **gatk** (is **gatkHaplotypeCaller** in the toolset), **mutect**  (is **mutect1** in the toolset), **mutect2**, **vardict**, **lofreq**, **strelka2**, **freebayes**, **scalpel**
- CNV detection: **sequenza**, **exomecnv**
- contamination estimation: **contEst**
- variant annotation: **snpsift** (associate with transvar)
- qc: **qc**
- data processing: **samtools**

A workflow toolset could contain the following popular options:

- `toolset=vardict+mutect1+gatkHaplotypeCaller`  
- `toolset=mutect2+strelka2`  
- `toolset=mutect1+scalpel`

### Software requirements

Before the Fonda launch it is necessary to prepare execution environment to successful workflow launch. 

-  Install common:

```bash
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

```bash
echo "deb http://cran.rstudio.com/bin/linux/ubuntu xenial/" | tee -a /etc/apt/sources.list && \ 
gpg --keyserver keyserver.ubuntu.com --recv-key E084DAB9 && \ 
gpg -a --export E084DAB9 | apt-key add - && \ 
apt-get update && \ 
apt-get install r-base r-base-dev 

#[install R packages] 
#[enter to R environment and run]
install.packages("plyr", repos="http://cran.r-project.org") 
```

-  Install **gatk**:

```bash
cd /opt  && \
wget -q "https://console.cloud.google.com/storage/browser/_details/gatk-software/package-archive/gatk/GenomeAnalysisTK-3.7-0-gcfedb67.tar.bz2" && \
tar -xf GenomeAnalysisTK-3.7-0-gcfedb67.tar.bz2
```

-  Install **vardict**:

```bash
cd /opt  && \
git clone --recursive https://github.com/AstraZeneca-NGS/VarDictJava.git && \
./gradlew clean installDist
```

-  Install **transvar**:

```bash
# Install 3rd party dependencies
apt-get install -y libevent-dev && \
pip install transvar && \
transvar config --download_anno --refversion [reference name] && \
transvar config --download_ref --refversion [reference name]
```

-  Install **mutect1**:

```bash
cd /opt  && \
git clone git@github.com:broadinstitute/mutect.git  && \

# get the GATK source and set to the latest tested version
git clone git@github.com:broadgsa/gatk-protected.git  && \
cd gatk-protected  && \
git reset --hard 3.1   && \

# build the GATK first and install it to the local mvn repo
mvn -Ddisable.queue install  && \

# build MuTect (the target jar will be in target/mutect-*.jar)
cd ../mutect  && \
mvn verify
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

- Install **strelka2**

```bash
cd /opt  && \
wget https://github.com/Illumina/strelka/releases/download/v2.9.2/strelka-2.9.2.centos6_x86_64.tar.bz2  && \
tar xvjf strelka-2.9.2.centos6_x86_64.tar.bz2
```

- Install **scalpel**

```bash
cd /opt  && \
git clone https://github.com/sleuthkit/scalpel.git && \
cd scalpel && \
./bootstrap && \
./configure && \
make
```

-  Install **samtools**:

```bash
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

```bash
cd /opt  && \
wget -q "https://github.com/broadinstitute/picard/releases/download/2.10.3/picard.jar"
```

- Install **sequenza**

```bash
#[enter to R environment and run] 
# Install 3rd party dependencies
# From the 3.6 R version 
if (!requireNamespace("BiocManager", quietly = TRUE))
    install.packages("BiocManager")
BiocManager::install("copynumber")

# Prior to 3.5 R version
source("https://bioconductor.org/biocLite.R")
biocLite("copynumber")

install.packages("sequenza")
```

- Install **ExomeCNV**

```bash
#[enter to R environment and run] 
# Install 3rd party dependencies
if (!requireNamespace("BiocManager", quietly = TRUE))
    install.packages("BiocManager")
BiocManager::install("DNAcopy")

# Package ‘ExomeCNV’ was removed from the CRAN repository. Install ExomeCNV obtained from the archive.
wget -q "https://cran.r-project.org/src/contrib/Archive/ExomeCNV/ExomeCNV_1.4.tar.gz" && \
/usr/bin/R CMD INSTALL ExomeCNV_1.4.tar.gz
```

### Building Fonda 

Fonda package contains two major components:

- Fonda `.jar` file
- `src` folder

The following command will generate a build folder in the current directory. In the build folder, the user can find a `libs` folder, which contains the Fonda `.jar` file and the `src` directory:

``` bash
./gradlew clean build zip
```

Please make sure `src` folder and `.jar` file are put in the same parental directory for proper usages. Fonda needs to call some external scripts from the `src` folder (`python` and `R` subfolders) in some pipeline usages.

### Workflow launching

-   Set required input parameters:

Prepare **global_config** file that represents a configuration file for a particular pipeline version.

| Section | Parameters |
| --- | --- |
| **\[Queue_Parameters\]** | NUMTHREADS (4)<br/>MAXMEM (8g)<br/>QUEUE (all.q/c32.q)<br/>PE (-pe threaded) |
| **\[all\_tools\]**<br/>need to install properly before running Fonda pipeline | java, python, Rscript, gatk, vardict, mutect, lofreq, strelka2, freebayes, samtools, picard, transvar, snpsift, xenome, src_scripts |
| **\[Databases\]**<br/>need to download/prepare properly before running Fonda pipeline | SPECIES (human/mouse)<br/>BED<br/>BED_WITH_HEADER<br/>BED_FOR_COVERAGE<br/>SNPSIFTDB (for snpsift)<br/>CONTEST_POPAF (for contEst)<br/>CANONICAL_TRANSCRIPT<br/>GENOME<br/>GENOME_BUILD (hg19/GRCh38/mm10)<br/> |
| **\[Pipeline_Info\]** | workflow<br/>toolset<br/>flag_xenome (yes/no)<br/>read_type (paired/single) |

Example template of the **DnaAmpliconVar_Bam** workflow **global\_config** file:

```bash
[Queue_Parameters]
NUMTHREADS = 2
MAXMEM = 24g
QUEUE = all.q
PE = -pe threaded

[Databases]
SPECIES = human
GENOME_BUILD = hg19
GENOME = /ngs/test/data/hg19.decoy.fa]
MUTECT_NORMAL_PANEL = /ngs/test/data/refseq_exome_hg19_1kg_normal_panel_decoy.vcf
BED_PRIMER = /ngs/test/data/CHP2_amplicon_regions_including_primer.bed
BED = /ngs/test/data/data_padded.bed
BED_WITH_HEADER = /ngs/test/data/CHP2_target_regions_hg19.interval_list
BED_FOR_COVERAGE = /ngs/test/data/CHP2_target_regions_hg19_interval_for_coverage.txt
SNPSIFTDB = /opt/SnpEff/snpEff_v4.3p/snpEff/db
CANONICAL_TRANSCRIPT = /ngs/test/data/prefer_ensembl_transcript.txt
CONTEST_POPAF = /ngs/test/data/hg19_population_stratified_af_hapmap_3.3.vcf
SEQUENZA_GC50 = /opt/sequenza/hg19.decoy.gc50Base.txt.gz

[all_tools]
bedtools = /opt/bedtools2/v2.2.1/bin/bedtools
java = /usr/lib/jvm/java-8-openjdk-amd64/bin/java
mutect_java = /usr/lib/jvm/java-7-openjdk-amd64/bin/java
samtools = /opt/samtools/v0.1.19/samtools
picard = /opt/picard/v2.10.3/picard.jar
snpsift = /opt/SnpEff/snpEff_v4.3p/snpEff/SnpSift.jar
transvar = /usr/local/bin/transvar
mutect = /opt/MuTect/v1.1.7/mutect-1.1.7.jar
scalpel = /opt/scalpel/v0.5.3
python = /usr/bin/python
Rscript = /usr/bin/Rscript
gatk = /opt/GATK/v3.7/GenomeAnalysisTK.jar
vardict = /opt/VarDictJava/v1.5.0
lofreq = /opt/lofreq/v2.1.2/bin/lofreq
freebayes = /opt/freebayes/bin/freebayes
sequenza = /opt/sequenza
exomecnv = /opt/exomecnv
strelka2 = /opt/strelka2/v2.9.6/bin

[Pipeline_Info]
workflow = DnaAmpliconVar_Bam
toolset = mutect1+scalpel
flag_xenome = no
read_type = single
```

Prepare **study_config** file that represents a configuration file for a particular study for a specific the NGS data analysis.  
Example template of the **DnaAmpliconVar_Bam** workflow **study\_config** file:

```bash
[Series_Info]
job_name = pe_job
dir_out = /home/fonda/DnaAmpliconVar_Bam_test
bam_list = /home/fonda/DnaAmplicon_SampleBamPaths.txt
LibraryType = DNAAmpliconSeq_Single
DataGenerationSource = Internal
Date = 20200401
Project = Example_project
Run = run1234
Cufflinks.library_type = fr-unstranded  
```

- Run **DnaAmpliconVar_Bam** workflow in the **_local machine mode_**:

``` bash
java -jar fonda-<VERSION>.jar -global_config global_config_DnaAmpliconVar_Bam.txt -study_config config_DnaAmpliconVar_Bam_test.txt -local
```
