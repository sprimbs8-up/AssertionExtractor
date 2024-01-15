package de.uni_passau.fim.se2.assertion_exctractor.parsing;

import java.util.stream.Stream;

import de.uni_passau.fim.se2.assertion_exctractor.data.JavaDocMethod;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.code.CustomCodeParser;
import de.uni_passau.fim.se2.assertion_exctractor.utils.ErrorChecker;
import de.uni_passau.fim.se2.assertion_exctractor.visitors.JavaDocCollector;
import de.uni_passau.fim.se2.assertion_exctractor.visitors.MethodTokenVisitor;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.parser.CodeParser;

public class FocalMethodParser {

    private final JavaDocCollector javaDocCollector = new JavaDocCollector();
    protected final MethodTokenVisitor methodTokenVisitor = new MethodTokenVisitor();
    protected final CodeParser codeParser = new CustomCodeParser();

    public Stream<JavaDocMethod> parseClassToJavaDocMethods(final String code) {
        ErrorChecker.getInstance().resetError();
        var codeFragment = codeParser.parseCodeFragment(code);
        if (ErrorChecker.getInstance().errorOccurred()) {
            return Stream.empty();
        }
        javaDocCollector.visitCompilationUnit(codeFragment.compilationUnit());
        return javaDocCollector.getCollectedJavaDocs();
    }

    public Stream<String> parseMethodToMethodTokens(final String code) {
        ErrorChecker.getInstance().resetError();
        var codeFragment = codeParser.parseCodeFragment(code);
        if (ErrorChecker.getInstance().errorOccurred()) {
            return Stream.empty();
        }
        methodTokenVisitor.visitClassBodyDeclaration(codeFragment.classBodyDeclaration());
        return methodTokenVisitor.getCollectedTokens();
    }

    public Stream<String> parseClassToClassTokens(final String code) {
        ErrorChecker.getInstance().resetError();
        var codeFragment = codeParser.parseCodeFragment(code);
        if (ErrorChecker.getInstance().errorOccurred()) {
            return Stream.empty();
        }
        methodTokenVisitor.visitCompilationUnit(codeFragment.compilationUnit());
        return methodTokenVisitor.getCollectedTokens();
    }

}
