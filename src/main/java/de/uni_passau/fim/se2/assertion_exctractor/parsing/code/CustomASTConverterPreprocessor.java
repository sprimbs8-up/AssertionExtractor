package de.uni_passau.fim.se2.assertion_exctractor.parsing.code;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import de.uni_passau.fim.se2.assertion_exctractor.utils.Utils;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.AstNode;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.CompilationUnit;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.Modifier;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.declaration.ConstructorDeclaration;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.declaration.MemberDeclarator;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.declaration.MethodDeclaration;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.declaration.TypeDeclarator;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.parser.ParseException;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.CommonPreprocessorOptions;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.ProcessingException;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.ast_conversion.AstConverterPreprocessor;

/**
 * The {@link CustomASTConverterPreprocessor} class extends {@link AstConverterPreprocessor} and provides custom methods
 * for processing and parsing Java code fragments using a custom code parser (CustomAstCodeParser).
 */
public class CustomASTConverterPreprocessor extends AstConverterPreprocessor {

    public CustomASTConverterPreprocessor(
        CommonPreprocessorOptions commonOptions, boolean singleMethod, boolean dotGraph
    ) {
        super(commonOptions, singleMethod, dotGraph);
    }

    /**
     * Processes a single method code fragment and returns the processed code as a String.
     *
     * @param code The Java code fragment of a single method to be processed.
     * @return An optional containing the processed code as a String, or empty if processing fails.
     */
    @Override
    public Optional<String> processSingleMethod(String code) {
        return this.processSingleElement(code, true).map(Object::toString).findFirst();
    }

    /**
     * Parses a single method code fragment and returns the resulting AstNode.
     *
     * @param code The Java code fragment of a single method to be parsed.
     * @return An optional containing the parsed AstNode, or empty if parsing fails.
     */
    public Optional<AstNode> parseSingleMethod(String code) {
        return this.processSingleElement(code, true).findAny();
    }

    /**
     * Parses a single class code fragment and returns the resulting AstNode representing the class.
     *
     * @param code The Java code fragment of a single class to be parsed.
     * @return An optional containing the parsed AstNode representing the class, or empty if parsing fails.
     */
    public Optional<AstNode> parseSingleClass(String code) {
        return this.processSingleElement(Utils.removeJavaDocs(code), false)
            .filter(CompilationUnit.class::isInstance)
            .map(CompilationUnit.class::cast)
            .map(CompilationUnit::typeDeclarations)
            .map(declarations -> {
                if (declarations.size() == 1) {
                    return Optional.of(declarations.get(0));
                }
                else {
                    // simplification: we assume that we only care about the single public class
                    // here and ignore other (package-)private classes in the file
                    return getPublicType(declarations);
                }
            })
            .flatMap(Optional::stream)
            .map(AstNode.class::cast)
            .findAny();
    }

    private static Optional<TypeDeclarator> getPublicType(final List<TypeDeclarator> declarations) {
        // there can only be up to a single public top-level type in a file
        return declarations.stream()
            .filter(decl -> decl.modifiers().basicModifiers().contains(Modifier.PUBLIC))
            .findFirst();
    }

    /**
     * Processes a single code element (method or class) and returns a stream of AstNodes.
     *
     * @param code         The Java code fragment to be processed.
     * @param singleMethod A flag indicating whether to process a single method or the entire class.
     * @return A stream of AstNodes representing the processed code elements.
     * @throws ProcessingException If an error occurs during processing.
     */
    @Override
    protected Stream<AstNode> processSingleElement(String code, boolean singleMethod) throws ProcessingException {
        CustomAstCodeParser codeParser = new CustomAstCodeParser();
        if (singleMethod) {
            Optional<MemberDeclarator<MethodDeclaration>> methodOpt = codeParser.parseMethodSkipErrors(code);
            if (methodOpt.isPresent()) {
                Stream<MemberDeclarator<MethodDeclaration>> stream = methodOpt.stream();
                Objects.requireNonNull(AstNode.class);
                return stream.map(AstNode.class::cast);
            }
            Optional<MemberDeclarator<ConstructorDeclaration>> constOpt = codeParser.parseConstructorSkipErrors(code);
            Objects.requireNonNull(AstNode.class);
            return constOpt.stream().map(AstNode.class::cast);
        }
        try {
            var node = codeParser.parseCodeToCompilationUnit(code);
            return Stream.of(node);
        }
        catch (ParseException e) {
            return Stream.empty();
        }
    }
}
