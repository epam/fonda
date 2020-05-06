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

package com.epam.fonda;

import com.epam.fonda.entity.configuration.Configuration;
import com.epam.fonda.entity.configuration.GlobalConfig;
import com.epam.fonda.entity.configuration.StudyConfig;
import com.epam.fonda.entity.configuration.CommonOutdir;
import com.epam.fonda.tools.impl.AbstractTest;
import com.epam.fonda.utils.PipelineUtils;
import org.apache.commons.cli.ParseException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.stream.Stream;

import static com.epam.fonda.utils.PipelineUtils.NA;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.params.provider.Arguments.of;

public class CmdParserTest extends AbstractTest {
    private static final CmdParser CMD_PARSER = new CmdParser();
    private static final String ROOT_OUTDIR = "output";
    private static final String TEST_FILE_NAME = format("/%s/file.fa", ROOT_OUTDIR);
    private static final String OUT_DIR = ROOT_OUTDIR + "/RNAexpression_Fastq_test";

    private static Configuration expectedConfigurationForDnaCaptureVar;
    private static Configuration expectedConfigurationWithAllPossibleFields;
    private static Configuration expectedConfigurationWithOneWrongField;
    private static GlobalConfig globalConfig;
    private static StudyConfig studyConfig;

    @Override
    public void deleteTestDirectory() {
        //no op
    }


    @BeforeAll
    static void initAll() {
        PipelineUtils.createDir(ROOT_OUTDIR);
        expectedConfigurationForDnaCaptureVar = createExpectedConfiguration(
                globalConfigForDnaCaptureVar(),
                studyConfigForDnaCaptureVar());
        expectedConfigurationWithAllPossibleFields = createExpectedConfiguration(
                globalConfigWithAllPossibleFields(),
                studyConfigWithAllPossibleFields());
        expectedConfigurationWithOneWrongField = createExpectedConfiguration(
                new GlobalConfig(),
                studyConfigWithAllPossibleFields());
    }

    @AfterAll
    static void cleanAll() throws IOException {
        FileUtils.deleteDirectory(new File(ROOT_OUTDIR));
    }

    @ParameterizedTest
    @ArgumentsSource(ArgumentsProviderForEqualsTests.class)
    void testParseArgsWithAssertEquals(final Configuration expectedConfiguration,
                                       final Configuration actualConfiguration) {
        assertEquals(expectedConfiguration, actualConfiguration);
    }

    @ParameterizedTest
    @ArgumentsSource(ArgumentsProviderForNotEqualsTests.class)
    void testParseArgsWithAssertNotEquals(final Configuration expectedConfiguration,
                                          final Configuration actualConfiguration) {
        assertNotEquals(expectedConfiguration, actualConfiguration);
    }

    static class ArgumentsProviderForEqualsTests implements ArgumentsProvider {
        final File globalConfigFile = getFile("global_config/global_config_DnaCaptureVar_Fastq_v1.1.txt");
        final File studyConfigFile = getFile("study_config/config_DnaCaptureVar_Fastq_test.txt");

        final File globalConfigFile2 = getFile("global_config/global_config_with_all_possible_fields.txt");
        final File studyConfigFile2 = getFile("study_config/study_config_with_all_possible_fields.txt");

        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) throws Exception {
            Configuration actualConfiguration = getActualConfiguration(globalConfigFile, studyConfigFile);
            Configuration actualConfiguration2 = getActualConfiguration(globalConfigFile2, studyConfigFile2);
            return Stream.of(
                    of(expectedConfigurationForDnaCaptureVar, actualConfiguration),
                    of(expectedConfigurationWithAllPossibleFields, actualConfiguration2));
        }

        private File getFile(final String path) {
            return new File(Objects.requireNonNull(getClass().getClassLoader().getResource(path)).getFile());
        }
    }

    static class ArgumentsProviderForNotEqualsTests implements ArgumentsProvider {
        final File globalConfigFile = getFile("global_config/global_config_with_one_wrong_field.txt");
        final File studyConfigFile = getFile("study_config/study_config_with_one_wrong_field.txt");

        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) throws Exception {
            Configuration actualConfiguration = getActualConfiguration(globalConfigFile, studyConfigFile);
            return Stream.of(
                    of(expectedConfigurationWithOneWrongField, actualConfiguration));
        }

        private File getFile(final String path) {
            return new File(Objects.requireNonNull(getClass().getClassLoader().getResource(path)).getFile());
        }
    }

    private static Configuration getActualConfiguration(final File globalConfigFile, final File studyConfigFile)
            throws ParseException, IOException {
        return CMD_PARSER.parseArgs(new String[]{
            "-global_config",
            globalConfigFile.getAbsolutePath(),
            "-study_config",
            studyConfigFile.getAbsolutePath(),
            "-test"});
    }

    private static Configuration createExpectedConfiguration(final GlobalConfig globalConfig,
                                                             final StudyConfig studyConfig) {
        globalConfig.getQueueParameters().setNumThreads(4);
        globalConfig.getQueueParameters().setMaxMem("24g");
        globalConfig.getQueueParameters().setQueue("all.q");
        globalConfig.getQueueParameters().setPe("-pe threaded");

        Configuration configuration = new Configuration();
        configuration.setGlobalConfig(globalConfig);
        configuration.setStudyConfig(studyConfig);
        CommonOutdir commonOutdir = new CommonOutdir(studyConfig.getDirOut());
        configuration.setCommonOutdir(commonOutdir);
        configuration.setSyncMode(false);
        configuration.setLocalMode(false);
        configuration.setTestMode(true);

        return configuration;
    }

    private static GlobalConfig globalConfigForDnaCaptureVar() {
        globalConfig = new GlobalConfig();

        globalConfig.getDatabaseConfig().setSpecies("human");
        globalConfig.getDatabaseConfig().setGenomeBuild("hg19");
        globalConfig.getDatabaseConfig().setGenome(format("/%s/hg19.decoy.fa", ROOT_OUTDIR));
        globalConfig.getDatabaseConfig().setNovoIndex(format("/%s/hg19.decoy.nix", ROOT_OUTDIR));
        globalConfig.getDatabaseConfig().setBed(format("/%s/test_data_padded.bed", ROOT_OUTDIR));
        globalConfig.getDatabaseConfig().setBedWithHeader(format("/%s/test_data_padded_decoy.txt", ROOT_OUTDIR));
        globalConfig.getDatabaseConfig().setBedForCoverage(format("/%s/test_data_padded_decoy.txt",
                ROOT_OUTDIR));
        globalConfig.getDatabaseConfig().setSnpsiftdb(format("/%s/db", ROOT_OUTDIR));
        globalConfig.getDatabaseConfig().setCanonicalTranscript(format("/%s/prefer_ensembl_transcript.txt",
                ROOT_OUTDIR));
        globalConfig.getDatabaseConfig().setAdapterFWD("AGATCGGAAGAGCACACGTCTGAFJFKJFNVJAKACTCCAGTCAC");
        globalConfig.getDatabaseConfig()
                .setAdapterREV("AGATCGGAAGAGAGAGAGAGGGAGCGTCGTGTAGGGAAAGAGTGTAGATCTCGGTGGTCGCCGTATCATT");

        // [all_tools]
        globalConfig.getToolConfig().setBedTools(format("/%s/bedtools", ROOT_OUTDIR));
        globalConfig.getToolConfig().setSeqpurge(format("/%s/SeqPurge", ROOT_OUTDIR));
        globalConfig.getToolConfig().setNovoalign(format("/%s/novoalign", ROOT_OUTDIR));
        globalConfig.getToolConfig().setJava(format("/%s/java", ROOT_OUTDIR));
        globalConfig.getToolConfig().setSamTools(format("/%s/samtools", ROOT_OUTDIR));
        globalConfig.getToolConfig().setPicardVersion("v2.10.3");
        globalConfig.getToolConfig().setPicard(format("/%s/picard.jar", ROOT_OUTDIR));
        globalConfig.getToolConfig().setSnpsift(format("/%s/SnpSift.jar", ROOT_OUTDIR));
        globalConfig.getToolConfig().setTransvar(format("/%s/transvar", ROOT_OUTDIR));
        globalConfig.getToolConfig().setVardict(format("/%s/v1.5.0", ROOT_OUTDIR));
        globalConfig.getToolConfig().setAbra2(format("/%s/abra2-2.07.jar", ROOT_OUTDIR));
        globalConfig.getToolConfig().setPython(format("/%s/python", ROOT_OUTDIR));
        globalConfig.getToolConfig().setRScript(format("/%s/Rscript", ROOT_OUTDIR));
        globalConfig.getToolConfig().setDoubleDetectionPython(format("/%s/python", ROOT_OUTDIR));

        // [Pipeline_Info]
        globalConfig.getPipelineInfo().setWorkflow("DnaCaptureVar_Fastq");
        globalConfig.getPipelineInfo().setToolset(new LinkedHashSet<>(
                Arrays.asList("seqpurge", "novoalign", "abra_realign", "picard", "qc", "vardict")));
        globalConfig.getPipelineInfo().setFlagXenome(false);
        globalConfig.getPipelineInfo().setReadType("paired");

        return globalConfig;
    }

    private static GlobalConfig globalConfigWithAllPossibleFields() {
        globalConfig = new GlobalConfig();

        // [Databases]
        globalConfig.getDatabaseConfig().setSpecies("human");
        globalConfig.getDatabaseConfig().setGenomeBuild("hg38");
        globalConfig.getDatabaseConfig().setGenome(TEST_FILE_NAME);
        globalConfig.getDatabaseConfig().setStarIndex(TEST_FILE_NAME);
        globalConfig.getDatabaseConfig().setNovoIndex(format("/%s/file2.fa", ROOT_OUTDIR));
        globalConfig.getDatabaseConfig().setBed(TEST_FILE_NAME);
        globalConfig.getDatabaseConfig().setBedWithHeader(TEST_FILE_NAME);
        globalConfig.getDatabaseConfig().setBedForCoverage(TEST_FILE_NAME);
        globalConfig.getDatabaseConfig().setKnownIndelsMills(format("/%s/hg19_decoy.vcf", ROOT_OUTDIR));
        globalConfig.getDatabaseConfig().setKnownIndelsPhase1(format("/%s/hg29_decoy.vcf", ROOT_OUTDIR));
        globalConfig.getDatabaseConfig().setDbsnp("/dbsnp_138.vcf");
        globalConfig.getDatabaseConfig().setAdapterFWD("AAAGAGATCGGAAGAG");
        globalConfig.getDatabaseConfig().setAdapterREV("AGATCGGAATGAGGAGCGTCGTGTAGGGAAAGAGT");
        globalConfig.getDatabaseConfig().setSnpsiftdb(format("/%ssnpEff/db", ROOT_OUTDIR));
        globalConfig.getDatabaseConfig().setCanonicalTranscript(format("/%s/prefer_ensembl_transcript.txt",
                ROOT_OUTDIR));
        globalConfig.getDatabaseConfig().setAnnotgene("/gencode.trxlevel1-3.gtf");
        globalConfig.getDatabaseConfig().setAnnotgenesaf(format("/%s.trxlevel1-3.saf", ROOT_OUTDIR));
        globalConfig.getDatabaseConfig().setTranscriptome(format("/%s.pc_transcripts.fa", ROOT_OUTDIR));
        globalConfig.getDatabaseConfig().setCosmic("/290514_decoy.vcf");
        globalConfig.getDatabaseConfig().setMutectNormalPanel("/mutect_normal_panel/vcf");
        globalConfig.getDatabaseConfig().setBedPrimer(format("/%s/primer.bed", ROOT_OUTDIR));
        globalConfig.getDatabaseConfig().setRRNABED(format("/%s/gencode.v26.rRNA.list", ROOT_OUTDIR));
        globalConfig.getDatabaseConfig().setStarFusionLib(format("/%s/STAR-Fusion-Lib", ROOT_OUTDIR));
        globalConfig.getDatabaseConfig().setBowtieIndex("/GRCh38/Index/Bowtie");

        // [all_tools]
        globalConfig.getToolConfig().setBedTools("/bedtools2/bedtools");
        globalConfig.getToolConfig().setStar(format("/%s/bin/Linux_x86_64/STAR", ROOT_OUTDIR));
        globalConfig.getToolConfig().setSeqpurge(format("/%s/SeqPurge", ROOT_OUTDIR));
        globalConfig.getToolConfig().setNovoalign(format("/%s/novoalign", ROOT_OUTDIR));
        globalConfig.getToolConfig().setJava(format("/%s/bin/java", ROOT_OUTDIR));
        globalConfig.getToolConfig().setSamTools(format("/%s/v0.1.19/samtools", ROOT_OUTDIR));
        globalConfig.getToolConfig().setPicardVersion("v2.10.3");
        globalConfig.getToolConfig().setPicard(format("/%s/v2.10.3/picard.jar", ROOT_OUTDIR));
        globalConfig.getToolConfig().setSnpsift(format("/%s/SnpSift.jar", ROOT_OUTDIR));
        globalConfig.getToolConfig().setTransvar(format("/%s/transvar", ROOT_OUTDIR));
        globalConfig.getToolConfig().setGatk(format("/%s/GenomeAnalysisTK.jar", ROOT_OUTDIR));
        globalConfig.getToolConfig().setPython(format("/%s/v2.7.2/bin/python", ROOT_OUTDIR));
        globalConfig.getToolConfig().setRScript(format("/%s/Rscript", ROOT_OUTDIR));
        globalConfig.getToolConfig().setVardict(format("/%s/VarDictJava/v1.5.0", ROOT_OUTDIR));
        globalConfig.getToolConfig().setAbra2(format("/%s/abra2-2.07.jar", ROOT_OUTDIR));
        globalConfig.getToolConfig().setDoubleDetectionPython(format("/%s/3.5.2/bin/python", ROOT_OUTDIR));
        globalConfig.getToolConfig().setBwa(format("/%s/bwa", ROOT_OUTDIR));
        globalConfig.getToolConfig().setRnaseqcJava(format("/%s/jre1.7.0_60/bin/java", ROOT_OUTDIR));
        globalConfig.getToolConfig().setMutectJava(format("/%s/bin/java", ROOT_OUTDIR));
        globalConfig.getToolConfig().setRnaseqc(format("/%s/RNA-SeQC_v1.1.8.jar", ROOT_OUTDIR));
        globalConfig.getToolConfig().setMutect(format("/%s/mutect-1.1.7.jar", ROOT_OUTDIR));
        globalConfig.getToolConfig().setScalpel(format("/%s/scalpel/v0.5.3", ROOT_OUTDIR));
        globalConfig.getToolConfig().setOptitype(format("/%s/OptiType/v1.2.1", ROOT_OUTDIR));
        globalConfig.getToolConfig().setCufflinks(format("/%s/cufflinks", ROOT_OUTDIR));
        globalConfig.getToolConfig().setFeatureCount(format("/%s/featureCounts", ROOT_OUTDIR));
        globalConfig.getToolConfig().setStarFusion(format("/%s/STAR-Fusion", ROOT_OUTDIR));
        globalConfig.getToolConfig().setCellranger(format("/%s/cellranger", ROOT_OUTDIR));
        globalConfig.getToolConfig().setBowtie2(format("/%s/v2.2.9/bowtie2", ROOT_OUTDIR));
        globalConfig.getToolConfig().setMixcr(format("/%s/MiXCR/v2.1.3/mixcr", ROOT_OUTDIR));

        // [cellranger]
        globalConfig.getCellrangerConfig().setCellrangerExpectedCells("5000");
        globalConfig.getCellrangerConfig().setCellrangerForcedCells(NA);
        globalConfig.getCellrangerConfig().setCellrangerNosecondary("FALSE");
        globalConfig.getCellrangerConfig().setCellrangerChemistry("auto");
        globalConfig.getCellrangerConfig().setCellrangerR1Length(NA);
        globalConfig.getCellrangerConfig().setCellrangerR2Length(NA);
        globalConfig.getCellrangerConfig().setCellrangerLanes(NA);
        globalConfig.getCellrangerConfig().setCellrangerIndices(NA);

        // [Pipeline_Info]
        globalConfig.getPipelineInfo().setWorkflow("DnaCaptureVar_Fastq");
        globalConfig.getPipelineInfo().setToolset(new LinkedHashSet<>(Arrays.asList("bwa", "picard", "qc")));
        globalConfig.getPipelineInfo().setFlagXenome(true);
        globalConfig.getPipelineInfo().setReadType("paired");

        return globalConfig;
    }

    private static StudyConfig studyConfigForDnaCaptureVar() {
        studyConfig = new StudyConfig();

        // StudyConfig
        // [Series_Info]
        studyConfig.setJobName("pe_job");
        studyConfig.setDirOut(format("%s/DnaCaptureVar_Fastq_test", ROOT_OUTDIR));
        studyConfig.setFastqList(format("/%s/DnaCaptureVar_WES_SampleFastqPaths.txt", ROOT_OUTDIR));
        studyConfig.setLibraryType("DNAWholeExomeSeq_Paired");
        studyConfig.setDataGenerationSource("Internal");
        studyConfig.setDate("20140318");
        studyConfig.setProject("Example_project");
        studyConfig.setRun("run1234");
        studyConfig.setCufflinksLibraryType("fr-unstranded");

        return studyConfig;
    }

    private static StudyConfig studyConfigWithAllPossibleFields() {
        studyConfig = new StudyConfig();

        // StudyConfig
        // [Series_Info]
        studyConfig.setJobName("pe_job");
        studyConfig.setDirOut(OUT_DIR);
        studyConfig.setFastqList(format("%s/RnaExpression_RNASeq_SampleFastqPaths.txt", ROOT_OUTDIR));
        studyConfig.setLibraryType("RNASeq_Single");
        studyConfig.setRnaSeqConfiguration("SingleEnd");
        studyConfig.setDataGenerationSource("Internal");
        studyConfig.setDate("20180314");
        studyConfig.setProject("Example_project");
        studyConfig.setRun("run1234");
        studyConfig.setCufflinksLibraryType("fr-unstranded");

        return studyConfig;
    }
}
