package de.uni_passau.fim.se2.assertion_exctractor.parsing;

import java.util.List;
import java.util.stream.Stream;

public record TryCatchAssertion(List<String> tryCatchTokens) implements TestElement {

    @Override
    public String toString() {
        return "TRY_CATCH";
    }

    @Override
    public List<String> tokens() {
        return List.of(toString());
    }
}
