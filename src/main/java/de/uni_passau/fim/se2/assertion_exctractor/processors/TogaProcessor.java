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

import javax.swing.text.html.Option;

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
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                writerHashMap.get(type).writeNext(getHeader(), false);
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
                boolean tryCatchAssertion = assertions.get(i) instanceof TryCatchAssertion;
                Optional<String[]> contentOpt = getRowContent(tryCatchAssertion, testCase, i, focalMethod, docString, assertions.get(i));
                contentOpt.ifPresent(content -> writerHashMap.get(dataPoint.type()).writeNext(content, false));
            }
        }

        protected abstract Optional<String[]> getRowContent(boolean tryCatchAssertion, TestCase testCase, int assertionPosition, List<String> focalMethod, String docString, TestElement assertion);




        @Override
        protected void shutDown() {
            writerHashMap.keySet().forEach(key -> {
                try {
                    writerHashMap.get(key).close();
                } catch (IOException e) {
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
            return new String[]{"label", "test", "fm", "docstring"};
        }

        protected Optional<String[]> getRowContent(boolean tryCatchAssertion, TestCase testCase, int assertionPosition, List<String> focalMethod, String docString, TestElement assertion) {
            return Optional.of(new String[]{
                    tryCatchAssertion ? "1" : "0",
                    testCase.replaceAssertion(assertionPosition),
                    String.join(" ", focalMethod),
                    docString
            });

    }}

    private static class AssertionTogaProcessor extends IntermediateTogaProcessor {
        private int idx = 0;

        private AssertionTogaProcessor(String dataDir, String saveDir, int maxAssertions) {
            super(dataDir, Path.of(saveDir, "assertions").toString(), maxAssertions);
        }

        @Override
        protected Optional<String[]> getRowContent(boolean tryCatchAssertion, TestCase testCase, int assertionPosition, List<String> focalMethod, String docString, TestElement assertion) {
            if(tryCatchAssertion){
                return Optional.empty();
            }
            String[] lineContent = new String[]{
                String.valueOf(idx++),
                    "0",
                    String.join(" ", focalMethod),
                    testCase.replaceAssertion(assertionPosition),
                    String.join(" ",assertion.tokens())
            };
            return Optional.of(lineContent);
        }

        @Override
        protected String[] getHeader() {
            return new String[]{"idx", "label", "fm", "test", "assertion"};
        }

    }
}
