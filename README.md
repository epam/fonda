[![Build Status](https://travis-ci.com/epam/fonda.svg?token=AmpT6giGMGEgYrC2MDsL&branch=develop)](https://travis-ci.com/epam/fonda)
[![codecov](https://codecov.io/gh/epam/fonda/branch/develop/graph/badge.svg?token=XJCQIlChRJ)](https://codecov.io/gh/epam/fonda)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/2d4993e3a192484cbb3e95c6839e64ae)](https://www.codacy.com?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=epam/fonda&amp;utm_campaign=Badge_Grade)

# Fonda

Fonda is a framework which offers scalable and automatic analysis of multiple **NGS** sequencing data types.

- [Required environment setup](#required-environment-setup)
- [Build Fonda](#build-fonda)
- [Fonda installation](#fonda-installation)
- [Available workflows in Fonda](#available-workflows-in-fonda)
- [Before running Fonda…](#before-running-fonda)
- [Run Fonda](#run-fonda-actual-example-for-rnaexpressionfastq-workflow)

## Required environment setup

- Unix
- Java 8

## Build Fonda

To launch all unit and integration tests run the command:

``` bash
./gradlew test
```

To launch all unit and integration tests, to perform the source code analysis (via `PMD`), to check the code adherement to a coding standard (via `checkstyle`) and to count the code coverage (via `JaCoCo`) run the command:

``` bash
./gradlew check
```

To build Fonda run the command:

``` bash
./gradlew clean build zip
```

- **`clean`** - deletes the Fonda `build` directory for a fresh compile
- **`build`** - creates Fonda `.jar` file and `src` folder in `build/libs`
- **`zip`** - packs Fonda `.jar` and `src` folder into a zip file located in `build/distributions`

**_Note_**: before building a specific Fonda version, please check the Fonda version in the `build.gradle` file is the correct one.

## Fonda installation

Fonda package contains two components:

1. Fonda **`.jar`** file
2. **`src`** folder

If the `src_scripts` option in global config is not set, please make sure `src` folder and `.jar` file are put in the same parental directory for proper usages. This is necessary because Fonda needs to call some external scripts from `src` folder (`python` and `R` subfolders) in some pipeline usages.  
For different pipeline utilities, the user needs to make sure the corresponding software prerequisites are properly installed before executing a specific Fonda pipeline. The user can check the required software and databases in the `global_config` files.

## Available workflows in Fonda

| Workflow | Description |
| --- | --- |
| **DnaCaptureVar_Fastq** | DNA Captured sequencing data for genomic variant detection using fastq data |
| **DnaCaptureVar_Bam** | DNA Captured sequencing data for genomic variant detection using bam data |
| **DnaAmpliconVar_Fastq** | DNA Amplicon sequencing data for genomic variant detection using fastq data |
| **DnaAmpliconVar_Bam** | DNA Amplicon sequencing data for genomic variant detection using bam data |
| **DnaWgsVar_Fastq** | DNA whole genome sequencing data for genomic variant detection using fastq data |
| **DnaWgsVar_Bam** | DNA whole genome sequencing data for genomic variant detection using bam data |
| **RnaCaptureVar_Fastq** | RNA Captured sequencing data for genomic variant detection using fastq data |
| **HlaTyping_Fastq** | DNA sequencing data for genomic HLA type prediction using fastq data |
| **Bam2Fastq** | Convert bam file to fastq files |
| **RnaExpression_Fastq** | RNA sequencing data for gene expression analysis using fastq data |
| **RnaExpression_Bam** | RNA sequencing data for gene expression analysis using bam data |
| **scRnaExpression_Fastq** | single cell RNA sequencing data for gene expression analysis using fastq data |
| **scRnaExpression_CellRanger_Fastq** | 10X single cell RNA sequencing data for gene expression analysis using fastq data |
| **scRnaImmuneProfile_CellRanger_Fastq** | 10X single cell RNA/TCR/BCR sequencing data for immune profiling analysis using fastq data |
| **scRnaExpression_Bam** | single cell RNA sequencing data for gene expression analysis using bam data |
| **RnaFusion_Fastq** | RNA sequencing data for gene fusion detection using fastq data |
| **TcrRepertoire_Fastq** | DNA or RNA sequencing data for TCR or BCR repertoire detection using fastq data |

## Before running Fonda…

### Show help message

``` bash
java -jar fonda-<VERSION>.jar -help
```

Possible options:

| Option | Description |
| --- | --- |
| **Required** |  |
| **`-global_config`** \<arg\> | Configuration file for the particular workflow |
| **`-study_config`** \<arg\> | Configuration file for the specific study |
| **Non-required** |  |
| **`-detail`** | Show the details of the Fonda framework |
| **`-local`** | Default: no. Running the job on local machine |
| **`-test`** | Default: no. Test the commands without actually running the job |
| **`-help`** | Show help utility message |

### Elaboration of required config arguments

**`-global_config`** file - sets a configuration file for a particular pipeline version (such as _RnaExpression\_Fastq 1095.1_). In the config file, there are 4 sections:

- \[all\_tools\] - contains paths to used tools
- \[Databases\] - contains input data/paths to input datasets
- \[Pipeline\_Info\] - contains workflow and toolset settings
- \[Queue\_Parameters\] - contains `sge` settings

If the user likes to change a parameter, a new version should be generated and recorded. However, different studies can share an identical pipeline.

Available parameter options for the **global_config** files you can see [here](doc/User_guide.md#available-parameter-options-in-globalconfig-for-major-workflows).  
Examples of the **global_config** files you can see [here](example/global_config/).

> Please keep in mind that in each **global_config** file the only tools and databases are included that are required for executing this specific pipeline version.  
> For example, `global_config_RnaExpression_Fastq_v1.1.txt` may list out the databases, tools and parameters for a particular `RnaExpression_Fastq` pipeline version 1. Later on, `global_config_RnaExpression_Fastq_v1.2.txt` may be prepared for another `RnaExpression_Fastq` pipeline version 2. In the second config the required databases, tools and parameters might be quite different from the first one.  
> Therefore, all potential databases, tools and parameter options for each available workflow shall be listed out to make sure users can take the full advantage of using Fonda in different projects.

To control the line-endings behavior the `line_ending` option was introduced in the `[Pipeline_Info]` section. The option can be specified as `LF` (Unix-style end-of-line marker) or `CRLF` (Windows-style end-of-line marker) value. If the option is not specified, the `LF` line separator was set as the default one.

**`-study_config`** file - sets a configuration file for a particular study - for cases when a specific study is selected to perform the NGS data analysis. In this config file, there is 1 section - [Series_Info].  
Required parameters for each workflow:

| Parameter | Description |
| --- | --- |
| **job_name** | Sets the job ID |
| **dir_out** | Sets the output directory for the analysis |
| **fastq_list** / **bam_list** | Sets the path to the input manifest file |
| **LibraryType** | Sets the sequencing library type - _DNAWholeExomeSeq\_Paired_, _DNAWholeExomeSeq\_Single_, _DNATargetSeq\_Paired_, _DNATargetSeq\_Single_, _DNAAmpliconSeq\_Paired_, _RNASeq\_Paired_, _RNASeq\_Single_, etc. |
| **DataGenerationSource** | Sets the data generation source - _Internal_, _IGR_, _Broad_, etc. |
| **Date** | Sets the sequencing run date |
| **Project** | Sets the project ID |
| **Run** | Sets the run ID |

The format of input manifest files see [here](doc/User_guide.md#the-format-of-the-input-manifest-file-for-batch-processing).  
Examples of the **study_config** files you can see [here](example/study_config/).

### Elaboration of additional arguments

**`-help`** - to show the help message  
**`-detail`** - to show the workflow details available in the current Fonda framework  
**`-local`** - to run the job on the local machine without being submitted to the cluster  
**`-test`** - to have a pilot run in the command line interface without actually submitting jobs to the cluster

## Run Fonda: actual example for **RnaExpression_Fastq** workflow

### Test mode

``` bash
java -jar /path_to_data/fonda/<VERSION>/fonda-<VERSION>.jar -global_config /path_to_data/fonda/global_config/global_config_RnaExpression_Fastq_v1.1.txt -study_config /path_to_data/config_RnaExpression_Fastq_test.txt -test
```

For the **_test mode_**, no job will be submitted to the cluster for actual run. In this case, you will be able to check whether the contents in each shell scripts are properly organized. This is important for debugging purposes.

### Submit jobs to cluster

``` bash
java -jar /path_to_data/fonda/<VERSION>/fonda-<VERSION>.jar -global_config /path_to_data/fonda/global_config/global_config_RnaExpression_Fastq_v1.1.txt -study_config /path_to_data/config_RnaExpression_Fastq_test.txt
```

### Local machine mode

``` bash
java -jar /path_to_data/fonda/<VERSION>/fonda-<VERSION>.jar -global_config /path_to_data/fonda/global_config/global_config_RnaExpression_Fastq_v1.1.txt -study_config /path_to_data/config_RnaExpression_Fastq_test.txt -local
```

For the **_local machine mode_**, the individual jobs will be run on the local machine, without being submitted to the cluster.  
In this case, scripts will be the same as in the **_cluster mode_**. The only difference is the jobs are not submitted to the cluster. This is important for debugging purpose.
