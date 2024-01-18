package de.uni_passau.fim.se2.assertion_exctractor.data;

import java.util.List;

/**
 * The TestElement interface defines a contract for elements within a test case, providing methods for obtaining tokens,
 * extracting only tokens, generating a token string, and checking if the element represents an assertion.
 */
public interface TestElement {

    /**
     * Retrieves a list of tokens associated with the test element.
     *
     * @return A list of tokens.
     */
    List<String> tokens();

    /**
     * Retrieves a list containing only tokens without any additional information.
     *
     * @return A list of only tokens.
     */
    List<String> onlyTokens();

    /**
     * Generates a string representation of the tokens, typically for debugging or logging purposes.
     *
     * @return A string representation of the tokens.
     */
    default String tokenString() {
        return toString();
    }

    /**
     * Checks if the test element represents an assertion.
     *
     * @return True if the test element is an assertion, otherwise false.
     */
    boolean isAssertion();
}
