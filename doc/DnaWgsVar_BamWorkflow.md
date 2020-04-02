# Fonda workflows

## DnaWgsVar_Bam workflow

The following documentation describes the Fonda **DnaWgsVar_Bam** workflow launching.  
This document contains a description of the installation requirements, the steps of Fonda building and the launch of the **DnaWgsVar_Bam** workflow.

### Overall workflow description

**DnaWgsVar_Bam** workflow is responsible for DNA whole genome sequencing data for genomic variant detection using bam data.
The workflow provides the following available tools for each analysis step:

- variant detection: **gatk**, **mutect**, **lofreq**, **strelka2**, **freebayes**
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
star = /opt/STAR/STAR-STAR_2.4.0h1/bin/Linux_x86_64/STAR
seqpurge = /opt/ngs_bits/ngs-bits/bin/SeqPurge
cufflinks = /opt/cufflinks/cufflinks-2.2.1.Linux_x86_64/cufflinks
feature_count = /opt/subread/subread-1.4.5-p1-Linux-x86_64/bin/featureCounts
java = /usr/lib/jvm/java-8-openjdk-amd64/bin/java
rnaseqc_java = /usr/lib/jvm/java-7-openjdk-amd64/bin/java
samtools = /opt/samtools/samtools-0.1.19/samtools
picard = /opt/picard/picard.jar
rnaseqc = /opt/rnaseqc/RNA-SeQC_v1.1.8.jar
python = /usr/bin/python
vardict = /opt/VardictJava/VarDictJava/
Rscript = /usr/bin/Rscript
bwa = /opt/bwa/bwa
trimmomatic = /opt/trimmomatic/trimmomatic-0.38.jar
novoalign = /usr/bin/novoalign
xenome = /usr/bin/xenome
transvar = /usr/local/bin/transvar
snpsift = /opt/snpEff/snpEff/SnpSift.jar 
gatk = /usr/bin/gatk
freebayes = /usr/bin/freebayes
lofreq = /usr/bin/lofreq
mutect = /opt/mutect/mutect-1.1.7.jar
mutect_java = /usr/lib/jvm/java-8-openjdk-amd64/bin/java
scalpel = /opt/scalpel
strelka2 = /opt/strelka2/strelka-2.9.2.centos6_x86_64/bin/
abra2 = /opt/abra2/abra2-2.12.jar
bedtools = /opt/bedtools/bin/bedtools

[Databases]
SPECIES = human
GENOME_BUILD = genome.build
GENOME = /ngs/data/genome
NOVOINDEX = /ngs/data/novoindexDB/novoindex.nix
BED = /ngs/data/S03723314_Padded.bed
BED_WITH_HEADER = /ngs/data/S03723314_Padded_decoy.txt
BED_FOR_COVERAGE = /ngs/data/S03723314_Padded_decoy.txt
BED_PRIMER = /ngs/data/S03723314_Padded_decoy.txt
ADAPTER_FWD = AGATCGGAAGAGCACACGTCTGAACTCCAGTCAC
ADAPTER_REV = AGATCGGAAGAGCGTCGTGTAGGGAAAGAGTGTAGATCTCGGTGGTCGCCGTATCATT
ADAPTER_SEQ = AGATCGGAAGAT
MOUSEXENOMEINDEX = /ngs/data/xenomeIdx/xenome.idx
DBSNP = /ngs/data/db/mutect.dbsnp
COSMIC = /ngs/data/db/cosmic
MUTECT_NORMAL_PANEL = /ngs/data/db/mormal_panel
KNOWN_INDELS_MILLS = 10
KNOWN_INDELS_PHASE1 = 100
CONTEST_POPAF = 100
SNPSIFTDB = /path/to/snpsiftdb
CANONICAL_TRANSCRIPT = /path/to/canonical/transcript

[Pipeline_Info]
workflow = DnaWgsVar_Bam
toolset = strelka2
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
Date = 200320
Project = Example_project
Run = run1234

```

- Run **DnaWgsVar_Bam** workflow in the **_local machine mode_**:

``` bash
java -jar fonda-<VERSION>.jar -global_config global_config_DnaWgsVar_Bam_v1.1.txt -study_config config_DnaWgsVar_Bam_test.txt -local
```
