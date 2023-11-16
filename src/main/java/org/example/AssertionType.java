package org.example;

import java.util.Arrays;
import java.util.Optional;

public enum AssertionType {



    ASSERT_EQUALS("assertEquals",2),
    ASSERT_NOT_EQUALS("assertNotEquals",2),
    ASSERT_TRUE("assertTrue",1),
    ASSERT_FALSE("assertFalse",1),
    ASSERT_NULL("assertNull",1),
    ASSERT_NOT_NULL("assertNotNull",1),
    ASSERT_THROWS("assertThrows",2);

    private final String identifier;
    private final int numParameters;

    AssertionType(final String identifier, final int numParameters) {
        this.identifier = identifier;
        this.numParameters = numParameters;
    }

    public String getIdentifier() {
        return identifier;
    }
    public int getNumParameters(){
        return numParameters;
    }

    public static Optional<AssertionType> parseAssertion(String string) {
        return Arrays.stream(values()).filter(assertion -> string.equals(assertion.identifier)).findAny();
    }


}
