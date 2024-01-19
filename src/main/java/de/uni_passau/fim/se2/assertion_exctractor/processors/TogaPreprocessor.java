package de.uni_passau.fim.se2.assertion_exctractor.processors;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

import com.opencsv.CSVWriter;

import de.uni_passau.fim.se2.assertion_exctractor.data.Assertion;
import de.uni_passau.fim.se2.assertion_exctractor.data.DataPoint;
import de.uni_passau.fim.se2.assertion_exctractor.data.DatasetType;
import de.uni_passau.fim.se2.assertion_exctractor.data.FineMethodData;
import de.uni_passau.fim.se2.assertion_exctractor.data.TestCase;
import de.uni_passau.fim.se2.assertion_exctractor.data.TestElement;
import de.uni_passau.fim.se2.assertion_exctractor.data.TryCatchAssertion;
import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;

/**
 * The {@link TogaPreprocessor} class extends the AssertionPreprocessor and is designed for processing assertion data
 * using the Toga model. It includes specific processors for different types of assertions, such as Try-Catch assertions
 * and regular assertions. The processed data is written to CSV files with different headers based on the assertion
 * type.
 */
public class TogaPreprocessor extends AssertionPreprocessor {

    /**
     * Set of IntermediateTogaProcessor instances that handle specific types of assertions.
     */
    private final Set<IntermediateTogaProcessor> togaProcessors;

    public TogaPreprocessor(String dataDir, String saveDir, int maxAssertions) {
        super(dataDir, saveDir, maxAssertions);
        togaProcessors = new HashSet<>();
    }

    @Override
    protected String getModelName() {
        return "toga";
    }

    /**
     * Delegates the exportTestCases operation to each individual Toga processor in the set.
     *
     * @param dataPointPair The pair containing a string and corresponding data point.
     */
    @Override
    protected void exportTestCases(Pair<String, DataPoint> dataPointPair) {
        togaProcessors.forEach(processor -> processor.exportTestCases(dataPointPair));
    }

    @Override
    protected void setup() {
        togaProcessors.add(new TryCatchTogaProcessor(dataDir, saveDir + "/" + getModelName(), maxAssertions));
        togaProcessors.add(new AssertionTogaProcessor(dataDir, saveDir + "/" + getModelName(), maxAssertions));
        togaProcessors
            .add(new CombinedTogaPreprocessor(dataDir, saveDir + "/" + getModelName(), maxAssertions));
        togaProcessors.forEach(AssertionPreprocessor::setup);
    }

    @Override
    protected void shutDown() {
        togaProcessors.forEach(AssertionPreprocessor::shutDown);
    }

    /**
     * The IntermediateTogaProcessor class is an abstract class that extends AssertionPreprocessor and provides common
     * functionality for Toga processors.
     */
    private static abstract class IntermediateTogaProcessor extends AssertionPreprocessor {

        protected final HashMap<DatasetType, CSVWriter> writerHashMap = new HashMap<>();

        private IntermediateTogaProcessor(String dataDir, String saveDir, int maxAssertions) {
            super(dataDir, saveDir, maxAssertions);
        }

        @Override
        protected String getModelName() {
            return null;
        }

        @Override
        protected void setup() {
            super.setup();
            Arrays.stream(DatasetType.values()).forEach(type -> {
                try {
                    writerHashMap
                        .put(type, new CSVWriter(new FileWriter(saveDir + "/" + type.name().toLowerCase() + ".csv")));
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
                writerHashMap.get(type).writeNext(getHeader(), false);
            });
        }

        @Override
        protected void exportTestCases(Pair<String, DataPoint> dataPointPair) {
            DataPoint dataPoint = dataPointPair.b();
            FineMethodData methodData = dataPoint.methodData();
            TestCase testCase = methodData.testCase();
            List<String> focalMethod = methodData.focalMethodTokens();
            String docString = methodData.documentation();
            List<TestElement> assertions = testCase.testElements().stream()
                .filter(((Predicate<TestElement>) Assertion.class::isInstance).or(TryCatchAssertion.class::isInstance))
                .toList();
            for (int i = 0; i < assertions.size(); i++) {
                boolean tryCatchAssertion = assertions.get(i) instanceof TryCatchAssertion;
                Optional<String[]> contentOpt = getRowContent(
                    tryCatchAssertion, testCase, i, focalMethod, docString, assertions.get(i), dataPoint.type()
                );
                contentOpt.ifPresent(content -> writerHashMap.get(dataPoint.type()).writeNext(content, false));
            }
        }

        /**
         * Retrieves the content for a single row in the CSV file associated with the Toga processor.
         *
         * @param tryCatchAssertion Whether the assertion is a Try-Catch assertion.
         * @param testCase          The test case associated with the assertion.
         * @param assertionPosition The position of the assertion in the test case.
         * @param focalMethod       The tokens of the focal method.
         * @param docString         The documentation string associated with the method.
         * @param assertion         The TestElement representing the assertion.
         * @param type              The DatasetType associated with the data point.
         * @return An optional containing the content for a single row, or empty if not applicable.
         */
        protected abstract Optional<String[]> getRowContent(
            boolean tryCatchAssertion, TestCase testCase, int assertionPosition, List<String> focalMethod,
            String docString, TestElement assertion, DatasetType type
        );

        @Override
        protected void shutDown() {
            writerHashMap.keySet().forEach(key -> {
                try {
                    writerHashMap.get(key).close();
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        protected abstract String[] getHeader();

    }

    /**
     * The TryCatchTogaProcessor class extends IntermediateTogaProcessor and handles Try-Catch assertions.
     */
    private static class TryCatchTogaProcessor extends IntermediateTogaProcessor {

        private TryCatchTogaProcessor(String dataDir, String saveDir, int maxAssertions) {
            super(dataDir, Path.of(saveDir, "exceptions").toString(), maxAssertions);
        }

        @Override
        protected String[] getHeader() {
            return new String[] { "label", "test", "fm", "docstring" };
        }

        protected Optional<String[]> getRowContent(
            boolean tryCatchAssertion, TestCase testCase, int assertionPosition, List<String> focalMethod,
            String docString, TestElement assertion, DatasetType type
        ) {
            return Optional.of(
                new String[] {
                    tryCatchAssertion ? "1" : "0",                      // try catch assertion required ?
                    testCase.replaceAssertion(assertionPosition),       // test case without assertion
                    String.join(" ", focalMethod),               // focal method tokens
                    docString                                           // documentation
                }
            );

        }
    }

    /**
     * The AssertionTogaProcessor class extends IntermediateTogaProcessor and handles regular assertions.
     */
    private static class AssertionTogaProcessor extends IntermediateTogaProcessor {

        private final Map<DatasetType, Integer> counterMap = new EnumMap<>(DatasetType.class);

        private AssertionTogaProcessor(String dataDir, String saveDir, int maxAssertions) {
            super(dataDir, Path.of(saveDir, "assertions").toString(), maxAssertions);
        }

        @Override
        protected Optional<String[]> getRowContent(
            boolean tryCatchAssertion, TestCase testCase, int assertionPosition, List<String> focalMethod,
            String docString, TestElement assertion, DatasetType type
        ) {
            if (tryCatchAssertion) {
                return Optional.empty();
            }
            int idx = counterMap.compute(type, (x, y) -> y != null ? y + 1 : 0);
            String[] lineContent = new String[] {
                String.valueOf(idx),                            // idx
                "0",                                            // label
                String.join(" ", focalMethod),          // focal method tokens
                testCase.replaceAssertion(assertionPosition),   // test case without assertions
                String.join(" ", assertion.tokens())    // assertion tokens
            };
            return Optional.of(lineContent);
        }

        @Override
        protected String[] getHeader() {
            return new String[] { "idx", "label", "fm", "test", "assertion" };
        }

    }

    /**
     * The CombinedTogaPreprocessor class extends the IntermediateTogaProcessor and handles combined assertions for the
     * Toga model. It writes the processed data to a CSV file with a specific header for combined assertions.
     */
    private static class CombinedTogaPreprocessor extends IntermediateTogaProcessor {

        private final Map<DatasetType, Integer> counterMap = new EnumMap<>(DatasetType.class);

        private CombinedTogaPreprocessor(String dataDir, String saveDir, int maxAssertions) {
            super(dataDir, Path.of(saveDir, "combined").toString(), maxAssertions);
        }

        @Override
        protected Optional<String[]> getRowContent(
            boolean tryCatchAssertion, TestCase testCase, int assertionPosition, List<String> focalMethod,
            String docString, TestElement assertion, DatasetType type
        ) {
            int idx = counterMap.compute(type, (x, y) -> y != null ? y + 1 : 0);
            String[] lineContent = new String[] {
                String.valueOf(idx),                            // idx
                String.join(" ", focalMethod),          // focal method tokens
                testCase.replaceAssertion(assertionPosition),   // test case without assertions
                tryCatchAssertion ? "TRY_CATCH" : String.join(" ", assertion.tokens()),     // assertion tokens
                docString
            };
            return Optional.of(lineContent);
        }

        @Override
        protected String[] getHeader() {
            return new String[] { "idx", "fm", "test", "assertion", "docstring" };
        }

    }
}
