package org.example;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.antlr.v4.runtime.tree.ParseTree;

import de.uni_passau.fim.se2.deepcode.toolbox.ast.generated.JavaParser;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.generated.JavaParserBaseVisitor;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.parser.CodeParser;
import de.uni_passau.fim.se2.deepcode.toolbox.tokens.MethodTokenExtractor;

public class MethodTokenExtractorAssertions extends MethodTokenExtractor {

    public TestCase extractAssertions(
        final String code
    ) {
        final CodeParser codeParser = new CodeParser();
        final MethodTokenVisitor visitor = new MethodTokenVisitor();
        visitor.visitClassBodyDeclaration(codeParser.parseCodeFragment(code).classBodyDeclaration());
        return new TestCase(visitor.testElements.toList());
    }

    private static class MethodTokenVisitor extends JavaParserBaseVisitor<Void> {

        private Stream<String> codeStream = Stream.empty();
        public Stream<TestElement> testElements = Stream.empty();

        @Override
        public Void visitClassBodyDeclaration(JavaParser.ClassBodyDeclarationContext ctx) {
            traverseTestCase(ctx);
            testElements = Stream.concat(testElements, Stream.of(new TestSequence(List.copyOf(codeStream.toList()))));
            return null;
        }

        private void traverseTestCase(ParseTree parseTree) {
            if (parseTree.getChildCount() == 0) {
                codeStream = Stream.concat(codeStream, Stream.of(parseTree.getText()));
                return;
            }
            for (int i = 0; i < parseTree.getChildCount(); i++) {
                ParseTree child = parseTree.getChild(i);
                if (
                    child instanceof JavaParser.MethodCallContext methodCallContext &&
                        methodCallContext.getChild(0) instanceof JavaParser.IdentifierContext identifierContext
                ) {
                    Optional<AssertionType> assertionTypeOpt = AssertionType
                        .parseAssertion(identifierContext.getText());
                    if (assertionTypeOpt.isPresent()) {
                        testElements = Stream
                            .concat(testElements, Stream.of(new TestSequence(List.copyOf(codeStream.toList()))));
                        codeStream = Stream.empty();
                        testElements = Stream.concat(
                            testElements,
                            Stream.of(new Assertion(assertionTypeOpt.get(), parseAssertion(child).toList()))
                        );
                        continue;

                    }
                }
                traverseTestCase(child);
            }

        }

        private Stream<String> parseAssertion(ParseTree assertionChild) {
            if (assertionChild.getChildCount() == 0) {
                return Stream.of(assertionChild.getText());
            }
            Stream<String> stream = Stream.empty();
            for (int i = 0; i < assertionChild.getChildCount(); i++) {
                stream = Stream.concat(stream, parseAssertion(assertionChild.getChild(i)));
            }
            return stream;
        }

        @Override
        public Void visitBlockStatement(JavaParser.BlockStatementContext ctx) {
            if (
                ctx.getChild(0) instanceof JavaParser.StatementContext statementContext &&
                    statementContext.getChild(0) instanceof JavaParser.ExpressionContext expressionContext &&
                    expressionContext.getChild(0) instanceof JavaParser.MethodCallContext methodCallContext &&
                    methodCallContext.getChild(0) instanceof JavaParser.IdentifierContext identifierContext
                    && identifierContext.children.get(0).getText().contains("assert")
            ) {
                for (var child : methodCallContext.children) {
                    System.out.println(child.getText());
                }
            }
            return super.visitBlockStatement(ctx);
        }
    }
}
