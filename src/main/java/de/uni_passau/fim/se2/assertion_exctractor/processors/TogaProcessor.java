package de.uni_passau.fim.se2.assertion_exctractor.processors;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.opencsv.CSVWriter;
import de.uni_passau.fim.se2.assertion_exctractor.data.MethodData;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.Assertion;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestElement;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TryCatchAssertion;

public class TogaProcessor extends Processor {
    private final HashMap<DataType, CSVWriter> writerHashMap = new HashMap<>();

    public TogaProcessor(String dataDir, String saveDir, int maxAssertions) {
        super(dataDir, saveDir, maxAssertions);
        Arrays.stream(DataType.values()).forEach(type -> {
            try {
                writerHashMap.put(type, new CSVWriter(new FileWriter(saveDir + "/" + type.name().toLowerCase() + ".csv")));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            writerHashMap.get(type).writeNext(new String[]{"label", "test", "fm", "docstring"}, false);
        });
    }

    @Override
    protected void exportTestCases(MethodData x, DataType type) {
        List<TestElement> assertions = x.testCase().testElements().stream()
                .filter(v -> v instanceof Assertion || v instanceof TryCatchAssertion)
                .toList();
        String focalMethod = x.focalMethod();
        String docString = x.documentation();
        for (int i = 0; i < assertions.size(); i++) {
            boolean tryCatchAssertion = assertions.get(i) instanceof TryCatchAssertion;


            String[] content = {tryCatchAssertion ? "1" : "0", x.testCase().replaceAssertion(i), focalMethod, docString};
            writerHashMap.get(type).writeNext(content, false);

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
