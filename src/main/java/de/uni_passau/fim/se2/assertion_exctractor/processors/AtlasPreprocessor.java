package de.uni_passau.fim.se2.assertion_exctractor.processors;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_passau.fim.se2.assertion_exctractor.data.DataPoint;
import de.uni_passau.fim.se2.assertion_exctractor.data.DatasetType;
import de.uni_passau.fim.se2.assertion_exctractor.data.FineMethodData;
import de.uni_passau.fim.se2.assertion_exctractor.data.TestCase;
import de.uni_passau.fim.se2.assertion_exctractor.data.TestElement;
import de.uni_passau.fim.se2.assertion_exctractor.utils.TokenAbstractionComparator;
import de.uni_passau.fim.se2.assertion_exctractor.utils.Utils;
import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;

/**
 * The {@link AtlasPreprocessor} class extends the {@link AssertionPreprocessor} and is designed specifically for
 * processing assertion data using the "atlas" model. It provides methods to export test cases in both abstract and raw
 * formats, along with utility methods for writing data to files.
 */
public class AtlasPreprocessor extends AssertionPreprocessor {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public AtlasPreprocessor(String dataDir, String saveDir, int maxAssertions) {
        super(dataDir, saveDir, maxAssertions);
    }

    @Override
    protected String getModelName() {
        return "atlas";
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
        Map<String, String> abstractTokenMap = Utils.collectAbstractMethodTokens(methodData, preprocessor);
        DatasetType type = dataPoint.type();
        for (int i = 0; i < assertions.size(); i++) {
            final int pos = i;
            List<String> assertTokens = assertions.get(i);
            Supplier<Stream<String>> testCaseStreamSupplier = () -> testCase.replaceAssertionStream(pos);
            Supplier<Stream<String>> focalMethodTokens = () -> methodData.focalMethodTokens().stream();
            exportDataPointAbstract(
                type, assertTokens, testCaseStreamSupplier.get(), focalMethodTokens.get(), abstractTokenMap
            );
            exportDataPointRaw(type, assertTokens, testCaseStreamSupplier.get(), focalMethodTokens.get());
        }
    }

    protected void exportDataPointAbstract(
        DatasetType type, List<String> assertionTokens, Stream<String> currentAssertionStream,
        Stream<String> currentFocalStream,
        Map<String, String> abstractTokenMap
    ) {
        Map<String, String> invertedSortedMap = new TreeMap<>(new TokenAbstractionComparator());
        invertedSortedMap.putAll(Utils.inverseMap(abstractTokenMap));

        String assertionString = assertionTokens.stream()
            .map(token -> abstractTokenMap.getOrDefault(token, token))
            .collect(Collectors.joining(" "));
        String inputString = buildInputString(
            currentAssertionStream, currentFocalStream, token -> getOrDefault(abstractTokenMap, token)
        );

        writeDataToFile("abstract", type, assertionString, "assertLines.txt");
        writeDataToFile("abstract", type, inputString, "testMethods.txt");
        try {
            writeDataToFile("abstract", type, MAPPER.writeValueAsString(invertedSortedMap), "dict.jsonl");
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        type.getRefresh().set(true);
    }

    private String buildInputString(
        Stream<String> currentAssertionStream, Stream<String> currentClassStream, Function<String, String> tokenFunction
    ) {
        String testCaseString = currentAssertionStream
            .map(tokenFunction)
            .collect(Collectors.joining(" "));
        String focalMethodString = currentClassStream
            .map(tokenFunction)
            .collect(Collectors.joining(" "));
        return "TEST_METHOD: " + testCaseString + " FOCAL_METHOD: " + focalMethodString;

    }

    private static String getOrDefault(Map<String, String> abstractTokenMap, String token) {
        if (abstractTokenMap.containsKey(token)) {
            return abstractTokenMap.get(token);
        }
        try {
            double doubleValue = Float.parseFloat(token);
            return abstractTokenMap.getOrDefault(String.valueOf(doubleValue), token);
        }
        catch (NumberFormatException e) {
            return token;
        }
    }

    protected void exportDataPointRaw(
        DatasetType type, List<String> assertionTokens, Stream<String> currentAssertionStream,
        Stream<String> currentFocalStream
    ) {
        String assertionString = String.join(" ", assertionTokens);
        String inputString = buildInputString(currentAssertionStream, currentFocalStream, token -> token);

        writeDataToFile("raw", type, assertionString, "assertLines.txt");
        writeDataToFile("raw", type, inputString, "testMethods.txt");
        type.getRefresh().set(true);
    }

    private void writeDataToFile(String dir, DatasetType type, String data, String fileName) {

        writeStringsToFile(dir + "/" + type.name().toLowerCase() + "/" + fileName, type.getRefresh(), data);

    }

}
