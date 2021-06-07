#!/bin/bash -x

# --- SGE options --- #

#$ -V
#$ -wd RNAexpression_Fastq_test
#$ -N RnaExpression_Fastq_cufflinks_for_SW1271_analysis
#$ -o RNAexpression_Fastq_test/log_files/RnaExpression_Fastq_cufflinks_for_SW1271_analysis.log
#$ -e RNAexpression_Fastq_test/err_files/RnaExpression_Fastq_cufflinks_for_SW1271_analysis.err
#$ -q all.q
#$ -R y
#$ -pe threaded 4
#$ -m a

# --- The commands to be executed --- #

cd RNAexpression_Fastq_test

echo `date` Begin the job execution...

echo `date` Begin Step: Cufflinks...
/ngs/data/tools/cufflinks/v2.2.1/cufflinks --library-type fr-unstranded --num-threads 4 -b /ngs/data/reference_genome/GRCh38/Sequence/GRCh38.genome.fa --GTF /ngs/data/reference_genome/GRCh38/Annotation/Gencode_v26/gencode.v26.annotation.knowntrx.exon.level1-2.trxlevel1-3.gtf --output-dir RNAexpression_Fastq_test/SW1271/cufflinks RNAexpression_Fastq_test/SW1271/bam/SW1271.star.sorted.mkdup.bam
mv RNAexpression_Fastq_test/SW1271/cufflinks/genes.fpkm_tracking RNAexpression_Fastq_test/SW1271/cufflinks/SW1271.cufflinks.gene.expression.results
mv RNAexpression_Fastq_test/SW1271/cufflinks/isoforms.fpkm_tracking RNAexpression_Fastq_test/SW1271/cufflinks/SW1271.cufflinks.isoform.expression.results
if [ $? -eq 0 ]
then
	echo `date` Successful Step: cufflinks
	sleep 8
else
	echo `date` Error Step: cufflinks

	echo `date` The job was aborted due to ERRORS found.
	exit 1;
fi

echo `date` Finish the job execution!
