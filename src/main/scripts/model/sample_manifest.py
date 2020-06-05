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
import os
import sys
from abc import abstractmethod, ABC
from os.path import join, exists

from jinja2 import Environment, FileSystemLoader

from model.sample_utilities import get_sample_name


class SampleManifest(ABC):

    def __init__(self, read_type, parameter_type):
        self.read_type = read_type
        self.parameter_type = parameter_type
        self.sample_name = None

    def write(self, extension, files, workflow_name, library_type):
        data = {
            "parameter_type": self.parameter_type,
            "files": files,
            "tab": '\t'
        }
        sample_type = 'Fastq' if extension.split('.')[0] == 'fastq' else 'Bam'
        main_dir = os.path.dirname(os.path.realpath(__import__("__main__").__file__))
        env = Environment(loader=FileSystemLoader("{}/config_templates/templates".format(main_dir)), trim_blocks=True,
                          lstrip_blocks=True)
        template = "sample_manifest_paired_template.txt" \
            if self.read_type == 'paired' and self.parameter_type == "fastqFile" \
            else "sample_manifest_single_template.txt"
        global_template = env.get_template(template)
        list_path = "{}_{}_Sample{}Paths.txt".format(workflow_name, library_type, sample_type)
        with open(list_path, "w") as f:
            f.write(global_template.render(data))
            f.close()

        return "{}/{}".format(os.getcwd(), list_path)

    def write_from_list(self, extension, list_r1, list_r2, workflow_name, library_type):
        files = []
        for i, f in enumerate(list_r1):
            sample_dir = os.path.dirname(list_r1[i])
            self.sample_name = get_sample_name(f, sample_dir)
            if self.read_type == 'paired' and self.parameter_type == "fastqFile":
                if 'None' in list_r2:
                    print('The comma-delimited fastq file list for R2 is required for paired mode.')
                    sys.exit(2)
                if len(list_r2) != len(list_r1):
                    print('The comma-delimited fastq file lists for R1 and R2 are not the same size.')
                    sys.exit(2)
                self.add_sample(files, f, list_r2[i])
            else:
                self.add_sample(files, f, None)
        return self.write(extension, files, workflow_name, library_type)

    def write_from_dir(self, extension, sample_dir, sample_files, workflow_name, library_type):
        files = []
        for f in sample_files:
            self.sample_name = get_sample_name(f, sample_dir)
            parameter1 = join(sample_dir, f)
            if self.read_type == 'paired' and self.parameter_type == "fastqFile":
                parameter2 = join(sample_dir, f.replace('R1', 'R2'))
                if not exists(parameter2):
                    print("{} fastq file for paired mode is not found".format(parameter2))
                    sys.exit(2)
                self.add_sample(files, parameter1, parameter2)
            else:
                self.add_sample(files, parameter1, None)
        return self.write(extension, files, workflow_name, library_type)

    def add_sample(self, files, parameter1, parameter2):
        if parameter2 is None:
            files.append(
                {
                    "sample_name": self.sample_name,
                    "parameter_1": parameter1
                })
            return
        files.append(
            {
                "sample_name": self.sample_name,
                "parameter_1": parameter1,
                "parameter_2": parameter2
            })

    @abstractmethod
    def create_by_folder(self, sample_dir, workflow_name, library_type):
        pass

    @abstractmethod
    def create_by_list(self, file_list_1, file_list_2, workflow_name, library_type):
        pass
