package de.uni_passau.fim.se2.assertion_exctractor.processors;

import java.util.List;
import java.util.function.Predicate;

import de.uni_passau.fim.se2.assertion_exctractor.data.Assertion;
import de.uni_passau.fim.se2.assertion_exctractor.data.DataPoint;
import de.uni_passau.fim.se2.assertion_exctractor.data.DatasetType;
import de.uni_passau.fim.se2.assertion_exctractor.data.FineMethodData;
import de.uni_passau.fim.se2.assertion_exctractor.data.TestCase;
import de.uni_passau.fim.se2.assertion_exctractor.data.TestElement;
import de.uni_passau.fim.se2.assertion_exctractor.data.TryCatchAssertion;
import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;

public class AtlasProcessor extends Processor {

    public AtlasProcessor(String dataDir, String saveDir, int maxAssertions) {
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
            .filter(((Predicate<TestElement>) Assertion.class::isInstance).or(TryCatchAssertion.class::isInstance))
            .map(TestElement::tokens)
            .toList();
        DatasetType type = dataPoint.type();
        for (int i = 0; i < assertions.size(); i++) {
            writeStringsToFile(
                dataPoint.type().name().toLowerCase() + "/assertLines.txt", type.getRefresh(),
                String.join(" ", assertions.get(i))
            );
            writeStringsToFile(
                dataPoint.type().name().toLowerCase() + "/testMethods.txt", type.getRefresh(),
                testCase.replaceAssertion(i)
            );
            dataPoint.type().getRefresh().set(true);
        }
    }

}
