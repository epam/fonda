/*
 * Copyright 2017-2020 Sanofi and EPAM Systems, Inc. (https://www.epam.com/)
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

@Slf4j
public final class ConfigurationUtils {

    private static final String PATH_TO_FONDA_DETAILS = "fonda_details.txt";

    private ConfigurationUtils() {
    }

    /**
     * @param arg the arguments for parsing
     * @return the {@link CommandLine} with parsed arguments from arg
     * @throws ParseException if there are any problems encountered while parsing the command line tokens.
     * @throws IOException    if an I/O error occurs opening the file
     */
    public static CommandLine parseInputArgumentsToOptions(final String[] arg)
            throws ParseException, IOException {
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
        options.addOption(new Option(OptionName.MASTER.getName(), false,
                MessageConstant.MASTER_DESCRIPTION));
        final CommandLine commandLine = new DefaultParser().parse(options, arg);
        printInfo(commandLine, options, new HelpFormatter());
        return commandLine;
    }

    /**
     * @param configFilePath the path to config file
     * @return the {@link java.util.Map} with parsed parameters and values from config file
     * @throws IOException if an I/O error occurs opening the file
     */
    public static Map<String, String> parseConfigFileLinesToMap(final String configFilePath) throws IOException {
        try(Stream<String> file = Files.lines(Paths.get(configFilePath))) {
            return file.filter(ConfigurationUtils::isValidLine)
                    .map(ConfigurationUtils::parseConfigLine)
                    .collect(toMap(Pair::getKey, Pair::getValue, (p1, p2) -> p2));
        }
    }

    public static Properties getProperties() throws IOException {
        try (final InputStream input = ConfigurationUtils.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            final Properties properties = new Properties();
            properties.load(input);
            return properties;
        }
    }

    private static boolean isValidLine(final String line) {
        return line.length() > 0
                && !line.startsWith("#")
                && !line.startsWith("[")
                && !line.endsWith("]");
    }

    private static Pair<String, String> parseConfigLine(final String line) {
        final String[] chunks = line.split("=");
        final String key = chunks[0].trim();
        final String value = chunks.length == 2 ? getValue(key, chunks[1].trim()) : StringUtils.EMPTY;
        return new ImmutablePair<>(key, value);
    }

    private static String getValue(final String key, final String value) {
        return key.equals("PE") ? value : value.replaceAll(" ", "_");
    }

    private static void printInfo(final CommandLine commandLine, final Options options, final HelpFormatter formatter)
            throws IOException {

        if (commandLine.hasOption(OptionName.DETAIL.getName())) {
            final String details =
                    Files.lines(Paths.get(getFile(PATH_TO_FONDA_DETAILS).getAbsolutePath())).collect(joining("\n"));
            log.info(details);
            System.exit(1);
        } else if (commandLine.hasOption(OptionName.HELP.getName())
                || !(commandLine.hasOption(OptionName.STUDY_CONFIG.getName())
                && commandLine.hasOption(OptionName.GLOBAL_CONFIG.getName()))) {
            formatter.printHelp(MessageConstant.HEADER, MessageConstant.VERSION_MESSAGE, options, "");
            System.exit(1);
        }
    }

    private static File getFile(final String path) {
        return new File(Objects.requireNonNull(ConfigurationUtils.class.getClassLoader().getResource(path)).getFile());
    }
}
