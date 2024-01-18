package de.uni_passau.fim.se2.assertion_exctractor.data;

import java.util.List;

/**
 * The JavaDocMethod record represents a JavaDoc comment associated with a method. It encapsulates the text content of
 * the JavaDoc comment and the list of tokens extracted from the method's code.
 *
 * @param text         The text content of the JavaDoc comment.
 * @param methodTokens The list of tokens extracted from the method's code.
 */
public record JavaDocMethod(String text, List<String> methodTokens) {
}
