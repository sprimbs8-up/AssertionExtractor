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

public class TokenParser {

    private final JavaDocCollector javaDocCollector = new JavaDocCollector();
    protected final MethodTokenVisitor methodTokenVisitor = new MethodTokenVisitor();
    protected final CodeParser codeParser = new CustomCodeParser();

    public Stream<JavaDocMethod> parseClassToJavaDocMethods(final String code) {
        return traverseASTTree(code, javaDocCollector, javaDocCollector::getCollectedJavaDocs);
    }

    public Stream<String> convertCodeToTokenStrings(final String code) {
        return traverseASTTree(code, methodTokenVisitor, methodTokenVisitor::getCollectedTokens);
    }

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
