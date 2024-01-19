package de.uni_passau.fim.se2.assertion_exctractor.utils;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * The {@link ErrorListener} class extends {@link BaseErrorListener} and provides a custom implementation to handle
 * syntax errors during the parsing phase. It sets the error flag in the ErrorChecker singleton instance when a syntax
 * error is encountered.
 */
public class ErrorListener extends BaseErrorListener {

    @Override
    public void syntaxError(
        Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg,
        RecognitionException e
    ) {
        ErrorChecker.getInstance().setError();
    }
}
