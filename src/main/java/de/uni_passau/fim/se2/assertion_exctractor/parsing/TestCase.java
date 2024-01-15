package de.uni_passau.fim.se2.assertion_exctractor.parsing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;

public record TestCase(List<TestElement> testElements) {

    public static final String ASSERTION_MASK = "<ASSERTION>";

    public String replaceAssertion(int pos) {
        return replaceAssertion(pos, ASSERTION_MASK);
    }

    public String replaceAssertion(int pos, String maskToken) {
       return replaceAssertionStream(pos,maskToken) .collect(Collectors.joining(" "));
    }
    public Stream<String> replaceAssertionStream(int pos){
        return replaceAssertionStream(pos, ASSERTION_MASK);
    }
    public Stream<String> replaceAssertionStream(int pos, String maskToken) {
        List<Pair<TestElement, Integer>> assertPosPairs = generateAssertionPositionPair();
        return assertPosPairs.stream().flatMap(x -> {
            if (x.b() == pos) {
                if (x.a() instanceof Assertion) {
                    return Optional.ofNullable(maskToken).stream();
                }
                else if (x.a() instanceof TryCatchAssertion tryCatchAssertion) {
                    List<String> tryCatTokens = tryCatchAssertion.tryCatchTokens();
                    return   Stream.concat(Optional.ofNullable(maskToken).stream(), tryCatTokens.subList(1, tryCatTokens.size() - 1).stream());
                }
            }
            return x.a().tokens().stream();
        });
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
        return (int) testElements.stream().filter(TestElement::isAssertion).count();
    }

    @Override
    public String toString() {
        return testElements.stream().map(TestElement::onlyTokens).flatMap(Collection::stream)
            .collect(Collectors.joining(" "));
    }
}
