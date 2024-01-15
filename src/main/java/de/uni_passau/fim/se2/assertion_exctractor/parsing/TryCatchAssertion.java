package de.uni_passau.fim.se2.assertion_exctractor.parsing;

import java.util.List;

public record TryCatchAssertion(List<String> tryCatchTokens) implements TestElement {

    @Override
    public String toString() {
        return "TRY_CATCH";
    }

    @Override
    public String tokenString() {
        return String.join(" ", tryCatchTokens);
    }

    @Override
    public List<String> tokens() {
        return List.of(toString());
    }

    @Override
    public List<String> onlyTokens() {
        return tryCatchTokens;
    }

    @Override
    public boolean isAssertion() {
        return true;
    }
}
