package de.uni_passau.fim.se2.assertion_exctractor.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link StatisticsContainer} class is a singleton container for tracking and logging statistics related to the
 * preprocessing of test cases. It provides methods for notifying various events such as parsing, errors, and extraction
 * during the preprocessing phase.
 */
public final class StatisticsContainer {
    // Constants for statistics categories

    private static final String FOCAL_METHOD = "focal-method";
    private static final String TEST_METHOD = "test-method";
    private static final String FOCAL_CLASS = "focal-class";
    private static final String TEST_CLASS = "test-class";
    private static final String TEST_METHOD_AFTER = "test-method-after";
    private static final String FOCAL_METHOD_AFTER = "focal-method-after";

    // Logger for logging statistics

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsContainer.class);
    // Singleton instance

    private static StatisticsContainer instance;
    // Counters for statistics

    private int usedTestCases = 0;
    private int notParseable = 0;
    private int parsedTestCases = 0;
    private int tooLongTestCases = 0;
    private int afterAssertionExtraction = 0;

    private final Map<String, Integer> notParsableMap = new HashMap<>();
    // Private constructor to enforce singleton pattern

    private StatisticsContainer() {
        this.notParsableMap.put(FOCAL_METHOD, 0);
        this.notParsableMap.put(FOCAL_CLASS, 0);
        this.notParsableMap.put(TEST_METHOD, 0);
        this.notParsableMap.put(TEST_CLASS, 0);
        this.notParsableMap.put(TEST_METHOD_AFTER, 0);
        this.notParsableMap.put(FOCAL_METHOD_AFTER, 0);

    }

    /**
     * Returns the singleton instance of the StatisticsContainer class.
     *
     * @return The singleton instance of the StatisticsContainer class.
     */
    public static StatisticsContainer getInstance() {
        if (instance == null) {
            instance = new StatisticsContainer();
        }
        return instance;
    }

    /**
     * Notifies that a test case is being processed.
     */
    public synchronized void notifyTestCase() {
        usedTestCases++;
    }

    /**
     * Notifies that a test case is not parseable, and updates the counters.
     *
     * @param focalMethod True if the focal method is not parseable, false otherwise.
     * @param testMethod  True if the test method is not parseable, false otherwise.
     * @param focalClass  True if the focal class is not parseable, false otherwise.
     * @param testClass   True if the test class is not parseable, false otherwise.
     */
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

    /**
     * Notifies that an after assertion extraction is not parseable, and updates the counters.
     *
     * @param focalMethod True if the focal method is not parseable, false otherwise.
     * @param testMethod  True if the test method is not parseable, false otherwise.
     */
    public synchronized void notifyNotParseableAfter(
        boolean focalMethod, boolean testMethod
    ) {
        notParseable++;
        BiFunction<String, Integer, Integer> updateFunction = (x, y) -> (y != null ? y + 1 : 1);
        if (focalMethod) {
            notParsableMap.compute(FOCAL_METHOD_AFTER, updateFunction);
        }
        if (testMethod) {
            notParsableMap.compute(TEST_METHOD_AFTER, updateFunction);
        }
    }

    /**
     * Notifies that a test case has been successfully parsed.
     */
    public synchronized void notifyParsedTestCase() {
        parsedTestCases++;
    }

    /**
     * Notifies that an unusable test case without assertions has been encountered.
     */
    public synchronized void notifiedUnusableTestCaseWithoutAssertions() {
        afterAssertionExtraction++;
    }

    /**
     * Notifies that a test case is too long.
     */
    public synchronized void notifyTooLongTestCase() {
        tooLongTestCases++;
    }

    /**
     * Logs the preprocessing statistics, including counts and percentages.
     */
    public void logPreprocessingStats() {
        int totalTestCases = ProgressBarContainer.getInstance().getTotalCount();
        float percentageUsed = (float) usedTestCases / totalTestCases * 100;
        float percentageCorrupt = (float) notParseable / (parsedTestCases + notParseable + tooLongTestCases) * 100;
        float percentageTooLong = (float) tooLongTestCases / (parsedTestCases + notParseable + tooLongTestCases) * 100;
        LOG.info("==================================================================");
        LOG.info("Statistics:");
        LOG.info("------------------------------------------------------------------");
        LOG.info("Usable test cases:                                    {}", usedTestCases);
        LOG.info("                  (in percentage):                    {}%", percentageUsed);
        LOG.info("------------------------------------------------------------------");
        LOG.info("Too long test cases:                                  {}", tooLongTestCases);
        LOG.info("                  (in percentage):                    {}%", percentageTooLong);
        LOG.info("------------------------------------------------------------------");
        LOG.info("Corrupt instances (of usable test cases):             {}", notParseable);
        LOG.info("                  (in percentage):                    {}%", percentageCorrupt);
        LOG.info("                  ------------------------------------------------");
        LOG.info("                  (focal methods):                    {}", notParsableMap.get(FOCAL_METHOD));
        LOG.info("                  (test Classes):                     {}", notParsableMap.get(TEST_CLASS));
        LOG.info("                  (test methods):                     {}", notParsableMap.get(TEST_METHOD));
        LOG.info("                  (focal classes):                    {}", notParsableMap.get(FOCAL_CLASS));
        LOG.info("                  ------------------------------------------------");
        LOG.info("                  (after assertion clean):            {}", afterAssertionExtraction);
        LOG.info("                  (test instances after extraction):  {}", notParsableMap.get(TEST_METHOD_AFTER));
        LOG.info("                  (focal instances after extraction): {}", notParsableMap.get(FOCAL_METHOD_AFTER));
        LOG.info("------------------------------------------------------------------");
        LOG.info("Total testCases:                                      {}", totalTestCases);
        LOG.info("===================================================================");
    }

}
