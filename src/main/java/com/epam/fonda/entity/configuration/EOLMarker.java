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
package com.epam.fonda.entity.configuration;

import lombok.Getter;

@Getter
public enum EOLMarker {

    /**
     * Unix-style end-of-line marker ("\n")
     */
    LF("\n"),

    /**
     * Windows-style end-of-line marker ("\r\n")
     */
    CRLF("\r\n");

    private final String lineSeparator;

    EOLMarker(final String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }
}
