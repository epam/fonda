package com.epam.fonda.utils;

public final class TestTemplateUtils {

    private static final String INDENT = "[ ]{4,}";

    private TestTemplateUtils() {
    }

    public static String trimNotImportant(final String str) {
        return str.trim()
                .replaceAll(INDENT, "")
                .replaceAll("\\r", "");
    }
}