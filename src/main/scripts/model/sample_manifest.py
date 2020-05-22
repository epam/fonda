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
from os.path import join, exists, basename, normpath

from jinja2 import Environment, FileSystemLoader


class SampleManifest(ABC):

    def __init__(self, read_type, parameter_type):
        self.read_type = read_type
        self.parameter_type = parameter_type
        self.sample_name = None

    def write(self, extension, sample_dir, sample_files, workflow_name, library_type):
        if basename(normpath(sample_dir)).split('Sample_')[1] in sample_files[0]:
            self.sample_name = basename(normpath(sample_dir)).split('Sample_')[1]
        else:
            self.sample_name = sample_files[0].split('_')[0]
        files = []
        for f in sample_files:
            parameter1 = join(sample_dir, f)
            if self.read_type == 'paired' and self.parameter_type == "fastqFile":
                parameter2 = join(sample_dir, f.replace('R1', 'R2'))
                if not exists(parameter2):
                    print("{} fastq file for paired mode is not found".format(parameter2))
                    sys.exit(2)
                files.append(
                    {
                        "sample_name": self.sample_name,
                        "parameter_1": parameter1,
                        "parameter_2": parameter2
                    }
                )
            else:
                files.append(
                    {
                        "sample_name": self.sample_name,
                        "parameter_1": parameter1
                    }
                )
        data = {
            "parameter_type": self.parameter_type,
            "files": files,
            "tab": '\t'
        }
        sample_type = 'Fastq' if extension.split('.')[0] == 'fastq' else 'Bam'
        env = Environment(loader=FileSystemLoader("config_templates/templates"), trim_blocks=True,
                          lstrip_blocks=True)
        template = "sample_manifest_paired_template.txt" \
            if self.read_type == 'paired' and self.parameter_type == "fastqFile"\
            else "sample_manifest_single_template.txt"
        global_template = env.get_template(template)
        list_path = "{}_{}_Sample{}Paths.txt".format(workflow_name, library_type, sample_type)
        with open(list_path, "w") as f:
            f.write(global_template.render(data))
            f.close()

        return "{}/{}".format(os.getcwd(), list_path)

    @abstractmethod
    def create(self, sample_dir, workflow_name, library_type):
        pass
