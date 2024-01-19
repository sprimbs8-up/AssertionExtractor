package de.uni_passau.fim.se2.assertion_exctractor.utils;

import java.util.Comparator;

/**
 * The {@link TokenAbstractionComparator} class implements the {@link Comparator} interface for comparing strings in the
 * format "String_Integer". It is specifically designed to compare tokens based on their String part and then on their
 * Integer part. The comparison is performed in a way that ensures a natural order when sorting tokens.
 */
public class TokenAbstractionComparator implements Comparator<String> {

    /**
     * Compares two tokens in the format "String_Integer".
     *
     * @param o1 The first token to compare.
     * @param o2 The second token to compare.
     * @return A negative integer, zero, or a positive integer if the first token is less than, equal to, or greater
     *         than the second token.
     */
    @Override
    public int compare(String o1, String o2) {
        String[] o1String = o1.split("_");
        String[] o2String = o2.split("_");
        int res = o1String[0].compareTo(o2String[0]);
        if (res != 0) {
            return res;
        }
        int o1Int = Integer.parseInt(o1String[1]);
        int o2Int = Integer.parseInt(o2String[1]);
        return Integer.compare(o1Int, o2Int);
    }
}
