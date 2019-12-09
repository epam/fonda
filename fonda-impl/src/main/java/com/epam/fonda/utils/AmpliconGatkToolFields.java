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
import lombok.Data;

@Data
public class AmpliconGatkToolFields {

    private String java;
    private String gatk;

    /**
     * This method initializes fields of the ToolFields {@link AmpliconGatkToolFields} class.
     *
     * @param configuration is the type of {@link Configuration} which contains
     *                      its fields: java, gatk.
     * @return {@link AmpliconGatkToolFields} with its fields.
     **/
    public static AmpliconGatkToolFields init(final Configuration configuration) {
        AmpliconGatkToolFields fields = new AmpliconGatkToolFields();
        fields.setGatk(configuration.getGlobalConfig().getToolConfig().getGatk());
        fields.setJava(configuration.getGlobalConfig().getToolConfig().getJava());
        return fields;
    }
}
