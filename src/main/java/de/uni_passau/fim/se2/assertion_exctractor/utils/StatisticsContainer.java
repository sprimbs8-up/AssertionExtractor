package de.uni_passau.fim.se2.assertion_exctractor.utils;

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

    public synchronized void notifyTestCase() {
        usedTestCases++;
    }

    public synchronized int getUsedTestCases() {
        return usedTestCases;
    }
}
