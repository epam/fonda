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

package com.epam.fonda.utils;

import com.epam.fonda.OptionName;
import com.epam.fonda.tools.impl.AbstractTest;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationUtilsTest extends AbstractTest {

    private static final String PATH_TO_GLOBAL_CONFIG = "global_config/global_config_DnaCaptureVar_Fastq_v1.1.txt";
    private static final String PATH_TO_STUDY_CONFIG = "study_config/config_DnaCaptureVar_Fastq_test.txt";

    @Test
    void parseInputArgumentsToOptions() throws ParseException, IOException {
        final Options options = new Options();
        options.addOption(new Option(OptionName.HELP.getName(), false,
                MessageConstant.HELP_DESCRIPTION));
        options.addOption(new Option(OptionName.DETAIL.getName(), false,
                MessageConstant.DETAIL_DESCRIPTION));
        options.addOption(new Option(OptionName.TEST.getName(), false,
                MessageConstant.TEST_DESCRIPTION));
        options.addOption(new Option(OptionName.LOCAL.getName(), false,
                MessageConstant.LOCAL_DESCRIPTION));
        options.addOption(new Option(OptionName.STUDY_CONFIG.getName(), true,
                MessageConstant.STUDY_CONFIG_DESCRIPTION));
        options.addOption(new Option(OptionName.GLOBAL_CONFIG.getName(), true,
                MessageConstant.GLOBAL_CONFIG_DESCRIPTION));
        options.addOption(new Option(OptionName.SYNC.getName(), false,
                MessageConstant.SYNC_DESCRIPTION));
        final String[] arguments = new String[]{
            "-global_config", PATH_TO_GLOBAL_CONFIG,
            "-study_config", PATH_TO_STUDY_CONFIG,
            "-local",
            "sync",
            "-test"};
        final CommandLine expectedCommandLine = new DefaultParser().parse(options, arguments);
        final CommandLine actualCommandLine = ConfigurationUtils.parseInputArgumentsToOptions(arguments);

        assertEquals(expectedCommandLine.getArgList(), actualCommandLine.getArgList());
        assertArrayEquals(expectedCommandLine.getOptions(), actualCommandLine.getOptions());
        assertEquals(
                Arrays.toString(expectedCommandLine.getOptions()),
                Arrays.toString(actualCommandLine.getOptions()));
    }

    @Test
    void parseConfigFileLinesToMap() throws IOException {
        Map<String, String> globalConfigMap = ConfigurationUtils.parseConfigFileLinesToMap(
                getPath(PATH_TO_GLOBAL_CONFIG));

        assertEquals("-pe threaded", globalConfigMap.get("PE"));
        assertEquals("/output/bedtools", globalConfigMap.get("bedtools"));
        assertNull(globalConfigMap.get("[Queue_Parameters]"));
    }

    @Test
    void getProperties() throws IOException {
        assertTrue(ConfigurationUtils.getProperties().stringPropertyNames()
                .containsAll(Arrays.asList("rootOutdir", "shOutdir", "logOutdir", "errorOutdir")));
    }

    private String getPath(String path) {
        File file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource(path)).getFile());
        return Paths.get(file.getAbsolutePath()).toString();
    }
}
