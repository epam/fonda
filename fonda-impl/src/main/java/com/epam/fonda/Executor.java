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

import com.epam.fonda.utils.PipelineUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * This class provides a methods to launch the generated shell scripts
 */
@Slf4j
public final class Executor {

    private Executor() {}

    /**
     * The list of Futures of results of all the running command line tasks
     */
    private static final List<Future<Integer>> RUNNING_TASKS = new ArrayList<>();

    /**
     * Execute a shell command in a separate watching thread, that waits for it's execution to finish and then returns
     * the exit code. That exit code is returned via Future of this watching thread.
     *
     * @param cmd a shell command to execute
     */
    public static void execute(String cmd) {
        FutureTask<Integer> task = new FutureTask<>(() -> {
            try {
                Process p = Runtime.getRuntime().exec(cmd);
                p.waitFor();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    reader.lines().forEach(l -> log.debug(cmd + ": " + l));
                }

                return p.exitValue();
            } catch (IOException e) {
                log.error("In executing the command: " + cmd + ": " + e.getMessage());
                return PipelineUtils.ERROR_STATUS;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return PipelineUtils.ERROR_STATUS;
            }
        });
        Thread t = new Thread(task);
        t.start();

        RUNNING_TASKS.add(task);
    }

    /**
     * Method is used to get executor result
     */
    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    public static Optional<Integer> getExecutorResult() {
        return Executor.RUNNING_TASKS.stream()
                .map(t -> {
                    try {
                        return t.get();
                    } catch(InterruptedException | ExecutionException e) {
                        Thread.currentThread().interrupt();
                        throw new CancellationException(e.getMessage());
                    }
                })
                .max(Comparator.naturalOrder());
    }
}
