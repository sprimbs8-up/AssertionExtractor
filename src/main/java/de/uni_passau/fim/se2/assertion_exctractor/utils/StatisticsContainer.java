package de.uni_passau.fim.se2.assertion_exctractor.utils;

import de.uni_passau.fim.se2.assertion_exctractor.processors.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StatisticsContainer {
    private static final Logger LOG = LoggerFactory.getLogger(StatisticsContainer.class);

    private static StatisticsContainer instance;

    private int usedTestCases = 0;
    private int notParseable = 0;
    private int parsedTestCases = 0;
    private int tooLongTestCases = 0;

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

    public synchronized void notifyNotParseable() {
        notParseable++;
    }
    public synchronized void notifyParsedTestCase() {
        parsedTestCases++;
    }
    public synchronized void notifyTooLongTestCase() {
        tooLongTestCases++;
    }

    public void logPreprocessingStats() {
        int totalTestCases = ProgressBarContainer.getInstance().getTotalCount();
        float percentageUsed = (float) usedTestCases / totalTestCases * 100;
        float percentageCorrupt = (float) notParseable / (parsedTestCases+notParseable+tooLongTestCases) * 100;
        float percentageTooLong = (float) tooLongTestCases / (parsedTestCases+notParseable+tooLongTestCases) * 100;
        LOG.info("================================================================");
        LOG.info("Statistics:");
        LOG.info("Usable test cases:                        {}", usedTestCases);
        LOG.info("                  (in percentage):        {}%", percentageUsed);
        LOG.info("Too long test cases:                      {}", tooLongTestCases);
        LOG.info("                  (in percentage):        {}%", percentageTooLong);
        LOG.info("Corrupt instances (of usable test cases): {}", notParseable);
        LOG.info("                  (in percentage):        {}%", percentageCorrupt);
        LOG.info("Total testCases:                          {}", totalTestCases);
        LOG.info("================================================================");
    }
}
