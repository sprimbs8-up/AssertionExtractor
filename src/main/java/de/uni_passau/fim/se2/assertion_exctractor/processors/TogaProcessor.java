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

public class TogaProcessor extends AssertionPreprocessor {

    private final Set<IntermediateTogaProcessor> togaProcessors;

    public TogaProcessor(String dataDir, String saveDir, int maxAssertions) {
        super(dataDir, saveDir, maxAssertions);
        togaProcessors = new HashSet<>();
    }

    @Override
    protected String getModelName() {
        return "toga";
    }

    @Override
    protected void exportTestCases(Pair<String, DataPoint> dataPointPair) {
        togaProcessors.forEach(processor -> processor.exportTestCases(dataPointPair));
    }

    @Override
    protected void setup() {
        togaProcessors.add(new TryCatchTogaProcessor(dataDir, saveDir + "/" + getModelName(), maxAssertions));
        togaProcessors.add(new AssertionTogaProcessor(dataDir, saveDir + "/" + getModelName(), maxAssertions));
        togaProcessors
            .add(new AssertionExceptionsTogaProcessor(dataDir, saveDir + "/" + getModelName(), maxAssertions));
        togaProcessors.forEach(AssertionPreprocessor::setup);
    }

    @Override
    protected void shutDown() {
        togaProcessors.forEach(AssertionPreprocessor::shutDown);
    }

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
                String.join(" ", focalMethod),           // focal method tokens
                testCase.replaceAssertion(assertionPosition),   // test case without assertions
                String.join(" ", assertion.tokens())     // assertion tokens
            };
            return Optional.of(lineContent);
        }

        @Override
        protected String[] getHeader() {
            return new String[] { "idx", "label", "fm", "test", "assertion" };
        }

    }

    private static class AssertionExceptionsTogaProcessor extends IntermediateTogaProcessor {

        private final Map<DatasetType, Integer> counterMap = new EnumMap<>(DatasetType.class);

        private AssertionExceptionsTogaProcessor(String dataDir, String saveDir, int maxAssertions) {
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
                String.join(" ", focalMethod),           // focal method tokens
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
