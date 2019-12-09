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
## This script requires python3 to work properly

import os,subprocess,sys,getopt
import numpy as np
import doubletdetection
import time
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

def run (cmd):
    subprocess.call(cmd, shell = True)
    return
	
working_dir,sampleID,genomes= parse_arguments(sys.argv)
		
## Perform doublet detection for each sample sequencially
for genome in genomes.split(','):
	if os.path.isfile(working_dir+'/count/'+sampleID+'/outs/filtered_feature_bc_matrix/matrix.mtx.gz'):
		matrix_path = working_dir+'/count/'+sampleID+'/outs/filtered_feature_bc_matrix/matrix.mtx.gz'
		# . Remove columns with all 0s
		raw_counts = doubletdetection.load_mtx(matrix_path)
		zero_genes = np.sum(raw_counts, axis=0) == 0
		raw_counts = raw_counts[:, ~zero_genes]
			
		clf = doubletdetection.BoostClassifier(n_iters=50)

		start = time.time()
		doublets = clf.fit(raw_counts).predict(p_thresh=1e-7, voter_thresh=0.8)
		end = time.time()
		#print('Time elapsed: {:.2f} seconds, {:.2f}sec/iteration, for {} iterations'.format(end-start, (end-start) / clf.n_iters, clf.n_iters))
			
		output_dir=working_dir+'/count/'+sampleID+'/outs/analysis/doubletdetection'
		if not os.path.isdir(output_dir):
			os.makedirs(output_dir)
				
		output_name_f0 = output_dir+'/'+sampleID+'_'+genome+'_doubletdetection_doublets_tmp.txt'
		output_doublets = open(output_dir+'/'+sampleID+'_'+genome+'_doubletdetection_doublets.txt','w')
		np.savetxt(output_name_f0, doublets, delimiter='\t')
			
		if os.path.isfile(working_dir+'/count/'+sampleID+'/outs/filtered_feature_bc_matrix/barcodes.tsv.gz'):
			barcode_path = working_dir+'/count/'+sampleID+'/outs/filtered_feature_bc_matrix/barcodes.tsv.gz'
				
			barcodesList=list()
			for line in gzip.open(barcode_path, 'rb'):
				barcodesList.append(line.rstrip())
				
			doubletValueList=list()
			for line in open(output_name_f0):
				doubletValueList.append(line.rstrip())
					
			if len(barcodesList)==len(doubletValueList):
				output_doublets.write("barcode\tdoublet_value\n")
				for i in range(len(barcodesList)):
					output_doublets.write("%s\t%s\n" % (barcodesList[i],doubletValueList[i]))
					
				## Remove the file that contains only the doublet values
				cmd = 'rm '+output_name_f0+'\n'
				run(cmd)
					
			else:
				print ("The number of barcodes in expression matrix does not equal to the number of barcodes in doublet detection!\n")
				sys.exit()
					
		else:
			print ("The barcode file for %s does not exist, please check!\n"	% sampleID)		
			sys.exit()
				
		output_name_f1 = output_dir+'/'+sampleID +'_'+genome+ '_doubletdetection.convergence.pdf' 
		f1 = doubletdetection.plot.convergence(clf, save = output_name_f1,
												   show = False, p_thresh = 1e-7, voter_thresh = 0.8)

		output_name_f2 = output_dir+'/'+sampleID +'_'+genome+ '_doubletdetection.tsne.pdf' 
		f2, tsne_coords, clusters = doubletdetection.plot.tsne(raw_counts, doublets, random_state = 1,
																   save = output_name_f2, show = False)
																   
		output_name_f3 = output_dir+'/'+sampleID +'_'+genome+ '_doubletdetection.threshold.pdf' 
		f3 = doubletdetection.plot.threshold(clf, save = output_name_f3, show = False, p_step = 6)
			
	else:
		print ("The gene-barcode matrix for %s does not exist, please check!\n" % sampleID)
		print (working_dir+'/count/'+sampleID+'/outs/filtered_feature_bc_matrix/matrix.mtx.gz')
		sys.exit()
