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
package com.epam.fonda;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

/**
 * Helper class to provide common functionality of integration tests
 */
public abstract class AbstractIntegrationTest {

    /**
     * @param outputDir a path to the directory need to be deleted
     * @param innerTest must be true if it is a path to the directory not located in build/resources/... folder
     * @throws IOException
     */
    public void cleanOutputDirForNextTest(String outputDir, boolean innerTest) throws IOException {
        Path dirToDelete = null;
        if (innerTest) {
            dirToDelete = Paths.get(outputDir);
        }
        try {
            Path dirResource = Paths.get(this.getClass().getClassLoader().getResource(outputDir).toURI());
            if (dirResource != null) {
                dirToDelete = Paths.get(dirResource.toUri());
            }
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }


        if (dirToDelete != null && dirToDelete.toFile().exists()) {
            Files.walk(dirToDelete, FileVisitOption.FOLLOW_LINKS)
                    .map(Path::toFile)
                    .sorted(Comparator.reverseOrder())
                    .forEach(File::delete);
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
}
