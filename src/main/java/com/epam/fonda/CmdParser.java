/*
 * Copyright 2017-2021 Sanofi and EPAM Systems, Inc. (https://www.epam.com/)
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

package com.epam.fonda;

import com.beust.jcommander.JCommander;
import com.epam.fonda.entity.configuration.CommonOutdir;
import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.entity.configuration.StudyConfig;
import com.epam.fonda.utils.ConfigurationUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Reads command line arguments and parse required configurations files
 * fields to {@link Configuration} class fields.
 */
@Slf4j
public class CmdParser {

    /**
     * This method parse command line arguments to {@link Configuration} class fields.
     *
     * @param arg The input command line arguments with {@link GlobalConfig} and {@link Configuration}
     *            files paths and required  mode (Test, sync or local)
     * @return {@link Configuration} object with all filed fields which exists in {@link GlobalConfig}
     * and {@link StudyConfig} files
     * @throws ParseException thrown during parsing of a command-line
     * @throws IOException    if an I/O error occurs opening the file
     */
    public Configuration parseArgs(final String[] arg) throws ParseException, IOException {
        final CommandLine commandLine = ConfigurationUtils.parseInputArgumentsToOptions(arg);
        final StudyConfig studyConfig = new StudyConfig();
        final GlobalConfig globalConfig = new GlobalConfig();
        final Map<String, String> studyConfigMap = ConfigurationUtils.parseConfigFileLinesToMap(
                commandLine.getOptionValue(OptionName.STUDY_CONFIG.getName()));
        final Map<String, String> globalConfigMap = ConfigurationUtils.parseConfigFileLinesToMap(
                commandLine.getOptionValue(OptionName.GLOBAL_CONFIG.getName()));

        parseFieldsFromMapToConfigClass(studyConfigMap, studyConfig);
        parseFieldsFromMapToConfigClass(globalConfigMap, new Object[] {
                globalConfig.getQueueParameters(),
                globalConfig.getPipelineInfo(),
                globalConfig.getToolConfig(),
                globalConfig.getDatabaseConfig(),
                globalConfig.getCellrangerConfig()
        });

        final Properties properties = ConfigurationUtils.getProperties();
        final String rootOutdir = studyConfig.getDirOut();
        final CommonOutdir commonOutdir = new CommonOutdir(rootOutdir,
                properties.getProperty("shOutdir"),
                properties.getProperty("logOutdir"),
                properties.getProperty("errorOutdir"));

        commonOutdir.createDirectory();
        return buildConfiguration(commandLine, globalConfig, studyConfig, commonOutdir);
    }

    private void parseFieldsFromMapToConfigClass(final Map<String, String> configMap, final Object config) {
        JCommander.newBuilder()
                .addObject(config)
                .build()
                .parse(mapToStringArray(configMap));
    }

    private String[] mapToStringArray(final Map<String, String> map) {
        return map.entrySet()
                .stream()
                .flatMap(e -> Stream.of(e.getKey(), e.getValue()))
                .toArray(String[]::new);
    }

    private Configuration buildConfiguration(final CommandLine cmd,
                                             final GlobalConfig globalConfig,
                                             final StudyConfig studyConfig,
                                             final CommonOutdir commonOutdir) {
        Configuration configuration = new Configuration();
        configuration.setStudyConfig(studyConfig);
        configuration.setGlobalConfig(globalConfig);
        configuration.setTestMode(cmd.hasOption(OptionName.TEST.getName()));
        configuration.setLocalMode(cmd.hasOption(OptionName.LOCAL.getName()));
        configuration.setSyncMode(cmd.hasOption(OptionName.SYNC.getName()));
        configuration.setMasterMode(cmd.hasOption(OptionName.MASTER.getName()));
        configuration.setCommonOutdir(commonOutdir);
        return configuration;
    }
}
