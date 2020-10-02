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
from os.path import basename, normpath


def get_sample_name(f, sample_dir):
    last_folder = basename(normpath(sample_dir))
    if not sample_dir.strip():
        raise RuntimeError('Failed to extract directory path from %s fastq' % f)
    if len(last_folder.split('Sample_')) > 1 and \
            last_folder.split('Sample_')[1] in f:
        return last_folder.split('Sample_')[1]
    elif len(sample_dir.strip()) != 0 and len(f.split(sample_dir)) > 0:
        if last_folder in f.split(sample_dir)[1]:
            return last_folder
        sample_name = f.split(sample_dir)[1].split('_')[0]
        return sample_name[1:] if str(sample_name).startswith('/') else sample_name
    raise RuntimeError('Failed to extract sample name from %s fastq path' % f)
