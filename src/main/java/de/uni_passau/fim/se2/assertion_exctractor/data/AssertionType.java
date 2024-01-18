package de.uni_passau.fim.se2.assertion_exctractor.data;

import java.util.Arrays;
import java.util.Optional;

/**
 * The AssertionType enum represents different types of assertions commonly used in testing, such as assertEquals,
 * assertNotEquals, assertTrue, assertFalse, assertNull, assertNotNull, and assertThrows. Each assertion type is
 * associated with a unique identifier and the number of parameters it requires.
 */
public enum AssertionType {

    /**
     * Represents the assertEquals assertion.
     */
    ASSERT_EQUALS("assertEquals", 2),

    /**
     * Represents the assertNotEquals assertion.
     */
    ASSERT_NOT_EQUALS("assertNotEquals", 2),

    /**
     * Represents the assertTrue assertion.
     */
    ASSERT_TRUE("assertTrue", 1),

    /**
     * Represents the assertFalse assertion.
     */
    ASSERT_FALSE("assertFalse", 1),

    /**
     * Represents the assertNull assertion.
     */
    ASSERT_NULL("assertNull", 1),

    /**
     * Represents the assertNotNull assertion.
     */
    ASSERT_NOT_NULL("assertNotNull", 1),

    /**
     * Represents the assertThrows assertion.
     */
    ASSERT_THROWS("assertThrows", 2);

    private final String identifier;
    private final int numParameters;

    /**
     * Constructs an AssertionType with the specified identifier and the number of parameters it requires.
     *
     * @param identifier    The unique identifier of the assertion type.
     * @param numParameters The number of parameters required by the assertion type.
     */
    AssertionType(final String identifier, final int numParameters) {
        this.identifier = identifier;
        this.numParameters = numParameters;
    }

    /**
     * Gets the unique identifier of the assertion type.
     *
     * @return The identifier of the assertion type.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Gets the number of parameters required by the assertion type.
     *
     * @return The number of parameters required.
     */
    public int getNumParameters() {
        return numParameters;
    }

    /**
     * Parses a string representation of an assertion and returns the corresponding AssertionType.
     *
     * @param string The string representation of the assertion.
     * @return An Optional containing the parsed AssertionType, or empty if not found.
     */
    public static Optional<AssertionType> parseAssertion(String string) {
        return Arrays.stream(values()).filter(assertion -> string.equals(assertion.identifier)).findAny();
    }
}
