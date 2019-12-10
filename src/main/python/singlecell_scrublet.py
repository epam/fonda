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

## This script is used to detect the doublets during single cell data analysis
## This script requires python2.7 to work properly

import os,subprocess,sys,getopt
import numpy as np
from scipy.io import mmread
import scrublet as scr
import matplotlib.pyplot as plt
import gzip

def usage():
	print ('Usage:\n')
	print ('	--work_dir (required)        The full path of the working directory.\n')
	print ('	--sample_id (required)       The sample id.\n')
	print ('	--genome_build (required)    The genome build of the target dataset.\n')

class ArgumentError(Exception):
	pass

def parse_arguments(argv):

	wd,sampleID,genome_builds = None,None,None

	try:
		opts, args = getopt.getopt(argv[1:], "h", ["work_dir=", "sample_id=","genome_build="])
	except getopt.GetoptError as err:
		# print help information and exit:
		print (str(err))  # will print something like "option -a not recognized"
		usage()

	for opt, arg in opts:
		if opt == '-h':
			usage()

		elif opt in ("--work_dir"):
			wd = arg
			if not os.path.isdir(wd):
				raise ArgumentError("Argument Error: Invalid working directory passed.")
		
		elif opt in ("--sample_id"):
			sampleID = arg
		
		elif opt in ("--genome_build"):
			genome_builds = arg
			
		else:
			raise ArgumentError("Bad argument: I don't know what %s is" % arg)

	if wd is None or sampleID is None or genome_builds is None:
		raise ArgumentError("You need to supply a working directory, a sample metadata file and a genome build!") 
		
    # return argument values
	return wd,sampleID,genome_builds

	
working_dir,sampleID,genomes= parse_arguments(sys.argv)
		
## Perform doublet detection for each sample sequencially
for genome in genomes.split(','):
	if os.path.isfile(working_dir+'/count/'+sampleID+'/outs/filtered_feature_bc_matrix/matrix.mtx.gz'):
		matrix_path = working_dir+'/count/'+sampleID+'/outs/filtered_feature_bc_matrix/matrix.mtx.gz'
		
		raw_counts = mmread(matrix_path).T.tocsc()

		scrub = scr.Scrublet(raw_counts, expected_doublet_rate=0.06)
		doublet_scores, predicted_doublets = scrub.scrub_doublets()
			
		output_dir=working_dir+'/count/'+sampleID+'/outs/analysis/doubletdetection'
		if not os.path.isdir(output_dir):
			os.makedirs(output_dir)

		output_doublets = open(output_dir+'/'+sampleID+'_'+genome+'_scrublet_doublets.txt','w')
			
		if os.path.isfile(working_dir+'/count/'+sampleID+'/outs/filtered_feature_bc_matrix/barcodes.tsv.gz'):
			barcode_path = working_dir+'/count/'+sampleID+'/outs/filtered_feature_bc_matrix/barcodes.tsv.gz'
				
			barcodesList=list()
			for line in gzip.open(barcode_path, 'rb'):
				barcodesList.append(line.rstrip())
					
			if len(barcodesList)==len(doublet_scores):
				output_doublets.write("barcode\tdoublet_score\tpredicted_doublets\n")
				for i in range(len(barcodesList)):
					output_doublets.write("%s\t%.3f\t%s\n" % (barcodesList[i],doublet_scores[i],predicted_doublets[i]))
					
			else:
				print ("The number of barcodes in expression matrix does not equal to the number of barcodes in doublet detection!\n")
				sys.exit()
					
		else:
			print ("The barcode file for %s does not exist, please check!\n"	% sampleID)		
			sys.exit()
			
		output_name_f2 = output_dir+'/'+sampleID +'_'+genome+ '_scrublet.doubletScore.pdf'
		f2=scrub.plot_histogram()
		plt.savefig(output_name_f2)
																   
		output_name_f3 = output_dir+'/'+sampleID +'_'+genome+ '_scrublet.tsne.pdf' 
		scrub.set_embedding('tSNE', scr.get_tsne(scrub.manifold_obs_, angle=0.9))
		f3=scrub.plot_embedding('tSNE', order_points=True)
		plt.savefig(output_name_f3)
			
	else:
		print ("The gene-barcode matrix for %s does not exist, please check!\n" % sampleID)
		print (working_dir+'/count/'+sampleID+'/outs/filtered_feature_bc_matrix/matrix.mtx.gz')
		sys.exit()
