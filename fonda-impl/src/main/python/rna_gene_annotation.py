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

## This script is used to convert the gene_id to gene_symbol during gene expression analysis
## The output format is similar to the input format, the only difference is to replace the gene_id with gene_symbol
## Based on the selected annotation file, the length of the input and output file could be different

import os,subprocess,sys,getopt,re

def usage():
	print 'Usage:\n'
	print '	-i, --input (required)           The full path of the input gene expression file.\n'
	print '	-o, --output (required)          The full path of the output annotated gene expression file.\n'
	print '	-t, --tool (required)            The name of the expression estimation tool such as rsem,etc.\n'
	print '	-a, --annotation (required)      The full path of the gene annotation file.\n'
	
class ArgumentError(Exception):
	pass

def parse_arguments(argv):
	srcfile,outfile,tool,annosaf = None,None,None,None

	try:
		opts, args = getopt.getopt(argv[1:], "i:o:t:a:", ["input=", "output=", "tool=","annotation="])
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
		
		elif opt in ("-a", "--annotation"):
			annosaf = arg
			if not os.path.isfile(annosaf):
				raise ArgumentError("Argument Error: Invalid annotation file passed.")
				
		elif opt in ("-o", "--output"):
			outfile = arg
				
		elif opt in ("-t", "--tool"):
			tool = arg
			
		else:
			raise ArgumentError("Bad argument: I don't know what %s is" % arg)

	if srcfile is None or outfile is None or tool is None or annosaf is None:
		raise ArgumentError("You need to supply an input, an output, an annotation file and an expression estimation tool both!") 
		
    # return argument values
	return srcfile,outfile,tool,annosaf

srcfile,outfile,tool,annosaf= parse_arguments(sys.argv)

## Match the gene_id and gene_symbol pairs
geneID2Symbol = dict()
gene_anno = open(annosaf).readlines()
for data in gene_anno:
	data = data.rstrip().split("\t")
	gene_id,gene_symbol=data[0].split("|")
	if gene_id not in geneID2Symbol:
		geneID2Symbol[gene_id]=gene_symbol

if tool=="rsem":
	gene_res=open(srcfile).readlines()
	gene_anno_res=open(outfile,"w")
	gene_anno_res.write("gene_symbol\tgene_id\ttranscript_id(s)\tlength\teffective_length\texpected_count\tTPM\tFPKM\n")
	for data in gene_res[1:]:
		data = data.rstrip().split("\t")
		gene_id=data[0]
		if gene_id in geneID2Symbol:
			gene_symbol=geneID2Symbol[gene_id]
			gene_anno_res.write("%s\t%s\n" % (gene_symbol,'\t'.join(data)))
			
