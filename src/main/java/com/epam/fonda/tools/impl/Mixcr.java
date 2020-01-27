/*
 * Copyright 2017-2020 Sanofi and EPAM Systems, Inc. (https://www.epam.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.fonda.tools.impl;

import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.tools.Tool;
import com.epam.fonda.tools.results.MixcrResult;
import lombok.Data;
import org.thymeleaf.TemplateEngine;

public class Mixcr implements Tool<MixcrResult> {

    private static final String MIXCR_TOOL_TEMPLATE_NAME = "mixcr_tool_template";

    @Data
    private class MixcrFiels {
        private String mixcr;
        private String smixcrOutdir;
        private String sampleName;
        private String libraryType;
        private String species;
        private String fastq1;
        private String fastq2;
        private String mixcrAlignVdjca;
        private String mixcrContigVdjca;
        private String mixcrAssembly;
        private String mixcrClones;
        private String spe;
        private int nThreads;
    }

    @Override
    public MixcrResult generate(Configuration configuration, TemplateEngine templateEngine) {
        return null;
    }
}
