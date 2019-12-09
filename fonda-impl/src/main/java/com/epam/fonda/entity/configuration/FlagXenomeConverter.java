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

package com.epam.fonda.entity.configuration;

import com.beust.jcommander.IStringConverter;

/**
 * The <tt>FlagXenomeConverter</tt> class represents converter for xenome parameter from global config
 */
public class FlagXenomeConverter implements IStringConverter<Boolean> {

    @Override
    public Boolean convert(String value) {
        if (value.equalsIgnoreCase("yes")) {
            return true;
        } else if (value.equalsIgnoreCase("no")) {
            return false;
        }
        throw new IllegalArgumentException("Wrong xenome flag. Should be \"yes\" or \"no\".");
    }
}
