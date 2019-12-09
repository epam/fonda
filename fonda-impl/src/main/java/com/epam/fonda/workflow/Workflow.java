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
import com.epam.fonda.samples.Sample;

import java.io.IOException;
import java.util.List;

/**
 * The <tt>Workflow</tt> interface provides default method of processing data from configuration.
 * @param <T> is the type of {@link Sample}
 */
public interface Workflow<T extends Sample> {

    /**
     * The method run workflow per sample and post analysis for all samples at the end.
     * @param configuration is the type of {@link Configuration} which contains the user specified parameters for tools
     * @throws IOException if an I/O error has occurred
     */
    default void process(Configuration configuration) throws IOException {
        List<T> samples = provideSample(configuration);
        samples.forEach(sample -> unsafe(() -> this.run(configuration, sample)));
        this.postProcess(configuration, samples);
    }

    List<T> provideSample(final Configuration config) throws IOException;

    void run(final Configuration configuration, final T sample) throws IOException;

    void postProcess(final Configuration configuration, final List<T> samples) throws IOException;

    interface UnsafeRunnable {
        void run() throws IOException;
    }

    static void unsafe(UnsafeRunnable r) {
        try {
            r.run();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
