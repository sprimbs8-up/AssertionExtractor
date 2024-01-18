package de.uni_passau.fim.se2.assertion_exctractor.visitors;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import de.uni_passau.fim.se2.assertion_exctractor.parsing.AssertionType;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.generated.JavaParser;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.generated.JavaParserBaseVisitor;

public class ValidAssertionVisitor extends JavaParserBaseVisitor<Boolean> {

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

    @Override
    public Boolean visitMethodCall(JavaParser.MethodCallContext ctx) {
        List<ParseTree> filteredChildren = filterTerminalNodes(ctx.children);
        return filteredChildren.size() == 2
            && filteredChildren.get(0) instanceof JavaParser.IdentifierContext identifierContext
            && identifierContext.accept(this)
            && filteredChildren.get(1) instanceof JavaParser.ExpressionListContext exListCtx
            && visitExpressionListContextList(exListCtx, identifierContext);
    }

    @Override
    public Boolean visitIdentifier(JavaParser.IdentifierContext ctx) {
        return Arrays.stream(AssertionType.values())
            .map(AssertionType::getIdentifier)
            .anyMatch(type -> type.equals(ctx.getText()));
    }

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

    private List<ParseTree> filterTerminalNodes(List<ParseTree> ruleContexts) {
        return ruleContexts.stream().filter(Predicate.not(ctx -> ctx instanceof TerminalNode)).toList();
    }
}
