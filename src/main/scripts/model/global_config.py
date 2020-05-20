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
        self.genome_build = None

    def create(self, config_json, additional_options, flag_xenome=False):
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

        if self.species == "human":
            self.genome_build = "GRCh38"
        else:
            self.genome_build = "mm10"
        flag_xenome = "yes" if flag_xenome else "no"
        args_to_json = {"workflow": self.workflow,
                        "species": self.species,
                        "genome_build": self.genome_build,
                        "read_type": self.read_type,
                        "toolset": self.toolset,
                        "flag_xenome": flag_xenome}

        with open("{}/config_templates/global_config/{}".format(os.getcwd(), config_json)) as file:
            data = json.load(file)
            data.update(args_to_json)
            if additional_options:
                data.update(additional_options)

        env = Environment(loader=FileSystemLoader("config_templates/templates"), trim_blocks=True, lstrip_blocks=True)
        global_template = env.get_template(self.template)

        with open("global_template_{}.txt".format(self.workflow), "w") as f:
            f.write(global_template.render(data))

        return "{}/global_template_{}.txt".format(os.getcwd(), self.workflow)
