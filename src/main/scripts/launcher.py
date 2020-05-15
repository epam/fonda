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

import subprocess
from pathlib import Path


class Launcher:

    FONDA_VERSION = "2.0.0"

    def __init__(self):
        pass

    @staticmethod
    def launch(global_config, study_config, mode=None, jar_folder=None):
        """
            Entry point to workflow launching
        """
        if not jar_folder:
            jar_folder = "{}/build/libs/".format(Path(__file__).parent.parent.parent.parent.absolute())
        elif jar_folder is not None and not str(jar_folder).endswith("/"):
            jar_folder += "/"
        cmd = "java -jar {}fonda-{}.jar -global_config {} -study_config {} {} > fonda_launch_out.txt"\
            .format(jar_folder, Launcher.FONDA_VERSION, global_config, study_config, mode)
        proc = subprocess.Popen(cmd.split(), stdout=subprocess.PIPE, stderr=subprocess.PIPE)

        o, e = proc.communicate()
        print('Output: ' + o.decode('ascii'))
        print('Error: ' + e.decode('ascii'))
        print('code: ' + str(proc.returncode))
