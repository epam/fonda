# Copyright 2017-2019 Sanofi and EPAM Systems, Inc. (https://www.epam.com/)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

## This script is used to wrap-up QC metrics generated based on Bam file
## This script is compatible with the outputs of RNA-SeQC v1.1.7 and RNA-SeQC v1.1.8
## This script has not been tested for the outputs from other RNA-SeQC versions, please be adviced.

import os,subprocess,sys,getopt,re

def usage():
	print 'Usage:\n'
	print ' --sample (required)          The sample ID.\n'
	print '	--rnaseq (optional)          The full path of the read rna sequencing summary qc file.\n'
	print '	--duplicate (optional)       The full path of the read duplication summary qc file.\n'
	print '	--gcbias (optional)          The full path of the read GC bias summary qc file.\n'
	print '	--project (required)         The project ID.\n'
	print '	--run (required)             The run ID.\n'
	print '	--date (required)            The data of the run.\n'
	print '	--output (required)          The full path of the wrap-up qc metrics file.\n'
	
class ArgumentError(Exception):
	pass

def parse_arguments(argv):
	sample, rnaseq, duplicate,gcbias, project, run, date= None,None,None,None,None,None,None
	
	try:
		opts, args = getopt.getopt(argv[1:], "h", \
					["sample=","rnaseq=","duplicate=","gcbias=","output=","project=","run=","date=","help"])
	except getopt.GetoptError as err:
		# print help information and exit:
		print str(err)  # will print something like "option -a not recognized"
		usage()

	for opt, arg in opts:
		if opt == ('-h','--help'):
			usage()
		
		elif opt in ("--rnaseq"):
			rnaseq = arg
			if not os.path.isfile(rnaseq):
				raise ArgumentError("Argument Error: Invalid rna sequencing summary file passed.")
		
		elif opt in ("--duplicate"):
			duplicate = arg
			if not os.path.isfile(duplicate):
				raise ArgumentError("Argument Error: Invalid duplicate summary file passed.")
		
		elif opt in ("--gcbias"):
			gcbias = arg
			if not os.path.isfile(gcbias):
				raise ArgumentError("Argument Error: Invalid gcbias summary file passed.")
				
		elif opt in ("--output"):
			outfile = arg
		
		elif opt in ("--sample"):
			sample = arg
		
		elif opt in ("--project"):
			project = arg
			
		elif opt in ("--run"):
			run = arg
			
		elif opt in ("--date"):
			date = arg
			
		else:
			raise ArgumentError("Bad argument: I don't know what %s is" % arg)
	
    # return argument values
	return sample, rnaseq, duplicate, gcbias, project, run, date, outfile

sample, rnaseq_file, duplicate_file, gcbias_file, project_id, run_id, date, outfile= parse_arguments(sys.argv)

## This function is to calculate the number of bases that achieve different thresholds of read coverage
def percentBaseCoverageRelative2Mean(coverages,N):
	pct_mean_100,pct_mean_75,pct_mean_50,pct_mean_25,pct_mean_10=0,0,0,0,0
	mean=sum(coverages)/float(N)
	for value in coverages:
		if value>=0.1*mean:
			pct_mean_10+=1
		if value>=0.25*mean:
			pct_mean_25+=1
		if value>=0.5*mean:
			pct_mean_50+=1
		if value>=0.75*mean:
			pct_mean_75+=1
		if value>=mean:
			pct_mean_100+=1
	return mean,pct_mean_100,pct_mean_75,pct_mean_50,pct_mean_25,pct_mean_10	

	
## Collect the variables and their corresponding values from each individual summary files
var2val=dict()

		
## Extract the information from the rna sequencing summary file using mkdup bam
if rnaseq_file!=None:
	rnaseqSum=open(rnaseq_file).readlines()
	for i in range(len(rnaseqSum)):
		if rnaseqSum[i].startswith('Sample'):
			header = rnaseqSum[i].rstrip().split("\t")
			for j in range(len(header)):
				if header[j]=="End 2 Mapping Rate":
					end2MappingRateIndex = j
				if header[j]=="Chimeric Pairs":
					chimericPairIndex = j
				if header[j]=="Intragenic Rate":
					intragenicRateIndex = j
				if header[j]=="Num. Gaps":
					numOfGapsIndex = j
				if header[j]=="Exonic Rate":
					exonicRateIndex = j
				if header[j]=="Mapping Rate":
					mappingRateIndex = j
				if header[j]=="5' Norm":
					norm5PrimeIndex = j
				if header[j]=="Genes Detected":
					geneDetectedIndex = j
				if header[j]=="Unique Rate of Mapped":
					uniqueMapRateIndex = j
				if header[j]=="Read Length":
					readLengthIndex = j
				if header[j]=="Mean Per Base Cov.":
					meanCovBaseIndex = j
				if header[j]=="End 1 Mismatch Rate":
					end1MismatchRateIndex = j
				if header[j]=="Fragment Length StdDev":
					fragLengthStdIndex = j
				if header[j]=="Mapped":
					mappedIndex = j
				if header[j]=="Intergenic Rate":
					intergenicRateIndex = j
				if header[j]=="Total Purity Filtered Reads Sequenced":
					totalPurityFilteredReadsIndex = j
				if header[j]=="rRNA":
					rRNAIndex = j
				if header[j]=="Failed Vendor QC Check":
					failedVendorQCIndex = j
				if header[j]=="Mean CV":
					meanCVIndex = j
				if header[j]=="Transcripts Detected":
					transcriptsDetectedIndex = j
				if header[j]=="Mapped Pairs":
					mappedPairsIndex = j
				if header[j]=="Cumul. Gap Length":
					cumGapLengthIndex = j
				if header[j]=="Gap %":
					gapPercentIndex = j
				if header[j]=="Unpaired Reads":
					unpairedReadsIndex = j
				if header[j]=="Intronic Rate":
					intronicRateIndex = j
				if header[j]=="Mapped Unique Rate of Total":
					mappedUniqueRateOfTotalIndex = j
				if header[j]=="Expression Profiling Efficiency":
					expressionProfilingEfficiencyIndex = j
				if header[j]=="Mapped Unique":
					mappedUniqueIndex = j
				if header[j]=="End 2 Mismatch Rate":
					end2MismatchRateIndex = j
				if header[j]=="End 2 Antisense":
					end2AntisenseIndex = j
				if header[j]=="Alternative Aligments":
					alterAlignmentIndex = j
				if header[j]=="End 2 Sense":
					end2SenseIndex = j
				if header[j]=="Fragment Length Mean":
					meanFragLengthIndex = j
				if header[j]=="rRNA":
					rRNAIndex = j
				if header[j]=="End 1 Antisense":
					end1AntisenseIndex = j
				if header[j]=="Base Mismatch Rate":
					baseMismatchRateIndex = j
				if header[j]=="End 1 Sense":
					end1SenseIndex = j
				if header[j]=="End 1 % Sense":
					end1SenseRateIndex = j
				if header[j]=="End 2 % Sense":
					end2SenseRateIndex = j
				if header[j]=="rRNA rate":
					rRnaRateIndex = j
				if header[j]=="End 1 Mapping Rate":
					end1MappingRateIndex = j
				if header[j]=="Estimated Library Size":
					estLibrarySizeIndex = j
			break
	value = rnaseqSum[i+1].rstrip().split("\t")
	for j in range(len(value)):
		var2val["PCT_END_2_MAPPING"]=str(100*float(value[end2MappingRateIndex]))
		var2val["CHIMERIC_PAIRS"]=value[chimericPairIndex]
		var2val["PCT_INTRAGENIC"]=str(100*float(value[intragenicRateIndex]))
		var2val["NUM_OF_GAPS"]=value[numOfGapsIndex]
		var2val["EXONIC_RATE"]=str(100*float(value[exonicRateIndex]))
		var2val["MAPPING_RATE"]=str(100*float(value[mappingRateIndex]))
		var2val["5PRIME_NORM"]=value[norm5PrimeIndex]
		var2val["GENES_DETECTED"]=value[geneDetectedIndex]
		var2val["PCT_UNIQUE_MAPPING"]=str(100*float(value[uniqueMapRateIndex]))
		var2val["READ_LENGTH"]=value[readLengthIndex]
		var2val["MEAN_COVERAGE_PER_BASE"]=value[meanCovBaseIndex]
		var2val["PCT_END_1_MISMATCH"]=str(100*float(value[end1MismatchRateIndex]))
		var2val["FRAGMENT_LENGTH_STDDEV"]=value[fragLengthStdIndex]
		var2val["MAPPED_READS"]=value[mappedIndex]
		var2val["PCT_INTERGENIC"]=str(100*float(value[intergenicRateIndex]))
		var2val["TOTAL_PURITY_FILTERED_READS"]=value[totalPurityFilteredReadsIndex]
		var2val["rRNA_RATE"]=str(100*float(value[rRNAIndex]))
		var2val["FAILED_VENDOR_QC_CHECK"]=value[failedVendorQCIndex]
		var2val["MEAN_CV"]=value[meanCVIndex]
		var2val["TRANSCRIPTS_DETECTED"]=value[transcriptsDetectedIndex]
		var2val["MAPPED_PAIRS"]=value[mappedPairsIndex]
		var2val["CUMULATIVE_GAP_LENGTH"]=value[cumGapLengthIndex]
		var2val["PCT_GAP"]=str(100*float(value[gapPercentIndex]))
		var2val["UNPAIRED_READS"]=value[unpairedReadsIndex]
		var2val["PCT_INTRONIC"]=str(100*float(value[intronicRateIndex]))
		var2val["PCT_UNIQUE_MAPPING_OF_TOTAL"]=str(100*float(value[mappedUniqueRateOfTotalIndex]))
		var2val["EXPRESSION_PROFILING_EFFICIENCY"]=value[expressionProfilingEfficiencyIndex]
		var2val["MAPPED_UNIQUE_READS"]=value[mappedUniqueIndex]
		var2val["END_2_MISMATCH_RATE"]=str(100*float(value[end2MismatchRateIndex]))
		var2val["END_2_ANTISENSE"]=value[end2AntisenseIndex]
		var2val["ALTERNATIVE_ALIGNMENT_READS"]=value[alterAlignmentIndex]
		var2val["END_2_SENSE"]=value[end2SenseIndex]
		var2val["MEAN_FRAGMENT_LENGTH"]=value[meanFragLengthIndex]
		var2val["END_1_ANTISENSE"]=value[end1AntisenseIndex]
		var2val["PCT_BASE_MISMATCH"]=str(100*float(value[baseMismatchRateIndex]))
		var2val["END_1_SENSE"]=value[end1SenseIndex]
		var2val["PCT_END_1_SENSE"]=str(100*float(value[end1SenseRateIndex]))
		var2val["PCT_END_2_SENSE"]=str(100*float(value[end2SenseRateIndex]))
		var2val["PCT_rRNA"]=str(100*float(value[rRnaRateIndex]))
		var2val["PCT_END_1_MAPPING"]=str(100*float(value[end1MappingRateIndex]))
		var2val["ESTIMATED_LIBRARY_SIZE"]=value[estLibrarySizeIndex]
		

## Extract the information from the duplication summary file using dedup bam
if duplicate_file!=None:
	duplicateSum=open(duplicate_file).readlines()
	for i in range(len(duplicateSum)):
		if duplicateSum[i].startswith('LIBRARY'):
			header = duplicateSum[i].rstrip().split("\t")
			for j in range(len(header)):
				if header[j]=="PERCENT_DUPLICATION":
					pctDuplicateIndex = j
			break
	value = duplicateSum[i+1].rstrip().split("\t")
	for j in range(len(value)):
		var2val["PERCENT_DUPLICATION"]=str(100*float(value[pctDuplicateIndex]))

## Extract the information from the gcbias summary file using mkdup bam
if gcbias_file!=None:
	gcbiasSum=open(gcbias_file).readlines()
	for i in range(len(gcbiasSum)):
		if gcbiasSum[i].startswith('ACCUMULATION_LEVEL'):
			header = gcbiasSum[i].rstrip().split("\t")
			for j in range(len(header)):
				if header[j]=="GC_NC_0_19":
					gc0_Index = j					
				if header[j]=="GC_NC_20_39":
					gc20_Index = j
				if header[j]=="GC_NC_40_59":
					gc40_Index = j
				if header[j]=="GC_NC_60_79":
					gc60_Index = j
				if header[j]=="GC_NC_80_100":
					gc80_Index = j
			break
	value = gcbiasSum[i+1].rstrip().split("\t")
	for j in range(len(value)):
		var2val["GC_NC_0_19"]=value[gc0_Index]
		var2val["GC_NC_20_39"]=value[gc20_Index]
		var2val["GC_NC_40_59"]=value[gc40_Index]
		var2val["GC_NC_60_79"]=value[gc60_Index]
		var2val["GC_NC_80_100"]=value[gc80_Index]
		
output=open(outfile,"w")
output.write("## SAMPLE\tThe sample ID\n")
output.write("## PROJECT_ID\tThe project ID\n")
output.write("## RUN_ID\tThe Run ID\n")
output.write("## DATE\tThe date of the sequencing run\n")
output.write("## ESTIMATED_LIBRARY_SIZE\tThe number of fragments that were covered by the RNA library\n")
output.write("## PERCENT_DUPLICATION\tThe percentage of duplicated reads in the RNA library\n")
output.write("## 5PRIME_NORM\t\n")
output.write("## ALTERNATIVE_ALIGNMENT_READS\tDuplicating read entries providing alternative coordinates\n")
output.write("## CHIMERIC_PAIRS\tPairs whose mates map to different genes\n")
output.write("## CUMULATIVE_GAP_LENGTH\tCumulative read length from mapping GAP\n")
output.write("## END_1_ANTISENSE\tThe number of end 1 reads that were sequenced in the anti-sense direction\n")
output.write("## END_1_SENSE\tThe number of end 1 reads that were sequenced in the sense direction\n")
output.write("## END_2_ANTISENSE\tThe number of end 2 reads that were sequenced in the anti-sense direction\n")
output.write("## END_2_SENSE\tThe number of end 2 reads that were sequenced in the sense direction\n")
output.write("## END_2_MISMATCH_RATE\tThe percentage of end 2 reads that have alignment mismatch\n")
output.write("## EXONIC_RATE\tThe percentage of mapping reads in the exonic regions\n")
output.write("## EXPRESSION_PROFILING_EFFICIENCY\tThe fraction of exonic reads to total reads\n")
output.write("## FAILED_VENDOR_QC_CHECK\tReads which have been designated as failed by the sequencer\n")
output.write("## FRAGMENT_LENGTH_STDDEV\tThe standard deviation of the read fragment lengths\n")
output.write("## GENES_DETECTED\tThe number of genes that received at least 5 reads\n")
output.write("## GC_NC_0_19\tNormalized coverage over quintile of GC content ranging from 0 - 19\n")
output.write("## GC_NC_20_39\tNormalized coverage over quintile of GC content ranging from 20 - 39\n")
output.write("## GC_NC_40_59\tNormalized coverage over quintile of GC content ranging from 40 - 59\n")
output.write("## GC_NC_60_79\tNormalized coverage over quintile of GC content ranging from 60 - 79\n")
output.write("## GC_NC_80_100\tNormalized coverage over quintile of GC content ranging from 80 - 100\n")
output.write("## MAPPED_PAIRS\tThe number of mapped read pairs\n")
output.write("## UNPAIRED_READS\tThe number of mapped read in unpaired\n")
output.write("## MAPPED_READS\tThe total number of mapped reads\n")
output.write("## MAPPED_UNIQUE_READS\tThe number of reads that were mapped and without duplicates\n")
output.write("## MAPPING_RATE\tThe percentage of mapped reads to total reads\n")
output.write("## MEAN_COVERAGE_PER_BASE\tThe mean read coverage for all analyzed bases\n")
output.write("## MEAN_CV\tThe coefficient variance of the mean read coverages\n")
output.write("## MEAN_FRAGMENT_LENGTH\tThe mean read fragment lengths\n")
output.write("## NUM_OF_GAPS\tThe number of genomic regions that do not receive any reads\n")
output.write("## PCT_BASE_MISMATCH\tThe percentage of bases not matching the reference divided by the total number of aligned bases\n")
output.write("## PCT_END_1_MAPPING\tThe percentage of mapped end 1 reads to total number of end 1 reads\n")
output.write("## PCT_END_1_MISMATCH\tThe percentage of end 1 bases that do not match to reference to total number of mapped end 1 bases\n")
output.write("## PCT_END_1_SENSE\tThe percentage of end 1 sense direction reads to the total number of end 1 reads\n")
output.write("## PCT_END_2_MAPPING\tThe percentage of mapped end 2 reads to total number of end 2 reads\n")
output.write("## PCT_END_2_MISMATCH\tThe percentage of end 2 bases that do not match to reference to total number of mapped end 2 bases\n")
output.write("## PCT_END_2_SENSE\tThe percentage of end 2 sense direction reads to the total number of end 2 reads\n")
output.write("## PCT_GAP\tThe raito of total gap length to total cumulative transcript lengths\n")
output.write("## PCT_INTERGENIC\tThe percentage of mapping reads in the genomic space between genes\n")
output.write("## PCT_INTRAGENIC\tThe percentage of mapping reads within gene regions(both exonic and intronic)\n")
output.write("## PCT_INTRONIC\tThe percentage of mapping reads in the intronic regions\n")
output.write("## PCT_UNIQUE_MAPPING\tThe percentage of reads that were uniquely aligned and without duplicates in the mapped reads\n")
output.write("## PCT_UNIQUE_MAPPING_OF_TOTAL\tThe percentage of reads that were uniquely aligned and without duplicates in the total reads\n")
output.write("## PCT_rRNA\tThe percentage of reads in the rRNA region\n")
output.write("## READ_LENGTH\tThe detected max read length\n")
output.write("## TOTAL_PURITY_FILTERED_READS\tThe reads filtered for vendor fail flags and exclude alternative alignment reads\n")
output.write("## TRANSCRIPTS_DETECTED\tThe number of transcripts that received at least 5 reads\n")
output.write("## rRNA_RATE\tThe percentage of rRNA reads to the total reads\n")

output.write("SAMPLE\t%s\n" % sample)
output.write("PROJECT_ID\t%s\n" % project_id)
output.write("RUN_ID\t%s\n" % run_id)
output.write("DATE\t%s\n" % date)
for key in sorted(var2val.keys()):
	output.write("%s\t%s\n" % (key,var2val[key]))
	
	