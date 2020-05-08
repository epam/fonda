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

import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfigFormat;
import org.apache.commons.lang3.Validate;

public final class ToolUtils {

    public static final String BAM = "Bam";
    public static final String CONTROL_BAM = "controlBam";
    public static final String SAMPLE_NAME = "sample_name";
    public static final String CONTROL_SAMPLE_NAME = "control_sample_name";
    private static final double THE_LAST_OLD_PICARD_VERSION = 1.123;

    private ToolUtils() {
        // no-op
    }

    /**
     * Validates tool command line argument that should be passed though input configs
     * @param field the field that should be checked
     * @param fieldName the field name for error message
     * @return checked field
     */
    public static String validate(final String field, final String fieldName) {
        Validate.notBlank(field, String.format("Argument '%s' is required", fieldName));
        return field;
    }

    /**
     * Validates the picard version
     * @param configuration is the type of {@link Configuration} which contains tool configs
     * @return true if this is old picard version
     */
    public static boolean validateOldPicardVersion(final Configuration configuration) {
        final String validatedPicardVersion = validate(
                configuration.getGlobalConfig().getToolConfig().getPicardVersion(), GlobalConfigFormat.PICARD_VERSION);
        final String picardVersion = validatedPicardVersion.startsWith("v")
                ? validatedPicardVersion.split("v")[1]
                : validatedPicardVersion;
        return Double.parseDouble(picardVersion.split("\\.").length < 3
                ? picardVersion
                : picardVersion.substring(0, picardVersion.lastIndexOf('.'))) <= THE_LAST_OLD_PICARD_VERSION;
    }
}
