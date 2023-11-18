package de.uni_passau.fim.se2.assertion_exctractor.processors;

import de.uni_passau.fim.se2.assertion_exctractor.data.MethodData;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.Assertion;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestCase;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestElement;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TryCatchAssertion;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AtlasProcessor extends Processor {
    private final AtomicBoolean append = new AtomicBoolean(false);


    public AtlasProcessor(String dataDir, String saveDir, int maxAssertions) {
        super(dataDir, saveDir, maxAssertions);
    }

    @Override
    public void exportProcessedExamples() {

        super.loadMethodData()
                .filter(data -> data.testCase().getNumberAssertions() <= maxAssertions)
                .filter(data -> data.testCase().getNumberAssertions() >= 1)
                .forEachOrdered(x -> exportTestCases(x.testCase(), append));
    }

    private void exportTestCases(TestCase x, AtomicBoolean append) {
        List<List<String>> tokens = x.testElements().stream()
                .filter(v -> v instanceof Assertion || v instanceof TryCatchAssertion)
                .map(TestElement::tokens).toList();
        for (int i = 0; i < tokens.size(); i++) {
            writeStringsToFile("assertLines.txt", append, String.join(" ", tokens.get(i)));
            writeStringsToFile("testMethods.txt", append, x.replaceAssertion(i));
            append.set(true);
        }
    }

    private void writeStringsToFile(String file, AtomicBoolean append, String tokens) {
        try (FileWriter writer = new FileWriter(saveDir + "/" + file, append.get())) {
            writer.write(tokens + System.getProperty("line.separator"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
