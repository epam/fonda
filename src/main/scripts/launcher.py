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
import logging
import os
import subprocess
import sys
from pathlib import Path

LOG_FORMAT = "%(levelname)s: %(message)s"


class Launcher:
    FONDA_VERSION = "2.0.0"

    def __init__(self):
        pass

    @staticmethod
    def launch(global_config, study_config, sync, java_path, mode='', jar_folder=None, verbose=False):
        """
            Entry point to workflow launching
        """
        if verbose:
            logging.basicConfig(format=LOG_FORMAT, level=logging.DEBUG)
        else:
            logging.basicConfig(format=LOG_FORMAT)
        if not jar_folder:
            if os.environ.get('FONDA_HOME') is not None:
                jar_folder = os.environ['FONDA_HOME'] if str(jar_folder).endswith("/") \
                    else os.environ['FONDA_HOME'] + '/'
            else:
                jar_folder = "{}/build/libs/".format(Path(__file__).parent.parent.parent.parent.absolute())
                if not os.path.isfile('{}fonda-{}.jar'.format(jar_folder, Launcher.FONDA_VERSION)):
                    jar_folder = "{}/".format(Path(__file__).parent.parent.parent.absolute())
                    if not os.path.isfile('{}fonda-{}.jar'.format(jar_folder, Launcher.FONDA_VERSION)):
                        logging.error('Jar file was not found! Please put the jar file in a folder ' + jar_folder)
                        sys.exit(2)
        elif jar_folder is not None and not str(jar_folder).endswith("/"):
            jar_folder += "/"
        sync = '-sync' if sync is None or sync == 'true' else ''
        cmd = "{} -jar {}fonda-{}.jar -global_config {} -study_config {} {} {} > fonda_launch_out.txt" \
            .format(java_path, jar_folder, Launcher.FONDA_VERSION, global_config, study_config, sync, mode)
        proc = subprocess.Popen(cmd.split(), stdout=subprocess.PIPE, stderr=subprocess.PIPE)

        o, e = proc.communicate()

        exit_code = proc.wait()
        if exit_code != 0:
            exec_err_msg = 'Command \'%s\' execution has failed.\n Out: %s\n %s Exit code: %s' % \
                           (cmd, o.decode('ascii'), 'Err: %s.\n' % (e.decode('ascii')) if e else '',
                            exit_code)
            logging.error(exec_err_msg)
            raise RuntimeError(exec_err_msg)
        logging.debug('Output: ' + o.decode('ascii'))
        logging.debug('Exit code: ' + str(exit_code))
