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

package com.epam.fonda.tools;

import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.tools.results.Result;
import org.thymeleaf.TemplateEngine;

/**
 * The <tt>Tool</tt> interface represents a tool entity for launch at workflow run process
 * @param <R> is the type of {@link Result}
 */
public interface Tool<R extends Result> {

    R generate(final Configuration configuration, final TemplateEngine templateEngine);
}
