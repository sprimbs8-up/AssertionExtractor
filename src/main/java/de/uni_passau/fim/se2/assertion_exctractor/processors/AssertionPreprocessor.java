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
import de.uni_passau.fim.se2.deepcode.toolbox.ast.transformer.util.TransformMode;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.CommonPreprocessorOptions;
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
        SINGLE_METHOD_OPTIONS, true, false
    );

    protected static final CommonPreprocessorOptions SINGLE_METHOD_OPTIONS = new CommonPreprocessorOptions(
        null, null, false, new TransformMode.None()
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
                .filter(this::isASTConvertible)
                .map(raw2fineConverter::process)
                .map(Utils::flatten)
                .flatMap(Optional::stream)
                .filter(this::isASTConvertibleAnyMore);
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
        ;
        try (FileWriter writer = new FileWriter(savePath, append.get())) {
            writer.write(tokens + System.getProperty("line.separator"));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
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

}
