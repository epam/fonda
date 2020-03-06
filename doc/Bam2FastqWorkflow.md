# Fonda workflows

## Bam2Fastq workflow

The following documentation describes the Fonda **Bam2Fastq** workflow launching.  
This document contains a description of the installation requirements, the steps of Fonda building and the launch of the **Bam2Fastq** workflow.

### Overall workflow description

**Bam2Fastq** is workflow responsible for converting bam file to fastq files.
The workflow provides the following available tools for each analysis step:

- data processing: **picard**, **samtools**

A workflow toolset could contain the following popular option:

- `toolset=picard`

### Software requirements

Before the Fonda launch, it is necessary to prepare execution environment for successful workflow launch.

- Install common:

``` bash
sudo su && \
apt-get update -y && \
apt-get install -y wget curl openjdk-8-jdk unzip git libigraph0-dev \
libssl-dev libcrypto++-dev libxml2-dev libgmp-dev zlib1g-dev
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
bam_list = /ngs/data/demo/test/example/Test_BamToFastq_SampleBamPaths.txt
LibraryType = DNASeq_Paired
DataGenerationSource = Internal
Date = 20200306
Project = Example_project
Run = run1234
Cufflinks.library_type = fr-unstranded
```

- Run **Bam2Fastq** workflow in the **_local machine mode_**:

``` bash
java -jar fonda-<VERSION>.jar -global_config global_config_Bam2Fastq_v1.1.txt -study_config config_Bam2Fastq_test.txt -local
```
