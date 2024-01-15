package de.uni_passau.fim.se2.assertion_exctractor.parsing;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import de.uni_passau.fim.se2.assertion_exctractor.parsing.code.CustomCodeParser;
import de.uni_passau.fim.se2.assertion_exctractor.utils.AssertionNormalizer;
import de.uni_passau.fim.se2.assertion_exctractor.utils.ErrorChecker;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.generated.JavaParser;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.generated.JavaParserBaseVisitor;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.parser.CodeParser;

public class TestCaseParser {

    public Optional<TestCase> parseTestCase(final String code) {
        String cleanedCode = AssertionNormalizer.normalizeAssertions(code);
        final CodeParser codeParser = new CustomCodeParser();
        final MethodTokenVisitor visitor = new MethodTokenVisitor();
        ErrorChecker.getInstance().resetError();
        var codeFragment = codeParser.parseCodeFragment(cleanedCode);
        if (ErrorChecker.getInstance().errorOccurred()) {
            return Optional.empty();
        }
        visitor.visitClassBodyDeclaration(codeFragment.classBodyDeclaration());
        return Optional.of(new TestCase(visitor.testElements.toList()));
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
                    if (assertionTypeOpt.isPresent() && isValidAssertion(assertionTypeOpt.get(), child)) {
                        testElements = Stream
                            .concat(testElements, Stream.of(new TestSequence(List.copyOf(codeStream.toList()))));
                        codeStream = Stream.empty();
                        testElements = Stream.concat(
                            testElements,
                            Stream.of(
                                new Assertion(assertionTypeOpt.get(), getAssertionTokens(child).toList(), parseTree)
                            )
                        );
                        continue;

                    }
                }
                if (
                    child instanceof JavaParser.StatementContext stCtx
                        && stCtx.getChildCount() == 3
                        && stCtx.getChild(0) instanceof TerminalNode tNode
                        && tNode.getText().equals("try")
                        && stCtx.getChild(1) instanceof JavaParser.BlockContext blCtx

                ) {
                    var x = blCtx.children.stream().filter(Predicate.not(TerminalNode.class::isInstance))
                        .reduce((f, s) -> s);
                    if (
                        x.isPresent()
                            && x.get().getText().contains("fail")
                            && stCtx.getChild(2) instanceof JavaParser.CatchClauseContext caClaCtx
                            && caClaCtx.children.get(5) instanceof JavaParser.BlockContext caBlockCtx
                            && caBlockCtx.children.stream().filter(Predicate.not(TerminalNode.class::isInstance))
                                .count() <= 2
                    ) {
                        testElements = Stream
                            .concat(testElements, Stream.of(new TestSequence(List.copyOf(codeStream.toList()))));
                        codeStream = Stream.empty();
                        testElements = Stream.concat(
                            testElements,
                            Stream.of(new TryCatchAssertion(getAssertionTokens(blCtx).toList()))
                        );
                        continue;
                    }
                }
                traverseTestCase(child);
            }

        }

        private boolean isValidAssertion(AssertionType type, ParseTree possibleAssertion) {
            long count = IntStream.range(0, possibleAssertion.getChildCount())
                .mapToObj(possibleAssertion::getChild)
                .filter(JavaParser.ExpressionListContext.class::isInstance)
                .map(JavaParser.ExpressionListContext.class::cast)
                .flatMap(x -> x.children.stream())
                .filter(Predicate.not(TerminalNode.class::isInstance))
                .count();
            return type.getNumParameters() == count;
        }

        private Stream<String> getAssertionTokens(ParseTree assertionChild) {
            if (assertionChild.getChildCount() == 0) {
                return Stream.of(assertionChild.getText());
            }
            Stream<String> stream = Stream.empty();
            for (int i = 0; i < assertionChild.getChildCount(); i++) {
                if (!assertionChild.getChild(i).getText().contains("fail")) {
                    stream = Stream.concat(stream, getAssertionTokens(assertionChild.getChild(i)));
                }
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
