package de.uni_passau.fim.se2.assertion_exctractor.parsing.code;

import de.uni_passau.fim.se2.deepcode.toolbox.ast.generated.JavaParser;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.parser.AstCodeParser;

/**
 * The CustomAstCodeParser class extends the AstCodeParser and provides a custom implementation for parsing Java code
 * fragments using the CustomCodeParser.
 */
public class CustomAstCodeParser extends AstCodeParser {
    // Custom code parser instance

    private final CustomCodeParser codeParser = new CustomCodeParser();

    /**
     * Parses a Java code fragment using the custom code parser.
     *
     * @param code The Java code fragment to be parsed.
     * @return A JavaParser object representing the parsed code.
     */
    @Override
    public JavaParser parseCodeFragment(String code) {
        return codeParser.parseCodeFragment(code);
    }
}
