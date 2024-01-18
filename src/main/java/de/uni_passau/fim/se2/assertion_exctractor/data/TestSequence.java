package de.uni_passau.fim.se2.assertion_exctractor.data;

import java.util.List;

/**
 * The TestSequence record represents a sequence of tokens without any assertion as a single test element.
 *
 * @param tokens The list of tokens in the test sequence.
 */
public record TestSequence(List<String> tokens) implements TestElement {

    @Override
    public String toString() {
        return String.join(" ", tokens).replace("\n", " ");
    }

    @Override
    public List<String> onlyTokens() {
        return tokens;
    }

    @Override
    public boolean isAssertion() {
        return false;
    }
}
