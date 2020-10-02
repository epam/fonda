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
from os import listdir
from os.path import isfile, join

from model.sample_manifest import SampleManifest

EXTENSION = 'fastq.gz'


class FastqSampleManifest(SampleManifest):

    def __init__(self, read_type):
        super().__init__(read_type, "fastqFile")

    def create_by_folder(self, sample_dir, workflow_name, library_type):
        r1_gz_files = [f for f in listdir(sample_dir) if isfile(join(sample_dir, f)) and (EXTENSION and 'R1' in f)]
        return self.write_from_dir(EXTENSION, sample_dir, r1_gz_files, workflow_name, library_type)

    def create_by_list(self, fastq_list_r1, fastq_list_r2, workflow_name, library_type, sample_libtype=None,
                       sample_master=None):
        return self.write_from_list(EXTENSION, fastq_list_r1, fastq_list_r2, workflow_name, library_type,
                                    sample_libtype=sample_libtype, sample_master=sample_master)
