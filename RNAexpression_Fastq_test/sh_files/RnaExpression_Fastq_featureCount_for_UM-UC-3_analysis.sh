#!/bin/bash -x

# --- SGE options --- #

#$ -V
#$ -wd RNAexpression_Fastq_test
#$ -N RnaExpression_Fastq_featureCount_for_UM-UC-3_analysis
#$ -o RNAexpression_Fastq_test/log_files/RnaExpression_Fastq_featureCount_for_UM-UC-3_analysis.log
#$ -e RNAexpression_Fastq_test/err_files/RnaExpression_Fastq_featureCount_for_UM-UC-3_analysis.err
#$ -q all.q
#$ -R y
#$ -pe threaded 4
#$ -m a

# --- The commands to be executed --- #

cd RNAexpression_Fastq_test

echo `date` Begin the job execution...

echo `date` Begin Step: Featurecounts...
/ngs/data/tools/subread/v1.4.5-p1/bin/featureCounts -F SAF -M -s 0 -a /ngs/data/reference_genome/GRCh38/Annotation/Gencode_v26/gencode.v26.annotation.knowntrx.exon.level1-2.trxlevel1-3.saf -o RNAexpression_Fastq_test/UM-UC-3/feature_count/UM-UC-3_featureCount_gene.txt -Q 20 -T 4 RNAexpression_Fastq_test/UM-UC-3/bam/UM-UC-3.star.sorted.mkdup.bam
if [ $? -eq 0 ]
then
	echo `date` Successful Step: Run featureCount.
	sleep 8
else
	echo `date` Error Step: Run featureCount.

	echo `date` The job was aborted due to ERRORS found.
	exit 1;
fi

echo `date` Finish the job execution!
