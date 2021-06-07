#!/bin/bash -x

# --- SGE options --- #

#$ -V
#$ -wd RNAexpression_Fastq_test
#$ -N RnaExpression_Fastq_cufflinks_for_cohort_analysis
#$ -o RNAexpression_Fastq_test/log_files/RnaExpression_Fastq_cufflinks_for_cohort_analysis.log
#$ -e RNAexpression_Fastq_test/err_files/RnaExpression_Fastq_cufflinks_for_cohort_analysis.err
#$ -q all.q
#$ -R y
#$ -pe threaded 4
#$ -m a

# --- The commands to be executed --- #

cd RNAexpression_Fastq_test

echo `date` Begin the job execution...

logFile=RNAexpression_Fastq_test/log_files/RnaExpression_Fastq_cufflinks_for_SW780_analysis.log
str=""
while [[ $str = "" ]]
do
if [[ -f $logFile  ]];
then
	str=$(grep -Ei "((Error Step: (Merge fastqs|Seqpurge trimming|STAR alignment|Sort bam|Index bam|Mark duplicates|Index mkdup bam|RNA QC metrics|Merge RNA QC|Featurecounts|Cufflinks|QC summary analysis))|(Successful Step: cufflinks))" $logFile;)
fi
	echo Waiting for step: cufflinks
	sleep 60
done

if [[ $str == "*Error Step: cufflinks*" ]];
then
	echo $(date) Error gene expression results from SW780:
	echo $str
else
	echo $(date) Confirm gene expression results from SW780
fi

logFile=RNAexpression_Fastq_test/log_files/RnaExpression_Fastq_cufflinks_for_UM-UC-3_analysis.log
str=""
while [[ $str = "" ]]
do
if [[ -f $logFile  ]];
then
	str=$(grep -Ei "((Error Step: (Merge fastqs|Seqpurge trimming|STAR alignment|Sort bam|Index bam|Mark duplicates|Index mkdup bam|RNA QC metrics|Merge RNA QC|Featurecounts|Cufflinks|QC summary analysis))|(Successful Step: cufflinks))" $logFile;)
fi
	echo Waiting for step: cufflinks
	sleep 60
done

if [[ $str == "*Error Step: cufflinks*" ]];
then
	echo $(date) Error gene expression results from UM-UC-3:
	echo $str
else
	echo $(date) Confirm gene expression results from UM-UC-3
fi

logFile=RNAexpression_Fastq_test/log_files/RnaExpression_Fastq_cufflinks_for_SW684_analysis.log
str=""
while [[ $str = "" ]]
do
if [[ -f $logFile  ]];
then
	str=$(grep -Ei "((Error Step: (Merge fastqs|Seqpurge trimming|STAR alignment|Sort bam|Index bam|Mark duplicates|Index mkdup bam|RNA QC metrics|Merge RNA QC|Featurecounts|Cufflinks|QC summary analysis))|(Successful Step: cufflinks))" $logFile;)
fi
	echo Waiting for step: cufflinks
	sleep 60
done

if [[ $str == "*Error Step: cufflinks*" ]];
then
	echo $(date) Error gene expression results from SW684:
	echo $str
else
	echo $(date) Confirm gene expression results from SW684
fi

logFile=RNAexpression_Fastq_test/log_files/RnaExpression_Fastq_cufflinks_for_SW1573_analysis.log
str=""
while [[ $str = "" ]]
do
if [[ -f $logFile  ]];
then
	str=$(grep -Ei "((Error Step: (Merge fastqs|Seqpurge trimming|STAR alignment|Sort bam|Index bam|Mark duplicates|Index mkdup bam|RNA QC metrics|Merge RNA QC|Featurecounts|Cufflinks|QC summary analysis))|(Successful Step: cufflinks))" $logFile;)
fi
	echo Waiting for step: cufflinks
	sleep 60
done

if [[ $str == "*Error Step: cufflinks*" ]];
then
	echo $(date) Error gene expression results from SW1573:
	echo $str
else
	echo $(date) Confirm gene expression results from SW1573
fi

logFile=RNAexpression_Fastq_test/log_files/RnaExpression_Fastq_cufflinks_for_SW1271_analysis.log
str=""
while [[ $str = "" ]]
do
if [[ -f $logFile  ]];
then
	str=$(grep -Ei "((Error Step: (Merge fastqs|Seqpurge trimming|STAR alignment|Sort bam|Index bam|Mark duplicates|Index mkdup bam|RNA QC metrics|Merge RNA QC|Featurecounts|Cufflinks|QC summary analysis))|(Successful Step: cufflinks))" $logFile;)
fi
	echo Waiting for step: cufflinks
	sleep 60
done

if [[ $str == "*Error Step: cufflinks*" ]];
then
	echo $(date) Error gene expression results from SW1271:
	echo $str
else
	echo $(date) Confirm gene expression results from SW1271
fi

echo `date` Begin Step: Merge gene expression...
/ngs/data/app/R/v3.5.0/bin/Rscript /C:/Users/Aleksei_Zaichenkov/IdeaProjects/fonda/build/classes/java/main/src/R/rna_expression_data_analysis.R -i C:\Users\Aleksei_Zaichenkov\IdeaProjects\fonda\example\sample_manifest\RnaExpression_RNASeq_SampleFastqPaths.txt -d RNAexpression_Fastq_test -t cufflinks
if [ $? -eq 0 ]
then
	echo `date` Successful Step: Merge gene expression.
	sleep 8
else
	echo `date` Error Step: Merge gene expression.

	echo `date` The job was aborted due to ERRORS found.
	exit 1;
fi

echo `date` Finish the job execution!
