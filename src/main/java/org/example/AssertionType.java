package org.example;

import java.util.Arrays;
import java.util.Optional;

public enum AssertionType {
    ASSERT_EQUALS("assertEquals"),
    ASSERT_NOT_EQUALS("assertNotEquals"),
    ASSERT_TRUE("assertTrue"),
    ASSERT_FALSE("assertFalse"),
    ASSERT_NULL("assertNull"),
    ASSERT_NOT_NULL("assertNotNull"),
    ASSERT_THROWS("assertThrows");
    private final String identifier;

    AssertionType(final String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public static Optional<AssertionType> parseAssertion(String string) {
        return Arrays.stream(values()).filter(assertion -> string.equals(assertion.identifier)).findAny();
    }
}
