package de.uni_passau.fim.se2.assertion_exctractor.processors;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
            exportDataPointRaw(type, assertTokens, testCaseStreamSupplier.get());
            exportDataPointAbstract(type, assertTokens, testCaseStreamSupplier.get(), abstractTokenMap);
        }
    }

    protected void exportDataPointAbstract(
        DatasetType type, List<String> assertionTokens, Stream<String> currentAssertionStream,
        Map<String, String> abstractTokenMap
    ) {
        Map<String, String> invertedSortedMap = new TreeMap<>(new TokenAbstractionComparator());
        invertedSortedMap.putAll(Utils.inverseMap(abstractTokenMap));

        String assertionString = assertionTokens.stream()
            .map(token -> abstractTokenMap.getOrDefault(token, token))
            .collect(Collectors.joining(" "));
        String inputString = currentAssertionStream
            .map(token -> abstractTokenMap.getOrDefault(token, token))
            .collect(Collectors.joining(" "));

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

    protected void exportDataPointRaw(
        DatasetType type, List<String> assertionTokens, Stream<String> currentAssertionStream
    ) {
        String assertionString = String.join(" ", assertionTokens);
        String inputString = currentAssertionStream
            .collect(Collectors.joining(" "));

        writeDataToFile("raw", type, assertionString, "assertLines.txt");
        writeDataToFile("raw", type, inputString, "testMethods.txt");
        type.getRefresh().set(true);
    }

    private void writeDataToFile(String dir, DatasetType type, String data, String fileName) {

        writeStringsToFile(dir + "/" + type.name().toLowerCase() + "/" + fileName, type.getRefresh(), data);

    }

}
