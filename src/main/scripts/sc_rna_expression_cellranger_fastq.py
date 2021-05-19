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

WORKFLOW_NAME = "scRnaExpression_CellRanger_Fastq"
GLOBAL_CONFIG_TOOL_TEMPLATE_NAME_HUMAN_TOOLCHAIN1 = "ScRnaExpression_CellRanger_Fastq_tool_human_toolchain1.json"
GLOBAL_CONFIG_TOOL_TEMPLATE_NAME_HUMAN_TOOLCHAIN2 = "ScRnaExpression_CellRanger_Fastq_tool_human_toolchain2.json"
GLOBAL_CONFIG_TOOL_TEMPLATE_NAME_MOUSE_TOOLCHAIN1 = "ScRnaExpression_CellRanger_Fastq_tool_mouse_toolchain1.json"
GLOBAL_CONFIG_TOOL_TEMPLATE_NAME_MOUSE_TOOLCHAIN2 = "ScRnaExpression_CellRanger_Fastq_tool_mouse_toolchain2.json"
TEMPLATE = "global_template_scRnaExpression_CellRanger_Fastq.txt"


def usage():
    print('Usage:\n')
    print('-s <species> (required)          The species (human/mouse).\n')
    print('-t <read_type>                   The read type (paired/single).\n')
    print('-j <job_name>                    The job ID.\n')
    print('-o <dir_out> (required)          The output directory for the analysis.\n')
    print('-b <feature_reference>           A feature reference csv file that declares the set of Feature Barcode '
          'reagents in use in the experiment.\n')
print('-x <target_panel>                target panel csv path.\n')
    print('-l <fastq_list> (required)       The path to the input manifest file or fastq folder '
          'or comma-delimited fastq file list for R1.\n')
    print('-q <fastq_list_r2>               The comma-delimited fastq file list for R2.\n')
    print('-L <library_type>                The list of library types of each libraries will enable additional '
          'downstream processing, specifically for CRISPR Guide Capture and Antibody Capture.\n')
    print('-M <master>                      The list of the overall sample names.\n')
    print('-e <expected_cells>              The expected number of recovered cells.\n')
    print('-f <forced_cells>                Force pipeline to use this number of cells, bypassing the cell '
          'detection algorithm.\n')
    print('-c <chemistry> (required)        Assay configuration.\n')
    print('-a <nosecondary>                 Skip secondary analysis of the feature-barcode matrix.\n')
    print('-R <r1_length> (required)        Hard-trim the input R1 sequence to this length.\n')
    print('-r <r2_length> (required)        Hard-trim the input R2 sequence to this length.\n')
    print('-G <genome_build> (required)     Genome build.\n')
    print('-m <transcriptome> (required)    Path to the Cell Ranger compatible transcriptome reference.\n')
    print('-g <vdj_genome> (required)       Path to the Cell Ranger V(D)J compatible reference.\n')
    print('-d <detect_doublet>              If enabled, doubletdetection step will be added to the toolset.\n')
    print('-p <project>                     The project ID.\n')
    print('-u <run>                         The run ID.\n')
    print('-n <toolset>                     A number of tools to run in a specific pipeline.\n')
    print('-k <cores_per_sample>            A number of cores per sample for sge cluster.\n')
    print('--sync                           The flag (true/false) enables or disables "-sync" option '
          '("true" by default).\n')
    print('-i <sample_name_list>            The comma-delimited list of sample names.\n')
    print('-z <toolchain> (required)        A user-defined toolchain.\n')
    print('--master_mode                    The flag enables "-master" option.\n')
    print('-v <verbose>                     The enable debug verbosity output.\n')


def parse_arguments(script_name, argv):
    species = None
    read_type = None
    job_name = None
    dir_out = None
    feature_reference = None
    fastq_list = None
    fastq_list_r2 = None
    library_type = None
    master = None
    expected_cells = None
    forced_cells = None
    chemistry = None
    nosecondary = None
    r1_length = None
    r2_length = None
    genome_build = None
    transcriptome = None
    vdj_genome = None
    detect_doublet = None
    project = None
    run = None
    toolset = None
    cores_per_sample = None
    sync = None
    master_mode = None
    verbose = None
    sample_name_list = None
    toolchain = None
    target_panel = None
    try:
        opts, args = getopt.getopt(argv, "hs:t:j:o:b:l:q:L:M:e:f:c:a:R:r:G:m:g:d:p:u:n:k:i:z:v", ["help", "species=",
                                                                                                  "read_type=",
                                                                                                  "job_name=",
                                                                                                  "dir_out=",
                                                                                                  "feature_reference=",
                                                                                                  "target_panel=",
                                                                                                  "fastq_list=",
                                                                                                  "fastq_list_r2=",
                                                                                                  "library_type=",
                                                                                                  "master=",
                                                                                                  "expected_cells=",
                                                                                                  "forced_cells=",
                                                                                                  "chemistry=",
                                                                                                  "nosecondary=",
                                                                                                  "r1_length=",
                                                                                                  "r2_length=",
                                                                                                  "genome_build=",
                                                                                                  "transcriptome=",
                                                                                                  "vdj_genome=",
                                                                                                  "detect_doublet=",
                                                                                                  "project=", "run=",
                                                                                                  "toolset=",
                                                                                                  "cores_per_sample=",
                                                                                                  "sync=",
                                                                                                  "sample_name_list",
                                                                                                  "toolchain",
                                                                                                  "master_mode",
                                                                                                  "verbose"])
        for opt, arg in opts:
            if opt == '-h':
                print(script_name + ' -s <species> -t <read_type> -j <job_name> -o <dir_out> -b <feature_reference> '
                                    '-l <fastq_list> -q <fastq_list_r2> -L <library_type> -M <master> '
                                    '-e <expected_cells> -f <forced_cells> -c <chemistry> -a <nosecondary> '
                                    '-R <r1_length> -r <r2_length> -G <genome_build> -m <transcriptome> -g <vdj_genome>'
                                    ' -d <detect_doublet> -p <project> -u <run> -n <toolset> -k <cores_per_sample> '
                                    '<sync> -i <sample_name_list> -z <toolchain> <master_mode> -v <verbose>')
                sys.exit()
            elif opt in ("-s", "--species"):
                species = arg
            elif opt in ("-t", "--read_type"):
                read_type = arg
            elif opt in ("-j", "--job_name"):
                job_name = arg
            elif opt in ("-o", "--dir_out"):
                dir_out = arg
            elif opt in ("-b", "--feature_reference"):
                feature_reference = arg
            elif opt in ("-x", "--target_panel"):
                target_panel = arg
            elif opt in ("-l", "--fastq_list"):
                fastq_list = arg
            elif opt in ("-q", "--fastq_list_r2"):
                fastq_list_r2 = arg
            elif opt in ("-L", "--library_type"):
                library_type = arg
            elif opt in ("-M", "--master"):
                master = arg
            elif opt in ("-e", "--expected_cells"):
                expected_cells = arg
            elif opt in ("-f", "--forced_cells"):
                forced_cells = arg
            elif opt in ("-c", "--chemistry"):
                chemistry = arg
            elif opt in ("-a", "--nosecondary"):
                nosecondary = arg
            elif opt in ("-R", "--r1_length"):
                r1_length = arg
            elif opt in ("-r", "--r2_length"):
                r2_length = arg
            elif opt in ("-G", "--genome_build"):
                genome_build = arg
            elif opt in ("-m", "--transcriptome"):
                transcriptome = arg
            elif opt in ("-g", "--vdj_genome"):
                vdj_genome = arg
            elif opt in ("-d", "--detect_doublet"):
                detect_doublet = arg
            elif opt in ("-p", "--project"):
                project = arg
            elif opt in ("-u", "--run"):
                run = arg
            elif opt in ("-n", "--toolset"):
                toolset = arg
            elif opt in ("-k", "--cores_per_sample"):
                cores_per_sample = arg
            elif opt in "--sync":
                sync = arg
            elif opt in "--master_mode":
                master_mode = True
            elif opt in ("-v", "--verbose"):
                verbose = 'True'
            elif opt in ("-i", "--sample_name_list"):
                sample_name_list = arg
            elif opt in ("-z", "--toolchain"):
                toolchain = arg
        if not species:
            print('Species (-s <species>) is required')
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
        if not chemistry:
            print('The assay configuration (-c <chemistry>) is required')
            usage()
            sys.exit(2)
        if not r1_length:
            print('The length parameter of hard-trim the input R1 sequence (-R <r1_length>) is required')
            usage()
            sys.exit(2)
        if not r2_length:
            print('The length parameter of hard-trim the input R2 sequence (-r <r2_length>) is required')
            usage()
            sys.exit(2)
        if not genome_build:
            print('The genome build (-G <genome_build>) is required')
            usage()
            sys.exit(2)
        if not transcriptome:
            print('The transcriptome reference (-m <transcriptome>) is required')
            usage()
            sys.exit(2)
        if not vdj_genome:
            print('The path to the Cell Ranger V(D)J compatible reference (-g <vdj_genome>) is required')
            usage()
            sys.exit(2)
        return species, read_type, job_name, dir_out, feature_reference, fastq_list, fastq_list_r2, library_type, \
            master, expected_cells, forced_cells, chemistry, nosecondary, r1_length, r2_length, genome_build, \
            transcriptome, vdj_genome, detect_doublet, project, run, toolset, cores_per_sample, verbose, sync, \
            sample_name_list, master_mode, toolchain, target_panel
    except getopt.GetoptError:
        usage()
        sys.exit(2)


def main(script_name, argv):
    species, read_type, job_name, dir_out, feature_reference, fastq_list, fastq_list_r2, libtype, master, \
        expected_cells, forced_cells, chemistry, nosecondary, r1_length, r2_length, genome_build, transcriptome, \
        vdj_genome, detect_doublet, project, run, toolset, cores_per_sample, verbose, sync, sample_name_list, \
        master_mode, toolchain, target_panel = parse_arguments(script_name, argv)

    library_type = "scRNASeq"
    if not read_type:
        read_type = "paired"
    if not job_name:
        job_name = "{}_job".format(library_type)
    if not run:
        run = "{}_run".format(library_type)
    if not expected_cells:
        expected_cells = "3000"
    if not forced_cells:
        forced_cells = "NA"
    if not nosecondary:
        nosecondary = "FALSE"
    if not cores_per_sample:
        cores_per_sample = "8"
    additional_options = {"feature_reference": feature_reference,
                          "genome_build": genome_build,
                          "genome": transcriptome,
                          "transcriptome": transcriptome,
                          "vdj_genome": vdj_genome,
                          "expected_cells": expected_cells,
                          "forced_cells": forced_cells,
                          "chemistry": chemistry,
                          "nosecondary": nosecondary,
                          "r1_length": r1_length,
                          "r2_length": r2_length,
                          "target_panel": target_panel}
    if not toolset:
        toolset = "count+vdj+qc"
    if detect_doublet and "doubletdetection" not in toolset:
        toolset += "+doubletdetection"
    global_config = GlobalConfig(species, read_type, TEMPLATE, WORKFLOW_NAME, toolset)
    if species == "human":
        global_config_tool_template_name = GLOBAL_CONFIG_TOOL_TEMPLATE_NAME_HUMAN_TOOLCHAIN2 \
            if toolchain == "Cellranger_v6" else GLOBAL_CONFIG_TOOL_TEMPLATE_NAME_HUMAN_TOOLCHAIN1
    elif species == "mouse":
        global_config_tool_template_name = GLOBAL_CONFIG_TOOL_TEMPLATE_NAME_MOUSE_TOOLCHAIN2 \
            if toolchain == "Cellranger_v6" else GLOBAL_CONFIG_TOOL_TEMPLATE_NAME_MOUSE_TOOLCHAIN1
    else:
        raise RuntimeError('Failed to determine "species" parameter. Available species: "human" and "mouse"')

    global_config_path = global_config.create(global_config_tool_template_name, additional_options,
                                              cores_per_sample=cores_per_sample)
    if os.path.isdir(fastq_list):
        fastq_list = FastqSampleManifest(read_type).create_by_folder(fastq_list, WORKFLOW_NAME, library_type)
    elif 'fastq.gz' in str(fastq_list).split(',')[0]:
        sample_libtype = list(str(libtype).split(',')) if libtype else None
        sample_master = list(str(master).split(',')) if master else None
        sample_name_list = list(str(sample_name_list).split(',')) if sample_name_list else None
        fastq_list = FastqSampleManifest(read_type).create_by_list(list(str(fastq_list).split(',')),
                                                                   list(str(fastq_list_r2).split(',')),
                                                                   WORKFLOW_NAME, library_type,
                                                                   sample_libtype=sample_libtype,
                                                                   sample_master=sample_master,
                                                                   sample_names=sample_name_list)
    study_config = StudyConfig(job_name, dir_out, fastq_list, None, library_type, run, project=project)
    study_config_path = study_config.parse(workflow=WORKFLOW_NAME)
    Launcher.launch(global_config_path, study_config_path, sync, java_path=global_config.java_path, verbose=verbose,
                    master=master_mode)


if __name__ == "__main__":
    main(sys.argv[0], sys.argv[1:])
