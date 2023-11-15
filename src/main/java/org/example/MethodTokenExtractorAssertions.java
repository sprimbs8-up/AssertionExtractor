package org.example;

import de.uni_passau.fim.se2.deepcode.toolbox.ast.generated.JavaParser;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.generated.JavaParserBaseVisitor;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.parser.CodeParser;
import de.uni_passau.fim.se2.deepcode.toolbox.tokens.CodeTokenExtractor;
import de.uni_passau.fim.se2.deepcode.toolbox.tokens.MethodTokenExtractor;
import de.uni_passau.fim.se2.deepcode.toolbox.tokens.MethodTokens;
import de.uni_passau.fim.se2.deepcode.toolbox.util.StringUtil;
import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MethodTokenExtractorAssertions extends MethodTokenExtractor {

    /**
     * Takes a code string as input that has a valid java method syntax. Returns all token strings (without the method
     * name) separated by white space. Additionally, if required a start and end token will be put in front respectively
     * end.
     *
     * Replaces if given the method declaration name by new labels passed as parameter.
     *
     * @param code       The method code.
     * @return A stream of the processed method tokens (in this case it is a stream of one element).
     */
    public TestCase extractAssertions(
            final String code
    ) {
        final CodeParser codeParser = new CodeParser();
        final MethodTokenVisitor visitor = new MethodTokenVisitor();
        visitor.visitClassBodyDeclaration(codeParser.parseCodeFragment(code).classBodyDeclaration());
        return new TestCase(visitor.testElements.toList());
    }

    private static class MethodTokenVisitor extends JavaParserBaseVisitor<Void> {


        private  Stream<String> codeStream = Stream.empty();
        public Stream<TestElement> testElements = Stream.empty();


        @Override
        public Void visitClassBodyDeclaration(JavaParser.ClassBodyDeclarationContext ctx) {
            printChild(ctx);
            testElements= Stream.concat(testElements, Stream.of(new TestSequence(List.copyOf(codeStream.toList()))));
            return null;
        }

        private void printChild(ParseTree parseTree){
            if(parseTree.getChildCount() == 0){
                codeStream = Stream.concat(codeStream, Stream.of(parseTree.getText()));
                return;
            }
            for(int i = 0; i< parseTree.getChildCount(); i++){
                ParseTree child = parseTree.getChild(i);
                if(child instanceof JavaParser.MethodCallContext methodCallContext &&
                    methodCallContext.getChild(0) instanceof JavaParser.IdentifierContext identifierContext){
                    Optional<AssertionType> assertionTypeOpt =
                    AssertionType.parseAssertion(identifierContext.getText());
                    if(assertionTypeOpt.isPresent()){
                        testElements= Stream.concat(testElements, Stream.of(new TestSequence(List.copyOf(codeStream.toList()))));
                        codeStream = Stream.empty();
                        testElements= Stream.concat(testElements, Stream.of(new Assertion(assertionTypeOpt.get(),parseAssertion(child).toList())));
                        continue;

                    }
                }
                printChild(child);
            }


        }

        private Stream<String> parseAssertion(ParseTree assertionChild){
            if(assertionChild.getChildCount() == 0) {
                return Stream.of(assertionChild.getText());
            }
            Stream<String> stream = Stream.empty();
            for(int i = 0; i< assertionChild.getChildCount(); i++){
                stream = Stream.concat(stream, parseAssertion(assertionChild.getChild(i)));
            }
            return stream;
        }





        @Override
        public Void visitBlockStatement(JavaParser.BlockStatementContext ctx) {
            if(ctx.getChild(0) instanceof JavaParser.StatementContext statementContext &&
                    statementContext.getChild(0) instanceof JavaParser.ExpressionContext expressionContext &&
                    expressionContext.getChild(0) instanceof JavaParser.MethodCallContext methodCallContext &&
                    methodCallContext.getChild(0) instanceof JavaParser.IdentifierContext identifierContext
                    && identifierContext.children.get(0).getText().contains("assert")){
                for(var child : methodCallContext.children){
                    System.out.println(child.getText());
                }
            }
            return super.visitBlockStatement(ctx);
        }
    }
}
