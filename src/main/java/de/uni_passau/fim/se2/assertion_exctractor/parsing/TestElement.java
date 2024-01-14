package de.uni_passau.fim.se2.assertion_exctractor.parsing;

import java.util.List;

public interface TestElement {

    List<String> tokens();

    List<String> onlyTokens();

    default String tokenString() {
        return toString();
    }

    boolean isAssertion();
}
