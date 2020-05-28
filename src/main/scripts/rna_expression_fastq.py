# Copyright 2017-2020 Sanofi and EPAM Systems, Inc. (https://www.epam.com/)
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

import getopt
import os
import sys

from model.fastq_sample_manifest import FastqSampleManifest
from model.global_config import GlobalConfig
from launcher import Launcher
from model.study_config import StudyConfig

WORKFLOW_NAME = "RnaExpression_Fastq"
TEMPLATE = "global_template_RnaExpressionFastq.txt"
GLOBAL_CONFIG_TOOL_TEMPLATE_NAME = "RnaExpression_tool"


def usage():
    print('Usage:\n')
    print(' -s <species> (required)          The species (human/mouse).\n')
    print('	-t <read_type> (required)        The read type (paired/single).\n')
    print('	-j <job_name>                    The job ID.\n')
    print('	-d <dir_out> (required)          The output directory for the analysis.\n')
    print('	-f <fastq_list> (required)       The path to the input manifest file or fastq folder '
          'or comma-delimited fastq file list for R1.\n')
    print('	-q <fastq_list_r2>               The comma-delimited fastq file list for R2.\n')
    print('	-c <cufflinks_library_type> (required)          '
          'The cufflinks library type (fr-unstranded/fr-firststrand/fr-secondstrand).\n')
    print('	-l <library_type>                The sequencing library type: DNAWholeExomeSeq_Paired, '
          'DNAWholeExomeSeq_Single, DNATargetSeq_Paired, DNATargetSeq_Single, DNAAmpliconSeq_Paired, RNASeq_Paired, '
          'RNASeq_Single, etc.\n')
    print('	-p <project>                     The project ID.\n')
    print('	-r <run>                         The run ID.\n')
    print('	-n <toolset> (required)          A number of tools to run in a specific pipeline.\n')
    print('	-x <flag_xenome>                 A flag (true/false) to add xenome tool to the toolset.\n')
    print('	-v <verbose>                     The enable debug verbosity output.\n')


def parse_arguments(script_name, argv):
    species = None
    read_type = None
    job_name = None
    dir_out = None
    fastq_list = None
    fastq_list_r2 = None
    cufflinks_library_type = None
    library_type = None
    project = None
    run = None
    toolset = None
    flag_xenome = None
    verbose = None
    try:
        opts, args = getopt.getopt(argv, "hs:t:j:d:f:q:c:l:p:r:n:x:v", ["help", "species=", "read_type=", "job_name=",
                                                                        "dir_out=", "fastq_list=", "fastq_list_r2",
                                                                        "cufflinks_library_type=", "library_type=",
                                                                        "project=", "run=", "toolset=", "flag_xenome=",
                                                                        "verbose="])
        for opt, arg in opts:
            if opt == '-h':
                print(script_name + ' -s <species> -t <read_type> -j <job_name> -d <dir_out> -f <fastq_list> '
                                    '-q <fastq_list_r2> -c <cufflinks_library_type> -l <library_type> -p <project> '
                                    '-r <run> -n <toolset> -x <flag_xenome> -v <verbose>')
                sys.exit()
            elif opt in ("-s", "--species"):
                species = arg
            elif opt in ("-t", "--read_type"):
                read_type = arg
            elif opt in ("-j", "--job_name"):
                job_name = arg
            elif opt in ("-d", "--dir_out"):
                dir_out = arg
            elif opt in ("-f", "--fastq_list"):
                fastq_list = arg
            elif opt in ("-q", "--fastq_list_r2"):
                fastq_list_r2 = arg
            elif opt in ("-c", "--cufflinks_library_type"):
                cufflinks_library_type = arg
            elif opt in ("-l", "--library_type"):
                library_type = arg
            elif opt in ("-p", "--project"):
                project = arg
            elif opt in ("-r", "--run"):
                run = arg
            elif opt in ("-n", "--toolset"):
                toolset = arg
            elif opt in ("-x", "--flag_xenome"):
                flag_xenome = arg
            elif opt in ("-v", "--verbose"):
                verbose = 'True'
        if not species:
            print('Species (-s <species>) is required')
            usage()
            sys.exit(2)
        if not read_type:
            print('Read type (-t <read_type>) is required')
            usage()
            sys.exit(2)
        if not fastq_list:
            print('The path to the input manifest fastq file (-f <fastq_list>) is required')
            usage()
            sys.exit(2)
        if not dir_out:
            print('The output directory for the analysis (-d <dir_out>) is required')
            usage()
            sys.exit(2)
        if not cufflinks_library_type:
            print('The cufflinks library type (-c <cufflinks_library_type>) is required')
            usage()
            sys.exit(2)
        if not toolset:
            print('The set of tools (-n <toolset>) is required')
            usage()
            sys.exit(2)
        return species, read_type, job_name, dir_out, fastq_list, fastq_list_r2, cufflinks_library_type, library_type, \
            project, run, toolset, flag_xenome, verbose
    except getopt.GetoptError:
        usage()
        sys.exit(2)


def main(script_name, argv):
    species, read_type, job_name, dir_out, fastq_list, fastq_list_r2, cufflinks_library_type, library_type, project, \
        run, toolset, flag_xenome, verbose = parse_arguments(script_name, argv)
    if not library_type:
        library_type = "RNASeq"
    if not job_name:
        job_name = "{}_job".format(library_type)
    if not run:
        run = "{}_run".format(library_type)
    global_config = GlobalConfig(species, read_type, TEMPLATE, WORKFLOW_NAME, toolset)
    global_config_path = global_config.create(GLOBAL_CONFIG_TOOL_TEMPLATE_NAME, None, flag_xenome=flag_xenome)
    if os.path.isdir(fastq_list):
        fastq_list = FastqSampleManifest(read_type).create_by_folder(fastq_list, WORKFLOW_NAME, library_type)
    elif 'fastq.gz' in str(fastq_list).split(',')[0]:
        fastq_list = FastqSampleManifest(read_type).create_by_list(list(str(fastq_list).split(',')),
                                                                   list(str(fastq_list_r2).split(',')),
                                                                   WORKFLOW_NAME, library_type)
    study_config = StudyConfig(job_name, dir_out, fastq_list, cufflinks_library_type, library_type, run,
                               project=project)
    study_config_path = study_config.parse(workflow=WORKFLOW_NAME)
    Launcher.launch(global_config_path, study_config_path, verbose=verbose)


if __name__ == "__main__":
    main(sys.argv[0], sys.argv[1:])
