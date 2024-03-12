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

import de.uni_passau.fim.se2.assertion_exctractor.data.*;
import de.uni_passau.fim.se2.assertion_exctractor.utils.TokenAbstractionComparator;
import de.uni_passau.fim.se2.assertion_exctractor.utils.Utils;
import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;

/**
 * The JavaAsserT5Preprocessor class extends the {@link AssertionPreprocessor} and is designed specifically for
 * processing assertion data using the asserT5 model. It provides methods to export test cases in both abstract and raw
 * formats, along with utility methods for building input strings and writing data to files.
 */
class JavaAsserT5Preprocessor extends AssertionPreprocessor {

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
            .filter(TestElement::isAssertion)
            .map(TestElement::tokens)
            .toList();

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
            exportDataPointOnlyTest(type, assertTokens, testCaseStreamSupplier.get());
        }
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

    private <T> void writeDataToFile(String dir, DatasetType type, T data, ObjectMapper mapper) {
        try {
            writeStringsToFile(
                dir + "/" + type.name().toLowerCase() + ".jsonl", type.getRefresh(), mapper.writeValueAsString(data)
            );
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
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
            currentAssertionStream, currentFocalStream, token -> abstractTokenMap.getOrDefault(token, token)
        );

        AbstractExportData data = new AbstractExportData(assertionString, inputString, invertedSortedMap);

        writeDataToFile("abstract", type, data, MAPPER);
        type.getRefresh().set(true);
    }

    protected void exportDataPointRaw(
        DatasetType type, List<String> assertionTokens, Stream<String> currentAssertionStream,
        Stream<String> currentFocalStream
    ) {
        String assertionString = String.join(" ", assertionTokens);
        String inputString = buildInputString(currentAssertionStream, currentFocalStream, token -> token);

        RawExportData data = new RawExportData(assertionString, inputString);

        writeDataToFile("raw", type, data, MAPPER);
        type.getRefresh().set(true);
    }

    protected void exportDataPointOnlyTest(
            DatasetType type, List<String> assertionTokens, Stream<String> currentAssertionStream
    ) {
        String assertionString = String.join(" ", assertionTokens);
        String inputString = currentAssertionStream.collect(Collectors.joining(" "));

        RawExportData data = new RawExportData(assertionString, inputString);

        writeDataToFile("test-method", type, data, MAPPER);
        type.getRefresh().set(true);
    }

    private record AbstractExportData(String labels, String inputIDs, Map<String, String> dict) {
    }

    private record RawExportData(String labels, String inputIDs) {
    }
}
