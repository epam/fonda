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
package com.epam.fonda.tools.impl;

import com.epam.fonda.utils.PipelineUtils;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Data
public abstract class AbstractTest {
    protected static final String TEST_DIRECTORY = "output";

    @AfterEach
    public void deleteTestDirectory() throws IOException {
        FileUtils.deleteDirectory(new File(TEST_DIRECTORY));
        PipelineUtils.TASK_TO_CHECK.clear();
    }

    String readFile(Path path) throws IOException {
        byte[] fileBytes = Files.readAllBytes(path);
        return new String(fileBytes);
    }
}
