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
import sys

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
    print('	-j <job_name> (required)         The job ID.\n')
    print('	-d <dir_out> (required)          The output directory for the analysis.\n')
    print('	-f <fastq_list> (required)       The path to the input manifest file.\n')
    print('	-c <cufflinks_library_type> (required)          '
          'The cufflinks library type (fr-unstranded | fr-firststrand | fr-secondstrand).\n')
    print('	-l <library_type> (required)     The sequencing library type: DNAWholeExomeSeq_Paired, '
          'DNAWholeExomeSeq_Single, DNATargetSeq_Paired, DNATargetSeq_Single, DNAAmpliconSeq_Paired, RNASeq_Paired, '
          'RNASeq_Single, etc.\n')
    print('	-p <project> (required)          The project ID.\n')
    print('	-r <run> (required)              The run ID.\n')
    print('	-n <toolset> (required)          A number of tools to run in a specific pipeline.\n')


def parse_arguments(script_name, argv):
    species = None
    read_type = None
    job_name = None
    dir_out = None
    fastq_list = None
    cufflinks_library_type = None
    library_type = None
    project = None
    run = None
    toolset = None
    try:
        opts, args = getopt.getopt(argv, "hs:t:j:d:f:c:l:p:r:n:", ["help", "species=", "read_type=", "job_name=",
                                                                   "dir_out=", "fastq_list=", "cufflinks_library_type=",
                                                                   "library_type=", "project=", "run=", "toolset="])
        for opt, arg in opts:
            if opt == '-h':
                print(script_name + ' -s <species> -t <read_type> -j <job_name> -d <dir_out> -f <fastq_list> '
                                    '-c <cufflinks_library_type> -l <library_type> -p <project> -r <run> -n <toolset>')
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
        if not job_name:
            print('The job ID (-j <job_name>) is required')
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
        if not library_type:
            print('The sequencing library type (-l <library_type>) is required')
            usage()
            sys.exit(2)
        if not project:
            print('The project ID (-p <project>) is required')
            usage()
            sys.exit(2)
        if not run:
            print('The run ID (-r <run>) is required')
            usage()
            sys.exit(2)
        if not toolset:
            print('The set of tools (-n <toolset>) is required')
            usage()
            sys.exit(2)
        return species, read_type, job_name, dir_out, fastq_list, cufflinks_library_type, library_type, project, run, toolset
    except getopt.GetoptError:
        usage()
        sys.exit(2)


def main(script_name, argv):
    species, read_type, job_name, dir_out, fastq_list, cufflinks_library_type, library_type, project, run, toolset = \
        parse_arguments(script_name, argv)

    global_config = GlobalConfig(species, read_type, TEMPLATE, WORKFLOW_NAME, toolset)
    global_config_path = global_config.create(GLOBAL_CONFIG_TOOL_TEMPLATE_NAME, None, flag_xenome=False)
    study_config = StudyConfig(job_name, dir_out, fastq_list, cufflinks_library_type, library_type, project, run)
    study_config_path = study_config.parse(workflow=WORKFLOW_NAME)
    Launcher.launch(global_config_path, study_config_path)


if __name__ == "__main__":
    main(sys.argv[0], sys.argv[1:])
