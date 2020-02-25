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
package com.epam.fonda;

import com.epam.fonda.utils.TemplateEngineUtils;
import com.epam.fonda.workflow.TaskContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Objects;

import static com.epam.fonda.utils.PipelineUtils.getExecutionPath;

/**
 * Helper class to provide common functionality of integration tests
 */
public abstract class AbstractIntegrationTest {

    public static Context context;
    public static TemplateEngine templateEngine = TemplateEngineUtils.init();
    public static final String OUTPUT_DIR = "output/";

    @BeforeEach
    public void setUp() {
        context = new Context();
        context.setVariable("jarPath", getExecutionPath());
    }

    @AfterEach
    public void cleanUp() throws IOException {
        cleanOutputDirForNextTest(OUTPUT_DIR);
        TaskContainer.getTasks().clear();
    }

    /**
     * @param outputDir a path to the directory need to be deleted
     * @throws IOException
     */
    public void cleanOutputDirForNextTest(String outputDir) throws IOException {
        try {
            Path dirResource = Paths.get(
                    Objects.requireNonNull(this.getClass().getClassLoader().getResource(outputDir)).toURI());
            Path dirToDelete = Paths.get(dirResource.toUri());
            if (dirToDelete.toFile().exists()) {
                Files.walk(dirToDelete, FileVisitOption.FOLLOW_LINKS)
                        .map(Path::toFile)
                        .sorted(Comparator.reverseOrder())
                        .forEach(File::delete);
            }
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    /**
     * @param globalConfigName path to global config file located in integration test resources
     * @param studyConfigName  path to study config file located in integration test resources
     */
    public void startAppWithConfigs(String globalConfigName, String studyConfigName) {
        String globalConfig;
        String studyConfig;
        try {
            globalConfig = Paths.get(this.getClass().getClassLoader().getResource(globalConfigName).toURI()).toString();
            studyConfig = Paths.get(this.getClass().getClassLoader().getResource(studyConfigName).toURI()).toString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Cannot parse globalConfig or StudyConfig " + e);
        }
        String[] arg = new String[]{"-test", "-global_config", globalConfig, "-study_config", studyConfig};

        Main.main(arg);
    }

    /**
     * @param filePath path to a file
     * @return the template represented by {@code String}
     * @throws URISyntaxException if this URI cannot be converted to a URI
     * @throws IOException if an I/O error occurs reading from the stream
     */
    public String getCmd(final String filePath) throws URISyntaxException, IOException {
        Path path = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource(filePath)).toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        return new String(fileBytes);
    }
}
