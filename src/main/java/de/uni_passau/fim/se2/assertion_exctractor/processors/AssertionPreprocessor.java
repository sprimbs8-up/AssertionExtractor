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
import de.uni_passau.fim.se2.assertion_exctractor.data.RawMethodData;
import de.uni_passau.fim.se2.assertion_exctractor.loading.Method2TestLoader;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.code.CustomASTConverterPreprocessor;
import de.uni_passau.fim.se2.assertion_exctractor.utils.ErrorChecker;
import de.uni_passau.fim.se2.assertion_exctractor.utils.ProgressBarContainer;
import de.uni_passau.fim.se2.assertion_exctractor.utils.StatisticsContainer;
import de.uni_passau.fim.se2.assertion_exctractor.utils.Utils;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.ProcessingException;
import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;

/**
 * The AssertionPreprocessor abstract class is responsible for preprocessing and exporting examples for model training.
 * Subclasses need to implement specific methods for loading, processing, and exporting the data.
 *
 */
public abstract class AssertionPreprocessor {

    private static final Logger LOG = LoggerFactory.getLogger(AssertionPreprocessor.class);
    private final Raw2FineDataPStep raw2fineConverter = new Raw2FineDataPStep();
    private final TrainSplitPreprocessingStep orderDataset = new TrainSplitPreprocessingStep(80, 10, 10);
    protected final String dataDir;
    protected final String saveDir;
    protected final int maxAssertions;
    protected final CustomASTConverterPreprocessor preprocessor = new CustomASTConverterPreprocessor(
        Utils.SINGLE_METHOD_OPTIONS, true, false
    );

    /**
     * Constructs an AssertionPreprocessor with the specified parameters.
     *
     * @param dataDir       The directory containing the raw data.
     * @param saveDir       The directory where the processed data will be saved.
     * @param maxAssertions The maximum number of assertions allowed in a test case.
     */
    public AssertionPreprocessor(final String dataDir, final String saveDir, final int maxAssertions) {
        this.dataDir = dataDir;
        this.saveDir = saveDir;
        this.maxAssertions = maxAssertions;
    }

    /**
     * Loads the raw method data, processes it, and returns a stream of pairs containing string identifiers and the
     * corresponding fine-grained method data.
     *
     * @return A stream of pairs containing string identifiers and fine-grained method data.
     */
    protected Stream<Pair<String, FineMethodData>> loadMethodData() {
        try {
            return Method2TestLoader.loadDatasetAsJSON(dataDir)
                .peek(el -> ProgressBarContainer.getInstance().notifyStep())
                .filter(this::areRawMethodDataASTConvertible)
                .map(raw2fineConverter::process)
                .map(Utils::flatten)
                .flatMap(Optional::stream)
                .filter(this::areFineMethodDataASTConvertible);
        }
        catch (IOException e) {
            LOG.error("Error while loading json dataset", e);
            throw new RuntimeException(e);
        }

    }

    /**
     * Retrieves the name of the model.
     *
     * @return A {@link String} representing the model name.
     */
    protected abstract String getModelName();

    /**
     * Exports processed examples by performing the necessary setup, loading method data, filtering, processing, and
     * exporting test cases. Also, notifies relevant containers and logs preprocessing statistics.
     */
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

    /**
     * Performs necessary setup, including creating the save directory if it does not exist.
     */
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

    /**
     * Performs any necessary shutdown operations.
     */
    protected void shutDown() {
    }

    /**
     * Exports test cases based on the provided data point.
     *
     * @param dataPoint The data point containing the processed test case.
     */
    protected abstract void exportTestCases(Pair<String, DataPoint> dataPoint);

    /**
     * Writes the given string tokens to a file with the specified filename in the save directory.
     *
     * @param file   The filename to write the tokens to.
     * @param append An {@code AtomicBoolean} indicating whether to append to an existing file.
     * @param tokens The string tokens to write to the file.
     */
    protected void writeStringsToFile(String file, AtomicBoolean append, String tokens) {
        File savePath = Path.of(saveDir, getModelName(), file).toFile();
        if (!savePath.exists()) {
            savePath.getParentFile().mkdirs();
        }
        try (FileWriter writer = new FileWriter(savePath, append.get())) {
            writer.write(tokens + System.getProperty("line.separator"));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if the raw method data contained in the provided {@link Pair} is convertible to Abstract Syntax Trees
     * (AST).
     *
     * @param inputData The Pair containing the class file path and corresponding RawMethodData.
     * @return {@code true} if the raw method data is convertible to AST for both focal and test methods and classes,
     *         {@code false} otherwise.
     */
    private boolean areRawMethodDataASTConvertible(Pair<String, RawMethodData> inputData) {
        RawMethodData dataPoint = inputData.b();

        // Check the AST convertibility of focal and test methods and classes
        boolean focalMethodParseable = isMethodParseable(dataPoint.focalMethod());
        boolean testMethodParseable = isMethodParseable(dataPoint.testMethod());
        boolean focalClassParseable = isClassParseable(dataPoint.focalFile());
        boolean testClassParseable = isClassParseable(dataPoint.testFile());

        // Check if all components are convertible to AST
        boolean convertible = focalMethodParseable && testMethodParseable && focalClassParseable && testClassParseable;

        // If not convertible, notify statistics and handle errors
        if (!convertible) {
            StatisticsContainer.getInstance().notifyNotParseable(
                !focalMethodParseable, !testMethodParseable, !focalClassParseable, !testClassParseable
            );
            ErrorChecker.getInstance().currentInstance(inputData.a());
        }

        return convertible;
    }

    /**
     * Checks if the fine-grained method data contained in the provided {@link Pair} is convertible to Abstract Syntax
     * Trees (AST).
     *
     * @param inputData The Pair containing the class file path and corresponding FineMethodData.
     * @return {@code true} if the fine-grained method data is convertible to AST for both focal method and test case,
     *         {@code false} otherwise.
     */
    private boolean areFineMethodDataASTConvertible(Pair<String, FineMethodData> inputData) {
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

    /**
     * Checks if the given method code is parseable by the preprocessor.
     *
     * @param methodCode The code of the method to be parsed.
     * @return {@code true} if the method code is parseable, {@code false} otherwise.
     */
    private boolean isMethodParseable(String methodCode) {
        try {
            return preprocessor.processSingleMethod(methodCode).isPresent();
        }
        catch (ProcessingException e) {
            return false;
        }
    }

    /**
     * Checks if the given class code is parseable by the preprocessor.
     *
     * @param classCode The code of the class to be parsed.
     * @return {@code true} if the class code is parseable, {@code false} otherwise.
     */
    private boolean isClassParseable(String classCode) {
        try {
            return preprocessor.parseSingleClass(classCode).isPresent();
        }
        catch (ProcessingException e) {
            return false;
        }
    }

}
