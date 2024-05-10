package de.uni_passau.fim.se2.assertion_exctractor.processors;

import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_passau.fim.se2.assertion_exctractor.data.*;
import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;

/**
 * The {@link GPTPreprocessor} class extends the {@link AssertionPreprocessor} and is designed specifically for
 * creating the input data for a later ChatGPT prompt.
 */
public class GPTPreprocessor extends AssertionPreprocessor {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public GPTPreprocessor(String dataDir, String saveDir, int maxAssertions) {
        super(dataDir, saveDir, maxAssertions);
    }

    @Override
    protected String getModelName() {
        return "gpt";
    }

    @Override
    protected void exportTestCases(Pair<String, DataPoint> dataPointPair) {
        DataPoint dataPoint = dataPointPair.b();
        FineMethodData methodData = dataPoint.methodData();
        TestCase testCase = methodData.testCase();
        List<List<String>> assertions = testCase.testElements().stream()
            .filter(TestElement::isAssertion)
            .map(TestElement::tokens)
            .toList();
        DatasetType type = dataPoint.type();
        Optional<Map<String, String>> optMap = mapToHashMap(dataPointPair.a());
        if (optMap.isEmpty()) {
            return;
        }
        Map<String, String> map = optMap.get();
        for (List<String> assertTokens : assertions) {
            exportDataPoint(type, assertTokens, map.get("test_method"), map.get("focal_method"));
        }
    }

    private Optional<Map<String, String>> mapToHashMap(String dataString) {
        try {
            return Optional.of(MAPPER.readValue(dataString, HashMap.class));
        }
        catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    protected void exportDataPoint(
        DatasetType type, List<String> assertionTokens, String testMethod, String focalMethod
    ) {
        String replacedTestMethod = replaceAssertionInTestMethod(testMethod, assertionTokens);
        String assertionString = String.join(" ", assertionTokens);
        Map<String, String> exportMap = new HashMap<>();
        exportMap.put("testMethod", replacedTestMethod);
        exportMap.put("focalMethod", focalMethod);
        exportMap.put("expectedAssertion", assertionString);
        try {
            writeDataToFile(type, MAPPER.writeValueAsString(exportMap), "chat-gpt-input.jsonl");
            type.getRefresh().set(true);
        }
        catch (JsonProcessingException ignored) {
        }
    }

    private String replaceAssertionInTestMethod(String testMethod, List<String> assertTokens) {
        String[] testMethodLines = testMethod.split("\n");
        for (int i = 0; i < testMethodLines.length; i++) {
            String combinedCodeLine = testMethodLines[i].replaceAll(" ", "");
            String assertionLine = String.join("", assertTokens);
            if (combinedCodeLine.contains(assertionLine)) {
                testMethodLines[i] = "<ASSERTION> ;";
            }
        }
        return String.join("\n", testMethodLines);
    }

    private void writeDataToFile(DatasetType type, String data, String fileName) {
        writeStringsToFile(type.name().toLowerCase() + "/" + fileName, type.getRefresh(), data);
    }

}
