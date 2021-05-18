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
import json
import os
import sys

from jinja2 import Environment, FileSystemLoader


class GlobalConfig:

    def __init__(self, species, read_type, template, workflow, toolset):
        self.species = species
        self.read_type = read_type
        self.template = template
        self.workflow = workflow
        self.toolset = toolset
        self.java_path = None

    def create(self, config_json, additional_options, flag_xenome=False, cores_per_sample=4, genome_load=None):
        if not self.workflow:
            print("Workflow name is required")
            sys.exit(2)
        if not self.toolset:
            print("Toolset is required")
            sys.exit(2)
        if not self.template:
            print("Template file is required")
            sys.exit(2)
        if not config_json:
            print("Configuration json file is required")
            sys.exit(2)
        if not cores_per_sample:
            cores_per_sample = 4
        main_dir = os.path.dirname(os.path.realpath(__import__("__main__").__file__))
        config_path = "{}/config_templates/global_config/{}".format(main_dir, config_json)
        if not os.path.exists(config_path):
            raise RuntimeError('Required config json file %s not found' % config_path)
        flag_xenome = "yes" if flag_xenome == "True" or flag_xenome == "true" else "no"
        args_to_json = {"workflow": self.workflow,
                        "species": self.species,
                        "read_type": self.read_type,
                        "toolset": self.toolset,
                        "flag_xenome": flag_xenome,
                        "numthreads": cores_per_sample,
                        "genome_load": genome_load}

        with open(config_path) as file:
            data = json.load(file)
            self.java_path = data['java']
            data.update(args_to_json)
            if additional_options:
                data.update(additional_options)

        env = Environment(loader=FileSystemLoader("{}/config_templates/templates".format(main_dir)), trim_blocks=True,
                          lstrip_blocks=True)
        global_template = env.get_template(self.template)

        with open("global_template_{}.txt".format(self.workflow), "w") as f:
            f.write(global_template.render(data))
            f.close()
        return "{}/global_template_{}.txt".format(os.getcwd(), self.workflow)
