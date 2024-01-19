package de.uni_passau.fim.se2.assertion_exctractor.parsing.code;

import de.uni_passau.fim.se2.assertion_exctractor.utils.ErrorListener;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.generated.JavaParser;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.parser.CodeParser;

/**
 * The {@link CustomCodeParser} class extends CodeParser and provides a custom implementation for parsing Java code
 * fragments using the JavaParser library, with added error handling through a custom error listener.
 */
public class CustomCodeParser extends CodeParser {

    @Override
    public JavaParser parseCodeFragment(String code) {
        JavaParser parser = super.parseCodeFragment(code);
        parser.getErrorListeners().clear();
        parser.addErrorListener(new ErrorListener());
        return parser;
    }
}
