#!/bin/bash -x

# --- SGE options --- #

#$ -V
#$ -wd RNAexpression_Fastq_test
#$ -N RnaExpression_Fastq_qcsummary_for_cohort_analysis
#$ -o RNAexpression_Fastq_test/log_files/RnaExpression_Fastq_qcsummary_for_cohort_analysis.log
#$ -e RNAexpression_Fastq_test/err_files/RnaExpression_Fastq_qcsummary_for_cohort_analysis.err
#$ -q all.q
#$ -R y
#$ -pe threaded 4
#$ -m a

# --- The commands to be executed --- #

cd RNAexpression_Fastq_test

echo `date` Begin the job execution...

echo `date` Begin Step: QC summary analysis...
/ngs/data/app/R/v3.5.0/bin/Rscript /C:/Users/Aleksei_Zaichenkov/IdeaProjects/fonda/build/classes/java/main/src/R/QC_summary_analysis.R -i C:\Users\Aleksei_Zaichenkov\IdeaProjects\fonda\example\sample_manifest\RnaExpression_RNASeq_SampleFastqPaths.txt -d RNAexpression_Fastq_test -w RnaExpression_Fastq
if [ $? -eq 0 ]
then
	echo `date` Successful Step: QC summary analysis.
	sleep 8
else
	echo `date` Error Step: QC summary analysis.

	echo `date` The job was aborted due to ERRORS found.
	exit 1;
fi

echo `date` Finish the job execution!
