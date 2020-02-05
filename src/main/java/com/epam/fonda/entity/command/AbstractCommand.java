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

package com.epam.fonda.entity.command;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The abstract <tt>AbstractCommand</tt> class represents tool command for launch
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
public abstract class AbstractCommand {

    private String toolCommand;
    private Set<String> tempDirs;

    AbstractCommand(final String toolCommand) {
        this.toolCommand = toolCommand;
        this.tempDirs = new LinkedHashSet<>();
    }

    /**
     * The method sets temporary file paths for deletion later
     * @param temps is paths to resources for deletion
     * @return {@link List} of temporary paths
     */
    public Set<String> setTempDirs(final List<String> temps) {
        for (String s:temps) {
            if (s != null)
                tempDirs.add(s);
        }
        return tempDirs;
    }
}
