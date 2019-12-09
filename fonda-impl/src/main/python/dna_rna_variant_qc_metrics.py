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
## This script is compatible with the outputs of picard v2.1.0 and samtools v0.1.19
## This script has not been tested for the outputs from other picard and samtools versions, please be adviced.

import os,subprocess,sys,getopt,re
import math

def usage():
	print 'Usage:\n'
	print ' --sample (required)          The sample ID.\n'
	print '	--align (optional)           The full path of the read alignment summary qc file.\n'
	print '	--insert (optional)          The full path of the read insert size summary qc file.\n'
	print '	--gcbias (optional)          The full path of the read GC bias summary qc file.\n'
	print '	--mkdup_hybrid (optional)    The full path of the read hybridization summary qc file before read duplicate removal.\n'
	print '	--rmdup_hybrid (optional)    The full path of the read hybridization summary qc file after read duplicate removal.\n'
	print '	--duplicate (optional)       The full path of the read duplication summary qc file.\n'
	print '	--pileup (optional)          The full path of the read pileup file.\n'
	print '	--bedcov (optional)          The full path of the target bed coverage file.\n'
	print ' --type (optional)            The library type of the reads, either capture, wgs, wes or amplicon, default: capture.\n'
	print ' --read_type (optional)       The read type of the library, either single or paired, default: paired.\n'
	print '	--project (required)         The project ID.\n'
	print '	--run (required)             The run ID.\n'
	print '	--date (required)            The data of the run.\n'
	print '	-o, --output (required)      The full path of the wrap-up qc metrics file.\n'
	
class ArgumentError(Exception):
	pass

def parse_arguments(argv):
	sample, align, insert, gcbias, mkdup_hybrid, rmdup_hybrid, duplicate, pileup, bedcoverage, project, run, date, read_type= \
																					None,None,None,None,None,None,None,None,None,None,None,None,None
	type = "capture"
	read_type = "paired"
	
	try:
		opts, args = getopt.getopt(argv[1:], "h", \
					["sample=","align=","insert=","gcbias=","mkdup_hybrid=","rmdup_hybrid=","duplicate=","bedcov=","pileup=","type=","output=","project=","run=","date=","read_type=","help"])
	except getopt.GetoptError as err:
		# print help information and exit:
		print str(err)  # will print something like "option -a not recognized"
		usage()

	for opt, arg in opts:
		if opt == ('-h','--help'):
			usage()

		elif opt in ("--align"):
			align = arg
			if not os.path.isfile(align):
				raise ArgumentError("Argument Error: Invalid alignment summary file passed.")
		
		elif opt in ("--insert"):
			insert = arg
			if not os.path.isfile(insert):
				raise ArgumentError("Argument Error: Invalid insertsize summary file passed.")
				
		elif opt in ("--gcbias"):
			gcbias = arg
			if not os.path.isfile(gcbias):
				raise ArgumentError("Argument Error: Invalid gcbias summary file passed.")
				
		elif opt in ("--mkdup_hybrid"):
			mkdup_hybrid = arg
			if not os.path.isfile(mkdup_hybrid):
				raise ArgumentError("Argument Error: Invalid mkdup_hybrid summary file passed.")
		
		elif opt in ("--rmdup_hybrid"):
			rmdup_hybrid = arg
			if not os.path.isfile(rmdup_hybrid):
				raise ArgumentError("Argument Error: Invalid rmdup_hybrid summary file passed.")
				
		elif opt in ("--duplicate"):
			duplicate = arg
			if not os.path.isfile(duplicate):
				raise ArgumentError("Argument Error: Invalid duplicate summary file passed.")

		elif opt in ("--pileup"):
			pileup = arg
			if not os.path.isfile(pileup):
				raise ArgumentError("Argument Error: Invalid pileup file passed.")
		
		elif opt in ("--bedcov"):
			bedcoverage = arg
			if not os.path.isfile(bedcoverage):
				raise ArgumentError("Argument Error: Invalid target bed coverage file passed.")
				
		elif opt in ("--output"):
			outfile = arg
			if not os.path.isdir(os.path.dirname(outfile)):
				raise ArgumentError("Argument Error: Invalid output file containing directory passed.")
		
		elif opt in ("--sample"):
			sample = arg
		
		elif opt in ("--type"):
			type = arg
		
		elif opt in ("--read_type"):
			read_type = arg
			
		elif opt in ("--project"):
			project = arg
			
		elif opt in ("--run"):
			run = arg
			
		elif opt in ("--date"):
			date = arg
			
		else:
			raise ArgumentError("Bad Argument: I don't know what %s is" % arg)
	
    # return argument values
	return sample, align, insert, gcbias, mkdup_hybrid, rmdup_hybrid, duplicate, bedcoverage, pileup, type, project, run, date, read_type,outfile

sample, align_file, insert_file, gcbias_file, mkdup_hybrid_file, rmdup_hybrid_file, duplicate_file, coverage_file, pileup_file, lib_type, project_id, run_id, date, read_type,outfile= parse_arguments(sys.argv)

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

def stddev(list):
    """returns the standard deviation of list"""
    mean = float(sum(list))/len(list)
    variance = sum([(e-mean)**2 for e in list]) / len(list)
    return math.sqrt(variance)
	
	
## Collect the variables and their corresponding values from each individual summary files
var2val=dict()

## Extract the information from the alignment summary file using mkdup
if align_file!=None:
	alignSum=open(align_file).readlines()
	for i in range(len(alignSum)):
		if alignSum[i].startswith('CATEGORY'):
			header = alignSum[i].rstrip().split("\t")
			for j in range(len(header)):
				if header[j]=="TOTAL_READS":
					totalReadIndex = j
				if header[j]=="PF_READS_ALIGNED":
					pfReadAlignedIndex = j
				if header[j]=="PCT_PF_READS_ALIGNED":
					pctPfReadAlignedIndex = j
				if header[j]=="MEAN_READ_LENGTH":
					pfMeanReadLengthIndex = j
				if header[j]=="PCT_READS_ALIGNED_IN_PAIRS":
					PctReadAlignedPairIndex = j
		elif read_type=="paired" and alignSum[i].startswith('PAIR'):	
			value = alignSum[i].rstrip().split("\t")
			for j in range(len(value)):
				var2val["TOTAL_READS"]=value[totalReadIndex]
				var2val["PF_READS_ALIGNED"]=value[pfReadAlignedIndex]
				var2val["PERCENT_PF_READS_ALIGNED"]=str(100*float(value[pctPfReadAlignedIndex]))
				var2val["MEAN_READ_LENGTH"]=value[pfMeanReadLengthIndex]
				var2val["PERCENT_READS_ALIGNED_IN_PAIRS"]=str(100*float(value[PctReadAlignedPairIndex]))
		elif read_type=="single" and alignSum[i].startswith('UNPAIRED'):	
			value = alignSum[i].rstrip().split("\t")
			for j in range(len(value)):
				var2val["TOTAL_READS"]=value[totalReadIndex]
				var2val["PF_READS_ALIGNED"]=value[pfReadAlignedIndex]
				var2val["PERCENT_PF_READS_ALIGNED"]=str(100*float(value[pctPfReadAlignedIndex]))
				var2val["MEAN_READ_LENGTH"]=value[pfMeanReadLengthIndex]
				var2val["PERCENT_READS_ALIGNED_IN_PAIRS"]=str(100*float(value[PctReadAlignedPairIndex]))
			
## Extract the information from the hybridization summary file using mkdup bam
if mkdup_hybrid_file!=None:
	mkduphybridSum=open(mkdup_hybrid_file).readlines()
	if lib_type == "capture" or lib_type == "amplicon" or lib_type=="wes":
		for i in range(len(mkduphybridSum)):
			if mkduphybridSum[i].startswith('BAIT_SET'):
				header = mkduphybridSum[i].rstrip().split("\t")
				for j in range(len(header)):
					if header[j]=="TARGET_TERRITORY":
						targetTerritoryIndex = j
					if header[j]=="PCT_PF_UQ_READS":
						pctPfReadUniqueIndex = j
					if header[j]=="PCT_PF_UQ_READS_ALIGNED":
						pctPfReadUniqueAlignedIndex = j
					if header[j]=="PCT_OFF_BAIT":
						pctOffBaitIndex = j
					if header[j]=="PCT_SELECTED_BASES":
						pctSelectedBaseIndex = j
					if header[j]=="PCT_USABLE_BASES_ON_TARGET":
						pctBasesOnTargetIndex = j
					if header[j]=="ZERO_CVG_TARGETS_PCT":
						pctZeroCovTargetIndex = j
					if header[j]=="FOLD_80_BASE_PENALTY":
						fold80BasePenaltyIndex = j
					if header[j]=="HS_LIBRARY_SIZE":
						estLibrarySizeIndex = j
					
				break
		value = mkduphybridSum[i+1].rstrip().split("\t")
		for j in range(len(value)):
			targetSize=value[targetTerritoryIndex]
			var2val["PERCENT_PF_UQ_READS(DUP)"]=str(100*float(value[pctPfReadUniqueIndex]))
			var2val["PERCENT_PF_UQ_READS_ALIGNED(DUP)"]=str(100*float(value[pctPfReadUniqueAlignedIndex]))
			var2val["PERCENT_OFF_BAIT(DUP)"]=str(100*float(value[pctOffBaitIndex]))
			var2val["PERCENT_SELECTED_BASE(DUP)"]=str(100*float(value[pctSelectedBaseIndex]))
			var2val["PERCENT_USABLE_BASES_ON_TARGET(DUP)"]=str(100*float(value[pctBasesOnTargetIndex]))
			var2val["PERCENT_ZERO_COVERAGE_TARGET(DUP)"]=str(100*float(value[pctZeroCovTargetIndex]))
			var2val["FOLD_80_BASE_PENALTY(DUP)"]=value[fold80BasePenaltyIndex]
			var2val["ESTIMATED_LIBRARY_SIZE(DUP)"]=value[estLibrarySizeIndex]
			
	elif lib_type == "wgs":
		for i in range(len(mkduphybridSum)):
			if mkduphybridSum[i].startswith('GENOME_TERRITORY'):
				header = mkduphybridSum[i].rstrip().split("\t")
				for j in range(len(header)):
					if header[j]=="GENOME_TERRITORY":
						genomeTerritoryIndex = j
					if header[j]=="MEAN_COVERAGE":
						meanCovIndex = j
					if header[j]=="PCT_1X":
						pct1Index = j
					if header[j]=="PCT_5X":
						pct5Index = j
					if header[j]=="PCT_10X":
						pct10Index = j
					if header[j]=="PCT_20X":
						pct20Index = j
					if header[j]=="PCT_30X":
						pct30Index = j
					if header[j]=="PCT_50X":
						pct50Index = j
					if header[j]=="PCT_100X":
						pct100Index = j
				break
		value = mkduphybridSum[i+1].rstrip().split("\t")
		for j in range(len(value)):
			targetSize=value[genomeTerritoryIndex]
			var2val["MEAN_BASE_COVERAGE(DUP)"]=value[meanCovIndex]
			var2val["PERCENT_BASES_COVERAGE>=1(DUP)"]=str(100*float(value[pct1Index]))
			var2val["PERCENT_BASES_COVERAGE>=5(DUP)"]=str(100*float(value[pct5Index]))
			var2val["PERCENT_BASES_COVERAGE>=10(DUP)"]=str(100*float(value[pct10Index]))
			var2val["PERCENT_BASES_COVERAGE>=20(DUP)"]=str(100*float(value[pct20Index]))
			var2val["PERCENT_BASES_COVERAGE>=30(DUP)"]=str(100*float(value[pct30Index]))
			var2val["PERCENT_BASES_COVERAGE>=40(DUP)"]=str(100*float(value[pct50Index]))
			var2val["PERCENT_BASES_COVERAGE>=50(DUP)"]=str(100*float(value[pct100Index]))

## Extract the information from the hybridization summary file using rmdup bam
if rmdup_hybrid_file!=None:
	rmduphybridSum=open(rmdup_hybrid_file).readlines()
	if lib_type == "capture" or lib_type == "amplicon" or lib_type=="wes":
		for i in range(len(rmduphybridSum)):
			if rmduphybridSum[i].startswith('BAIT_SET'):
				header = rmduphybridSum[i].rstrip().split("\t")
				for j in range(len(header)):
					if header[j]=="TARGET_TERRITORY":
						targetTerritoryIndex = j
					if header[j]=="PCT_PF_UQ_READS":
						pctPfReadUniqueIndex = j
					if header[j]=="PCT_PF_UQ_READS_ALIGNED":
						pctPfReadUniqueAlignedIndex = j
					if header[j]=="PCT_OFF_BAIT":
						pctOffBaitIndex = j
					if header[j]=="PCT_SELECTED_BASES":
						pctSelectedBaseIndex = j
					if header[j]=="PCT_USABLE_BASES_ON_TARGET":
						pctBasesOnTargetIndex = j
					if header[j]=="ZERO_CVG_TARGETS_PCT":
						pctZeroCovTargetIndex = j
					if header[j]=="FOLD_80_BASE_PENALTY":
						fold80BasePenaltyIndex = j
				break
		value = rmduphybridSum[i+1].rstrip().split("\t")
		for j in range(len(value)):
			targetSize=value[targetTerritoryIndex]
			var2val["PERCENT_PF_UQ_READS(NODUP)"]=str(100*float(value[pctPfReadUniqueIndex]))
			var2val["PERCENT_PF_UQ_READS_ALIGNED(NODUP)"]=str(100*float(value[pctPfReadUniqueAlignedIndex]))
			var2val["PERCENT_OFF_BAIT(NODUP)"]=str(100*float(value[pctOffBaitIndex]))
			var2val["PERCENT_SELECTED_BASES(NODUP)"]=str(100*float(value[pctSelectedBaseIndex]))
			var2val["PERCENT_USABLE_BASES_ON_TARGET(NODUP)"]=str(100*float(value[pctBasesOnTargetIndex]))
			var2val["PERCENT_ZERO_COVERAGE_TARGET(NODUP)"]=str(100*float(value[pctZeroCovTargetIndex]))
			var2val["FOLD_80_BASE_PENALTY(NODUP)"]=value[fold80BasePenaltyIndex]
	elif lib_type == "wgs":
		for i in range(len(rmduphybridSum)):
			if rmduphybridSum[i].startswith('GENOME_TERRITORY'):
				header = rmduphybridSum[i].rstrip().split("\t")
				for j in range(len(header)):
					if header[j]=="GENOME_TERRITORY":
						genomeTerritoryIndex = j
					if header[j]=="MEAN_COVERAGE":
						meanCovIndex = j
					if header[j]=="PCT_1X":
						pct1Index = j
					if header[j]=="PCT_5X":
						pct5Index = j
					if header[j]=="PCT_10X":
						pct10Index = j
					if header[j]=="PCT_20X":
						pct20Index = j
					if header[j]=="PCT_30X":
						pct30Index = j
					if header[j]=="PCT_50X":
						pct50Index = j
					if header[j]=="PCT_100X":
						pct100Index = j
				break
		value = rmduphybridSum[i+1].rstrip().split("\t")
		for j in range(len(value)):
			targetSize=value[genomeTerritoryIndex]
			var2val["MEAN_BASE_COVERAGE(NODUP)"]=value[meanCovIndex]
			var2val["PERCENT_BASES_COVERAGE>=1(NODUP)"]=str(100*float(value[pct1Index]))
			var2val["PERCENT_BASES_COVERAGE>=5(NODUP)"]=str(100*float(value[pct5Index]))
			var2val["PERCENT_BASES_COVERAGE>=10(NODUP)"]=str(100*float(value[pct10Index]))
			var2val["PERCENT_BASES_COVERAGE>=20(NODUP)"]=str(100*float(value[pct20Index]))
			var2val["PERCENT_BASES_COVERAGE>=30(NODUP)"]=str(100*float(value[pct30Index]))
			var2val["PERCENT_BASES_COVERAGE>=40(NODUP)"]=str(100*float(value[pct50Index]))
			var2val["PERCENT_BASES_COVERAGE>=50(NODUP)"]=str(100*float(value[pct100Index]))

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

## Extract the information from the insertsize summary file using mkdup bam
if insert_file!=None:
	insertSum=open(insert_file).readlines()
	for i in range(len(insertSum)):
		if insertSum[i].startswith('MEDIAN_INSERT_SIZE'):
			header = insertSum[i].rstrip().split("\t")
			for j in range(len(header)):
				if header[j]=="MEAN_INSERT_SIZE":
					meanInsertSizeIndex = j
			break
	value = insertSum[i+1].rstrip().split("\t")
	for j in range(len(value)):
		var2val["MEAN_INSERT_SIZE"]=value[meanInsertSizeIndex]
else:
		var2val["MEAN_INSERT_SIZE"]="NA"
		
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
		

## Extract the information from the pileup file
N=targetSize
cov1,cov10,cov20,cov30,cov50,cov100,cov200,cov300,cov500,cov1000 = 0,0,0,0,0,0,0,0,0,0
pct_mean_100,pct_mean_75,pct_mean_50,pct_mean_25,pct_mean_10=0,0,0,0,0
coverages=list()
if pileup_file!=None:
	pileupSum=open(pileup_file)
	for line in pileupSum:
		value = line.rstrip().split("\t")
		coverages.append(int(value[3]))
		if int(value[3])>=1:
			cov1+=1
		if int(value[3])>=10:
			cov10+=1
		if int(value[3])>=20:
			cov20+=1
		if int(value[3])>=30:
			cov30+=1
		if int(value[3])>=50:
			cov50+=1
		if int(value[3])>=100:
			cov100+=1
		if int(value[3])>=200:
			cov200+=1
		if int(value[3])>=300:
			cov300+=1
		if int(value[3])>=500:
			cov500+=1
		if int(value[3])>=1000:
			cov1000+=1
			
	var2val["PERCENT_BASES_COVERAGE>=1"]=str(100*cov1/float(N))
	var2val["PERCENT_BASES_COVERAGE>=10"]=str(100*cov10/float(N))
	var2val["PERCENT_BASES_COVERAGE>=20"]=str(100*cov20/float(N))
	var2val["PERCENT_BASES_COVERAGE>=30"]=str(100*cov30/float(N))
	var2val["PERCENT_BASES_COVERAGE>=50"]=str(100*cov50/float(N))
	var2val["PERCENT_BASES_COVERAGE>=100"]=str(100*cov100/float(N))
	var2val["PERCENT_BASES_COVERAGE>=200"]=str(100*cov200/float(N))
	var2val["PERCENT_BASES_COVERAGE>=300"]=str(100*cov300/float(N))
	var2val["PERCENT_BASES_COVERAGE>=500"]=str(100*cov500/float(N))
	var2val["PERCENT_BASES_COVERAGE>=1000"]=str(100*cov1000/float(N))
	
	meanCov,pct_mean_100,pct_mean_75,pct_mean_50,pct_mean_25,pct_mean_10=percentBaseCoverageRelative2Mean(coverages,N)
	
	var2val["MEAN_BASE_COVERAGE"]=str(meanCov)
	var2val["PERCENT_BASES_COVERAGE>=0.1*MEAN"]=str(100*pct_mean_10/float(N))
	var2val["PERCENT_BASES_COVERAGE>=0.25*MEAN"]=str(100*pct_mean_25/float(N))
	var2val["PERCENT_BASES_COVERAGE>=0.5*MEAN"]=str(100*pct_mean_50/float(N))
	var2val["PERCENT_BASES_COVERAGE>=0.75*MEAN"]=str(100*pct_mean_75/float(N))
	var2val["PERCENT_BASES_COVERAGE>=MEAN"]=str(100*pct_mean_100/float(N))
	
## Extract the information from the bed coverage file
region2coverage=dict()
region_coverages=list()
region_coverage_mean,region_coverage_std,per_region_coverage_cv=0,0,0
if coverage_file!=None:
	coverageSum=open(coverage_file)
	for line in coverageSum:
		value = line.rstrip().split("\t")
		if value[4] not in region2coverage:
			region2coverage[value[4]]=[1,float(value[6])]
		else:
			region2coverage[value[4]][0]+=1
			region2coverage[value[4]][1]+=float(value[6])
	
	region2cov_outfile=re.sub('.txt','.base.coverage.per.targetRegion.txt',outfile)
	region2cov_output=open(region2cov_outfile,'w')
	region2cov_output.write("## REGION\tThe target capture region\n")
	region2cov_output.write("## MEAN_BASE_COVERAGE\tThe mean base coverage among different sites in the target region\n")
	region2cov_output.write("REGION\tMEAN_BASE_COVERAGE\n")
	for region in sorted(region2coverage.keys()):
		region2cov_output.write("%s\t%.3f\n" % (region,region2coverage[region][1]/region2coverage[region][0]))
		region_coverages.append(region2coverage[region][1]/region2coverage[region][0])
	region_coverage_mean=sum(region_coverages)/len(region_coverages)
	region_coverage_std=stddev(region_coverages)
	per_region_coverage_cv=region_coverage_mean/region_coverage_std
	var2val["MEAN_BASE_COVERAGE_PER_TARGET_REGION"]=str(region_coverage_mean)
	var2val["STD_BASE_COVERAGE_PER_TARGET_REGION"]=str(region_coverage_std)
	var2val["CV_BASE_COVERAGE_PER_TARGET_REGION"]=str(per_region_coverage_cv)
		
output=open(outfile,"w")
output.write("## SAMPLE\tThe sample ID\n")
output.write("## PROJECT_ID\tThe project ID\n")
output.write("## RUN_ID\tThe Run ID\n")
output.write("## DATE\tThe date of the sequencing run\n")
output.write("## ESTIMATED_LIBRARY_SIZE\tThe number of unique molecules in the DNA library\n")
output.write("## FOLD_80_BASE_PENALTY\tThe fold over-coverage necessary to raise 80% of bases in 'non-zero-cvg' targets to the mean coverage level in those targets\n")
output.write("## GC_NC_0_19\tNormalized coverage over quintile of GC content ranging from 0 - 19\n")
output.write("## GC_NC_20_39\tNormalized coverage over quintile of GC content ranging from 20 - 39\n")
output.write("## GC_NC_40_59\tNormalized coverage over quintile of GC content ranging from 40 - 59\n")
output.write("## GC_NC_60_79\tNormalized coverage over quintile of GC content ranging from 60 - 79\n")
output.write("## GC_NC_80_100\tNormalized coverage over quintile of GC content ranging from 80 - 100\n")
output.write("## MEAN_BASE_COVERAGE\tThe mean of base coverages in all target sites\n")
output.write("## MEAN_INSERT_SIZE\tThe mean of DNA insert sizes in the DNA library\n")
output.write("## MEAN_READ_LENGTH\tThe mean of DNA sequencing read length\n")
output.write("## PERCENT_BASES_COVERAGE>=0.1*MEAN\tThe percent of DNA sites that have a base coverage >= 10% of the mean base coverage\n")
output.write("## PERCENT_BASES_COVERAGE>=0.25*MEAN\tThe percent of DNA sites that have a base coverage >= 25% of the mean base coverage\n")
output.write("## PERCENT_BASES_COVERAGE>=0.5*MEAN\tThe percent of DNA sites that have a base coverage >= 50% of the mean base coverage\n")
output.write("## PERCENT_BASES_COVERAGE>=0.75*MEAN\tThe percent of DNA sites that have a base coverage >= 75% of the mean base coverage\n")
output.write("## PERCENT_BASES_COVERAGE>=MEAN\tThe percent of DNA sites that have a base coverage >= the mean base coverage\n")
output.write("## PERCENT_BASES_COVERAGE>=1\tThe percent of DNA sites that have base coverage >= 1\n")
output.write("## PERCENT_BASES_COVERAGE>=5\tThe percent of DNA sites that have base coverage >= 5 (Available in WGS)\n")
output.write("## PERCENT_BASES_COVERAGE>=10\tThe percent of DNA sites that have base coverage >= 10\n")
output.write("## PERCENT_BASES_COVERAGE>=20\tThe percent of DNA sites that have base coverage >= 20\n")
output.write("## PERCENT_BASES_COVERAGE>=30\tThe percent of DNA sites that have base coverage >= 30\n")
output.write("## PERCENT_BASES_COVERAGE>=40\tThe percent of DNA sites that have base coverage >= 40 (Available in WGS)\n")
output.write("## PERCENT_BASES_COVERAGE>=50\tThe percent of DNA sites that have base coverage >= 50\n")
output.write("## PERCENT_BASES_COVERAGE>=100\tThe percent of DNA sites that have base coverage >= 100\n")
output.write("## PERCENT_BASES_COVERAGE>=200\tThe percent of DNA sites that have base coverage >= 200\n")
output.write("## PERCENT_BASES_COVERAGE>=300\tThe percent of DNA sites that have base coverage >= 300\n")
output.write("## PERCENT_BASES_COVERAGE>=500\tThe percent of DNA sites that have base coverage >= 500\n")
output.write("## PERCENT_BASES_COVERAGE>=1000\tThe percent of DNA sites that have base coverage >= 1000\n")
output.write("## PERCENT_DUPLICATION\tThe percent of duplicated reads in the DNA library\n")
output.write("## PERCENT_OFF_BAIT\tThe percent of bases that mapped away from the bait region\n")
output.write("## PERCENT_PF_READS_ALIGNED\tThe percent of PF reads that were aligned to the reference sequence\n")
output.write("## PERCENT_PF_UQ_READS\tThe percent of PF reads that were not marked as duplicates\n")
output.write("## PERCENT_PF_UQ_READS_ALIGNED\tThe percent of PF reads that were not marked as duplicates and were aligned to reference sequence\n")
output.write("## PERCENT_READS_ALIGNED_IN_PAIRS\tThe percent of PF reads that were aligned to the reference sequence in pairs\n")
output.write("## PERCENT_SELECTED_BASES\tThe percent of bases that mapped to bait region or near bait region\n")
output.write("## PERCENT_USABLE_BASES_ON_TARGET\tThe percent of aligned, de-duped, on-target bases out of all of the PF bases available\n")
output.write("## PERCENT_ZERO_COVERAGE_TARGET\tThe percent of target regions that do not receive any reads in any bases\n")
output.write("## PF_READS_ALIGNED\tThe number of PF reads that were aligned to the reference sequence\n")
output.write("## TOTAL_READS\tThe total number of reads including all PF and non-PF reads\n")
output.write("## MEAN_BASE_COVERAGE_PER_TARGET_REGION\tThe mean of 'mean base coverage' among different target regions in the library\n")
output.write("## STD_BASE_COVERAGE_PER_TARGET_REGION\tThe standard deviation of 'mean base coverage' among different target regions in the library\n")
output.write("## CV_BASE_COVERAGE_PER_TARGET_REGION\tThe coefficient of variance of 'mean base coverage' among different target regions in the library\n")

output.write("SAMPLE\t%s\n" % sample)
output.write("PROJECT_ID\t%s\n" % project_id)
output.write("RUN_ID\t%s\n" % run_id)
output.write("DATE\t%s\n" % date)
output.write("READ_TYPE\t%s\n" % read_type)
for key in sorted(var2val.keys()):
	output.write("%s\t%s\n" % (key,var2val[key]))
	
	