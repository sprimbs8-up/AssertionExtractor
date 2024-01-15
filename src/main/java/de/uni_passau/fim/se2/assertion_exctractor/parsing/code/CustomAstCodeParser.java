package de.uni_passau.fim.se2.assertion_exctractor.parsing.code;

import de.uni_passau.fim.se2.deepcode.toolbox.ast.generated.JavaParser;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.parser.AstCodeParser;

public class CustomAstCodeParser extends AstCodeParser {

    private final CustomCodeParser codeParser = new CustomCodeParser();

    @Override
    public JavaParser parseCodeFragment(String code) {
        return codeParser.parseCodeFragment(code);
    }
}
