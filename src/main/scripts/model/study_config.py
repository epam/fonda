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

from datetime import datetime
from jinja2 import Environment, FileSystemLoader


class StudyConfig:

    def __init__(self, job_name, dir_out, fastq_list, cufflinks_library_type, library_type, run, project=None):
        self.job_name = job_name
        self.dir_out = dir_out
        self.fastq_list = fastq_list
        self.cufflinks_library_type = cufflinks_library_type
        self.library_type = library_type
        self.project = project
        self.run = run
        self.date = datetime.date(datetime.now())

    def parse(self, workflow):
        if not self.cufflinks_library_type:
            self.cufflinks_library_type = "fr-unstranded"
        args_to_json = {"job_name": self.job_name,
                        "dir_out": self.dir_out,
                        "fastq_list": self.fastq_list,
                        "cufflinks_library_type": self.cufflinks_library_type,
                        "library_type": self.library_type,
                        "project": self.project,
                        "run": self.run,
                        "date": self.date}

        env = Environment(loader=FileSystemLoader("config_templates/templates"), trim_blocks=True, lstrip_blocks=True)
        global_template = env.get_template('study_template.txt')

        with open("study_template_{}.txt".format(workflow), "w") as f:
            f.write(global_template.render(args_to_json))
            f.close()
        return "{}/study_template_{}.txt".format(os.getcwd(), workflow)
