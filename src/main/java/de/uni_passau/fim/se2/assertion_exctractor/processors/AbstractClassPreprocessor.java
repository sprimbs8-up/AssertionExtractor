package de.uni_passau.fim.se2.assertion_exctractor.processors;

import de.uni_passau.fim.se2.assertion_exctractor.data.DataPoint;
import de.uni_passau.fim.se2.assertion_exctractor.data.DatasetType;
import de.uni_passau.fim.se2.assertion_exctractor.data.FineMethodData;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.Assertion;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestCase;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestElement;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TryCatchAssertion;
import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;

import java.util.List;
import java.util.function.Predicate;

public class AbstractClassPreprocessor extends Processor {

    public AbstractClassPreprocessor(String dataDir, String saveDir, int maxAssertions) {
        super(dataDir, saveDir, maxAssertions);
    }

    @Override
    protected String getModelName() {
        return "experiment";
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