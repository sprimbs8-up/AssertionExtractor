package de.uni_passau.fim.se2.assertion_exctractor.utils;

import java.io.FileWriter;
import java.io.IOException;

public class ErrorChecker {

    private boolean errorFree = true;

    private static final String NOT_PARSEABLE_FILE = "hard-files.jsonl";
    private static final String TOO_LONG = "too-long-files.jsonl";

    private static final SpecialFileExporter EXPORTER_TOO_LONG_FILES = new SpecialFileExporter(TOO_LONG);
    private static final SpecialFileExporter EXPORTER_CORRUPT_FILES = new SpecialFileExporter(NOT_PARSEABLE_FILE);

    private ErrorChecker() {
    }

    private static final ErrorChecker instance = new ErrorChecker();

    public static ErrorChecker getInstance() {
        return instance;
    }

    public void setError() {
        errorFree = false;
    }

    public void resetError() {
        errorFree = true;
    }

    public boolean errorOccurred() {
        return !errorFree;
    }

    public void currentInstance(String instance) {
        EXPORTER_CORRUPT_FILES.log(instance);
    }

    public void logCurrentInstanceTooLong(String instance) {
        EXPORTER_TOO_LONG_FILES.log(instance);

    }

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
