package de.uni_passau.fim.se2.assertion_exctractor.parsing;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import de.uni_passau.fim.se2.assertion_exctractor.data.JavaDocMethod;
import de.uni_passau.fim.se2.assertion_exctractor.visitors.JavaDocCollector;
import de.uni_passau.fim.se2.assertion_exctractor.visitors.MethodTokenVisitor;
import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;

import de.uni_passau.fim.se2.assertion_exctractor.utils.ErrorChecker;
import de.uni_passau.fim.se2.assertion_exctractor.utils.ErrorListener;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.generated.JavaParser;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.generated.JavaParserBaseVisitor;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.parser.CodeParser;

public class FocalMethodParser {

    private final JavaDocCollector javaDocCollector = new JavaDocCollector();
    private final MethodTokenVisitor methodTokenVisitor = new MethodTokenVisitor();
    private final CodeParser codeParser = new CustomCodeParser();

    private static class CustomCodeParser extends CodeParser {

        @Override
        public JavaParser parseCodeFragment(String code) {
            JavaParser parser = super.parseCodeFragment(code);
            parser.getErrorListeners().clear();
            parser.addErrorListener(new ErrorListener());
            return parser;
        }
    }

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

}
