package de.uni_passau.fim.se2.assertion_exctractor.utils;

public class ErrorChecker {

    private boolean errorFree = true;

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
}
