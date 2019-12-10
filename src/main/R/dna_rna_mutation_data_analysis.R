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

#
# script to merge DNA/RNA mutation annotation results of the analyzed cohort
#

get_filename = function() {
    initial.options <- commandArgs(trailingOnly = FALSE)
    file.arg.name <- "--file="
    script.name <- sub(file.arg.name, "", initial.options[grep(file.arg.name, initial.options)])
    script.basename <- dirname(script.name)
    parse.file.name <- file.path(script.basename, "parse_options.R")
    return(parse.file.name)
}

parse.file.name <- get_filename()
source(parse.file.name)

#---------------
# option spec
#
spec = matrix(c(
  'help', 'h',
  'work_dir', 'd',
  'sample_infor', 'i',
  'mut_tool', 't'
), ncol=2, byrow=TRUE);

opt = parse(spec);

mutDetectors <- c("varscan","varscan2","vardict","lofreq","gatkHaplotypeCaller",
					"mutect1","mutect2","freebayes","strelka","strelka2","scalpel","pindel")

#----------------------
# print help menu
#
if (!is.null(opt$help) || is.null(opt$sample_infor) || is.null(opt$work_dir) || is.null(opt$mut_tool)) {
  opt = parse(spec,
              file = 'dna_rna_mutation_data_analysis.R',
              usage = T);
  cat(opt);
  q(stat=0);
}

wd <- opt$work_dir ## the working directory where user can find the results for each sample
infor.file = opt$sample_infor  ## the sample information table which was analyzed for the DNA mutation
tools = intersect(unlist(strsplit(opt$mut_tool,"\\+")),mutDetectors)  ## the software which was used to obtain the DNA mutation results

## Check the availability of the working directory
if (file.exists(wd)){
  setwd(wd)
}else{
  print (paste0("The working directory: ",wd," does not exist!"))
}

dir.create("Result_summary",showWarnings = F)

## Collect the sample list from the information table.
## Please make sure the column name for the sample list is 'shortName'
sample_infor <- read.table(infor.file,header=T,sep="\t",check.names=F,stringsAsFactors = F)
sampleList<-vector()
for (i in 1:nrow(sample_infor)){
  if (!sample_infor$shortName[i] %in% sampleList){
    sampleList<-append(sampleList,sample_infor$shortName[i])
  }
}

## Merge all the mutation results for individual samples into a data matrix
mut.mat<-matrix(nrow=0,ncol=0)
mut.mat.colname<-vector()
for (i in 1:length(sampleList)){
  sample <- sampleList[i]
  for (j in 1:length(tools)){
	tool <- tools[j]
	if (file.exists(paste(sample,tool,sep="/"))){
		files<-list.files(paste(sample,tool,sep="/"))
		id<-which(grepl(".variants.pass.annotation.tsv",files))
		mut.file<-paste(sample,tool,files[id],sep="/")
		mut.dat <-read.table(mut.file,header=T,sep="\t",check.names=F,stringsAsFactors = F,comment.char = "",
					colClasses = c("character"))
				
		if (i==1 && j==1){
			dim(mut.mat)[2]<-ncol(mut.dat)
			colnames(mut.mat)<-colnames(mut.dat)
			mut.mat<-rbind(mut.mat,mut.dat)
		}else{
			if (ncol(mut.mat)!=ncol(mut.dat)){
				print (paste0("Inconsistent number of columns in DNA mutation results, please check!"))
			}else{
				mut.mat<-rbind(mut.mat,mut.dat)
			}
		}
		
	}else{
		  print (paste0("The DNA mutation results of ",tool," for ",sample," does not exist!"))
	}
  }
}


## Record the merge data matrix
mut.fn<-paste0("Result_summary/merged.variants.pass.annotation.tsv")
write.table(mut.mat,mut.fn,sep="\t",row.names=F,quote=F)

