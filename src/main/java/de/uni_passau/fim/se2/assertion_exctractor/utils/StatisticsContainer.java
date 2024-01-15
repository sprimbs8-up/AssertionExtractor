package de.uni_passau.fim.se2.assertion_exctractor.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StatisticsContainer {

    private static final String FOCAL_METHOD = "focal-method";
    private static final String TEST_METHOD = "test-method";
    private static final String FOCAL_CLASS = "focal-class";
    private static final String TEST_CLASS = "test-class";
    private static final Logger LOG = LoggerFactory.getLogger(StatisticsContainer.class);

    private static StatisticsContainer instance;

    private int usedTestCases = 0;
    private int notParseable = 0;
    private int parsedTestCases = 0;
    private int tooLongTestCases = 0;
    private int afterAssertionExtraction = 0;

    private final Map<String, Integer> notParsableMap = new HashMap<>();

    private StatisticsContainer() {
        this.notParsableMap.put(FOCAL_METHOD, 0);
        this.notParsableMap.put(FOCAL_CLASS, 0);
        this.notParsableMap.put(TEST_METHOD, 0);
        this.notParsableMap.put(TEST_CLASS, 0);

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

    public synchronized void notifyNotParseable(
        boolean focalMethod, boolean testMethod, boolean focalClass, boolean testClass
    ) {
        notParseable++;
        BiFunction<String, Integer, Integer> updateFunction = (x, y) -> (y != null ? y + 1 : 1);
        if (focalMethod) {
            notParsableMap.compute(FOCAL_METHOD, updateFunction);
        }
        if (testMethod) {
            notParsableMap.compute(TEST_METHOD, updateFunction);
        }
        if (focalClass) {
            notParsableMap.compute(FOCAL_CLASS, updateFunction);
        }
        if (testClass) {
            notParsableMap.compute(TEST_CLASS, updateFunction);
        }
    }

    public synchronized void notifyParsedTestCase() {
        parsedTestCases++;
    }

    public synchronized void notifiedUnusableTestCaseWithoutAssertions() {
        afterAssertionExtraction++;
    }

    public synchronized void notifyTooLongTestCase() {
        tooLongTestCases++;
    }

    public void logPreprocessingStats() {
        int totalTestCases = ProgressBarContainer.getInstance().getTotalCount();
        float percentageUsed = (float) usedTestCases / totalTestCases * 100;
        float percentageCorrupt = (float) notParseable / (parsedTestCases + notParseable + tooLongTestCases) * 100;
        float percentageTooLong = (float) tooLongTestCases / (parsedTestCases + notParseable + tooLongTestCases) * 100;
        LOG.info("================================================================");
        LOG.info("Statistics:");
        LOG.info("----------------------------------------------------------------");
        LOG.info("Usable test cases:                         {}", usedTestCases);
        LOG.info("                  (in percentage):         {}%", percentageUsed);
        LOG.info("----------------------------------------------------------------");
        LOG.info("Too long test cases:                       {}", tooLongTestCases);
        LOG.info("                  (in percentage):         {}%", percentageTooLong);
        LOG.info("----------------------------------------------------------------");
        LOG.info("Corrupt instances (of usable test cases):  {}", notParseable);
        LOG.info("                  (in percentage):         {}%", percentageCorrupt);
        LOG.info("                  ----------------------------------------------");
        LOG.info("                  (focal methods):         {}", notParsableMap.get(FOCAL_METHOD));
        LOG.info("                  (test methods):          {}", notParsableMap.get(TEST_METHOD));
        LOG.info("                  (focal classes):         {}", notParsableMap.get(FOCAL_CLASS));
        LOG.info("                  (test Classes):          {}", notParsableMap.get(TEST_CLASS));
        LOG.info("                  ----------------------------------------------");
        LOG.info("                  (after assertion clean): {}", afterAssertionExtraction);
        LOG.info("----------------------------------------------------------------");
        LOG.info("Total testCases:                           {}", totalTestCases);
        LOG.info("================================================================");
    }

}
