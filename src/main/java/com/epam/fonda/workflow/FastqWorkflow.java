/*
 * Copyright 2017-2019 Sanofi and EPAM Systems, Inc. (https://www.epam.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.fonda.workflow;

import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.samples.SampleBuilder;
import com.epam.fonda.samples.fastq.FastqFileSample;

import java.io.IOException;
import java.util.List;

/**
 * The <tt>FastqWorkflow</tt> interface provides method of processing data {@link FastqFileSample}.
 */
public interface FastqWorkflow extends Workflow<FastqFileSample> {

    /**
     * Provides list of {@link FastqFileSample} that was built {@link SampleBuilder}
     * @param configuration is the type of {@link Configuration} which contains global and study configuration
     * @return list of {@link FastqFileSample} that builds from configuration.
     * @throws IOException if an I/O error has occurred
     */
    @Override
    default List<FastqFileSample> provideSample(final Configuration configuration) throws IOException {
        return new SampleBuilder(configuration.getGlobalConfig(), configuration.getStudyConfig())
                .buildFastqSamples(configuration.getCommonOutdir().getRootOutdir());
    }
}
