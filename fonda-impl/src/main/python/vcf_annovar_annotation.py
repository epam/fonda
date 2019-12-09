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

## This annotation variant script is used to annotate the consequence of the variants in vcf4 format.
## Currently supports vcf>=4.0
## Transvar is used to annotate the genomic variants to the cDNA/protein level
## For transvar annotated results, a scoring system is used to rank the annotated variants and choose the one that is potentially most impactful
## Annovar is used to annotate the functional impact of the genomic variants by incoporating multiple scoring databases

import os,subprocess,sys,getopt,re

def usage():
	print 'Usage:\n'
	print '	-s, --sample (required)          The sample name of the analysis.\n'
	print '	-i, --input (required)           The vcf file for annotation.\n'
	print ' -o, --output (optional)          The output annotation file.\n'
	print ' -r, --refversion (optional)      The reference version of transvar annotation databases, the default setting is hg19\n'
	print '	--canonical (required)           The list of canonical ensembl transcripts for annotation\n'
	print ' --transvar (required)            The executable for transvar annotation\n'
	print '	--coding_only (optional)         Specify this option if want to ouput only the coding region variants, the default setting is off.\n'
	print ' --pass_only (optional)           Specify this option if want to output only the PASS variants in vcf, the default setting is off.\n'
	print ' --annovar (required)             The directory in which annovar related scripts and documents locate\n'
	print '	--genome (optional)              The option to add 1000genome snp annotation, the default setting is False\n'
	print '	--esp6500 (optional)             The option to add esp6500 snp annotation, the default setting is False\n'
	print '	--dbsnp (optional)               The option to add dbsnp annotation, the default setting is False\n'
	print '	--clinvar (optional)             The option to add clinvar annotation, the default setting is False\n'
	print '	--cosmic (optional)              The option to add cosmic annotation, the default setting is False\n'
	print '	--multiscore (optional)          The option to add multiple functional annotations such as SIFT, Polyphen2, GERP, etc, the default setting is False\n'

class ArgumentError(Exception):
	pass

def parse_arguments(argv):
	sample,srcfile,annofile,tool,canonical_transcripts,annovar,transvar = None,None,None,None,None,None,None
	genome,esp6500,dbsnp,clinvar,cosmic,multiscore=False,False,False,False,False,False
	coding,pass_status = False,False
	refversion = 'hg19'

	try:
		opts, args = getopt.getopt(argv[1:], "s:i:o:r:h", \
			["sample=","input=", "output=", "refversion=", "canonical=","annovar=","transvar=", \
			"genome","esp6500","dbsnp","clinvar","cosmic","multiscore","coding_only","pass_only"])
	except getopt.GetoptError as err:
		# print help information and exit:
		print str(err)  # will print something like "option -a not recognized"
		usage()

	for opt, arg in opts:
		if opt == '-h':
			usage()
			sys.exit()
		
		elif opt in ("-s", "--sample"):
			sample = arg
			
		elif opt in ("-i", "--input"):
			srcfile = arg
			if not os.path.isfile(srcfile):
				raise ArgumentError("Argument Error: Invalid variant vcf file passed.")
			if annofile == None:
				annofile = srcfile+".annotation"
				
		elif opt in ("-o", "--output"):
			annofile = arg
			
		elif opt in ("-r", "--refversion"):
			refversion = arg
			
		elif opt in ("--canonical"):
			canonical_transcripts = arg
			if not os.path.isfile(canonical_transcripts):
				raise ArgumentError("Argument Error: Invalid canonical transcript passed.")
		
		elif opt in ("--annovar"):
			annovar = arg
			if not os.path.exists(annovar):
				raise ArgumentError("Argument Error: Invalid annovar directory passed.")
		
		elif opt in ("--transvar"):
			transvar = arg
			if not os.access(transvar, os.X_OK):
				raise ArgumentError("Argument Error: Invalid transvar executable passed.")
				
		elif opt in ("--genome"):
			genome = True
			
		elif opt in ("--esp6500"):
			esp6500 = True
			
		elif opt in ("--dbsnp"):
			dbsnp = True
		
		elif opt in ("--clinvar"):
			clinvar = True
			
		elif opt in ("--cosmic"):
			cosmic = True
			
		elif opt in ("--multiscore"):
			multiscore = True
		
		elif opt in ("--coding_only"):
			coding = True
		
		elif opt in ("--pass_only"):
			pass_status = True
			
		else:
			raise ArgumentError("Bad argument: I don't know what %s is" % arg)

	if sample is None or srcfile is None or canonical_transcripts is None or annovar is None or transvar is None:
		raise ArgumentError("You need to supply an input vcf, a canonical transcript list, an annovar directory and a transvar executable!") 
		
    # return argument values
	return sample,srcfile,annofile,refversion,canonical_transcripts,genome,esp6500,dbsnp,clinvar,cosmic,multiscore,annovar,transvar,coding,pass_status
	
def run (cmd):
    subprocess.call(cmd, shell = True)
    return

def variantType(region,dna,cdna,pro,info):
	variant_type = 'unknown'
	if pro.endswith('*'):
		variant_type = 'nonsense'
	elif 'nonsense' in info or 'stopgain' in info or 'Nonsense' in info or 'Stopgain' in info:
		variant_type = 'nonsense'
	elif 'stop_loss' in info or 'Stop_loss' in info:
		variant_type = 'nonstop'
	elif 'start_loss' in info or 'Start_loss' in info:
		variant_type = 'start_codon_loss'
	elif 'missense' in info or 'Missense' in info:
		variant_type = 'missense'
	elif 'fs*' in pro:
		if 'ins' in dna or 'ins' in cdna:
			variant_type = 'frame_shift_insertion'
		elif 'del' in dna or 'del' in cdna:
			variant_type = 'frame_shift_deletion'
		elif 'dup' in dna or 'dup' in cdna:
			variant_type = 'frame_shift_insertion'
	elif 'ins' in pro:
		variant_type = 'in_frame_insertion'
	elif 'del' in pro:
		variant_type = 'in_frame_deletion'
	elif 'dup' in pro:
		variant_type = 'in_frame_insertion'
	elif 'splice' in info or 'Splice' in info:
		variant_type = 'splice_site'
	elif 'silent' in info or 'synonymous' in info or 'Silent' in info or 'Synonymous' in info:
		variant_type = 'silent'
	elif '3-UTR' in info or '3-UTR' in region:
		variant_type = '3-UTR'
	elif '5-UTR' in info or '5-UTR' in region:
		variant_type = '5-UTR'
	elif 'intron' in info or 'intron' in region:
		variant_type = 'intron'
	elif 'noncoding' in info or 'noncoding' in region:
		variant_type = 'non-coding'
	
	return variant_type

def variantScore(transcript,consequence_type,region,variant_type,dna,cdna,pro):
	score1,score2 = 99,50
	scores1 = {
		'nonsense':0,
		'nonstop':0,
		'missense':1,
		'in_frame':1,
		'frameshift':2,
		'start_codon':3,
		'stop_codon':3,
		'splice_site':4,
		'miRNA':4,
		'silent':5,
		'synonymous':5,
		'3-UTR':6,
		'5-UTR':6,
		'intron':7,
		'flank':8,
		'noncoding':9,
	}
	scores2 = {
		'protein_coding':0,
		'novel_protein_coding':1,
		'nonsense_mediated_decay':2,
		'nonstop_decay':3,
		'processed_transcript':4,
		'retained_intron':5,
		'antisense':6,
		'pseudogene':7,
		'processed_pseudogene':8,
		'translated_pseudogene':9,
		'transcribed_pseudogene':10,
		'unprocessed_pseudogene':11,
		'polymorphic_pseudogene':12,
		'unitary_pseudogene':13,
		'non_coding':14,
		'lincRNA':15,
		'sense_intronic':16,
		'sense_overlapping':17,
		'macro_lncRNA':18,
		'miRNA':19,
		'piRNA':20,
		'rRNA':21,
		'siRNA':22,
		'snRNA':23,
		'snoRNA':24,
		'tRNA':25,
		'vaultRNA':26,
		'unclassified_processed_transcript':27
	}
	if variant_type == 'nonsense':
		score1 = 0
	elif variant_type == 'nonstop':
		score1 = 0
	elif variant_type == 'start_codon_loss':
		score1 = 0
	elif variant_type == 'missense':
		score1 = 1
	elif variant_type == 'in_frame_insertion' or variant_type == 'in_frame_deletion':
		score1 = 1
	elif variant_type == 'frame_shift_insertion' or variant_type == 'frame_shift_deletion':
		score1 = 2
	elif variant_type == 'splice_site':
		score1 = 4
	elif variant_type == 'silent' or variant_type == 'synonymous':
		score1 = 5
	elif variant_type == '3-UTR' or variant_type =='5-UTR':
		score1 = 6
	elif variant_type == 'intron':
		score1 = 7
	elif variant_type == 'non-coding':
		score1 = 9
		
	if consequence_type in scores2:
		score2 = scores2[consequence_type]
		
	return score1,score2

## underlying scoring system of choosing variants
## 1) whether all transcripts for the variant include the transcripts that were considered to be canonical;
		## if only one transcript found, then report the protein variant from that specific transcript;
		## if none was found, go to 2);
		## if more than one were found, reduce the candidate transcripts to the matched ones and then go to 2).
## 2) score the variant type, lower score indicates higher significance;
## 3) if ties, score the transcript type, lower score indicates higher significance;
## 4) if ties, check whether the transcripts with the highest significance contain the longest transcript.
		## if yes, report the longest transcript for the variant; if ties, sort the transcripts and report the first transcript in the bank;
		## if not, sort the transcripts and report the first transcript in the bank.
def variant_annotation(variant,annotates,prefer_transcripts,longest_transcripts):
	keep_one_anno=dict()
	for data in annotates:
		transcript,gene,strand,coordinate,region,info=data
		if len(transcript.split('('))==2:
			transcript,consequence_type=transcript.split('(')[0][:-1],transcript.split('(')[1][:-1]
		else:
			transcript,consequence_type='.','.'
		dna,cdna,pro=coordinate.split('/')
		variant_type = variantType(region,dna,cdna,pro,info)
		score1,score2 = variantScore(transcript,consequence_type,region,variant_type,dna,cdna,pro)
		keep_one_anno[transcript]=[score1,score2,strand,gene,consequence_type,region,variant_type,dna,cdna,pro]
	
	n=0
	intersect_transcripts=list()
	for transcript in keep_one_anno:	
		if transcript in prefer_transcripts:
			intersect_transcripts.append(transcript)
			n+=1
	if n==1:
		score1,score2,strand,gene,consequence_type,region,variant_type,dna,cdna,pro = keep_one_anno[intersect_transcripts[0]]
		return (strand,gene,transcript,consequence_type,region,variant_type,cdna,pro)
	elif n==0:
		if len(keep_one_anno)>1:
			tmp_transcripts=list()
			transcript,values=sorted(keep_one_anno.items(),key=lambda kv:(kv[1][0],kv[1][1],kv[0]))[0]
			score1,score2,strand,gene,consequence_type,region,variant_type,dna,cdna,pro = values
			for tmp_transcript,tmp_value in sorted(keep_one_anno.items(),key=lambda kv:(kv[1][0],kv[1][1],kv[0])):
				tmp_score1,tmp_score2,tmp_gene = tmp_value[0],tmp_value[1],tmp_value[3]
				if tmp_score1==score1 and tmp_score2==score2:
					tmp_transcripts.append(variant+'_'+tmp_gene+'_'+tmp_transcript)					
			for tmp_variant in tmp_transcripts:
				if tmp_variant in longest_transcripts:
					return longest_transcripts[tmp_variant]
					break
			else:
				return (strand,gene,transcript,consequence_type,region,variant_type,cdna,pro)
		else:
			transcript,values=sorted(keep_one_anno.items(),key=lambda kv:(kv[1][0],kv[1][1]))[0]
			score1,score2,strand,gene,consequence_type,region,variant_type,dna,cdna,pro = values
			return (strand,gene,transcript,consequence_type,region,variant_type,cdna,pro)
	else:
		keep_one_anno_sub=dict()
		for key in keep_one_anno:
			if key in intersect_transcripts:
				keep_one_anno_sub[key] =keep_one_anno[key]

		if len(keep_one_anno_sub)>1:
			tmp_transcripts=list()
			transcript,values=sorted(keep_one_anno_sub.items(),key=lambda kv:(kv[1][0],kv[1][1],kv[0]))[0]
			score1,score2,strand,gene,consequence_type,region,variant_type,dna,cdna,pro = values
			for tmp_transcript,tmp_value in sorted(keep_one_anno_sub.items(),key=lambda kv:(kv[1][0],kv[1][1],kv[0])):
				tmp_score1,tmp_score2,tmp_gene = tmp_value[0],tmp_value[1],tmp_value[3]
				if tmp_score1==score1 and tmp_score2==score2:
					tmp_transcripts.append(variant+'_'+tmp_gene+'_'+tmp_transcript)					
			for tmp_variant in tmp_transcripts:
				if tmp_variant in longest_transcripts:
					return longest_transcripts[tmp_variant]
					break
			else:
				return (strand,gene,transcript,consequence_type,region,variant_type,cdna,pro)
		else:
			transcript,values=sorted(keep_one_anno_sub.items(),key=lambda kv:(kv[1][0],kv[1][1]))[0]
			score1,score2,strand,gene,consequence_type,region,variant_type,dna,cdna,pro = values
			return (strand,gene,transcript,consequence_type,region,variant_type,cdna,pro)				
		
	keep_one_anno.close()
	keep_one_anno_sub.close()


def transvar_output(sample,passfile,transvar_annotation,output):
	tmp = open(output,'w')
	input = open(passfile)
	chrom_index,position_index,ref_index,alt_index,filter_index,format_index,sample_index="","","","","","",""
	## currently supports vcf>=4.0
	for value in input:
		if value.startswith("#CHROM"):
			header=value
			tmp.write("#CHROM\tPOS\tREF\tALT\tSAMPLE\tGENOTYPE\tCOVERAGE\tAllele_FQ\tMUT_STATUS\tSTRAND\tGENE_SYMBOL\tENSEMBL_TRANSCRIPT\tCONSEQUENCE_TYPE\tVARIANT_REGION\tVARIANT_TYPE\tcDNA_CHANGE\tAA_CHANGE\n")
			data = value.rstrip().split("\t")
			for i in range(len(data)):
				if data[i]=="#CHROM":
					chrom_index = i
				elif data[i]=="POS":
					position_index = i
				elif data[i]=="REF":
					ref_index = i
				elif data[i]=="ALT":
					alt_index = i
				elif data[i]=="INFO":
					info_index = i
				elif data[i]=="FORMAT":
					format_index = i
				elif data[i]==sample or data[i].upper()=="SAMPLE" or data[i].upper()=='TUMOR':
					sample_index = i
		## selectively output some variables in vcf file
		## chr,pos,ref,alt,sample,gt,dp,af
		if not value.startswith("#"):
			data = value.rstrip().split("\t")
			variant = data[chrom_index]+"_"+data[position_index]+"_"+data[ref_index]+"_"+data[alt_index]
			if variant in transvar_annotation:
				af,cov,ad,gt,status='-',0,0,'-','-'
				if info_index!="":
					infor = data[info_index].split(";")
					for inf in infor:
						if len(inf.split("="))==2:
							param,value=inf.split("=")
							if param=="AF":
								af=float(value)
							if param=="DP":
								cov=value
							if param=="STATUS":
								status=value.upper()
						elif len(inf.split("="))==1:
							if inf.upper()=="SOMATIC":
								status=inf.upper()
									
				if format_index!="":
					format = data[format_index].split(":")
					sampleValues = data[sample_index].split(":")
					for p in range(len(format)):
						if format[p]=="GT":
							gt=sampleValues[p]
						if af=='-':
							if format[p]=="DP":
								cov=sampleValues[p]
							if format[p]=="AF":
								af=float(sampleValues[p])
							if format[p]=="AO":
								ad=sampleValues[p]
							if format[p]=="AD":
								ad=sampleValues[p].split(",")[1]
								if cov==0:
									cov=str(int(sampleValues[p].split(",")[0])+int(sampleValues[p].split(",")[1]))
							if format[p]==data[alt_index]+"U":  ## Specific to strelka2 SNV vcf output
								ad=sampleValues[p].split(",")[0]
							if format[p]=="TIR":                ## Specific to strelka2 INDEL vcf output
								ad=sampleValues[p].split(",")[0]
								
					if af=='-':
						if int(ad)>0:
							af=float(ad)/float(cov)
							
				if status.endswith("SOMATIC") or status.startswith("SOMATIC") or status=="-":
					if af=='-':
						tmp.write("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n" \
							% (data[chrom_index],data[position_index],data[ref_index],data[alt_index],\
							sample,gt,cov,af,status,'\t'.join(transvar_annotation[variant])))
					else:
						tmp.write("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%.3f\t%s\t%s\n" \
							% (data[chrom_index],data[position_index],data[ref_index],data[alt_index],\
							sample,gt,cov,af,status,'\t'.join(transvar_annotation[variant])))
	tmp.close()


## The output files from annovar annotation are pretty adhoc, be sure to careful review this unit if a newer version of annovar is used
def annovar_annotation(merge,annovar_passfile,vcf2annovar_var_match,dbtype,refversion):
	if dbtype == 'multiscore':
		cmd = 'echo `date` begin annotate '+dbtype+' variants using ANNOVAR...\n'
		cmd += 'perl '+ANNOVAR_TABLE+' -buildver '+refversion+' -protocol '+db[dbtype]+' '
		cmd += annovar_passfile+' '+ANNOVAR_DB+' -operation f -nastring - \n'
		dropped = annovar_passfile+'.'+refversion+'_'+outname[dbtype]+'_dropped'
		filtered = annovar_passfile+'.'+refversion+'_'+outname[dbtype]+'_filtered'
		log = annovar_passfile+'.log'
		invalid = annovar_passfile+'.invalid_input'
		all = annovar_passfile+'.'+refversion+'_multianno.txt'
		cmd += 'echo `date` finish annotate '+dbtype+' variants using ANNOVAR...\n'
		run(cmd)
		
		annotated_variants=dict()
		anno=open(all)
		header = anno.readline()
		## SIFT                D: Deleterious (sift<=0.05); T: tolerated (sift>0.05)
		## PolyPhen 2 HDIV     D: Probably damaging (>=0.957), P: possibly damaging (0.453<=pp2_hdiv<=0.956); B: benign (pp2_hdiv<=0.452)
		## PolyPhen 2 HVar     D: Probably damaging (>=0.909), P: possibly damaging (0.447<=pp2_hdiv<=0.909); B: benign (pp2_hdiv<=0.446)
		## MutationTaster      A ("disease_causing_automatic"); "D" ("disease_causing"); "N" ("polymorphism"); "P" ("polymorphism_automatic")
		## MutationAssessor    H: high; M: medium; L: low; N: neutral. H/M means functional and L/N means non-functional
		## FATHMM              D: Deleterious; T: Tolerated
		## VEST3               higher scores are more deleterious
		## CADD raw            higher scores are more significant(>4)
		## CADD phred          20 means top 1%, 30 means top 0.1%. percentage = 10^(-CADD phred/10)
		## GERP++              higher scores are more deleterious(>2)
		## PhyloP              higher scores are more deleterious
		for data in anno:
			annotates = data.rstrip().split("\t")
			chr,start,end,ref,alt=annotates[:5]
			sift_score,sift_pred=annotates[5:7]
			poly2HDIV_score,poly2HDIV_pred=annotates[7:9]
			poly2HVAR_score,poly2HVAR_pred=annotates[9:11]
			MT_score,MT_pred=annotates[13:15]
			MA_score,MA_pred=annotates[15:17]
			FATHMM_score,FATHMM_pred=annotates[17:19]
			VEST3score=annotates[21]
			CADD_score,CADD_phred=annotates[22:24]
			gerpRS=annotates[33]
			phyloP20way_mammalian=annotates[35]
			phastCons20way_mammalian=annotates[37]
			anno_variant=chr+':'+start+'_'+end+ref+'/'+alt
			if anno_variant not in annotated_variants:
				annotated_variants[anno_variant]=[sift_score,sift_pred,poly2HDIV_score,poly2HDIV_pred,poly2HVAR_score,poly2HVAR_pred,
											 MT_score,MT_pred,MA_score,MA_pred,
											 FATHMM_score,FATHMM_pred,VEST3score,CADD_score,CADD_phred,gerpRS,
											 phyloP20way_mammalian,phastCons20way_mammalian]
		
		merge_dbtype = open(re.sub('\.annotation','.'+dbtype+'.annotation',merge),'w')
		input = open(merge)
		header = input.readline()
		data = header.rstrip().split("\t")
		for i in range(len(data)):
			if data[i]=="#CHROM":
				chrom_index = i
			elif data[i]=="POS":
				position_index = i
			elif data[i]=="REF":
				ref_index = i
			elif data[i]=="ALT":
				alt_index = i
		merge_dbtype.write("%s\tSIFT\tPolyPhen2_HDIV\tPolyPhen2_HDAR\tMutationTaster\tMutationAssessor\tFATHMM\tVEST3\tCADD_raw\tCADD_phred\tGERP++\tphyloP20way\tphastCons20way\n" % header.rstrip())
		for data in input:
			annotates = data.rstrip().split("\t")
			variant = annotates[chrom_index]+"_"+annotates[position_index]+"_"+annotates[ref_index]+"_"+annotates[alt_index]
			if variant in vcf2annovar_var_match:
				anno_variant = vcf2annovar_var_match[variant]
				if anno_variant in annotated_variants:
					sift_score,sift_pred,poly2HDIV_score,poly2HDIV_pred,poly2HVAR_score,poly2HVAR_pred,MT_score,MT_pred,MA_score,MA_pred,FATHMM_score,FATHMM_pred,VEST3score,CADD_score,CADD_phred,gerpRS,phyloP20way_mammalian,phastCons20way_mammalian = annotated_variants[anno_variant]
					merge_dbtype.write("%s\t%s(%s)\t%s(%s)\t%s(%s)\t%s(%s)\t%s(%s)\t%s(%s)\t%s\t%s\t%s\t%s\t%s\t%s\n" %
									   (data.rstrip(),sift_score,sift_pred,poly2HDIV_score,poly2HDIV_pred,poly2HVAR_score,poly2HVAR_pred,
										MT_score,MT_pred,MA_score,MA_pred,FATHMM_score,FATHMM_pred,VEST3score,CADD_score,CADD_phred,gerpRS,
										phyloP20way_mammalian,phastCons20way_mammalian))
				else:
					merge_dbtype.write("%s\t-(-)\t-(-)\t-(-)\t-(-)\t-(-)\t-(-)\t-\t-\t-\t-\t-\t-\n" % data.rstrip())
			else:
				merge_dbtype.write("%s\t-(-)\t-(-)\t-(-)\t-(-)\t-(-)\t-(-)\t-\t-\t-\t-\t-\t-\n" % data.rstrip())
		merge_dbtype.close()
		
		cmd = 'echo `date` begin remove '+dbtype+' temporary files...\n'
		cmd += 'rm -rf '+dropped+'\n'
		cmd += 'rm -rf '+filtered+'\n'
		cmd += 'rm -rf '+log+'\n'
		cmd += 'rm -rf '+invalid+'\n'
		cmd += 'rm -rf '+all+'\n'
		cmd += 'rm -rf '+merge+'\n'
		cmd += 'echo `date` finish remove '+dbtype+' temporary files...\n'
		run(cmd)
		merge = re.sub('\.annotation','.'+dbtype+'.annotation',merge)
		
	else:
		cmd = 'echo `date` begin annotate '+dbtype+' variants using ANNOVAR...\n'
		cmd += 'perl '+ANNOVAR_ANNO+' -buildver '+refversion+' -filter -dbtype '+db[dbtype]+' '
		cmd += annovar_passfile+' '+ANNOVAR_DB+'\n'
		dropped = annovar_passfile+'.'+refversion+'_'+outname[dbtype]+'_dropped'
		filtered = annovar_passfile+'.'+refversion+'_'+outname[dbtype]+'_filtered'
		log = annovar_passfile+'.log'
		invalid = annovar_passfile+'.invalid_input'
		cmd += 'echo `date` finish annotate '+dbtype+' variants using ANNOVAR...\n'
		run(cmd)
	
		annotated_variants=dict()
		for data in open(dropped):
			annotates = data.rstrip().split("\t")
			score,chr,start,end,ref,alt=annotates[1:7]
			anno_variant=chr+':'+start+'_'+end+ref+'/'+alt
			if anno_variant not in annotated_variants:
				annotated_variants[anno_variant]=score
		
		merge_dbtype = open(re.sub('\.annotation','.'+dbtype+'.annotation',merge),'w')
		input = open(merge)
		header = input.readline()
		data = header.rstrip().split("\t")
		for i in range(len(data)):
			if data[i]=="#CHROM":
				chrom_index = i
			elif data[i]=="POS":
				position_index = i
			elif data[i]=="REF":
				ref_index = i
			elif data[i]=="ALT":
				alt_index = i
		merge_dbtype.write("%s\t%s\n" % (header.rstrip(),alias[dbtype]))
		for data in input:
			annotates = data.rstrip().split("\t")
			variant = annotates[chrom_index]+"_"+annotates[position_index]+"_"+annotates[ref_index]+"_"+annotates[alt_index]
			if variant in vcf2annovar_var_match:
				anno_variant = vcf2annovar_var_match[variant]
				if anno_variant in annotated_variants:
					merge_dbtype.write("%s\t%s\n" % (data.rstrip(),annotated_variants[anno_variant]))
				else:
					merge_dbtype.write("%s\t-\n" % data.rstrip())
			else:
				merge_dbtype.write("%s\t-\n" % data.rstrip())
		merge_dbtype.close()
	
		cmd = 'echo `date` begin remove '+dbtype+' temporary files...\n'
		cmd += 'rm -rf '+dropped+'\n'
		cmd += 'rm -rf '+filtered+'\n'
		cmd += 'rm -rf '+log+'\n'
		cmd += 'rm -rf '+invalid+'\n'
		cmd += 'rm -rf '+merge+'\n'
		cmd += 'echo `date` finish remove '+dbtype+' temporary files...\n'
		run(cmd)
		merge = re.sub('\.annotation','.'+dbtype+'.annotation',merge)
		
	return (merge)

## load the inputs			
sample,srcfile,annofile,refversion,canonical_transcripts,genome,esp6500,dbsnp,clinvar,cosmic,multiscore,ANNOVAR_DIR,TRANSVAR,coding,pass_status= \
	parse_arguments(sys.argv)

## Prerequisite tools and documents
ANNOVAR_ANNO=ANNOVAR_DIR+'/annotate_variation.pl'
ANNOVAR_TABLE=ANNOVAR_DIR+'/table_annovar.pl'
VCF2ANNOVAR=ANNOVAR_DIR+'/convert2annovar.pl'
ANNOVAR_DB=ANNOVAR_DIR+'/humandb/'

## load the cononical transcripts
prefer_transcripts = dict()
transcripts = open(canonical_transcripts)
for transcript in transcripts:
	transcript=transcript.rstrip().split('.')[0]
	prefer_transcripts[transcript]=""

## annotate only pass variants
if pass_status:
	passfile = re.sub('\.vcf','.pass.vcf',srcfile)
	cmd = "echo `date` begin select only the PASS variants for annotation...\n"
	cmd += "grep '#\|PASS' "+srcfile+" > "+passfile+"\n"
	cmd += "echo `date` finish select only the PASS variants for annotation.\n"
	run(cmd)
## annotate all variants
else:
	passfile = srcfile
		
## perform transvar annotation on the input variants
transvar_anno = passfile+'.transvar'
transvar_anno_longest = passfile+'.transvar.longest'
cmd = 'echo `date` begin Transvar annotation...\n'
cmd += TRANSVAR+' ganno --ensembl --refversion '+refversion+' --vcf '+passfile+' > '+transvar_anno+'\n'
cmd += TRANSVAR+' ganno --ensembl --refversion '+refversion+' --longest --vcf '+passfile+' > '+transvar_anno_longest+'\n'
cmd += 'echo `date` finish Transvar annotation.\n\n'
run(cmd)

## load the information of longest transcript annotation
transvar_longest = open(transvar_anno_longest)
header = transvar_longest.readline()
longest_transcripts=dict()
for value in transvar_longest:
	if value.startswith("#CHROM"):
		data = value.rstrip().split("\t")
		for i in range(len(data)):
			if data[i]=="#CHROM":
				chrom_index = i
			elif data[i]=="POS":
				position_index = i
			elif data[i]=="REF":
				ref_index = i
			elif data[i]=="ALT":
				alt_index = i
			elif data[i]=="transcript":
				transcript_index = i
			elif data[i]=="gene":
				gene_index = i
			elif data[i]=="strand":
				strand_index = i
			elif data[i]=="coordinates(gDNA/cDNA/protein)":
				coordinate_index = i
			elif data[i]=="region":
				region_index = i	
			elif data[i]=="info":
				info_index = i
	if not value.startswith("#"):
		data = value.rstrip().split("\t")
		if len(data)>=info_index+1:
			input = data[chrom_index]+"_"+data[position_index]+"_"+data[ref_index]+"_"+data[alt_index] 
			transcript = data[transcript_index] 
			gene = data[gene_index] 
			strand = data[strand_index] 
			coordinate = data[coordinate_index]
			region = data[region_index]
			info = data[info_index]
			
			if len(transcript.split('('))==2:
				transcript,consequence_type=transcript.split('(')[0][:-1],transcript.split('(')[1][:-1]
			else:
				transcript,consequence_type='.','.'
			dna,cdna,pro=coordinate.split('/')
			variant_type = variantType(region,dna,cdna,pro,info)
			variant = input+'_'+gene+'_'+transcript
			if variant not in longest_transcripts:
				longest_transcripts[variant]=[strand,gene,transcript,consequence_type,region,variant_type,cdna,pro]

## use a scoring system to select the most impactful protein level variant for a given genomic variant
transvar = open(transvar_anno)
header = transvar.readline()
anno_for_one = dict()
keep_one_anno = dict()
for value in transvar:
	if value.startswith("#CHROM"):
		data = value.rstrip().split("\t")
		for i in range(len(data)):
			if data[i]=="#CHROM":
				chrom_index = i
			elif data[i]=="POS":
				position_index = i
			elif data[i]=="REF":
				ref_index = i
			elif data[i]=="ALT":
				alt_index = i
			elif data[i]=="transcript":
				transcript_index = i
			elif data[i]=="gene":
				gene_index = i
			elif data[i]=="strand":
				strand_index = i
			elif data[i]=="coordinates(gDNA/cDNA/protein)":
				coordinate_index = i
			elif data[i]=="region":
				region_index = i	
			elif data[i]=="info":
				info_index = i
	if not value.startswith("#"):
		data = value.rstrip().split("\t")
		if len(data)>=info_index+1:		
			input = data[chrom_index]+"_"+data[position_index]+"_"+data[ref_index]+"_"+data[alt_index] 
			transcript = data[transcript_index] 
			gene = data[gene_index] 
			strand = data[strand_index] 
			coordinate = data[coordinate_index]
			region = data[region_index]
			info = data[info_index]
			
			if input not in anno_for_one:
				if len(anno_for_one)==0:
					anno_for_one[input]=[]
				else:
					for key in anno_for_one:
						## select only the coding region variants
						value=variant_annotation(key,anno_for_one[key],prefer_transcripts,longest_transcripts)
						if coding:
							if "cds" in value[4]:
								keep_one_anno[key]=value
						else:
							keep_one_anno[key]=value
					anno_for_one.clear()
					anno_for_one[input]=[]
			if input in anno_for_one:
				anno_for_one[input].append([transcript,gene,strand,coordinate,region,info])
for key in anno_for_one:
	if key in keep_one_anno:
		print "%s was already annotated, please check!" % key
		sys.exit()
	else:
		## select only the coding region variants
		value=variant_annotation(key,anno_for_one[key],prefer_transcripts,longest_transcripts)
		if coding:
			if "cds" in value[4]:
				keep_one_anno[key]=value
		else:
			keep_one_anno[key]=value
transvar.close()


transvar_merge = passfile+'.transvar.annotation'
transvar_output(sample,passfile,keep_one_anno,transvar_merge)
merge = transvar_merge

## convert vcf file to the input format of annovar
cmd = 'echo `date` begin vcf2annovar format conversion...\n'
annovar_passfile = passfile+'.annovar.input'
cmd += 'perl '+VCF2ANNOVAR+' -format vcf '+passfile+' -includeinfo > '+annovar_passfile+'\n'
cmd += 'echo `date` finish vcf2annovar format conversion.\n'
run(cmd)

## match vcf variant to annovar variant inputs	
vcf2annovar_var_match=dict()	
for value in open(annovar_passfile):
	data = value.rstrip().split("\t")
	annovar_chr,annovar_start,annovar_end,annovar_ref,annovar_alt=data[:5]
	vcf_chr,vcf_pos,vcf_id,vcf_ref,vcf_alt=data[5:10]
	annovar_variant = annovar_chr+':'+annovar_start+'_'+annovar_end+annovar_ref+'/'+annovar_alt
	vcf_variant = vcf_chr+"_"+vcf_pos+"_"+vcf_ref+"_"+vcf_alt
	if vcf_variant not in vcf2annovar_var_match:
		vcf2annovar_var_match[vcf_variant]=annovar_variant
	
db = {
	'multiscore':'dbnsfp30a',
	'dbsnp':'avsnp147',
	'clinvar':'clinvar_20170130',
	'gerp':'gerp++gt2',
	'cosmic':'cosmic70',
	'esp6500':'esp6500siv2_all',
	'genome':'1000g2015aug_all'
	}

alias = {
	'multiscore':'dbnsfp30a',
	'dbsnp':'dbsnp_147',
	'clinvar':'clinvar_20170130',
	'gerp':'gerp++gt2',
	'cosmic':'cosmic70',
	'esp6500':'esp6500_freq',
	'genome':'1000genome_freq'
}

outname = {
	'multiscore':'dbnsfp30a',
	'dbsnp':'avsnp147',
	'clinvar':'clinvar_20170130',
	'gerp':'gerp++gt2',
	'cosmic':'cosmic70',
	'esp6500':'esp6500siv2_all',
	'genome':'ALL.sites.2015_08'
}

## perform annovar annotation by choosing different databases
if (genome):
	merge=annovar_annotation(merge,annovar_passfile,vcf2annovar_var_match,'genome',refversion)
	
if (esp6500):
	merge=annovar_annotation(merge,annovar_passfile,vcf2annovar_var_match,'esp6500',refversion)

if (dbsnp):
	merge=annovar_annotation(merge,annovar_passfile,vcf2annovar_var_match,'dbsnp',refversion)
	
if (clinvar):
	merge=annovar_annotation(merge,annovar_passfile,vcf2annovar_var_match,'clinvar',refversion)
	
if (cosmic):
	merge=annovar_annotation(merge,annovar_passfile,vcf2annovar_var_match,'cosmic',refversion)
	
if (multiscore):
	merge=annovar_annotation(merge,annovar_passfile,vcf2annovar_var_match,'multiscore',refversion)

cmd = 'mv '+merge+' '+annofile+'\n'
run(cmd)

## remove interim files that were produced during transvar and annovar variant annotation
cmd = 'echo `date` begin remove tmp files...\n'
cmd += 'rm '+annovar_passfile+'\n'
cmd += 'rm '+transvar_anno+'\n'
cmd += 'rm '+transvar_anno_longest+'\n'
cmd += 'echo `date` finish remove tmp files done.\n'
run(cmd)
