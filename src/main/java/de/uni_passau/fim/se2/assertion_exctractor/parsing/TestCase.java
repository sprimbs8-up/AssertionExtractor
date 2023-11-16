package de.uni_passau.fim.se2.assertion_exctractor.parsing;

import java.util.List;
import java.util.stream.Collectors;

public record TestCase(List<TestElement> testElements) {

    public void print() {
        testElements.forEach(System.out::println);
    }

    public String replaceAssertions() {
        return testElements.stream().map(x -> {
            if (x instanceof Assertion assertion) {
                return "<ASSERTION>";
            }
            if (x instanceof TryCatchAssertion tryCatchAssertion){
                return "<ASSERTION>";

            }
            return x.toString();
        }).collect(Collectors.joining(" "));
    }

    public int getNumberAssertions() {
        return (int) testElements.stream().filter(x-> x instanceof Assertion || x instanceof TryCatchAssertion).count();
    }
}
