package org.example;

import de.uni_passau.fim.se2.deepcode.toolbox.ast.generated.JavaParser;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.generated.JavaParserBaseVisitor;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.parser.CodeParser;
import de.uni_passau.fim.se2.deepcode.toolbox.tokens.CodeTokenExtractor;
import de.uni_passau.fim.se2.deepcode.toolbox.tokens.MethodTokenExtractor;
import de.uni_passau.fim.se2.deepcode.toolbox.tokens.MethodTokens;
import de.uni_passau.fim.se2.deepcode.toolbox.util.StringUtil;
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
     * @param startToken The start token.
     * @param endToken   The end token.
     * @param newLabel   An optional parameter for a new label replacing the default method name.
     * @return A stream of the processed method tokens (in this case it is a stream of one element).
     */
    public Stream<MethodTokens> getTokensForMethod(
            final String code, final Optional<String> startToken, final Optional<String> endToken, Optional<String> newLabel
    ) {
        final CodeParser codeParser = new CodeParser();
        final MethodTokenVisitor visitor = new MethodTokenVisitor(code, startToken, endToken, newLabel);
        visitor.visitClassBodyDeclaration(codeParser.parseCodeFragment(code).classBodyDeclaration());
        return visitor.getMethodTokens().stream();
    }

    private static class MethodTokenVisitor extends JavaParserBaseVisitor<Void> {

        private static final String METHOD_NAME_TOKEN = "<METHOD_NAME>";

        private final List<MethodTokens> tokens = new ArrayList<>();

        private final List<Token> usedTokens;

        private final Optional<String> startToken;
        private final Optional<String> endToken;
        private final Optional<String> newLabel;

        /**
         * Create the {@link MethodTokenExtractor}.
         *
         * @param code       The given complete code.
         * @param startToken The start token that should be put in front of a sequence line.
         * @param endToken   The end token that should be put at the end of a sequence line.
         */
        private MethodTokenVisitor(
                String code, Optional<String> startToken, Optional<String> endToken, Optional<String> newLabel
        ) {
            this.usedTokens = new CodeTokenExtractor().extractTokens(code);
            this.startToken = startToken;
            this.endToken = endToken;
            this.newLabel = newLabel;
        }

        /**
         * Returns the collected method tokens;
         *
         * @return The collected method tokens.
         */
        private List<MethodTokens> getMethodTokens() {
            return tokens;
        }

        @Override
        public Void visitClassBodyDeclaration(JavaParser.ClassBodyDeclarationContext ctx) {
            System.out.println(printChild(ctx).collect(Collectors.joining(" ")));
            return null;
        }

        private Stream<String> printChild(ParseTree parseTree){
            if(parseTree.getChildCount() == 0){
                return Stream.of(parseTree.getText());
            }
            Stream<String> stream = Stream.empty();
            for(int i = 0; i< parseTree.getChildCount(); i++){
                ParseTree child = parseTree.getChild(i);
                if(child instanceof JavaParser.MethodCallContext methodCallContext &&
                    methodCallContext.getChild(0) instanceof JavaParser.IdentifierContext identifierContext
                    && identifierContext.getText().contains("assert")){
                    stream = Stream.concat(stream, Stream.of("<ASSERTION>"));
                    continue;
                }
                stream = Stream.concat(stream, printChild(parseTree.getChild(i)));

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

        private MethodTokens buildMethodTokens(List<String> tokenList, String identifier) {
            final List<String> subTokenLabels = splitToSubTokens(identifier).map(x -> x.toLowerCase(Locale.ROOT))
                    .toList();
            return MethodTokens.from(identifier, tokenList, subTokenLabels, subTokenize(tokenList).toList());
        }

        private Stream<String> splitToSubTokens(String token) {
            return StringUtil.splitToSubtokenStream(token);
        }

        private Stream<String> subTokenize(List<String> tokens) {
            return tokens.stream().flatMap(this::subTokenize);
        }

        private Stream<String> subTokenize(String token) {
            // We keep tokens that have a length of one (like dots, commas or semicolons) and the placeholder for the
            // method name.
            return token.length() == 1 || METHOD_NAME_TOKEN.equals(token) ? Stream.of(token) : splitToSubTokens(token);
        }

        private boolean isAbstractMethod(List<JavaParser.ModifierContext> modifiers) {
            return modifiers.stream()
                    .map(RuleContext::getText)
                    .anyMatch("abstract"::equals);
        }

        private List<String> tokenizeMethodDeclaration(final JavaParser.MethodDeclarationContext ctx) {
            return usedTokens.stream()
                    .filter(x -> isInRange(ctx, x))
                    .map(x -> replaceIfLabelToken(x, ctx.identifier().getStart()))
                    .toList();
        }

        private boolean isInRange(JavaParser.MethodDeclarationContext ctx, Token token) {
            return token.getStartIndex() >= ctx.getStart().getStartIndex()
                    && token.getStopIndex() <= ctx.getStop().getStopIndex();
        }

        private boolean isLabelToken(Token token, Token labelToken) {
            return token.getStartIndex() == labelToken.getStartIndex()
                    && token.getStopIndex() == labelToken.getStopIndex();
        }

        private String replaceIfLabelToken(Token token, Token labelToken) {
            return isLabelToken(token, labelToken) && newLabel.isEmpty() ? METHOD_NAME_TOKEN : token.getText();
        }
    }
}
