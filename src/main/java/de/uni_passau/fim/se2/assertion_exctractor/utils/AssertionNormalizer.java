package de.uni_passau.fim.se2.assertion_exctractor.utils;

import de.uni_passau.fim.se2.assertion_exctractor.data.AssertionType;

public final class AssertionNormalizer {

    private AssertionNormalizer() {
    }

    public static String normalizeAssertions(String code) {
        for (AssertionType type : AssertionType.values()) {
            code = code.replaceAll("(([a-zA-Z]+)( )*.( )*)*" + type.getIdentifier(), type.getIdentifier());
        }
        return code;
    }

    public static String removeJavaDocs(String code) {
        return code.replaceAll("/\\*\\*(?s:(?!\\*/).)*\\*/", "");
    }
}
