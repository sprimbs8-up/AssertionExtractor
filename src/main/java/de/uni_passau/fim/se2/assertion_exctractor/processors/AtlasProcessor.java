package de.uni_passau.fim.se2.assertion_exctractor.processors;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import de.uni_passau.fim.se2.assertion_exctractor.data.MethodData;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.Assertion;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestCase;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestElement;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TryCatchAssertion;

public class AtlasProcessor extends Processor {

    public AtlasProcessor(String dataDir, String saveDir, int maxAssertions) {
        super(dataDir, saveDir, maxAssertions);
    }

    @Override
    protected void exportTestCases(MethodData x, DataType type) {
        List<List<String>> assertions = x.testCase().testElements().stream()
            .filter(v -> v instanceof Assertion || v instanceof TryCatchAssertion)
            .map(TestElement::tokens).toList();
        for (int i = 0; i < assertions.size(); i++) {
            writeStringsToFile(
                type.name().toLowerCase() + "/assertLines.txt", type.refresh, String.join(" ", assertions.get(i))
            );
            writeStringsToFile(type.name().toLowerCase() + "/testMethods.txt", type.refresh, x.testCase().replaceAssertion(i));
            type.getRefresh().set(true);
        }
    }


}
