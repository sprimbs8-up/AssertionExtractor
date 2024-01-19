package de.uni_passau.fim.se2.assertion_exctractor.utils;

import java.io.FileWriter;
import java.io.IOException;

/**
 * The ErrorChecker class is a singleton utility for managing and logging errors in file processing. It provides methods
 * to set and reset error flags, check if an error has occurred, and log instances of errors into specific files.
 */
public class ErrorChecker {

    private boolean errorFree = true;
    // Constants for special files that were not parseable.
    private static final String NOT_PARSEABLE_FILE = "hard-files.jsonl";
    private static final String TOO_LONG = "too-long-files.jsonl";

    private static final SpecialFileExporter EXPORTER_TOO_LONG_FILES = new SpecialFileExporter(TOO_LONG);
    private static final SpecialFileExporter EXPORTER_CORRUPT_FILES = new SpecialFileExporter(NOT_PARSEABLE_FILE);

    private ErrorChecker() {
    }

    // Singleton instance
    private static final ErrorChecker instance = new ErrorChecker();

    /**
     * Returns the singleton instance of the ErrorChecker class.
     *
     * @return The singleton instance of the ErrorChecker class.
     */
    public static ErrorChecker getInstance() {
        return instance;
    }

    /**
     * Sets the error flag to indicate an error has occurred.
     */
    public void setError() {
        errorFree = false;
    }

    /**
     * Resets the error flag to indicate no errors have occurred.
     */
    public void resetError() {
        errorFree = true;
    }

    /**
     * Checks if an error has occurred.
     *
     * @return true if an error has occurred, false otherwise.
     */
    public boolean errorOccurred() {
        return !errorFree;
    }

    /**
     * Logs the provided instance into the file for corrupt files.
     *
     * @param instance The instance to be logged.
     */
    public void currentInstance(String instance) {
        EXPORTER_CORRUPT_FILES.log(instance);
    }

    /**
     * Logs the provided instance into the file for too long files.
     *
     * @param instance The instance to be logged.
     */
    public void logCurrentInstanceTooLong(String instance) {
        EXPORTER_TOO_LONG_FILES.log(instance);

    }

    /**
     * Private nested class representing a file exporter for logging instances.
     */
    private static class SpecialFileExporter {

        private boolean append = false;
        private final String filename;

        SpecialFileExporter(String filename) {
            this.filename = filename;
        }

        public void log(String instance) {

            try (FileWriter fw = new FileWriter(filename, append)) {
                fw.write(instance + "\n");// appends the string to the file
                append = true;
            }
            catch (IOException ioe) {
                System.err.println("IOException: " + ioe.getMessage());
            }
        }

    }

}
