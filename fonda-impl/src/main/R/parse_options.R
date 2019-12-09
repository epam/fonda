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

parse = function (spec=NULL,file=NULL,usage=FALSE) {
    if (usage) {
        help = paste('', "Help: ", file, sep=' ')
        for (i in 1:(dim(spec))[1] ) {
            help = paste(help,' [-',spec[i, 1], '|', spec[i, 2], ' <character>', ']',sep='')
        }
        help = paste(help, "\n", sep='')
        return(help)
    } else {
        args = commandArgs(trailingOnly=TRUE)
    }

    if (length(args) == 0) {
        stop(paste("No one argument was not be supplied ", file), call.=FALSE)
    }

    opt = list()
    opt$ARGS = vector(mode="character")
    for (i in 1:length(args)) {
        option = args[i]
        if (substr(option, 0, 1) == '-') {
            opt[spec[grep(substr(option, 2, 3), spec[, 2],fixed=TRUE ), 1]] = args[i + 1]
        }
    }

    return(opt)
}
