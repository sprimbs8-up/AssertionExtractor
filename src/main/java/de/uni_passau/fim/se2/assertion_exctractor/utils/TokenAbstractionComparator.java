package de.uni_passau.fim.se2.assertion_exctractor.utils;

import java.util.Comparator;

public class TokenAbstractionComparator implements Comparator<String> {

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
