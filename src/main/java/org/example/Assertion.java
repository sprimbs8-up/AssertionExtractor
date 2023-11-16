package org.example;

import java.util.List;

public record Assertion(AssertionType type, List<String> tokens) implements TestElement {

    @Override
    public String toString() {
        return String.join(" ", tokens);
    }
}
