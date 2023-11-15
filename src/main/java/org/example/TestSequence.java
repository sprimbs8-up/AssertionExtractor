package org.example;

import java.util.List;
import java.util.stream.Stream;

public record TestSequence(List<String> testTokens) implements TestElement{
    @Override
    public String toString() {
        return String.join(" ", testTokens);
    }
}
