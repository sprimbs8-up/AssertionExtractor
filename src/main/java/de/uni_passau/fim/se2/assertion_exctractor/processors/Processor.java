package de.uni_passau.fim.se2.assertion_exctractor.processors;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import de.uni_passau.fim.se2.assertion_exctractor.parsing.code.CustomAstCodeParser;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.code.CustomCodeParser;
import de.uni_passau.fim.se2.assertion_exctractor.utils.ErrorChecker;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.AstNode;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.declaration.MemberDeclarator;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.declaration.MethodDeclaration;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.parser.AstCodeParser;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.transformer.util.TransformMode;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.CommonPreprocessorOptions;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.ProcessingException;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.ast_conversion.AstConverterPreprocessor;
import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_passau.fim.se2.assertion_exctractor.converters.MoveToDatapointPStep;
import de.uni_passau.fim.se2.assertion_exctractor.converters.Raw2FineDataPStep;
import de.uni_passau.fim.se2.assertion_exctractor.data.DataPoint;
import de.uni_passau.fim.se2.assertion_exctractor.data.FineMethodData;
import de.uni_passau.fim.se2.assertion_exctractor.data.Method2TestLoader;
import de.uni_passau.fim.se2.assertion_exctractor.utils.ProgressBarContainer;
import de.uni_passau.fim.se2.assertion_exctractor.utils.StatisticsContainer;

public abstract class Processor {

    private static final Logger LOG = LoggerFactory.getLogger(Processor.class);
    private final Raw2FineDataPStep raw2fineConverter = new Raw2FineDataPStep();
    private final MoveToDatapointPStep orderDataset = new MoveToDatapointPStep(80, 10, 10);
    protected final String dataDir;
    protected final String saveDir;
    protected final int maxAssertions;
    private final AstConverterPreprocessor preprocessor = new CustomAstConverterPreprocessor(SINGLE_METHOD_OPTIONS, true, false);


    protected static final CommonPreprocessorOptions SINGLE_METHOD_OPTIONS = new CommonPreprocessorOptions(
            null, null, false, new TransformMode.None()
    );

    public Processor(final String dataDir, final String saveDir, final int maxAssertions) {
        this.dataDir = dataDir;
        this.saveDir = saveDir;
        this.maxAssertions = maxAssertions;
    }

    protected Stream<Pair<String, FineMethodData>> loadMethodData() {
        try {
            return Method2TestLoader.loadDatasetAsJSON(dataDir)
                    .map(raw2fineConverter::process)
                    .map(this::flatten)
                    .flatMap(Optional::stream)
                    .filter(this::isASTConvertible)
                    .peek(el -> ProgressBarContainer.getInstance().notifyStep());
        } catch (IOException e) {
            LOG.error("Error while loading json dataset", e);
            throw new RuntimeException(e);
        }

    }

    private <A, B> Optional<Pair<A, B>> flatten(Pair<A, Optional<B>> pair) {
        Optional<B> optionalPart = pair.b();
        A firstPart = pair.a();
        return optionalPart.map(x -> Pair.of(firstPart, x));
    }

    private boolean isASTConvertible(Pair<String, FineMethodData> fineMethodDataPair) {
        FineMethodData fineMethodData = fineMethodDataPair.b();
        boolean convertible = parseMethod(fineMethodData.testCase().toString()).isPresent()
                && parseMethod(String.join(" ", fineMethodData.focalMethodTokens())).isPresent();
        if (!convertible) {
            // LOG.info("Excluded non convertible tokens: {}", fineMethodData.testCase().toString().replaceAll("\\s"," "));
            StatisticsContainer.getInstance().notifyNotParseable();
            ErrorChecker.getInstance().currentInstance(fineMethodDataPair.a());
        } else {
            StatisticsContainer.getInstance().notifyParsedTestCase();
        }
        return convertible;
    }

    private Optional<String> parseMethod(String methodCode) {
        try {
            return preprocessor.processSingleMethod(methodCode);
        } catch (ProcessingException e) {
            return Optional.empty();
        }
    }

    protected abstract String getModelName();

    public void exportProcessedExamples() {
        setup();
        loadMethodData()
                .filter(data -> data.b().testCase().getNumberAssertions() <= maxAssertions)
                .filter(data -> data.b().testCase().getNumberAssertions() >= 1)
                .peek(x -> StatisticsContainer.getInstance().notifyTestCase())
                .map(orderDataset::process)
                .forEach(this::exportTestCases);
        ProgressBarContainer.getInstance().notifyStop();
        StatisticsContainer.getInstance().logPreprocessingStats();
        shutDown();
    }

    protected void setup() {
        Path savePath = Path.of(saveDir);
        if (!savePath.toFile().exists()) {
            boolean successfullyCreated = savePath.toFile().mkdirs();
            if (successfullyCreated) {
                LOG.info(String.format("Created %s path successfully.", savePath));
            } else {
                LOG.warn(String.format("Creating %s path did not work successfully.", savePath));

            }
        }
    }

    protected void shutDown() {
    }

    protected abstract void exportTestCases(Pair<String, DataPoint> dataPoint);

    protected void writeStringsToFile(String file, AtomicBoolean append, String tokens) {
        File savePath = Path.of(saveDir, getModelName(), file).toFile();
        if (!savePath.exists()) {

            savePath.getParentFile().mkdirs();

        }
        ;
        try (FileWriter writer = new FileWriter(savePath, append.get())) {
            writer.write(tokens + System.getProperty("line.separator"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class CustomAstConverterPreprocessor extends AstConverterPreprocessor {

        public CustomAstConverterPreprocessor(CommonPreprocessorOptions commonOptions, boolean singleMethod, boolean dotGraph) {
            super(commonOptions, singleMethod, dotGraph);
        }

        @Override
        public Optional<String> processSingleMethod(String code) {
            return this.processSingleElement(code, true).map(Object::toString).findFirst();
        }

        @Override
        protected Stream<AstNode> processSingleElement(String code, boolean singleMethod) throws ProcessingException {
            AstCodeParser codeParser = new CustomAstCodeParser();
            Stream<MemberDeclarator<MethodDeclaration>> stream = codeParser.parseMethodSkipErrors(code).stream();
            Objects.requireNonNull(AstNode.class);
            return stream.map(AstNode.class::cast);
        }
    }
}
