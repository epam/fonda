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

WORKFLOW_NAME = "scRnaExpression_CellRanger_Fastq"
GLOBAL_CONFIG_TOOL_TEMPLATE_NAME = "ScRnaExpression_CellRanger_Fastq_tool"
TEMPLATE = "global_template_scRnaExpression_CellRanger_Fastq.txt"


def usage():
    print('Usage:\n')
    print(' -s <species> (required)          The species (human/mouse).\n')
    print('	-t <read_type> (required)        The read type (paired/single).\n')
    print('	-j <job_name>                    The job ID.\n')
    print('	-o <dir_out> (required)          The output directory for the analysis.\n')
    print('	-l <fastq_list> (required)       The path to the input manifest file.\n')
    print('	-e <expected_cells> (required)   The expected number of recovered cells.\n')
    print(' -f <forced_cells> (required)     Force pipeline to use this number of cells, '
          'bypassing the cell detection algorithm.\n')
    print(' -c <chemistry> (required)        Assay configuration.\n')
    print(' -R <r1_length> (required)        Hard-trim the input R1 sequence to this length.\n')
    print(' -r <r2_length>                   Hard-trim the input R2 sequence to this length.\n')
    print(' -d <detect_doublet>              If enabled, doubletdetection step will be added to the toolset.\n')
    print('	-p <project>                     The project ID.\n')
    print('	-u <run>                         The run ID.\n')
    print('	-n <toolset> (required)          A number of tools to run in a specific pipeline.\n')


def parse_arguments(script_name, argv):
    species = None
    read_type = None
    job_name = None
    dir_out = None
    fastq_list = None
    expected_cells = None
    forced_cells = None
    chemistry = None
    r1_length = None
    r2_length = None
    detect_doublet = None
    project = None
    run = None
    toolset = None
    try:
        opts, args = getopt.getopt(argv, "hs:t:j:o:l:e:f:c:R:r:d:p:u:n:", ["help", "species=", "read_type=",
                                                                           "job_name=", "dir_out=", "fastq_list=",
                                                                           "expected_cells=", "forced_cells=",
                                                                           "chemistry=", "r1_length=", "r2_length=",
                                                                           "detect_doublet=", "project=", "run=",
                                                                           "toolset="])
        for opt, arg in opts:
            if opt == '-h':
                print(script_name + ' -s <species> -t <read_type> -j <job_name> -o <dir_out> -l <fastq_list> '
                                    '-e <expected_cells> -f <forced_cells> -c <chemistry> -R <r1_length> -r <r2_length>'
                                    ' -d <detect_doublet> -p <project> -u <run> -n <toolset>')
                sys.exit()
            elif opt in ("-s", "--species"):
                species = arg
            elif opt in ("-t", "--read_type"):
                read_type = arg
            elif opt in ("-j", "--job_name"):
                job_name = arg
            elif opt in ("-o", "--dir_out"):
                dir_out = arg
            elif opt in ("-l", "--fastq_list"):
                fastq_list = arg
            elif opt in ("-e", "--expected_cells"):
                expected_cells = arg
            elif opt in ("-f", "--forced_cells"):
                forced_cells = arg
            elif opt in ("-c", "--chemistry"):
                chemistry = arg
            elif opt in ("-R", "--r1_length"):
                r1_length = arg
            elif opt in ("-r", "--r2_length"):
                r2_length = arg
            elif opt in ("-d", "--detect_doublet"):
                detect_doublet = arg
            elif opt in ("-p", "--project"):
                project = arg
            elif opt in ("-u", "--run"):
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
            print('The path to the input manifest fastq file (-l <fastq_list>) is required')
            usage()
            sys.exit(2)
        if not dir_out:
            print('The output directory for the analysis (-o <dir_out>) is required')
            usage()
            sys.exit(2)
        if not expected_cells:
            print('The expected number of recovered cells (-e <expected_cells>) is required')
            usage()
            sys.exit(2)
        if not forced_cells:
            print('The forced cells parameter (-f <forced_cells>) is required')
            usage()
            sys.exit(2)
        if not chemistry:
            print('The assay configuration (-c <chemistry>) is required')
            usage()
            sys.exit(2)
        if not r1_length:
            print('The length parameter of hard-trim the input R1 sequence (-R <r1_length>) is required')
            usage()
            sys.exit(2)
        if not r2_length and read_type == 'paired':
            print('The length parameter of hard-trim the input R2 sequence (-r <r2_length>) is required')
            usage()
            sys.exit(2)
        if not toolset:
            print('The set of tools (-n <toolset>) is required')
            usage()
            sys.exit(2)
        return species, read_type, job_name, dir_out, fastq_list, expected_cells, forced_cells, chemistry, r1_length, \
            r2_length, detect_doublet, project, run, toolset
    except getopt.GetoptError:
        usage()
        sys.exit(2)


def main(script_name, argv):
    species, read_type, job_name, dir_out, fastq_list, expected_cells, forced_cells, chemistry, r1_length, r2_length, \
        detect_doublet, project, run, toolset = parse_arguments(script_name, argv)
    additional_options = {"expected_cells": expected_cells,
                          "forced_cells": forced_cells,
                          "chemistry": chemistry,
                          "r1_length": r1_length,
                          "r2_length": r2_length}
    if detect_doublet and "doubletdetection" not in toolset:
        toolset += "+doubletdetection"

    library_type = "RNASeq"
    if not job_name:
        job_name = "{}_job".format(library_type)
    if not run:
        run = "{}_run".format(library_type)
    global_config = GlobalConfig(species, read_type, TEMPLATE, WORKFLOW_NAME, toolset)
    global_config_path = global_config.create(GLOBAL_CONFIG_TOOL_TEMPLATE_NAME, additional_options)
    if os.path.isdir(fastq_list):
        fastq_list = FastqSampleManifest(read_type).create_by_folder(fastq_list, WORKFLOW_NAME, library_type)
    study_config = StudyConfig(job_name, dir_out, fastq_list, None, library_type, run, project=project)
    study_config_path = study_config.parse(workflow=WORKFLOW_NAME)
    Launcher.launch(global_config_path, study_config_path)


if __name__ == "__main__":
    main(sys.argv[0], sys.argv[1:])