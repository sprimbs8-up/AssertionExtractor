package de.uni_passau.fim.se2.assertion_exctractor.visitors;

import java.util.Optional;
import java.util.stream.Stream;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;

import de.uni_passau.fim.se2.assertion_exctractor.data.JavaDocMethod;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.generated.JavaParser;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.generated.JavaParserBaseVisitor;

/**
 * The {@link JavaDocCollector} class is a visitor for parsing and collecting {@link JavaDocMethod} information
 * associated with method declarations in a Java class using the JavaParser library.
 */
public class JavaDocCollector extends JavaParserBaseVisitor<Void> {

    // Stream builder for collecting JavaDocMethod instances
    private Stream.Builder<JavaDocMethod> builder = Stream.builder();

    /**
     * Visits a class body declaration and extracts JavaDoc information from method declarations.
     *
     * @param ctx The context representing a class body declaration in the JavaParser tree.
     * @return Always returns null as this method doesn't produce a result.
     */
    @Override
    public Void visitClassBodyDeclaration(JavaParser.ClassBodyDeclarationContext ctx) {
        if (Optional.ofNullable(ctx.children).stream().noneMatch(ErrorNode.class::isInstance)) {
            var javaDocCtx = ctx.javadoc();
            var memberDeclaration = ctx.memberDeclaration();
            if (javaDocCtx != null && memberDeclaration != null && memberDeclaration.methodDeclaration() != null) {
                MethodTokenVisitor visitor = new MethodTokenVisitor();
                visitor.visitMethodDeclaration(memberDeclaration.methodDeclaration());
                Stream<String> c = Stream
                    .concat(ctx.modifier().stream().map(RuleContext::getText), visitor.getCollectedTokens());
                builder.add(new JavaDocMethod(cleanJavaDoc(ctx.javadoc().getText()), c.toList()));
            } else {
                if (memberDeclaration != null) {
                    memberDeclaration.accept(this);
                }
            }
        }
        return null;
    }

    /**
     * Cleans up raw JavaDoc content by removing unnecessary characters and whitespace.
     *
     * @param rawJavaDoc The raw JavaDoc content to be cleaned.
     * @return The cleaned JavaDoc content.
     */
    private static String cleanJavaDoc(String rawJavaDoc) {
        return rawJavaDoc.replaceAll("((\r)?\n( )*\\*)|/\\*\\*|/", " ")
            .replaceAll("\t", " ")
            .replaceAll("\n", " ")
            .replaceAll("\r", " ")
            .replaceAll(" +", " ")
            .strip();
    }

    /**
     * Retrieves the collected JavaDoc methods as a stream.
     *
     * @return A stream of JavaDocMethod instances.
     */
    public Stream<JavaDocMethod> getCollectedJavaDocs() {
        Stream<JavaDocMethod> builtStream = builder.build();
        builder = Stream.builder();
        return builtStream;
    }
}
