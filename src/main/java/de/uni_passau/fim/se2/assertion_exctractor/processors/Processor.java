package de.uni_passau.fim.se2.assertion_exctractor.processors;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_passau.fim.se2.assertion_exctractor.converters.Raw2FineDataPStep;
import de.uni_passau.fim.se2.assertion_exctractor.converters.TrainSplitPreprocessingStep;
import de.uni_passau.fim.se2.assertion_exctractor.data.DataPoint;
import de.uni_passau.fim.se2.assertion_exctractor.data.FineMethodData;
import de.uni_passau.fim.se2.assertion_exctractor.loading.Method2TestLoader;
import de.uni_passau.fim.se2.assertion_exctractor.data.RawMethodData;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.code.CustomASTConverterPreprocessor;
import de.uni_passau.fim.se2.assertion_exctractor.utils.ErrorChecker;
import de.uni_passau.fim.se2.assertion_exctractor.utils.ProgressBarContainer;
import de.uni_passau.fim.se2.assertion_exctractor.utils.StatisticsContainer;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.transformer.util.TransformMode;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.CommonPreprocessorOptions;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.ProcessingException;
import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;

public abstract class Processor {

    private static final Logger LOG = LoggerFactory.getLogger(Processor.class);
    private final Raw2FineDataPStep raw2fineConverter = new Raw2FineDataPStep();
    private final TrainSplitPreprocessingStep orderDataset = new TrainSplitPreprocessingStep(80, 10, 10);
    protected final String dataDir;
    protected final String saveDir;
    protected final int maxAssertions;
    protected final CustomASTConverterPreprocessor preprocessor = new CustomASTConverterPreprocessor(
        SINGLE_METHOD_OPTIONS, true, false
    );

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
                .peek(el -> ProgressBarContainer.getInstance().notifyStep())
                .filter(this::isASTConvertible)
                .map(raw2fineConverter::process)
                .map(this::flatten)
                .flatMap(Optional::stream)
                .filter(this::isASTConvertibleAnyMore);
        }
        catch (IOException e) {
            LOG.error("Error while loading json dataset", e);
            throw new RuntimeException(e);
        }

    }

    private <A, B> Optional<Pair<A, B>> flatten(Pair<A, Optional<B>> pair) {
        Optional<B> optionalPart = pair.b();
        A firstPart = pair.a();
        return optionalPart.map(x -> Pair.of(firstPart, x));
    }

    private boolean isASTConvertible(Pair<String, RawMethodData> inputData) {
        RawMethodData dataPoint = inputData.b();

        boolean focalMethodParseable = isMethodParseable(dataPoint.focalMethod());
        boolean testMethodParseable = isMethodParseable(dataPoint.testMethod());
        boolean focalClassParseable = isClassParseable(dataPoint.focalFile());
        boolean testClassParseable = isClassParseable(dataPoint.testFile());
        boolean convertible = focalMethodParseable && testMethodParseable && focalClassParseable && testClassParseable;
        if (!convertible) {
            StatisticsContainer.getInstance().notifyNotParseable(
                !focalMethodParseable, !testMethodParseable, !focalClassParseable, !testClassParseable
            );
            ErrorChecker.getInstance().currentInstance(inputData.a());
        }

        return convertible;
    }

    private boolean isASTConvertibleAnyMore(Pair<String, FineMethodData> inputData) {
        FineMethodData dataPoint = inputData.b();

        boolean focalMethodParseable = isMethodParseable(String.join(" ", dataPoint.focalMethodTokens()));
        boolean testMethodParseable = isMethodParseable(dataPoint.testCase().toString());

        boolean convertible = focalMethodParseable && testMethodParseable;
        if (!convertible) {
            StatisticsContainer.getInstance().notifyNotParseableAfter(!focalMethodParseable, !testMethodParseable);
            ErrorChecker.getInstance().currentInstance(inputData.a());
        }
        else {
            StatisticsContainer.getInstance().notifyParsedTestCase();
        }
        return convertible;
    }

    private boolean isMethodParseable(String methodCode) {
        try {
            return preprocessor.processSingleMethod(methodCode).isPresent();
        }
        catch (ProcessingException e) {
            return false;
        }
    }

    private boolean isClassParseable(String methodCode) {
        try {
            return preprocessor.parseSingleClass(methodCode).isPresent();
        }
        catch (ProcessingException e) {
            return false;
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
            }
            else {
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
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
