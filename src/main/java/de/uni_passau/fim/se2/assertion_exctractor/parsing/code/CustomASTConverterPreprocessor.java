package de.uni_passau.fim.se2.assertion_exctractor.parsing.code;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import de.uni_passau.fim.se2.assertion_exctractor.utils.AssertionNormalizer;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.AstNode;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.CompilationUnit;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.declaration.MemberDeclarator;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.declaration.MethodDeclaration;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.parser.AstCodeParser;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.parser.ParseException;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.CommonPreprocessorOptions;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.ProcessingException;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.ast_conversion.AstConverterPreprocessor;

public class CustomASTConverterPreprocessor extends AstConverterPreprocessor {

    public CustomASTConverterPreprocessor(
        CommonPreprocessorOptions commonOptions, boolean singleMethod, boolean dotGraph
    ) {
        super(commonOptions, singleMethod, dotGraph);
    }

    @Override
    public Optional<String> processSingleMethod(String code) {
        return this.processSingleElement(code, true).map(Object::toString).findFirst();
    }

    public Optional<String> processSingleClassInstance(String code) {
        return this.processSingleElement(code, false).map(Object::toString).findFirst();
    }

    public Optional<AstNode> parseSingleMethod(String code) {
        return this.processSingleElement(code, true).findAny();
    }

    public Optional<AstNode> parseSingleClass(String code) {
        return this.processSingleElement(AssertionNormalizer.removeJavaDocs(code), false)
            .filter(CompilationUnit.class::isInstance)
            .map(CompilationUnit.class::cast)
            .map(CompilationUnit::typeDeclarations)
            .filter(x -> x.size() == 1)
            .map(x -> x.get(0))
            .map(AstNode.class::cast)
            .findAny();
    }

    @Override
    protected Stream<AstNode> processSingleElement(String code, boolean singleMethod) throws ProcessingException {
        AstCodeParser codeParser = new CustomAstCodeParser();
        if (singleMethod) {
            Stream<MemberDeclarator<MethodDeclaration>> stream = codeParser.parseMethodSkipErrors(code).stream();
            Objects.requireNonNull(AstNode.class);
            return stream.map(AstNode.class::cast);
        }
        try {
            // see https://stackoverflow.com/questions/9078528/tool-to-remove-javadoc-comments
            var node = codeParser.parseCodeToCompilationUnit(code);
            return Stream.of(node);
        }
        catch (ParseException e) {
            return Stream.empty();
        }
    }

}
