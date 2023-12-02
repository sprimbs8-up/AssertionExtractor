package de.uni_passau.fim.se2.assertion_exctractor.processors;

import com.opencsv.CSVWriter;
import de.uni_passau.fim.se2.assertion_exctractor.data.DataPoint;
import de.uni_passau.fim.se2.assertion_exctractor.data.DatasetType;
import de.uni_passau.fim.se2.assertion_exctractor.data.FineMethodData;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.Assertion;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestCase;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestElement;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TryCatchAssertion;

import javax.xml.crypto.Data;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

public class TogaProcessor extends Processor {
    private final HashMap<DatasetType, CSVWriter> writerHashMap = new HashMap<>();

    public TogaProcessor(String dataDir, String saveDir, int maxAssertions) {
        super(dataDir, saveDir, maxAssertions);
        Arrays.stream(DatasetType.values()).forEach(type -> {
            try {
                writerHashMap.put(type, new CSVWriter(new FileWriter(saveDir + "/" + type.name().toLowerCase() + ".csv")));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            writerHashMap.get(type).writeNext(new String[]{"label", "test", "fm", "docstring"}, false);
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
            String[] content = {tryCatchAssertion ? "1" : "0", testCase.replaceAssertion(i), String.join(" ", focalMethod), docString};
            writerHashMap.get(dataPoint.type()).writeNext(content, false);
        }
    }

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
}
