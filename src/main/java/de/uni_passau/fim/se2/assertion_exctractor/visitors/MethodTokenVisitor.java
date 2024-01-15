package de.uni_passau.fim.se2.assertion_exctractor.visitors;

import java.util.stream.Stream;

import org.antlr.v4.runtime.tree.ParseTree;

import de.uni_passau.fim.se2.deepcode.toolbox.ast.generated.JavaParser;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.generated.JavaParserBaseVisitor;

public class MethodTokenVisitor extends JavaParserBaseVisitor<Void> {

    private Stream.Builder<String> codeStreamBuilder = Stream.builder();

    @Override
    public Void visitTypeDeclaration(JavaParser.TypeDeclarationContext ctx) {
        traverseTestCase(ctx);
        return null;
    }

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
            codeStreamBuilder.add(parseTree.getText());
            return;
        }
        for (int i = 0; i < parseTree.getChildCount(); i++) {
            ParseTree child = parseTree.getChild(i);
            traverseTestCase(child);
        }
    }

    public Stream<String> getCollectedTokens() {
        Stream<String> builtStream = codeStreamBuilder.build();
        codeStreamBuilder = Stream.builder();
        return builtStream;
    }
}
