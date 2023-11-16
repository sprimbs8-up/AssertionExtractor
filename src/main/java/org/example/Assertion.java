package org.example;

import java.util.List;
import org.antlr.v4.runtime.tree.ParseTree;

public record Assertion(AssertionType type, List<String> tokens, ParseTree parseTree) implements TestElement {

    @Override
    public String toString() {
        return String.join(" ", tokens);
    }
}
