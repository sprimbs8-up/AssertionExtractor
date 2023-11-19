package de.uni_passau.fim.se2.assertion_exctractor.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;

public record TestCase(List<TestElement> testElements) {

    public void print() {
        testElements.forEach(System.out::println);
    }

    public String replaceAssertion(int pos) {
        List<Pair<TestElement, Integer>> assertPosPairs = generateAssertionPositionPair();
        return assertPosPairs.stream().map(x -> {
            if (x.b() == pos) {
                if (x.a() instanceof Assertion) {
                    return "<ASSERTION>";
                }
                else if (x.a() instanceof TryCatchAssertion tryCatchAssertion) {
                    List<String> tryCatTokens = tryCatchAssertion.tryCatchTokens();
                    return String.join(" ", tryCatTokens.subList(1, tryCatTokens.size() - 1));

                }
            }
            return x.a().toString();
        }).collect(Collectors.joining(" "));
    }

    private List<Pair<TestElement, Integer>> generateAssertionPositionPair() {
        List<Pair<TestElement, Integer>> assertionsWithPositions = new ArrayList<>();
        int curPos = 0;
        for (TestElement el : testElements()) {
            if (!(el instanceof TestSequence)) {
                assertionsWithPositions.add(Pair.of(el, curPos++));
            }
            else {
                assertionsWithPositions.add(Pair.of(el, curPos));
            }
        }
        return assertionsWithPositions;
    }

    public int getNumberAssertions() {
        return (int) testElements.stream().filter(x -> x instanceof Assertion || x instanceof TryCatchAssertion)
            .count();
    }
}
