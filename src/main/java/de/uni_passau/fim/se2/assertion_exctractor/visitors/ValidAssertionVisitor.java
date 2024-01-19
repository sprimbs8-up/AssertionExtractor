package de.uni_passau.fim.se2.assertion_exctractor.visitors;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import de.uni_passau.fim.se2.assertion_exctractor.data.AssertionType;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.generated.JavaParser;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.generated.JavaParserBaseVisitor;

/**
 * The {@link ValidAssertionVisitor} class is a visitor for traversing the {@link ParseTree} of a Java class and
 * determining the validity of assertions based on predefined criteria using the JavaParser library.
 */
public class ValidAssertionVisitor extends JavaParserBaseVisitor<Boolean> {

    /**
     * Visits an expression context and checks if it represents a valid assertion.
     *
     * @param ctx The expression context to be visited.
     * @return true if the expression is a valid assertion, false otherwise.
     */
    @Override
    public Boolean visitExpression(JavaParser.ExpressionContext ctx) {
        boolean methodInvocation = ctx.getChildCount() == 1
            && ctx.getChild(0) instanceof JavaParser.MethodCallContext methodCallContext
            && methodCallContext.accept(this);
        boolean tryCatchInvocation = ctx.getChildCount() == 1
            && ctx.getChild(0) instanceof JavaParser.PrimaryContext primCtx
            && primCtx.getChildCount() == 1
            && primCtx.getChild(0) instanceof JavaParser.IdentifierContext identifierContext
            && identifierContext.getText().equals("TRY_CATCH");
        return methodInvocation || tryCatchInvocation;
    }

    /**
     * Visits a method call context and checks if it represents a valid assertion method call.
     *
     * @param ctx The method call context to be visited.
     * @return true if the method call is a valid assertion, false otherwise.
     */
    @Override
    public Boolean visitMethodCall(JavaParser.MethodCallContext ctx) {
        List<ParseTree> filteredChildren = filterTerminalNodes(ctx.children);
        return filteredChildren.size() == 2
            && filteredChildren.get(0) instanceof JavaParser.IdentifierContext identifierContext
            && identifierContext.accept(this)
            && filteredChildren.get(1) instanceof JavaParser.ExpressionListContext exListCtx
            && visitExpressionListContextList(exListCtx, identifierContext);
    }

    /**
     * Visits an identifier context and checks if it represents a valid assertion type.
     *
     * @param ctx The identifier context to be visited.
     * @return true if the identifier is a valid assertion type, false otherwise.
     */
    @Override
    public Boolean visitIdentifier(JavaParser.IdentifierContext ctx) {
        return Arrays.stream(AssertionType.values())
            .map(AssertionType::getIdentifier)
            .anyMatch(type -> type.equals(ctx.getText()));
    }

    /**
     * Checks if the parameters of an assertion match the expected number based on the assertion type.
     *
     * @param ctx          The expression list context to be checked.
     * @param assertionCtx The identifier context representing the assertion type.
     * @return true if the number of parameters matches the expected number, false otherwise.
     */
    private boolean visitExpressionListContextList(
        JavaParser.ExpressionListContext ctx, JavaParser.IdentifierContext assertionCtx
    ) {
        Optional<AssertionType> type = AssertionType.parseAssertion(assertionCtx.getText());
        if (type.isEmpty()) {
            return false;
        }
        AssertionType assertionType = type.get();
        List<ParseTree> filteredChildren = filterTerminalNodes(ctx.children);
        return filteredChildren.size() == assertionType.getNumParameters();
    }

    /**
     * Filters out terminal nodes from a list of ParseTree elements.
     *
     * @param ruleContexts The list of ParseTree elements to be filtered.
     * @return A list containing only non-terminal nodes.
     */
    private List<ParseTree> filterTerminalNodes(List<ParseTree> ruleContexts) {
        return ruleContexts.stream().filter(Predicate.not(TerminalNode.class::isInstance)).toList();
    }
}
