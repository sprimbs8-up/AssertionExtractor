package org.example;

import java.util.List;

public record TestSequence(List<String> tokens) implements TestElement{
    @Override
    public String toString() {
        return String.join(" ", tokens);
    }
}
