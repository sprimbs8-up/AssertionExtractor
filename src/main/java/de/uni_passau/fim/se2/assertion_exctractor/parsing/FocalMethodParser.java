package de.uni_passau.fim.se2.assertion_exctractor.parsing;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

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

    private static class CustomCodeParser extends CodeParser {

        @Override
        public JavaParser parseCodeFragment(String code) {
            JavaParser parser = super.parseCodeFragment(code);
            parser.getErrorListeners().clear();
            parser.addErrorListener(new ErrorListener());
            return parser;
        }
    }

    public Stream<JavaDoc> parseCompleteCode(String code) {
        final CodeParser codeParser = new CustomCodeParser();
        JavaDocParser p = new JavaDocParser();
        ErrorChecker.getInstance().resetError();
        var codeFragment = codeParser.parseCodeFragment(code);
        if (ErrorChecker.getInstance().errorOccurred()) {
            return Stream.empty();
        }
        p.visitCompilationUnit(codeFragment.compilationUnit());
        return p.builder.build();
    }

    public Stream<String> parseMethod(final String code) {
        final CodeParser codeParser = new CustomCodeParser();
        final MethodTokenVisitor visitor = new MethodTokenVisitor();
        ErrorChecker.getInstance().resetError();
        var codeFragment = codeParser.parseCodeFragment(code);
        if (ErrorChecker.getInstance().errorOccurred()) {
            return Stream.empty();
        }
        visitor.visitClassBodyDeclaration(codeFragment.classBodyDeclaration());
        return visitor.codeStream;
    }

    private static class MethodTokenVisitor extends JavaParserBaseVisitor<Void> {

        private Stream<String> codeStream = Stream.empty();

        @Override
        public Void visitClassBodyDeclaration(JavaParser.ClassBodyDeclarationContext ctx) {
            traverseTestCase(ctx);
            return null;
        }

        @Override
        public Void visitMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
            traverseTestCase(ctx);
            return null;
        }

        private void traverseTestCase(ParseTree parseTree) {
            if (parseTree.getChildCount() == 0) {
                codeStream = Stream.concat(codeStream, Stream.of(parseTree.getText()));
                return;
            }
            for (int i = 0; i < parseTree.getChildCount(); i++) {
                ParseTree child = parseTree.getChild(i);
                traverseTestCase(child);
            }
        }
    }

    private static class JavaDocParser extends JavaParserBaseVisitor<Void> {
        Stream.Builder<JavaDoc> builder =Stream.builder();

        @Override
        public Void visitClassBodyDeclaration(JavaParser.ClassBodyDeclarationContext ctx) {
            if(ctx.children.stream().noneMatch(x->x instanceof ErrorNode)) {
                var javaDocCtx = ctx.javadoc();
                var memberDeclaration = ctx.memberDeclaration();
                if (javaDocCtx != null && memberDeclaration != null && memberDeclaration.methodDeclaration() != null) {
                    MethodTokenVisitor visitor = new MethodTokenVisitor();
                    visitor.visitMethodDeclaration(memberDeclaration.methodDeclaration());
                    Stream<String> c = Stream.concat(ctx.modifier().stream().map(RuleContext::getText), visitor.codeStream);
                    builder.add(new JavaDoc(cleanJavaDoc(ctx.javadoc().getText()), c.toList()));
                }
            }
            return null;
        }
        public String cleanJavaDoc(String rawJavaDoc){
            return rawJavaDoc.replaceAll("((\r)?\n( )*\\*)|/\\*\\*|/", " ")
                    .replaceAll("\t"," ")
                    .replaceAll("\n"," ")
                    .replaceAll("\r"," ")
                    .replaceAll(" +"," ")
                    .strip();
        }
    }



    public record JavaDoc(String text, List<String> methodTokens) {}
}
