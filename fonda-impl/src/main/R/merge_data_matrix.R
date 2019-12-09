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
# script to merge data matrices
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
  'dat1', 'a',
  'dat2', 'b',
  'output', 'o'
), ncol=2, byrow=TRUE);

opt = parse(spec);

#----------------------
# print help menu
#
if (!is.null(opt$help) || is.null(opt$dat1) || is.null(opt$dat2) || is.null(opt$output)) {
  opt = parse(spec,
              file = 'merge_data_matrix.R',
              usage = T);
  cat(opt);
  q(stat=0);
}

dat1 = opt$dat1 ## the input data file 1
dat2 = opt$dat2  ## the input data file 2
output = opt$output  ## the output file

dat1 <- read.table(dat1,header=T,sep="\t",check.names=F,stringsAsFactors = F,comment.char = "%")
dat1 <- t(dat1)

dat2 <- read.table(dat2,header=T,sep="\t",check.names=F,stringsAsFactors = F,comment.char = "%")
dat2 <- t(dat2)

merge_dat <- merge(dat1, dat2, by="row.names", all=TRUE)
rownames(merge_dat)<-merge_dat[,1]
merge_dat<-t(merge_dat[,-1])
merge_dat[is.na(merge_dat)]<-0

write.table(merge_dat,output,sep="\t",row.names=F,quote=F)



