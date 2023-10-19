package org.example;

import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.AstNode;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.declaration.MemberDeclarator;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.declaration.MethodDeclaration;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.CommonPreprocessorOptions;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.ast_conversion.AstConverterPreprocessor;

import java.util.Optional;

public class ASTConverterExtension extends AstConverterPreprocessor {
    public ASTConverterExtension(CommonPreprocessorOptions commonOptions, boolean singleMethod, boolean dotGraph) {
        super(commonOptions, singleMethod, dotGraph);
    }
    public Optional<MemberDeclarator<MethodDeclaration>> processSingleMethodAST(String code) {
        return this.processSingleElement(code, true).map(y->(MemberDeclarator<MethodDeclaration>)y).findAny();
    }

}
