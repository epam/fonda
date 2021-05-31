# Copyright 2017-2021 Sanofi and EPAM Systems, Inc. (https://www.epam.com/)
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

WORKFLOW_NAME = "DnaCaptureVar_Fastq"
TEMPLATE = "global_template_DnaCaptureVar_Fastq.txt"
GLOBAL_CONFIG_TOOL_TEMPLATE_NAME_HUMAN_SURE_SELECT_V4 = "DnaCaptureVar_Fastq_tool_human_sure_select_v4.json"
GLOBAL_CONFIG_TOOL_TEMPLATE_NAME_HUMAN_SURE_SELECT_V5 = "DnaCaptureVar_Fastq_tool_human_sure_select_v5.json"
GLOBAL_CONFIG_TOOL_TEMPLATE_NAME_HUMAN_SURE_SELECT_V6 = "DnaCaptureVar_Fastq_tool_human_sure_select_v6.json"
GLOBAL_CONFIG_TOOL_TEMPLATE_NAME_MOUSE_EXOME_V1 = "DnaCaptureVar_Fastq_tool_mouse_exome_v1.json"


def usage():
    print('Usage:\n')
    print('-s <species> (required)          The species (human/mouse).\n')
    print('-t <read_type> (required)        The read type (paired/single).\n')
    print('-g <genome>                      The genome name.\n')
    print('-k <targeted_kit> (required)     The targeted kit name (Mouse_Exome_v1, SureSelect_v4, SureSelect_v5, '
          'SureSelect_v6).\n')
    print('-j <job_name>                    The job ID.\n')
    print('-d <dir_out> (required)          The output directory for the analysis.\n')
    print('-f <fastq_list> (required)       The path to the input manifest file or fastq folder '
          'or comma-delimited fastq file list for R1.\n')
    print('-q <fastq_list_r2>               The comma-delimited fastq file list for R2.\n')
    print('-l <library_type>                The sequencing library type: DNAWholeExomeSeq_Paired, '
          'DNAWholeExomeSeq_Single, DNATargetSeq_Paired, DNATargetSeq_Single, DNAAmpliconSeq_Paired, etc.\n')
    print('-p <project>                     The project ID.\n')
    print('-r <run>                         The run ID.\n')
    print('-n <toolset> (required)          A number of tools to run in a specific pipeline.\n')
    print('-x <flag_xenome>                 A flag (yes/no) to add xenome tool to the toolset.\n')
    print('-c <cores_per_sample>            A number of cores per sample for sge cluster.\n')
    print('--sync                           The flag (true/false) enables or disables "-sync" option '
          '("true" by default).\n')
    print('-i <sample_name_list>            The comma-delimited list of sample names.\n')
    print('-a <novoalign_tune>              The --tune option to tune Novoalign tool.\n')
    print('--master_mode                    The flag enables "-master" option.\n')
    print('-v <verbose>                     The enable debug verbosity output.\n')


def parse_arguments(script_name, argv):
    species = None
    read_type = None
    genome = None
    targeted_kit = None
    job_name = None
    dir_out = None
    fastq_list = None
    fastq_list_r2 = None
    library_type = None
    project = None
    run = None
    toolset = None
    flag_xenome = None
    cores_per_sample = None
    sync = None
    master_mode = None
    verbose = None
    sample_name_list = None
    novoalign_tune = None
    try:
        opts, args = getopt.getopt(argv, "hs:t:g:k:j:d:f:q:l:p:r:n:x:c:i:a:v", ["help", "species=", "read_type=",
                                                                                "genome=", "targeted_kit=", "job_name=",
                                                                                "dir_out=", "fastq_list=",
                                                                                "fastq_list_r2", "library_type=",
                                                                                "project=", "run=",
                                                                                "toolset=", "flag_xenome=",
                                                                                "cores_per_sample=", "sync=",
                                                                                "sample_name_list", "novoalign_tune",
                                                                                "master_mode", "verbose"])
        for opt, arg in opts:
            if opt == '-h':
                print(script_name + ' -s <species> -t <read_type> -g <genome> -k <targeted_kit> -j <job_name> '
                                    '-d <dir_out> -f <fastq_list> -q <fastq_list_r2> -l <library_type> -p <project> '
                                    '-r <run> -n <toolset> -x <flag_xenome> -c <cores_per_sample> <sync> '
                                    '-i <sample_name_list> -a <novoalign_tune> <master_mode> -v <verbose>')
                sys.exit()
            elif opt in ("-s", "--species"):
                species = arg
            elif opt in ("-t", "--read_type"):
                read_type = arg
            elif opt in ("-g", "--genome"):
                genome = arg
            elif opt in ("-k", "--targeted_kit"):
                targeted_kit = arg
            elif opt in ("-j", "--job_name"):
                job_name = arg
            elif opt in ("-d", "--dir_out"):
                dir_out = arg
            elif opt in ("-f", "--fastq_list"):
                fastq_list = arg
            elif opt in ("-q", "--fastq_list_r2"):
                fastq_list_r2 = arg
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
            elif opt in ("-c", "--cores_per_sample"):
                cores_per_sample = arg
            elif opt in "--sync":
                sync = arg
            elif opt in "--master_mode":
                master_mode = True
            elif opt in ("-v", "--verbose"):
                verbose = 'True'
            elif opt in ("-i", "--sample_name_list"):
                sample_name_list = arg
            elif opt in ("-a", "--novoalign_tune"):
                novoalign_tune = arg
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
        if not toolset:
            print('The set of tools (-n <toolset>) is required')
            usage()
            sys.exit(2)
        if not targeted_kit:
            print('The targeted kit name (-k <targeted_kit>) is required')
            usage()
            sys.exit(2)
        return species, read_type, job_name, dir_out, fastq_list, fastq_list_r2, library_type, project, run, toolset, \
            flag_xenome, cores_per_sample, verbose, sync, genome, targeted_kit, sample_name_list, master_mode, \
            novoalign_tune
    except getopt.GetoptError:
        usage()
        sys.exit(2)


def main(script_name, argv):
    species, read_type, job_name, dir_out, fastq_list, fastq_list_r2, library_type, project, run, toolset, \
        flag_xenome, cores_per_sample, verbose, sync, genome, targeted_kit, sample_name_list, master_mode, \
        novoalign_tune = parse_arguments(script_name, argv)
    if not library_type:
        library_type = "DNASeq"
    if not job_name:
        job_name = "{}_job".format(library_type)
    if not run:
        run = "{}_run".format(library_type)
    if not cores_per_sample:
        cores_per_sample = "4"
    if not novoalign_tune:
        novoalign_tune = "NA"
    if species == "human":
        human_kit = {
            'SureSelect_v4': GLOBAL_CONFIG_TOOL_TEMPLATE_NAME_HUMAN_SURE_SELECT_V4,
            'SureSelect_v5': GLOBAL_CONFIG_TOOL_TEMPLATE_NAME_HUMAN_SURE_SELECT_V5,
            'SureSelect_v6': GLOBAL_CONFIG_TOOL_TEMPLATE_NAME_HUMAN_SURE_SELECT_V6
        }
        try:
            global_config_tool_template_name = human_kit[targeted_kit]
        except KeyError as e:
            raise ValueError('Only SureSelect_v4, SureSelect_v5, SureSelect_v6 target kits are available for human.',
                             e.args[0])
    elif species == "mouse":
        if targeted_kit != "Mouse_Exome_v1":
            raise ValueError('Only Mouse_Exome_v1 target kit is available for mouse.')
        global_config_tool_template_name = GLOBAL_CONFIG_TOOL_TEMPLATE_NAME_MOUSE_EXOME_V1
    else:
        raise ValueError('Failed to determine "species" parameter. Available species: "human"/"mouse"')

    additional_options = {"novoalign_tune": novoalign_tune}
    global_config = GlobalConfig(species, read_type, TEMPLATE, WORKFLOW_NAME, toolset)
    global_config_path = global_config.create(global_config_tool_template_name, additional_options,
                                              flag_xenome=flag_xenome, cores_per_sample=cores_per_sample)
    if os.path.isdir(fastq_list):
        fastq_list = FastqSampleManifest(read_type).create_by_folder(fastq_list, WORKFLOW_NAME, library_type)
    elif 'fastq.gz' in str(fastq_list).split(',')[0]:
        sample_name_list = list(str(sample_name_list).split(',')) if sample_name_list else None
        fastq_list = FastqSampleManifest(read_type).create_by_list(list(str(fastq_list).split(',')),
                                                                   list(str(fastq_list_r2).split(',')),
                                                                   WORKFLOW_NAME, library_type,
                                                                   sample_names=sample_name_list)
    study_config = StudyConfig(job_name, dir_out, fastq_list, None, library_type, run, project=project)
    study_config_path = study_config.parse(workflow=WORKFLOW_NAME)
    Launcher.launch(global_config_path, study_config_path, sync, java_path=global_config.java_path, verbose=verbose,
                    master=master_mode)


if __name__ == "__main__":
    main(sys.argv[0], sys.argv[1:])
