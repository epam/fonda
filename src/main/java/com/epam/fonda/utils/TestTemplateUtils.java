package com.epam.fonda.utils;

import com.epam.fonda.entity.configuration.orchestrator.MasterScript;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.context.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static java.lang.String.format;

public final class TestTemplateUtils {

    private static final String INDENT = "[ ]{4,}";

    private TestTemplateUtils() {
    }

    public static String trimNotImportant(final String str) {
        return str.trim()
                .replaceAll(INDENT, "")
                .replaceAll("\\r", "");
    }

    public static Context getContextForMaster(final Context context,
                                        final List<MasterScript.SampleScripts> samplesProcessScripts,
                                        final String postProcessScript) {
        context.setVariable("samplesProcessScripts", samplesProcessScripts);
        context.setVariable("postProcessScript", postProcessScript);
        return context;
    }

    public static List<MasterScript.SampleScripts> getSamplesScripts(final String expectedBaseScript,
                                                               final String expectedSecondScript,
                                                               final Integer numberOfScripts) {
        List<MasterScript.SampleScripts> alignmentScripts = new LinkedList<>();
        for (int i = 1; i <= numberOfScripts; i++) {
            List<String> baseScripts = new ArrayList<>();
            baseScripts.add(format(expectedBaseScript, i));
            List<String> secondaryScripts = new ArrayList<>();
            if (StringUtils.isNotBlank(expectedSecondScript)) {
                secondaryScripts.add(format(expectedSecondScript, i));
            }
            alignmentScripts.add(new MasterScript.SampleScripts(baseScripts, secondaryScripts));
        }
        return alignmentScripts;
    }

    public static List<MasterScript.SampleScripts> getSamplesScripts(final String[] expectedBaseScript,
                                                               final String[] expectedSecondScript) {
        List<MasterScript.SampleScripts> alignmentScripts = new LinkedList<>();
        alignmentScripts.add(new MasterScript.SampleScripts(
                Arrays.asList(expectedBaseScript),
                Arrays.asList(expectedSecondScript)
        ));
        return alignmentScripts;
    }
}
