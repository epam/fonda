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

## ------------------------------------------------------------------------------------------------------ ##
## This script is to extract the useful information from oncotator annotated variant results.
## Supports oncotator v1.8.0.0
## Oncotator gives a lot of confusing annotations
## Oncotator also lacks of some important information sometimes (such as ref and alt counts)
## Currently, this script does not work on vcf file that does not have info column for particular samples (such as lofreq)
## ------------------------------------------------------------------------------------------------------ ##

import os,subprocess,sys,getopt,re

def usage():
	print 'Usage:\n'
	print '	-s, --sample (required)          The sample variants to include in the output.\n'
	print '	-e, --exclude_sample (required)  The sample variants to exclude from the output.\n'
	print ' -i, --oncotator (required)       The oncatotor annotated results.\n'
	print '	-o, --output (optional)          The output cleanup annotation file.\n'

class ArgumentError(Exception):
	pass

def parse_arguments(argv):
	sample,oncofile,annofile= None,None,None
	exclude_sample='--exclude--'
	
	try:
		opts, args = getopt.getopt(argv[1:], "s:i:e:o:h", ["sample=", "exclude_sample=","oncotator=", "output="])
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
		
		elif opt in ("-e", "--exclude_sample"):
			exclude_sample = arg
			
		elif opt in ("-i", "--oncotator"):
			oncofile = arg
			if not os.path.isfile(oncofile):
				raise ArgumentError("Argument Error: Invalid oncotator annotated variant file passed.")
			if annofile == None:
				annofile = oncofile+".clean.tsv"
				
		elif opt in ("-o", "--output"):
			annofile = arg
	
		else:
			raise ArgumentError("Bad argument: I don't know what %s is" % arg)

	if sample is None or oncofile is None:
		raise ArgumentError("You need to supply a sample name and an oncotator annotated results!") 
		
    # return argument values
	return sample,exclude_sample,oncofile,annofile

## load the inputs			
sample,exclude_sample,oncofile,annofile= parse_arguments(sys.argv)

## use a scoring system to select the most impactful protein level variant for a given genomic variant
oncoVar = open(oncofile)
header = oncoVar.readline()
annoVar = open(annofile,'w')
annoVar.write("Chromosome\tStart_position\tEnd_position\tReference_Allele\tAlternative_Allele\tSample\tHugo_Symbol\tAnnotation_Transcript\t"\
	"Transcript_Strand\tCoverage\tAllele_Frequency\tGenome_Change\tTranscript_Exon\tcDNA_Change\tProtein_Change\tVariant_Classification\t"\
	"Variant_Type\tCOSMIC_overlapping_mutations\tCOSMIC_fusion_genes\tCOSMIC_tissue_types_affected\tCCLE_ONCOMAP_overlapping_mutations\t"\
	"dbSNP_RS\t1000gp3_AF\tClinVar_TYPE\tExAC_AF\tExAC_ESP_AF_GLOBAL\tdbNSFP_1000Gp1_AF\tdbNSFP_GERP++_RS\tdbNSFP_FATHMM_pred\t"\
	"dbNSFP_MutationAssessor_pred\tdbNSFP_MutationTaster_pred\tdbNSFP_Polyphen2_HVAR_pred\tdbNSFP_SIFT_pred\n")
for value in oncoVar:
	if value.startswith("#"):
		pass
	elif value.startswith("Hugo_Symbol"):
		data = value.rstrip().split("\t")
		for i in range(len(data)):
			if data[i]=="Hugo_Symbol":
				gene_index = i
			elif data[i]=="Chromosome":
				chrom_index = i
			elif data[i]=="Start_position":
				start_index = i
			elif data[i]=="End_position":
				end_index = i
			elif data[i]=="Reference_Allele":
				ref_index = i
			elif data[i]=="Tumor_Seq_Allele2":
				alt_index = i
			elif data[i]=="Annotation_Transcript":
				transcript_index = i
			elif data[i]=="Transcript_Strand":
				strand_index = i
			elif data[i]=="t_alt_count":
				altCount_index = i
			elif data[i]=="t_ref_count":
				refCount_index = i
			elif data[i]=="Genome_Change":
				genomic_index = i	
			elif data[i]=="Transcript_Exon":
				tranExon_index = i
			elif data[i]=="cDNA_Change":
				cdna_index = i
			elif data[i]=="Protein_Change":
				protein_index = i
			elif data[i]=="Tumor_Sample_Barcode":
				tSample_index = i
			elif data[i]=="Matched_Norm_Sample_Barcode":
				nSample_index = i
			elif data[i]=="Variant_Classification":
				varClass_index = i
			elif data[i]=="Variant_Type":
				varType_index = i
			elif data[i]=="COSMIC_overlapping_mutations":
				cosmicMut_index = i
			elif data[i]=="COSMIC_fusion_genes":
				cosmicFus_index = i
			elif data[i]=="COSMIC_tissue_types_affected":
				cosmicTissue_index = i
			elif data[i]=="CCLE_ONCOMAP_overlapping_mutations":
				ccleMut_index = i
			elif data[i]=="dbSNP_RS":
				dbsnp_index = i
			elif data[i]=="1000gp3_AF":
				genome3AF_index = i
			elif data[i]=="ClinVar_TYPE":
				clinvar_index = i
			elif data[i]=="ExAC_AF":
				exac_index = i
			elif data[i]=="ExAC_ESP_AF_GLOBAL":
				esp_index = i
			elif data[i]=="dbNSFP_1000Gp1_AF":
				genome1AF_index = i
			elif data[i]=="dbNSFP_GERP++_RS":
				gerp_index = i
			elif data[i]=="dbNSFP_FATHMM_pred":
				fathmm_index = i
			elif data[i]=="dbNSFP_MutationAssessor_pred":
				mutAsses_index = i
			elif data[i]=="dbNSFP_MutationTaster_pred":
				mutTaste_index = i
			elif data[i]=="dbNSFP_Polyphen2_HVAR_pred":
				polyphen_index = i
			elif data[i]=="dbNSFP_SIFT_pred":
				sift_index = i
				
	else:
		data = value.rstrip().split("\t")
		nSample = data[nSample_index]
		tSample = data[tSample_index]
		if exclude_sample not in [nSample,tSample]:
			chrom = data[chrom_index]
			start = data[start_index]
			end = data[end_index]
			ref = data[ref_index]
			alt = data[alt_index]
			gene = data[gene_index] 
			transcript = data[transcript_index] 
			strand = data[strand_index]
			coverage = int(data[refCount_index])+int(data[altCount_index])
			genomics = data[genomic_index]
			transExon = data[tranExon_index]
			cdna = data[cdna_index]
			protein = data[protein_index]
			varClass = data[varClass_index]
			varType = data[varType_index]
			cosmicMut = data[cosmicMut_index]
			cosmicFus = data[cosmicFus_index]
			cosmicTissue = data[cosmicTissue_index]
			ccleMut = data[ccleMut_index]
			dbsnp = data[dbsnp_index]
			genome3AF = data[genome3AF_index]
			clinvar = data[clinvar_index]
			exac = data[exac_index]
			esp = data[esp_index]
			genome1AF = data[genome1AF_index]
			gerp = data[gerp_index]
			fathmm = data[fathmm_index]
			mutAsses = data[mutAsses_index]
			mutTaste = data[mutTaste_index]
			polyphen = data[polyphen_index]
			sift = data[sift_index]
			
			if coverage>0:
				af = float(data[altCount_index])/coverage
				annoVar.write("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%d\t%.3f\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n" \
										% (chrom,start,end,ref,alt,sample,gene,transcript,strand,coverage,af,genomics,transExon,cdna,protein,\
										varClass,varType,cosmicMut,cosmicFus,cosmicTissue,ccleMut,dbsnp,genome3AF,clinvar,exac,esp,\
										genome1AF,gerp,fathmm,mutAsses,mutTaste,polyphen,sift))
			else:
				af = '-'
				annoVar.write("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%d\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n" \
										% (chrom,start,end,ref,alt,sample,gene,transcript,strand,coverage,af,genomics,transExon,cdna,protein,\
										varClass,varType,cosmicMut,cosmicFus,cosmicTissue,ccleMut,dbsnp,genome3AF,clinvar,exac,esp,\
										genome1AF,gerp,fathmm,mutAsses,mutTaste,polyphen,sift))
										