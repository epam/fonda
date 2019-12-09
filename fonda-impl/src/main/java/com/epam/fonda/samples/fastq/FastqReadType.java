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

package com.epam.fonda.samples.fastq;

/**
 * The <tt>FastqReadType</tt> class represents the type of reads: single or paired-end
 */
public enum FastqReadType {
    PAIRED("paired"),
    SINGLE("single");

    private final String type;

    FastqReadType(final String type) {
        this.type = type;
    }

    /**
     * @return lowercase type of enum
     */
    public String getType() {
        return type;
    }

    public static boolean contains(final String readType) {
        for (FastqReadType type : FastqReadType.values()) {
            if (type.name().equalsIgnoreCase(readType)) {
                return true;
            }
        }
        return false;
    }
}
