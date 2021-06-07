#!/bin/bash -x

# --- SGE options --- #

#$ -V
#$ -wd RNAexpression_Fastq_test
#$ -N RnaExpression_Fastq_alignment_for_SW684_analysis
#$ -o RNAexpression_Fastq_test/log_files/RnaExpression_Fastq_alignment_for_SW684_analysis.log
#$ -e RNAexpression_Fastq_test/err_files/RnaExpression_Fastq_alignment_for_SW684_analysis.err
#$ -q all.q
#$ -R y
#$ -pe threaded 4
#$ -m a

# --- The commands to be executed --- #

cd RNAexpression_Fastq_test

echo `date` Begin the job execution...

echo `date` Begin Step: Merge fastqs...

cp /ngs/data/demo/test/fastq/SW684_CGAACTTA_L003_R1_001.fastq.gz RNAexpression_Fastq_test/SW684/fastq/SW684.merged_R1.fastq.gz
cp /ngs/data/demo/test/fastq/SW684_CGAACTTA_L003_R2_001.fastq.gz RNAexpression_Fastq_test/SW684/fastq/SW684.merged_R2.fastq.gz
if [ $? -eq 0 ]
then
	echo `date` Successful Step: Merge fastqs.
	sleep 8
else
	echo `date` Error Step: Merge fastqs.

	echo `date` The job was aborted due to ERRORS found.
	exit 1;
fi

echo `date` Begin Step: Seqpurge trimming...
/ngs/data/app/ngs-bits/v1.0/bin/SeqPurge -threads 4 -in1 RNAexpression_Fastq_test/SW684/fastq/SW684.merged_R1.fastq.gz -in2 RNAexpression_Fastq_test/SW684/fastq/SW684.merged_R2.fastq.gz -out1 RNAexpression_Fastq_test/SW684/fastq/SW684.trimmed.R1.fastq.gz -out2 RNAexpression_Fastq_test/SW684/fastq/SW684.trimmed.R2.fastq.gz -qcut 20 -a1 AGATCGGAAGAGCACACGTCTGAACTCCAGTCAC -a2 AGATCGGAAGAGCGTCGTGTAGGGAAAGAGTGTAGATCTCGGTGGTCGCCGTATCATT
if [ $? -eq 0 ]
then
	echo `date` Successful Step: Seqpurge trimming.
	sleep 8
else
	echo `date` Error Step: Seqpurge trimming.

	echo `date` The job was aborted due to ERRORS found.
	exit 1;
fi

echo `date` Begin Step: STAR alignment...
/ngs/data/tools/STAR/v2.4.0h1/bin/Linux_x86_64/STAR --genomeDir /ngs/data/reference_genome/GRCh38/Index/STAR_gc26 --genomeLoad LoadAndRemove --readFilesIn RNAexpression_Fastq_test/SW684/fastq/SW684.trimmed.R1.fastq.gz RNAexpression_Fastq_test/SW684/fastq/SW684.trimmed.R2.fastq.gz --outFileNamePrefix RNAexpression_Fastq_test/SW684/bam/SW684. --outFilterMatchNmin 0 --outStd Log --outFilterMultimapNmax 5 --outFilterMatchNminOverLread 0.66 --outFilterScoreMinOverLread 0.66 --outSAMunmapped Within --outFilterMultimapScoreRange 1 --outSAMstrandField intronMotif --outFilterScoreMin 0 --alignSJoverhangMin 8 --alignSJDBoverhangMin 1 --runThreadN 4 --outSAMtype BAM Unsorted --outSAMattrRGline ID:SW684 SM:SW684 LB:RNA PL:Illumina CN:cr --readFilesCommand zcat
if [ $? -eq 0 ]
then
	echo `date` Successful Step: STAR alignment.
	sleep 8
else
	echo `date` Error Step: STAR alignment.

	echo `date` The job was aborted due to ERRORS found.
	exit 1;
fi

echo `date` Begin Step: Sort bam...
/ngs/data/app/java/v1.8.0u121/bin/java -jar /ngs/data/tools/picard/v2.10.3/picard.jar SortSam INPUT=RNAexpression_Fastq_test/SW684/bam/SW684.Aligned.out.bam OUTPUT=RNAexpression_Fastq_test/SW684/bam/SW684.star.sorted.bam SORT_ORDER=coordinate VALIDATION_STRINGENCY=SILENT
if [ $? -eq 0 ]
then
	echo `date` Successful Step: Sort bam.
	sleep 8
else
	echo `date` Error Step: Sort bam.

	echo `date` The job was aborted due to ERRORS found.
	exit 1;
fi

echo `date` Begin Step: Index bam...
/ngs/data/tools/samtools/v0.1.19/samtools index RNAexpression_Fastq_test/SW684/bam/SW684.star.sorted.bam
if [ $? -eq 0 ]
then
	echo `date` Successful Step: Index bam.
	sleep 8
else
	echo `date` Error Step: Index bam.

	echo `date` The job was aborted due to ERRORS found.
	exit 1;
fi

echo `date` Begin Step: Mark duplicates...
/ngs/data/app/java/v1.8.0u121/bin/java -Xmx16g -jar /ngs/data/tools/picard/v2.10.3/picard.jar MarkDuplicates INPUT=RNAexpression_Fastq_test/SW684/bam/SW684.star.sorted.bam OUTPUT=RNAexpression_Fastq_test/SW684/bam/SW684.star.sorted.mkdup.bam METRICS_FILE=RNAexpression_Fastq_test/SW684/qc/SW684.star.sorted.mkdup.metrics ASSUME_SORTED=true REMOVE_DUPLICATES=false VALIDATION_STRINGENCY=SILENT TMP_DIR=RNAexpression_Fastq_test/SW684/tmp
if [ $? -eq 0 ]
then
	echo `date` Successful Step: Mark duplicates.
	sleep 8
else
	echo `date` Error Step: Mark duplicates.

	echo `date` The job was aborted due to ERRORS found.
	exit 1;
fi

echo `date` Begin Step: Index mkdup bam...
/ngs/data/tools/samtools/v0.1.19/samtools index RNAexpression_Fastq_test/SW684/bam/SW684.star.sorted.mkdup.bam
if [ $? -eq 0 ]
then
	echo `date` Successful Step: Index mkdup bam.
	sleep 8
else
	echo `date` Error Step: Index mkdup bam.

	echo `date` The job was aborted due to ERRORS found.
	exit 1;
fi

echo `date` Begin Step: RNA QC metrics...
/ngs/data/tools/java/jre1.7.0_60/bin/java -Xmx16g -jar /ngs/data/tools/RNA-SeQC/v1.1.8/RNA-SeQC_v1.1.8.jar -r /ngs/data/reference_genome/GRCh38/Sequence/GRCh38.genome.fa -t /ngs/data/reference_genome/GRCh38/Annotation/Gencode_v26/gencode.v26.annotation.knowntrx.exon.level1-2.trxlevel1-3.gtf -n 1000 -s 'SW684|RNAexpression_Fastq_test/SW684/bam/SW684.star.sorted.mkdup.bam|RNASEQC analysis' -o RNAexpression_Fastq_test/SW684/qc -rRNA /ngs/data/reference_genome/GRCh38/Annotation/Gencode_v26/gencode.v26.rRNA.list VALIDATION_STRINGENCY=SILENT TMP_DIR=RNAexpression_Fastq_test/SW684/tmp -bwa /opt/bwa/bwa-0.7.9a/bwa
/ngs/data/app/java/v1.8.0u121/bin/java -Xmx16g -jar /ngs/data/tools/picard/v2.10.3/picard.jar CollectGcBiasMetrics REFERENCE_SEQUENCE=/ngs/data/reference_genome/GRCh38/Sequence/GRCh38.genome.fa INPUT=RNAexpression_Fastq_test/SW684/bam/SW684.star.sorted.mkdup.bam OUTPUT=RNAexpression_Fastq_test/SW684/qc/SW684.star.sorted.mkdup.gcbias.metrics SUMMARY_OUTPUT=RNAexpression_Fastq_test/SW684/qc/SW684.star.sorted.mkdup.gc.summary.metrics CHART=RNAexpression_Fastq_test/SW684/qc/SW684.star.sorted.mkdup.gcbias.pdf VALIDATION_STRINGENCY=SILENT TMP_DIR=RNAexpression_Fastq_test/SW684/tmp
if [ $? -eq 0 ]
then
	echo `date` Successful Step: RNA QC metrics.
	sleep 8
else
	echo `date` Error Step: RNA QC metrics.

	echo `date` The job was aborted due to ERRORS found.
	exit 1;
fi

echo `date` Begin Step: Merge RNA QC...
/ngs/data/app/python/v2.7.2/bin/python /C:/Users/Aleksei_Zaichenkov/IdeaProjects/fonda/build/classes/java/main/src/python/rna_qc_metrics.py --sample SW684 --rnaseq RNAexpression_Fastq_test/SW684/qc/metrics.tsv --duplicate RNAexpression_Fastq_test/SW684/qc/SW684.star.sorted.mkdup.metrics --gcbias RNAexpression_Fastq_test/SW684/qc/SW684.star.sorted.mkdup.gc.summary.metrics --project Example_project --run run1234 --date 20140318 --output RNAexpression_Fastq_test/SW684/qc/SW684.alignment.merged.QC.metric.txt
if [ $? -eq 0 ]
then
	echo `date` Successful Step: Merge RNA QC.
	sleep 8
else
	echo `date` Error Step: Merge RNA QC.

	echo `date` The job was aborted due to ERRORS found.
	exit 1;
fi

echo `date` Finish the job execution!
