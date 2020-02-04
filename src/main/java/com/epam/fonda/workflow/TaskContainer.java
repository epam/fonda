package com.epam.fonda.workflow;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public final class TaskContainer {

    private static TaskContainer container;
    private Set<String> tasks;

    private TaskContainer() {
        this.tasks = new LinkedHashSet<>();
    }

    public static Set<String> getTasks() {
        if (container == null) {
            container = new TaskContainer();
        }
        return container.tasks;
    }

    public static void addTasks(String... tasks) {
        getTasks().addAll(Arrays.asList(tasks));
    }
}
