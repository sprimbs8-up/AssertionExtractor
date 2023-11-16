package de.uni_passau.fim.se2.assertion_exctractor.parsing;

import java.util.List;

public record TryCatchAssertion(List<String> tokens) implements TestElement {
    @Override
    public String toString() {
        return String.join(" ", tokens);
    }
}
