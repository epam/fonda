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

## This script is used to convert the variant formats come from different variant callers to a uniform format that is required by annovar annotation
## This input format for annovar
## First five column: chr, start, end, ref_allele, alt_allele
## snv: start=end, start is the 1-based position of the ref_allele, end is the 1-based position of the alt_allele
## mnv: start!=end, start is the 1-based start position of the ref_allele, end is the 1-based end position of the ref_allele
## insertion: start=end, start is the 1-based start position of the alt_allele, end is the 1-based start position of the alt_allele
## deletion: start!=end, start is the 1-based start position of the ref_allele, end is the 1-based end position of the ref_allele

import os,subprocess,sys,getopt,re

def usage():
	print 'Usage:\n'
	print '	-i, --input (required)           The full path of the input mutation file.\n'
	print '	-o, --output (required)          The full path of the output vcf format file (to be used for annotation).\n'
	print '	-t, --tool (required)            The name of the variant caller such as varscan2, vardict, mutect, scapel,etc.\n'
	print ' -m, --mode (required)            The mode of the variant detection (either unpaired or paired).\n'
	
class ArgumentError(Exception):
	pass

def parse_arguments(argv):
	srcfile,outfile,tool,mode = None,None,None,None

	try:
		opts, args = getopt.getopt(argv[1:], "i:o:t:m:", ["input=", "output=", "tool=", "mode="])
	except getopt.GetoptError as err:
		# print help information and exit:
		print str(err)  # will print something like "option -a not recognized"
		usage()

	for opt, arg in opts:
		if opt == '-h':
			usage()

		elif opt in ("-i", "--input"):
			srcfile = arg
			if not os.path.isfile(srcfile):
				raise ArgumentError("Argument Error: Invalid input passed.")
		
		elif opt in ("-o", "--output"):
			outfile = arg
				
		elif opt in ("-t", "--tool"):
			tool = arg
		
		elif opt in ("-m", "--mode"):
			mode = arg
			
		else:
			raise ArgumentError("Bad argument: I don't know what %s is" % arg)

	if srcfile is None or outfile is None or tool is None or mode is None:
		raise ArgumentError("You need to supply an input, a output, a tool and a variant detection mode both!") 
		
    # return argument values
	return srcfile,outfile,tool,mode

srcfile,outfile,tool,mode= parse_arguments(sys.argv)

if tool == 'varscan2':
	output = open(outfile,'w')
	output.write("Chr\tStart\tEnd\tREF\tALT\tstrand\tAlleleFreq\tCoverage\n")
	if mode == 'paired':
		for data in open(srcfile):
			data = data.rstrip().split('\t')
			chr,pos,ref,alt,ref_read,alt_read,percentage = data[0],data[1],data[2],data[3],data[8],data[9],data[10]
			coverage = str(int(ref_read)+int(alt_read))
			if alt.startswith("+"):
				start = str(int(pos)+1)
				end = str(int(pos)+1)
				ref="-"
				alt=alt[1:]
			elif alt.startswith('-'):
				start = str(int(pos)+1)
				end = str(int(pos)+len(alt)-1)
				ref=alt[1:]
				alt="-"
			else:
				start = pos
				end = str(int(pos)+len(alt)-1)
			output.write("%s\t%s\t%s\t%s\t%s\t+\t%s\t%s\n" % (chr,start,end,ref,alt,percentage,coverage))
	elif mode == "unpaired":
		for data in open(srcfile):
			data = data.rstrip().split('\t')
			chr,pos,ref,alt,ref_read,alt_read,percentage = data[0],data[1],data[2],data[3],data[8],data[9],data[10]
			coverage = str(int(ref_read)+int(alt_read))
			if alt.startswith("+"):
				start = str(int(pos)+1)
				end = str(int(pos)+1)
				ref="-"
				alt=alt[1:]
			elif alt.startswith('-'):
				start = str(int(pos)+1)
				end = str(int(pos)+len(alt)-1)
				ref=alt[1:]
				alt="-"
			else:
				start = pos
				end = pos
			outfile.write("%s\t%s\t%s\t%s\t%s\t+\t%s\t%s\n" % (chr,start,end,ref,alt,percentage,coverage))
		
if tool == 'vardict':
	output = open(outfile,'w')
	output.write("Chr\tStart\tEnd\tREF\tALT\tstrand\tAlleleFreq\tCoverage\n")
	if mode == 'unpaired':
		for data in open(srcfile):
			if not data.startswith('#'):
				data = data.rstrip().split('\t')
				chr,start,end,ref,alt,read_depth,alt_read,f_alt_read,r_alt_read,percentage,var_type \
					= data[2],data[3],data[4],data[5],data[6],data[7],data[8],data[11],data[12],data[14],data[-1]
				coverage = read_depth
				if var_type == 'Insertion':
					start = str(int(start)+1)
					end = str(int(start)+1)
					ref="-"
					alt=alt[1:]
				elif var_type == 'Deletion':
					start = str(int(start)+1)
					end = end
					ref=ref[1:]
					alt="-"
				else:
					start = start
					end = end
				## filters for keeping vardict snv,mnv and indel variants
				## number of reads cover the alt allele in tumor >=3
				## allele frequency of the alt allele in tumor >= 0.05
				## no strand bias for variant reads (<95% variant reads on one direction)
				if (int(alt_read)>=3):
					if (float(f_alt_read)/float(alt_read)>0.05 and float(f_alt_read)/float(alt_read)<0.95 and float(percentage)>=0.05):
						output.write("%s\t%s\t%s\t%s\t%s\t+\t%s\t%s\n" % (chr,start,end,ref,alt,percentage,coverage))	
	elif mode == 'paired':
		for data in open(srcfile):
			if not data.startswith('#'):
				data = data.rstrip().split('\t')
				chr,start,end,ref,alt,read_depth,alt_read,f_alt_read,r_alt_read,percentage,var_type \
					= data[2],data[3],data[4],data[5],data[6],data[7],data[8],data[11],data[12],data[14],data[-1]
				coverage = read_depth
				if var_type == 'Insertion':
					start = str(int(start)+1)
					end = str(int(start)+1)
					ref="-"
					alt=alt[1:]
				elif var_type == 'Deletion':
					start = str(int(start)+1)
					end = end
					ref=ref[1:]
					alt="-"
				else:
					start = start
					end = end
				## filters for keeping vardict snv,mnv and indel variants
				## number of reads cover the alt allele in tumor >=3
				## allele frequency of the alt allele in tumor >= 0.05
				## no strand bias for variant reads (<95% variant reads on one direction)
				if (int(alt_read)>=3):
					if (float(f_alt_read)/float(alt_read)>0.05 and float(f_alt_read)/float(alt_read)<0.95 and float(percentage)>=0.05):
						output.write("%s\t%s\t%s\t%s\t%s\t+\t%s\t%s\n" % (chr,start,end,ref,alt,percentage,coverage))
					
if tool == 'scalpel':
	output = open(outfile,'w')
	output.write("Chr\tStart\tEnd\tREF\tALT\tstrand\tAlleleFreq\tCoverage\n")
	if mode == 'paired':
		for data in open(srcfile):
			if not data.startswith('#'):
				data = data.rstrip().split('\t')
				chr,start,end,ref,alt = data[:5]
				alt_read = data[9]
				ref_read = data[11]
				af = data[12]
				fisher_score=float(data[14])
				inh = data[15]
				## filters for keeping scapel indel variants:
				## fisher_score > 10
				## inh = no
				## number of reads cover the alt allele in tumor >=5
				## allele frequency of the alt allele in tumor >= 0.05
				if fisher_score>10 and inh=="no" and int(alt_read)>=5 and float(af)>=0.05:
					coverage = str(int(alt_read)+int(ref_read))
					output.write("%s\t%s\t%s\t%s\t%s\t+\t%s\t%s\n" % (chr,start,end,ref,alt,af,coverage))
	elif mode == 'upaired':
		for data in open(srcfile):
			if not data.startswith('#'):
				data = data.rstrip().split('\t')
				chr,start,end,ref,alt = data[:5]
				alt_read = data[11]
				af = data[12]
				coverage = int(float(alt_read)/float(af))
				## filters for keeping scapel indel variants:
				## number of reads cover the alt allele in tumor >=5
				## allele frequency of the alt allele in tumor >= 0.05
				if int(alt_read)>=5 and float(af)>=0.05:
					output.write("%s\t%s\t%s\t%s\t%s\t+\t%s\t%s\n" % (chr,start,end,ref,alt,af,coverage))

if tool == "lofreq":
	output = open(outfile,'w')
	output.write("Chr\tStart\tEnd\tREF\tALT\tstrand\tAlleleFreq\tCoverage\n")
	if mode == "paired":
	else if model == "unpaired":
		for data in open(srcfile):
			if not data.startswith('#'):
	
if tool == 'mutect':
	output = open(outfile,'w')
	output.write("Chr\tStart\tEnd\tREF\tALT\tstrand\tAlleleFreq\tCoverage\n")
	if mode == 'paired':
		for data in open(srcfile):
			if not data.startswith('#') and not data.startswith('contig'):
				data = data.rstrip().split('\t')
				CHROM,POS,CONTEXT,REF,ALT=data[:5]
				T_VAF = data[21]
				T_COVERAGE = int(data[25])+int(data[26])
				T_ALT_READ = int(data[26])
				N_VAF = data[35]
				N_ALT_READ = int(data[38])
				FILTER=data[-1]
				## filters for keeping mutect snv variants:
				## the variant detection filter = KEEP
				## number of reads cover the alt allele in tumor >=3
				## allele frequency of the alt allele in tumor >= 0.05
				## number of reads cover the alt allele in normal <=2
				## allele frequency of the alt allele in normal <= 0.02
				if FILTER == 'KEEP' and float(T_VAF)>=0.05 and float(N_VAF)<=0.02 and T_ALT_READ>=3 and N_ALT_READ<=2:
					output.write("%s\t%s\t%s\t%s\t%s\t+\t%s\t%d\n" % (CHROM,POS,POS,REF,ALT,T_VAF,T_COVERAGE))
