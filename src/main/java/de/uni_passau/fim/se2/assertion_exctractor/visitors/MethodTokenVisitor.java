package de.uni_passau.fim.se2.assertion_exctractor.visitors;

import java.util.stream.Stream;

import org.antlr.v4.runtime.tree.ParseTree;

import de.uni_passau.fim.se2.deepcode.toolbox.ast.generated.JavaParser;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.generated.JavaParserBaseVisitor;

/**
 * The {@link MethodTokenVisitor} class is a visitor for traversing the {@link ParseTree} of a Java class and collecting
 * the code tokens present in type declarations, class body declarations, and method declarations using the JavaParser
 * library.
 */
public class MethodTokenVisitor extends JavaParserBaseVisitor<Void> {
    // Stream builder for collecting code tokens

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

    /**
     * Traverses the ParseTree of a given context, collecting code tokens.
     *
     * @param parseTree The ParseTree to be traversed.
     */
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

    /**
     * Retrieves the collected code tokens as a stream.
     *
     * @return A stream of code tokens.
     */
    public Stream<String> getCollectedTokens() {
        Stream<String> builtStream = codeStreamBuilder.build();
        codeStreamBuilder = Stream.builder();
        return builtStream;
    }
}
