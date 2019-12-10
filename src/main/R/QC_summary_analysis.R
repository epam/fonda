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
# script to merge gene expression results of the analyzed cohort and perform enrichment analysis
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
  'workflow', 'w'
), ncol=2, byrow=TRUE);

opt = parse(spec);


#----------------------
# print help menu
#
if (!is.null(opt$help) || is.null(opt$sample_infor) || is.null(opt$work_dir) || is.null(opt$workflow)) {
  opt = parse(spec,
              file = 'QC_summary_analysis.R',
              usage = T);
  cat(opt);
  q(stat=0);
}

wd <- opt$work_dir ## the working directory where user can find the results for each sample
infor.file = opt$sample_infor  ## the sample information table which was analyzed for the gene expression
workflow = opt$workflow  ## the software which was used to obtain the gene expression results, currently support rsem, cufflinks, and stringtie

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

## Merge all the QC metric for individual samples into a data matrix

if (workflow=="DnaCaptureVar_Fastq"|workflow=="DnaWgsVar_Fastq"|workflow=="DnaAmpliconVar_Fastq"|
	workflow=="RnaCaptureVar_Fastq"|workflow=="RnaExpression_Fastq"|workflow=="scRnaExpression_Fastq"){
	for (i in 1:length(sampleList)){
	  sample <- sampleList[i]
	  if (file.exists(sample)){
		qc.file<-paste0(sample,'/qc/',sample,'.alignment.merged.QC.metric.txt')
		if (file.exists(qc.file)){
		  
		  qc.dat <-read.table(qc.file,header=F,sep="\t",check.names=F,row.names=1,stringsAsFactors = F,comment.char = "#")
		  if (i==1){
		    qc.mat<-matrix(nrow=nrow(qc.dat),ncol=0)
			rownames(qc.mat)<-rownames(qc.dat)
			qc.mat <- cbind(qc.mat,qc.dat)
		  }else{
		    qc.mat <- cbind(qc.mat,qc.dat)
		  }
		}else{
		  print (paste0("The QC metric of for ",sample," does not exist!"))
		}
	  }else{
		print (paste0("The QC metric for ",sample," does not exist!"))
	  }
	}
	qc.mat<-t(qc.mat)
	
	mat.fn<- "Result_summary/alignment.merged.QC.metrics.txt"
	write.table(qc.mat,mat.fn,row.names=F,quote=F,sep="\t")
	
}else if(workflow=="scRnaExpression_CellRanger_Fastq"){
	for (i in 1:length(sampleList)){
	  sample <- sampleList[i]
	  if (file.exists(paste0('count/',sample))){
		qc.file<-paste0('count/',sample,'/outs/metrics_summary.csv')
		if (file.exists(qc.file)){
		  
		  qc.dat <-read.csv(qc.file,header=T,sep=",",check.names=F,stringsAsFactors = F,comment.char = "#")
		  if (i==1){
			qc.mat<-matrix(nrow=0,ncol=ncol(qc.dat)+1)
			colnames(qc.mat)<-c('SAMPLE',colnames(qc.dat))
			qc.mat <- rbind(qc.mat,c(sample,qc.dat))
		  }else{
		    qc.mat <- rbind(qc.mat,c(sample,qc.dat))
		  }
		}else{
		  print (paste0("The QC metric of for ",sample," does not exist!"))
		}
	  }else{
		print (paste0("The QC metric for ",sample," does not exist!"))
	  }
	}

	mat.fn<- "Result_summary/merged.alignment.QC.metrics.txt"
	write.table(qc.mat,mat.fn,row.names=F,quote=F,sep="\t")
}else if(workflow=="scImmuneProfile_CellRanger_Fastq"){
	for (i in 1:length(sampleList)){
	  sample <- sampleList[i]
	  if (file.exists(paste0('vdj/',sample))){
		qc.file<-paste0('vdj/',sample,'/outs/metrics_summary.csv')
		if (file.exists(qc.file)){
		  
		  qc.dat <-read.csv(qc.file,header=T,sep=",",check.names=F,stringsAsFactors = F,comment.char = "#")
		  if (i==1){
			qc.mat<-matrix(nrow=0,ncol=ncol(qc.dat)+1)
			colnames(qc.mat)<-c('SAMPLE',colnames(qc.dat))
			qc.mat <- rbind(qc.mat,c(sample,qc.dat))
		  }else{
		    qc.mat <- rbind(qc.mat,c(sample,qc.dat))
		  }
		}else{
		  print (paste0("The QC metric of for ",sample," does not exist!"))
		}
	  }else{
		print (paste0("The QC metric for ",sample," does not exist!"))
	  }
	}

	mat.fn<- "Result_summary/merged.alignment.QC.metrics.txt"
	write.table(qc.mat,mat.fn,row.names=F,quote=F,sep="\t")
}
