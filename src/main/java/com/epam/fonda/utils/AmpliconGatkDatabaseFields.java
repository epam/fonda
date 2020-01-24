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

import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfigFormat;
import lombok.Builder;
import lombok.Data;

import static com.epam.fonda.utils.ToolUtils.validate;

@Data
@Builder
public class AmpliconGatkDatabaseFields {

    private String genome;
    private String knownIndelsMills;
    private String knownIndelsPhase1;
    private String dbSnp;

    /**
     * This method initializes fields of the DatabaseFields {@link AmpliconGatkDatabaseFields} class.
     *
     * @param configuration is the type of {@link Configuration} which contains
     *                      its fields: genome, knownIndelsMills, knownIndelsPhase1.
     * @return {@link AmpliconGatkDatabaseFields} with its fields.
     **/
    public static AmpliconGatkDatabaseFields init(final Configuration configuration) {
        return AmpliconGatkDatabaseFields.builder()
                .dbSnp(validate(configuration.getGlobalConfig().getDatabaseConfig().getDbsnp(),
                        GlobalConfigFormat.DBSNP))
                .genome(validate(configuration.getGlobalConfig().getDatabaseConfig().getGenome(),
                        GlobalConfigFormat.GENOME))
                .knownIndelsMills(validate(configuration.getGlobalConfig().getDatabaseConfig().getKnownIndelsMills(),
                        GlobalConfigFormat.KNOWN_INDELS_MILLS))
                .knownIndelsPhase1(validate(configuration.getGlobalConfig().getDatabaseConfig().getKnownIndelsPhase1(),
                        GlobalConfigFormat.KNOWN_INDELS_PHASE1))
                .build();
    }
}
