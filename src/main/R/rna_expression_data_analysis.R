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

#---------------
# option spec
#
spec = matrix(c(
  'help', 'h',
  'work_dir', 'd',
  'sample_infor', 'i',
  'exp_tool', 't'
), ncol=2, byrow=TRUE);

opt = parse(spec);

## Calculate the TPM value based on gene RPKM/FPKM value
## exp_mat is a readCount data matrix with samples (columns) and genes (rows)
## return the TPM matrix with the identical dimension as exp_mat
fpkm2TPM <- function(exp_mat){
  for (j in 1:ncol(exp_mat)){
    tmp.sum<-sum(exp_mat[,j])
    exp_mat[,j]<-exp_mat[,j]*10^6/tmp.sum
  }
  return (exp_mat)
}

#----------------------
# print help menu
#
if (!is.null(opt$help) || is.null(opt$sample_infor) || is.null(opt$work_dir) || is.null(opt$exp_tool)) {
  opt = parse(spec,
              file = 'rna_expression_data_analysis.R',
              usage = T);
  cat(opt);
  q(stat=0);
}

wd <- opt$work_dir ## the working directory where user can find the results for each sample
infor.file = opt$sample_infor  ## the sample information table which was analyzed for the gene expression
tool = opt$exp_tool  ## the software which was used to obtain the gene expression results, currently support rsem, cufflinks, and stringtie

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
exp.mat<-matrix(nrow=0,ncol=0)
exp.mat.colname<-vector()
for (i in 1:length(sampleList)){
  sample <- sampleList[i]
  if (file.exists(sample)){
    if (file.exists(paste(sample,tool,sep="/"))){
      files<-list.files(paste(sample,tool,sep="/"))
      id<-which(grepl(".gene.expression.results",files))
      exp.file<-paste(sample,tool,files[id],sep="/")
      exp.dat <-read.table(exp.file,header=T,sep="\t",check.names=F,stringsAsFactors = F,comment.char = "#")
      if (tool=="cufflinks"){
        exp.dat<-exp.dat[order(exp.dat$gene_id),]
        if (i==1){
          dim(exp.mat)[1]<-nrow(exp.dat)
          rownames(exp.mat)<-paste(exp.dat$tracking_id, exp.dat$gene_id, exp.dat$gene_short_name, exp.dat$locus, sep = '|')
          exp.mat.gname<-paste(exp.dat$tracking_id, exp.dat$gene_id, exp.dat$gene_short_name, exp.dat$locus, sep = '|')
          exp.mat<-cbind(exp.mat,exp.dat$FPKM)
          exp.mat.colname<-append(exp.mat.colname,sample)
        }else{
          exp.mat<-cbind(exp.mat,exp.dat$FPKM)
          exp.mat.colname<-append(exp.mat.colname,sample)
        }
      }else if (tool=="rsem"){
        exp.dat<-exp.dat[order(exp.dat$gene_id),]
        if (i==1){
          dim(exp.mat)[1]<-nrow(exp.dat)
          rownames(exp.mat)<-exp.dat$gene_id
          exp.mat.gname<-exp.dat$gene_symbol
          exp.mat<-cbind(exp.mat,exp.dat$TPM)
          exp.mat.colname<-append(exp.mat.colname,sample)
        }else{
          exp.mat<-cbind(exp.mat,exp.dat$TPM)
          exp.mat.colname<-append(exp.mat.colname,sample)
        }
      }else if (tool=="stringtie"){
        exp.dat<-exp.dat[order(exp.dat$`Gene ID`),]
        if (i==1){
          dim(exp.mat)[1]<-nrow(exp.dat)
          rownames(exp.mat)<-exp.dat$`Gene ID`
          exp.mat.gname<-exp.dat$`Gene Name`
          exp.mat<-cbind(exp.mat,exp.dat$TPM)
          exp.mat.colname<-append(exp.mat.colname,sample)
        }else{
          exp.mat<-cbind(exp.mat,exp.dat$TPM)
          exp.mat.colname<-append(exp.mat.colname,sample)
        }
      }
    }else{
      print (paste0("The gene expression results of ",tool," for ",sample," does not exist!"))
    }
  }else{
    print (paste0("The gene expression results for ",sample," does not exist!"))
  }
}
colnames(exp.mat)<-exp.mat.colname

## Record the merge data matrix
## Note duplicated genes might exist
if (tool=="cufflinks"){
  fpkm.exp.fn<-paste0("Result_summary/merged_",tool,"_fpkm.gene.expression.results")
  write.table(cbind(gene_symbol=exp.mat.gname,exp.mat),fpkm.exp.fn,sep="\t",row.names=F,quote=F)
  
  ## Convert FPKM matrix to TPM matrix
  class(exp.mat)<-"numeric"
  exp.mat<-fpkm2TPM(exp.mat)
  tpm.exp.fn<-paste0("Result_summary/merged_",tool,"_tpm.gene.expression.results")
  write.table(cbind(gene_symbol=exp.mat.gname,exp.mat),tpm.exp.fn,sep="\t",row.names=F,quote=F)
}else if(tool=="rsem"||tool=="stringtie"){
  tpm.exp.fn<-paste0("Result_summary/merged_",tool,"_tpm.gene.expression.results")
  write.table(cbind(gene_symbol=exp.mat.gname,exp.mat),tpm.exp.fn,sep="\t",row.names=F,quote=F)
}
