package com.epam.fonda;

import com.epam.fonda.utils.TemplateEngineUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HlaTypingFastqIntegrationTest extends AbstractIntegrationTest {

    private static final String OUTPUT_DIR = "output";
    private static final String G_CONFIG_PATH = "HlaTypingFastq/gHlaTypingFastq_v1.1.txt";
    private static final String S_CONFIG_PATH = "HlaTypingFastq/sHlaTypingFastq.txt";
    private static final String TEMPLATE_OUTPUT_SMV1_SUFFIX = "HlaTyping_Fastq_hlatyping_for_smv1_analysis";
    private static final String TEMPLATE_OUTPUT_SMV2_SUFFIX = "HlaTyping_Fastq_hlatyping_for_smv2_analysis";
    private static final String TEMPLATE_OUTPUT_SMV3_SUFFIX = "HlaTyping_Fastq_hlatyping_for_smv3_analysis";
    private static final String TEMPLATE_OUTPUT_SMV4_SUFFIX = "HlaTyping_Fastq_hlatyping_for_smv4_analysis";
    private TemplateEngine templateEngine = TemplateEngineUtils.init();
    private Context context = new Context();

    @AfterEach
    public void cleanupDir() throws IOException {
        cleanOutputDirForNextTest(OUTPUT_DIR, false);
    }

    @ParameterizedTest(name = "{1}-test")
    @MethodSource("initParameters")
    void testHlaTypingFastq(final String outputShFile, final String templatePath)
            throws IOException, URISyntaxException {
        startAppWithConfigs(G_CONFIG_PATH, S_CONFIG_PATH);
        final String expectedCmd = templateEngine.process(templatePath, context);
        assertEquals(expectedCmd.trim(), getCmd(outputShFile).trim());
    }

    @SuppressWarnings("PMD")
    private static Stream<Arguments> initParameters() {
        return Stream.of(
                Arguments.of(
                        RNA_EXPRESSION_FASTQ_G_FLAG_XENOME_YES,
                        OUTPUT_SH_FILE,
                        RNA_EXPRESSION_FASTQ_ALIGNMENT_FOR_SMV1_ANALYSIS_TEMPLATE_PATH));
    }

}
