package de.uni_passau.fim.se2.assertion_exctractor.processors;

import de.uni_passau.fim.se2.assertion_exctractor.data.MethodData;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.Assertion;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestCase;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestElement;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TryCatchAssertion;

import java.util.List;

public class TogaProcessor extends Processor{
    public TogaProcessor(String dataDir, String saveDir, int maxAssertions) {
        super(dataDir, saveDir, maxAssertions);
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
            StringBuilder stb = new StringBuilder();
            stb.append(tryCatchAssertion ? 1: 0) .append(",").append(x.testCase().replaceAssertion(0)).append(",").append(focalMethod).append(",").append(docString);
            writeStringsToFile(type.name().toLowerCase()+".csv",type.refresh,stb.toString());
        }
    }
}
