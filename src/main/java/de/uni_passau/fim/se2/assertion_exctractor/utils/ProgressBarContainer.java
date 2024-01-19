package de.uni_passau.fim.se2.assertion_exctractor.utils;

import me.tongfei.progressbar.ProgressBar;

/**
 * The {@link ProgressBarContainer} class is a singleton container for managing a progress bar instance. It provides
 * methods for setting up, updating, and retrieving information from the progress bar.
 */
public final class ProgressBarContainer {

    // Singleton instance
    private static ProgressBarContainer instance;
    // Progress bar instance
    private ProgressBar progressBar;

    private ProgressBarContainer() {
    }

    /**
     * Returns the singleton instance of the ProgressBarContainer class.
     *
     * @return The singleton instance of the ProgressBarContainer class.
     */
    public static ProgressBarContainer getInstance() {
        if (instance == null) {
            instance = new ProgressBarContainer();
        }
        return instance;
    }

    /**
     * Sets up the progress bar with the given description and counter.
     *
     * @param description The description for the progress bar.
     * @param counter     The initial counter value for the progress bar.
     */
    public void setProgressBar(String description, int counter) {
        progressBar = new ProgressBar(description, counter);
    }

    /**
     * Notifies the start of the progress bar.
     */
    public void notifyStart() {
        progressBar.start();
    }

    /**
     * Notifies a step in the progress bar.
     */
    public void notifyStep() {
        progressBar.step();
    }

    /**
     * Notifies the stop of the progress bar.
     */
    public void notifyStop() {
        progressBar.stop();
    }

    /**
     * Retrieves the total count of the progress bar.
     *
     * @return The total count of the progress bar.
     */
    public int getTotalCount() {
        return (int) progressBar.getMax();
    }
}
