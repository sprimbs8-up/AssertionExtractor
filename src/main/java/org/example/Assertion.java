package org.example;


import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.statement.Statement;

import java.util.List;

public record Assertion(AssertionType type, List<String> assertionTokens) implements TestElement {
    @Override
    public String toString() {
        return String.join(" ", assertionTokens);
    }
}
