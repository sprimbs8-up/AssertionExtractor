package de.uni_passau.fim.se2.assertion_exctractor.processors;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

import com.opencsv.CSVWriter;

import de.uni_passau.fim.se2.assertion_exctractor.data.DataPoint;
import de.uni_passau.fim.se2.assertion_exctractor.data.DatasetType;
import de.uni_passau.fim.se2.assertion_exctractor.data.FineMethodData;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.Assertion;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestCase;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestElement;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TryCatchAssertion;

public class TogaProcessor extends Processor {

    private final Set<IntermediateTogaProcessor> togaProcessors;

    public TogaProcessor(String dataDir, String saveDir, int maxAssertions) {
        super(dataDir, saveDir, maxAssertions);
        togaProcessors = new HashSet<>();
    }

    @Override
    protected void exportTestCases(DataPoint dataPoint) {

        togaProcessors.forEach(processor -> processor.exportTestCases(dataPoint));
    }

    @Override
    protected void setup() {
        togaProcessors.add(new TryCatchTogaProcessor(dataDir, saveDir, maxAssertions));
        togaProcessors.add(new AssertionTogaProcessor(dataDir, saveDir, maxAssertions));
        togaProcessors.forEach(Processor::setup);
    }

    @Override
    protected void shutDown() {
        togaProcessors.forEach(Processor::shutDown);
    }

    private static abstract class IntermediateTogaProcessor extends Processor {

        protected final HashMap<DatasetType, CSVWriter> writerHashMap = new HashMap<>();

        private IntermediateTogaProcessor(String dataDir, String saveDir, int maxAssertions) {
            super(dataDir, saveDir, maxAssertions);
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
                writerHashMap.get(type).writeNext(new String[] { "label", "test", "fm", "docstring" }, false);
            });
        }

        @Override
        protected void exportTestCases(DataPoint dataPoint) {
            FineMethodData methodData = dataPoint.methodData();
            TestCase testCase = methodData.testCase();
            List<String> focalMethod = methodData.focalMethodTokens();
            String docString = methodData.documentation();
            List<TestElement> assertions = testCase.testElements().stream()
                .filter(((Predicate<TestElement>) Assertion.class::isInstance).or(TryCatchAssertion.class::isInstance))
                .toList();
            for (int i = 0; i < assertions.size(); i++) {
                buildLineEntry(dataPoint.type(), assertions, i, testCase, focalMethod, docString);
            }
        }

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

        protected abstract void buildLineEntry(
            DatasetType type, List<TestElement> assertions, int i, TestCase testCase, List<String> focalMethod,
            String docString
        );
    }

    private static class TryCatchTogaProcessor extends IntermediateTogaProcessor {

        private TryCatchTogaProcessor(String dataDir, String saveDir, int maxAssertions) {
            super(dataDir, Path.of(saveDir, "exceptions").toString(), maxAssertions);
        }

        protected void buildLineEntry(
            DatasetType type, List<TestElement> assertions, int i, TestCase testCase, List<String> focalMethod,
            String docString
        ) {
            boolean tryCatchAssertion = assertions.get(i) instanceof TryCatchAssertion;
            String[] content = { tryCatchAssertion ? "1" : "0", testCase.replaceAssertion(i),
                String.join(" ", focalMethod), docString };
            writerHashMap.get(type).writeNext(content, false);
        }
    }

    private static class AssertionTogaProcessor extends IntermediateTogaProcessor {

        private AssertionTogaProcessor(String dataDir, String saveDir, int maxAssertions) {
            super(dataDir, Path.of(saveDir, "assertions").toString(), maxAssertions);
        }

        protected void buildLineEntry(
            DatasetType type, List<TestElement> assertions, int i, TestCase testCase, List<String> focalMethod,
            String docString
        ) {
            boolean tryCatchAssertion = assertions.get(i) instanceof TryCatchAssertion;
            if (tryCatchAssertion) {
                String[] content = { String.valueOf(i), testCase.replaceAssertion(i),
                    String.join(" ", focalMethod), docString };
                writerHashMap.get(type).writeNext(content, false);
            }
        }
    }
}
