package de.uni_passau.fim.se2.assertion_exctractor.parsing.code;

import java.util.Optional;

import de.uni_passau.fim.se2.deepcode.toolbox.ast.generated.JavaParser;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.CompilationUnit;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.declaration.*;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.parser.AstCodeParser;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.parser.InternalParseException;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.parser.ParseException;

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

    public Optional<MemberDeclarator<ConstructorDeclaration>> parseConstructorSkipErrors(String code) {

        try {
            CompilationUnit unit = (CompilationUnit) parseCodeToCompilationUnit("public class Dummy { " + code + "}");
            ClassDeclaration x = (ClassDeclaration) unit.typeDeclarations().get(0).declaration();
            ClassBodyDeclaration decl = x.body().declarations().get(0);
            MemberDeclarator<ConstructorDeclaration> consDecl = ((MemberDeclarator<ConstructorDeclaration>) decl);
            return Optional.of(consDecl);
        }
        catch (InternalParseException | ParseException var4) {
            return Optional.empty();
        }
    }

}
