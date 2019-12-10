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
# script to merge gene expression results of the analyzed cohort
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
library(plyr)

#---------------
# option spec
#
spec = matrix(c(
  'help', 'h',
  'work_dir', 'd',
  'sample_infor', 'i',
  'genome_build', 'g'
), ncol=2, byrow=TRUE);

opt = parse(spec);

## Calculate the TPM value based on gene count value
## count_mat is a readCount data matrix with samples (columns) and genes (rows)
## return the TPM matrix (colnames same with count_mat, while rownames are the intersection of genes in count_mat and genes in gene length matrix
count2TPM <- function(count_mat,genelen_mat){
  overlap_genes<-intersect(rownames(count_mat),genelen_mat$gene_name)
  count_mat<-count_mat[match(overlap_genes,rownames(count_mat)),]
  genelen_mat<-genelen_mat[match(overlap_genes,genelen_mat$gene_name),]
  
  for (j in 1:ncol(count_mat)){
    tmp.sum<-sum(count_mat[,j]/genelen_mat$gene_size)
    count_mat[,j]<-(count_mat[,j]/genelen_mat$gene_size)*10^6/tmp.sum
  }
  return (count_mat)
}

#----------------------
# print help menu
#
if (!is.null(opt$help) || is.null(opt$sample_infor) || is.null(opt$work_dir)) {
  opt = parse(spec,
              file = 'scRna_CellRanger_expression_data_analysis.R',
              usage = T);
  cat(opt);
  q(stat=0);
}

wd = opt$work_dir ## the working directory where user can find the results for each sample
infor.file = opt$sample_infor  ## the sample information table which was analyzed for the gene expression
genome_build = opt$genome_build

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


## Merge all the expression results for individual samples into a data matrix
count.mat<-matrix(nrow=0,ncol=0)
count.mat.colname<-vector()
for (i in 1:length(sampleList)){
  sample <- sampleList[i]
  if (file.exists("count")){
    if (file.exists(paste("count",sample,"outs/filtered_feature_bc_matrix",sep="/"))){

	  count.file<-paste0("count/",sample,"_",genome_build,"_umi_count_matrix.tsv")
	  count.dat <-read.table(count.file,header=T,sep="\t",check.names=F,stringsAsFactors = F,comment.char = "%")
		
	  gene.file<-paste("count",sample,"outs/filtered_feature_bc_matrix","genes.tsv.gz",sep="/")
	  gene.anno <-read.table(gene.file,header=F,sep="\t",check.names=F,stringsAsFactors = F,comment.char = "%")
		  
	  colnames(count.dat)[1]<-'gene_symbol'
	  id<-match(count.dat$gene_symbol,gene.anno[,1])
	  count.dat$gene_symbol<-gene.anno[id,2]
	  count.dat<-count.dat[order(count.dat$gene_symbol),]
	  
	  count.dat<-aggregate(count.dat[-1],count.dat["gene_symbol"],sum)
	  #count.dat<-aggregate(. ~ gene_symbol, count.dat, sum)
	  #count.dat<-ddply(count.dat,"gene_symbol",numcolwise(sum))
	  
	  rownames(count.dat)<-count.dat[,1]
	  count.dat<-count.dat[,-1]
	  colnames(count.dat)<-paste(sample,colnames(count.dat),sep="_")
	  cat('Finish remove duplicated gene symbols...\n')
		  
	  if (i==1){
		dim(count.mat)[1]<-nrow(count.dat)
		rownames(count.mat)<-rownames(count.dat)
		count.mat<-cbind(count.mat,count.dat)
	  }else{
		count.mat<-cbind(count.mat,count.dat[match(rownames(count.mat),rownames(count.dat)),])
	  }
    }else{
      print (paste0("The gene expression results of for ",sample," does not exist!"))
    }
  }else{
    print (paste0("The gene read results for does not exist!"))
  }
}

## Record the merge data matrix
count.fn<-paste0("Result_summary/merged_",genome_build,"_umi.gene.count.tsv")
write.table(cbind(gene_symbol=rownames(count.mat),count.mat),count.fn,sep="\t",row.names=F,quote=F)

## Convert count matrix to TPM matrix
tpm.mat<-count.mat/10^6
tpm.exp.fn<-paste0("Result_summary/merged_",genome_build,"_tpm.gene.expression.tsv")
write.table(cbind(gene_symbol=rownames(tpm.mat),tpm.mat),tpm.exp.fn,sep="\t",row.names=F,quote=F)


