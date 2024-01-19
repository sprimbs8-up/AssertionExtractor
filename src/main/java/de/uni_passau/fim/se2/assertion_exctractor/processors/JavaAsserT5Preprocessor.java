package de.uni_passau.fim.se2.assertion_exctractor.processors;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_passau.fim.se2.assertion_exctractor.data.*;
import de.uni_passau.fim.se2.assertion_exctractor.utils.TokenAbstractionComparator;
import de.uni_passau.fim.se2.assertion_exctractor.utils.Utils;
import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;

public class JavaAsserT5Preprocessor extends AssertionPreprocessor {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public JavaAsserT5Preprocessor(String dataDir, String saveDir, int maxAssertions) {
        super(dataDir, saveDir, maxAssertions);
    }

    @Override
    protected String getModelName() {
        return "asserT5";
    }

    @Override
    protected void exportTestCases(Pair<String, DataPoint> dataPointPair) {

        DataPoint dataPoint = dataPointPair.b();
        FineMethodData methodData = dataPoint.methodData();
        Map<String, String> abstractTokenMap = Utils.collectAbstractTokens(methodData, preprocessor);

        TestCase testCase = methodData.testCase();
        List<List<String>> assertions = testCase.testElements().stream()
            .filter(((Predicate<TestElement>) Assertion.class::isInstance).or(TryCatchAssertion.class::isInstance))
            .map(TestElement::tokens)
            .map(testTokens -> testTokens.stream().map(token -> abstractTokenMap.getOrDefault(token, token)).toList())
            .toList();
        DatasetType type = dataPoint.type();
        for (int i = 0; i < assertions.size(); i++) {
            exportDataPoint(dataPoint, type, assertions, i, testCase, abstractTokenMap, methodData);
        }
    }

    private void exportDataPoint(
        DataPoint dataPoint, DatasetType type, List<List<String>> assertions, int i, TestCase testCase,
        Map<String, String> abstractTokenMap, FineMethodData methodData
    ) {
        String assertionString = String.join(" ", assertions.get(i));
        String inputString = buildInputString(i, testCase, abstractTokenMap, methodData);

        Map<String, String> invertedSortedMap = new TreeMap<>(new TokenAbstractionComparator());
        invertedSortedMap.putAll(Utils.inverseMap(abstractTokenMap));

        ExportData data = new ExportData(assertionString, inputString, invertedSortedMap);

        try {
            writeStringsToFile(
                dataPoint.type().name().toLowerCase() + ".jsonl", type.getRefresh(),
                MAPPER.writeValueAsString(data)
            );
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        dataPoint.type().getRefresh().set(true);
    }

    private static String buildInputString(
        int i, TestCase testCase, Map<String, String> abstractTokenMap, FineMethodData methodData
    ) {
        String testCaseString = testCase.replaceAssertionStream(i)
            .map(token -> abstractTokenMap.getOrDefault(token, token)).collect(Collectors.joining(" "));
        String focalMethodString = methodData.focalClassTokens().stream()
            .map(token -> abstractTokenMap.getOrDefault(token, token)).collect(Collectors.joining(" "));
        return "TEST_METHOD: " + testCaseString + " FOCAL_METHOD: " + focalMethodString;

    }

    private record ExportData(String labels, String inputIDs, Map<String, String> dict) {
    }

}
