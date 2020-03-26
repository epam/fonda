# Fonda workflows

## scRnaExpression_Fastq workflow

The following documentation describes the Fonda **scRnaExpression_Fastq** workflow launching.  
This document contains a description of the installation requirements, the steps of Fonda building and the launch of the **scRnaExpression_Fastq** workflow.

### Overall workflow description

**scRnaExpression_Fastq** RNA sequencing data for gene expression analysis using fastq data.

The workflow provides the following available tools for each analytic step:
 
- mouse sequence detection: **xenome**  
- sequence trimming: **trimmomatic**, **seqpurge**
- sequence alignment: **star**, **hisat2**
- expression estimation: **cufflinks**, **stringtie**, **rsem**, **salmon** 
- read count: **feature_count**
- qc: **qc**, **rnaseqc**
- data processing: **samtools**, **picard**
- expression data combination: **conversion**

A workflow toolset could contain the following popular options:

- `toolset=star+qc+featureCount+cufflinks+conversion`  
- `toolset=star+qc+featureCount+rsem+conversion`  
- `toolset=hisat2+qc+featureCount+stringtie`  
- `toolset=star+qc` (specific for bam reads QC examination)

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

-  Install **hisat2**:

``` bash
cd /opt  && \
wget -q "ftp://ftp.ccb.jhu.edu/pub/infphilo/hisat2/downloads/hisat2-2.1.0-Linux_x86_64.zip" && \
unzip hisat2-2.1.0-Linux_x86_64.zip
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

-  Install **salmon**:

``` bash
cd /opt  && \
wget -q "https://github.com/COMBINE-lab/salmon/releases/download/v1.1.0/salmon-1.1.0_linux_x86_64.tar.gz" && \
tar xzvf salmon-1.1.0_linux_x86_64.tar.gz
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
Example template of the **scRnaExpression_Fastq** workflow **global\_config** file:

``` bash
[Queue_Parameters]
NUMTHREADS = 4
MAXMEM = 24g
QUEUE = all.q
PE = -pe threaded
[Databases]
SPECIES = human
GENOME_BUILD = GRCh38
ANNOTGENE = /data/test-fonda/Annotation/Gencode_v26/gencode.v26.annotation.gtf
GENOME = /data/test-fonda/Sequence/GRCh38.genome.fa
TRANSCRIPTOME = /data/test-fonda/Sequence/GRCh38.gencode.v26.pc_transcripts.fa
STARINDEX = /data/test-fonda/Index/STAR_gc26
ANNOTGENESAF = /data/test-fonda/Annotation/Gencode_v26/gencode.v26.annotation.saf
rRNABED = /data/test-fonda/Annotation/Gencode_v26/gencode.v26.rRNA.list
ADAPTER_FWD = AGATCGGAAGAGCACACGTCTGAACTCCAGTCAC
ADAPTER_REV = AGATCGGAAGAGCGTCGTGTAGGGAAAGAGTGTAGATCTCGGTGGTCGCCGTATCATT
RSEMINDEX = /data/test-fonda/Index/RSEM_gc26/GRCh38.genome
MOUSEXENOMEINDEX = /data/test-fonda/Index/Xenome/GRCh38.genome
ADAPTER_SEQ = /data/test-fonda/trim_adapters
HISAT2INDEX = /data/test-fonda/Index/HISAT2_gc26/GRCh38.genome
SALMONINDEX = /data/test-fonda/Index/salmon_index

[all_tools]
star = /opt/STAR/STAR-STAR_2.4.0h1/bin/Linux_x86_64/STAR
seqpurge = /opt/ngs_bits/ngs-bits/bin/SeqPurge
cufflinks = /opt/cufflinks/cufflinks-2.2.1.Linux_x86_64/cufflinks
feature_count = /opt/subread/subread-1.4.5-p1-Linux-x86_64/bin/featureCounts
java = /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java
rnaseqc_java = /usr/lib/jvm/java-7-openjdk-amd64/jre/bin/java
samtools = /opt/samtools/samtools-0.1.19/samtools
picard = /opt/picard/picard.jar
rnaseqc = /opt/rnaseqc/RNA-SeQC_v1.1.8.jar
python = /opt/python
Rscript = /opt/Rscript
doubletdetection_python = /ngs/data/py/versions/3.5.2/bin/python
rsem = /opt/RSEM/
trimmomatic = /opt/trimmomatic/trimmomatic-0.38.jar
hisat2 = /opt/hisat2-2.1.0/hisat2
stringtie = /opt/stringtie-2.0.6/stringtie
xenome = /opt/xenome-1.0.1-r/xenome
salmon = /opt/stringtie-2.0.6/salmon-latest_linux_x86_64/bin/salmon

[Pipeline_Info]
workflow = scRnaExpression_Fastq
toolset = seqpurge+star+qc+featureCount+cufflinks+conversion
flag_xenome = no
read_type = paired
```

Prepare **study_config** file that represents a configuration file for a particular study for a specific the NGS data analysis.  
Example template of the **scRnaExpression_Fastq** workflow **study\_config** file:

``` bash
[Series_Info]
job_name = pe_job
dir_out = /home/fonda/scRnaExpression_Fastq_test
fastq_list = /home/fonda/RnaExpression_RNASeq_SampleFastqPaths.txt
LibraryType = RNASeq_Paired
DataGenerationSource = Internal
Date = 20191224
Project = Example_project
Run = run1234
Cufflinks.library_type = fr-unstranded  
```

- Run **scRnaExpression_Fastq** workflow in the **_local machine mode_**:

``` bash
java -jar fonda-<VERSION>.jar -global_config global_config_scRnaExpression_Fastq.txt -study_config config_scRnaExpression_Fastq_test.txt -local
```
