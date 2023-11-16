package de.uni_passau.fim.se2.assertion_exctractor.parsing;

import java.util.List;

public record TestCase(List<TestElement> testElements) {

    public void print() {
        testElements.forEach(System.out::println);
    }

    public void printAndReplaceAssertions() {
        testElements.stream().map(x -> {
            if (x instanceof Assertion assertion) {
                return "<" + assertion.type().getIdentifier().toUpperCase() + ">";
            }
            return x.toString();
        }).forEach(System.out::println);
    }
}
