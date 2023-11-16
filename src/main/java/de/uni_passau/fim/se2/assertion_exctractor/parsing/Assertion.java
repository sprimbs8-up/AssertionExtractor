package de.uni_passau.fim.se2.assertion_exctractor.parsing;

import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

public record Assertion(AssertionType type, List<String> tokens, ParseTree parseTree) implements TestElement {

    @Override
    public String toString() {
        return String.join(" ", tokens);
    }
}
