package de.uni_passau.fim.se2.assertion_exctractor.visitors;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.AstNode;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.declaration.MethodDeclaration;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.expression.literal.LiteralValueExpr;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.identifier.SimpleIdentifier;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.visitor.AstVisitorWithDefaults;

public class MethodTokensDictionaryCollector implements AstVisitorWithDefaults<Void, Map<String, String>> {

    private final Map<String, Integer> counterMap = new HashMap<>();

    private static final String IDENTIFIER = "IDENT";
    private static final String METHOD = "METHOD";
    private static final String CHARACTER = "CHAR";
    private static final String STRING = "STRING";
    private static final String INTEGER = "INT";
    private static final String BOOLEAN = "BOOL";
    private static final String BYTE = "BYTE";
    private static final String SHORT = "SHORT";
    private static final String DOUBLE = "DOUBLE";
    private static final String FLOAT = "FLOAT";

    public MethodTokensDictionaryCollector() {
        counterMap.put(IDENTIFIER, -1);
        counterMap.put(METHOD, -1);
        counterMap.put(CHARACTER, -1);
        counterMap.put(STRING, -1);
        counterMap.put(INTEGER, -1);
        counterMap.put(BOOLEAN, -1);
        counterMap.put(BYTE, -1);
        counterMap.put(SHORT, -1);
        counterMap.put(DOUBLE, -1);
        counterMap.put(FLOAT, -1);

    }

    @Override
    public Void defaultAction(AstNode node, Map<String, String> arg) {
        node.children().forEach(child -> child.accept(this, arg));
        return null;
    }

    @Override
    public Void visit(MethodDeclaration node, Map<String, String> map) {
        fillDict(METHOD, node.name().name(), map);
        node.children().stream().filter(Predicate.not(Predicate.isEqual(node.name())))
            .forEach(child -> child.accept(this, map));
        return null;
    }

    @Override
    public Void visit(SimpleIdentifier node, Map<String, String> map) {
        fillDict(IDENTIFIER, node.name(), map);
        return null;
    }

    @Override
    public <V> Void visit(LiteralValueExpr<V> node, Map<String, String> arg) {
        switch (node.value().getClass().getSimpleName()) {
            case "Character":
                char value = (char) node.value();
                if (Character.isAlphabetic(value) || Character.isDigit(value)) {
                    fillDict(CHARACTER, "'" + node.value() + "'", arg);
                }
                break;
            case "String":
                fillDict(STRING, "\"" + node.value() + "\"", arg);
                break;
            case "BigInteger":
                fillDict(INTEGER, node.value(), arg);
                break;
            case "Float":
                fillDict(FLOAT, node.value(), arg);
                break;
            case "Double":
                fillDict(DOUBLE, node.value(), arg);
                break;
            case "Byte":
                fillDict(BYTE, node.value(), arg);
                break;
            case "Short":
                fillDict(SHORT, node.value(), arg);
                break;

        }
        return null;
    }

    private <V> void fillDict(String identifier, V token, Map<String, String> map) {
        if (!map.containsKey(token.toString())) {
            Integer counterInt = counterMap.computeIfPresent(identifier, (key, value) -> value + 1);
            int counter = Optional.ofNullable(counterInt).orElse(0);
            map.put(token.toString(), identifier + "_" + counter);
        }
    }
}
