package de.uni_passau.fim.se2.assertion_exctractor.utils;

import me.tongfei.progressbar.ProgressBar;

public final class StatisticsContainer {
    private static StatisticsContainer instance;

    private int usedTestCases = 0;
    private StatisticsContainer() {
    }

    public static StatisticsContainer getInstance() {
        if (instance == null) {
            instance = new StatisticsContainer();
        }
        return instance;
    }

    public void notifyTestCase(){
        usedTestCases++;
    }

    public int getUsedTestCases(){
        return usedTestCases;
    }
}
