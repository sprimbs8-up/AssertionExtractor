package org.example;

import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.AstNode;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.declaration.MemberDeclarator;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.declaration.MethodDeclaration;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.expression.MethodInvocation;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.identifier.SimpleIdentifier;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.statement.Statement;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.transformer.util.TransformMode;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.visitor.AstVisitorWithDefaults;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.CommonPreprocessorOptions;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.ast_conversion.AstConverterPreprocessor;
import de.uni_passau.fim.se2.deepcode.toolbox.tokens.MethodTokenExtractor;
import de.uni_passau.fim.se2.deepcode.toolbox.tokens.MethodTokens;

import java.sql.SQLOutput;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        String code = """
                @Test
                public void testSum() {
                    int x = 3;
                    assertMul(x, 35);assertMul(x, 35);
                }""";
       // code = "@Test\n\tpublic void testBasicIteration() {\n\t\tseqStub s = makeSeq();\n\t\tfor (String val : DEFAULT_VALS) {\n\t\t\ts.bang();\n\t\t\tassertTrue(val, s.getLastStringValue());\n\t\t}\n\t\ts.bang();\n\t\tassertEquals(DEFAULT_VALS[0], s.getLastStringValue());\n\t}";
        MethodTokenExtractor extractor = new MethodTokenExtractorAssertions();
        Stream<MethodTokens> tokens = extractor.getTokensForMethod(code, Optional.empty(),Optional.empty(),Optional.empty());
        System.out.println(tokens.flatMap(x->x.tokens().stream()).flatMap(x->x.stream()).collect(Collectors.joining(" ")));
    }

    private static boolean isAcceptable(String code){
        return splitCode(code).stream().map(x->x.pos().size()).allMatch(x->x == 1);
    }


    private static Set<Assertion> splitCode(String code) {
        CommonPreprocessorOptions options = new CommonPreprocessorOptions(null, null, false, new TransformMode.None());
        ASTConverterExtension converterPreprocessor = new ASTConverterExtension(options, true, false);
        Optional<MemberDeclarator<MethodDeclaration>> parsedAst = converterPreprocessor.processSingleMethodAST(code);
        MemberDeclarator<MethodDeclaration> node = parsedAst.get();
        AssertionCollector collector = new AssertionCollector();
        node.declaration().body().get().accept(collector,new ArrayList<>());


        collector.assertions.forEach(System.out::println);
        return collector.assertions;
    }

    private static List<String> collectTokens(String code){
        CommonPreprocessorOptions options = new CommonPreprocessorOptions(null, null, false, new TransformMode.None());
        ASTConverterExtension converterPreprocessor = new ASTConverterExtension(options, true, false);
        Optional<MemberDeclarator<MethodDeclaration>> parsedAst = converterPreprocessor.processSingleMethodAST(code);
        MemberDeclarator<MethodDeclaration> node = parsedAst.get();
        return node.accept(new TokenVisitor(),splitCode(code)).toList();
    }

    private static class AssertionCollector implements AstVisitorWithDefaults<Void, List<Integer>> {
        private final Set<Assertion> assertions = new HashSet<>();

        @Override
        public Void defaultAction(AstNode node, List<Integer> arg) {
            int i = 0;
            for(AstNode child : node.children()){
                List<Integer> copiedPos = new ArrayList<>(List.copyOf(arg));
                copiedPos.add(i++);
                child.accept(this, copiedPos);
            }
            return null;
        }

        @Override
        public Void visit(ExpressionStmt node, List<Integer> arg) {
            if(node.expression() instanceof MethodInvocation methodInvocation
                &&methodInvocation.identifier().name().contains("assert")){
                assertions.add(new Assertion(List.copyOf(arg), node));
            }
            return null;
        }
    }

    private static class TokenVisitor implements AstVisitorWithDefaults<Stream<String>, Set<Assertion>> {
        @Override
        public Stream<String> defaultAction(AstNode node, Set<Assertion> arg) {
            if(arg.stream().map(Assertion::assertionStatement).toList().contains(node)){
                return Stream.of("<MASK>");
            }
            return node.children().stream().flatMap(x->x.accept(this, arg));
        }

        @Override
        public Stream<String> visit(SimpleIdentifier node, Set<Assertion> arg) {
            return Stream.of(node.name());
        }
    }
}