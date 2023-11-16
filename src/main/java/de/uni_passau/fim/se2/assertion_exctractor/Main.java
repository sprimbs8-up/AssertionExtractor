package de.uni_passau.fim.se2.assertion_exctractor;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestElement;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import de.uni_passau.fim.se2.assertion_exctractor.data.Method2TestLoader;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.Assertion;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestCaseParser;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TryCatchAssertion;

public class Main {

    public static void main(String[] args) throws IOException, ParseException {

        TestCaseParser testCaseParser = new TestCaseParser();

        AtomicBoolean append = new AtomicBoolean(false);
        Method2TestLoader.loadDatasetAsJSON("/root/master/methods2test-crawler/dataset/eval/120005344")
                .map(file -> (String) ((JSONObject) file.get("test_case")).get("body"))
                .map(testCaseParser::parseTestCase)
                .filter(tc -> tc.getNumberAssertions() <= 2)
                .filter(tc -> tc.getNumberAssertions() >= 1)
                .forEachOrdered(x -> {
                    List<List<String>> tokens = x.testElements().stream()
                            .filter(v -> v instanceof Assertion || v instanceof TryCatchAssertion)
                            .map(TestElement::tokens).toList();
                    for(int i = 0; i< tokens.size();i++){
                        writeStringsToFile("assertLines.txt", append, String.join(" ", tokens.get(i)));
                        writeStringsToFile("testMethods.txt", append, x.replaceAssertion(i));
                        append.set(true);
                    }
                });
    }

    private static void writeStringsToFile(String file, AtomicBoolean append, String tokens) {
        try (FileWriter writer = new FileWriter(file, append.get())) {
            writer.write(tokens + System.getProperty("line.separator"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
