package de.uni_passau.fim.se2.assertion_exctractor.parsing;

import java.util.function.Supplier;
import java.util.stream.Stream;

import de.uni_passau.fim.se2.assertion_exctractor.data.JavaDocMethod;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.code.CustomCodeParser;
import de.uni_passau.fim.se2.assertion_exctractor.utils.ErrorChecker;
import de.uni_passau.fim.se2.assertion_exctractor.visitors.JavaDocCollector;
import de.uni_passau.fim.se2.assertion_exctractor.visitors.MethodTokenVisitor;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.generated.JavaParserBaseVisitor;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.parser.CodeParser;

/**
 * The {@link TokenParser} class provides methods for parsing and extracting tokens from Java code, including JavaDoc
 * methods and other token strings.
 */
public class TokenParser {

    private final JavaDocCollector javaDocCollector = new JavaDocCollector();
    protected final MethodTokenVisitor methodTokenVisitor = new MethodTokenVisitor();
    protected final CodeParser codeParser = new CustomCodeParser();

    /**
     * Parses JavaDoc methods from the given code and returns a stream of JavaDocMethod objects.
     *
     * @param code The Java code containing JavaDoc comments.
     * @return A stream of JavaDocMethod objects parsed from the code.
     */
    public Stream<JavaDocMethod> parseClassToJavaDocMethods(final String code) {
        return traverseASTTree(code, javaDocCollector, javaDocCollector::getCollectedJavaDocs);
    }

    /**
     * Converts Java code into a stream of token strings using the MethodTokenVisitor.
     *
     * @param code The Java code to be tokenized.
     * @return A stream of token strings extracted from the code.
     */
    public Stream<String> convertCodeToTokenStrings(final String code) {
        return traverseASTTree(code, methodTokenVisitor, methodTokenVisitor::getCollectedTokens);
    }

    /**
     * Traverses the Abstract Syntax Tree (AST) of the provided code using the specified visitor and returns the result
     * as a stream of objects.
     *
     * @param code           The Java code to be processed.
     * @param visitor        The JavaParserBaseVisitor used for traversing the AST.
     * @param returnConsumer A supplier providing the stream of objects to be returned by the method.
     * @param <T>            The type of objects in the stream.
     * @return A stream of objects obtained by traversing the AST with the provided visitor.
     */
    private <T> Stream<T> traverseASTTree(
        String code, JavaParserBaseVisitor<Void> visitor, Supplier<Stream<T>> returnConsumer
    ) {
        ErrorChecker.getInstance().resetError();
        var codeFragment = codeParser.parseCodeFragment(code);
        if (ErrorChecker.getInstance().errorOccurred()) {
            return Stream.empty();
        }
        visitor.visitClassBodyDeclaration(codeFragment.classBodyDeclaration());
        return returnConsumer.get();
    }
}
