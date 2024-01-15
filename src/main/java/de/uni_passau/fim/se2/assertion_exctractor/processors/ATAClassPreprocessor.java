package de.uni_passau.fim.se2.assertion_exctractor.processors;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.assertion_exctractor.data.DataPoint;
import de.uni_passau.fim.se2.assertion_exctractor.data.DatasetType;
import de.uni_passau.fim.se2.assertion_exctractor.data.FineMethodData;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.*;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.code.CustomASTConverterPreprocessor;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.code.CustomAstCodeParser;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.code.CustomCodeParser;
import de.uni_passau.fim.se2.assertion_exctractor.visitors.MethodTokenVisitor;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.generated.JavaParser;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.AstNode;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.declaration.MemberDeclarator;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.declaration.MethodDeclaration;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.declaration.TypeDeclarator;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.expression.literal.LiteralValueExpr;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.identifier.SimpleIdentifier;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.switch_node.Switch;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.switch_node.SwitchCase;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.parser.AstCodeParser;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.parser.ParseException;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.visitor.AstVisitorWithDefaults;
import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;

public class ATAClassPreprocessor extends Processor {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public ATAClassPreprocessor(String dataDir, String saveDir, int maxAssertions) {
        super(dataDir, saveDir, maxAssertions);
    }

    @Override
    protected String getModelName() {
        return "ata";
    }

    @Override
    protected void exportTestCases(Pair<String, DataPoint> dataPointPair) {

        DataPoint dataPoint = dataPointPair.b();
        FineMethodData methodData = dataPoint.methodData();
        Map<String, String > abstractTokenMap = collectAbstractTokens(methodData);

        TestCase testCase = methodData.testCase();
        List<List<String>> assertions = testCase.testElements().stream()
                .filter(((Predicate<TestElement>) Assertion.class::isInstance).or(TryCatchAssertion.class::isInstance))
                .map(TestElement::tokens)
                .map(testTokens -> testTokens.stream().map(token -> abstractTokenMap.getOrDefault(token ,token)).toList())
                .toList();
        DatasetType type = dataPoint.type();
        for (int i = 0; i < assertions.size(); i++) {
            writeStringsToFile(
                    dataPoint.type().name().toLowerCase() + "/assertLines.txt", type.getRefresh(),
                    String.join(" ", assertions.get(i))
            );
            writeStringsToFile(
                    dataPoint.type().name().toLowerCase() + "/testMethods.txt", type.getRefresh(),
                    testCase.replaceAssertionStream(i).map(token-> abstractTokenMap.getOrDefault(token, token)).collect(Collectors.joining(" "))
            );
            Map<String, String> invertedSortedMap = new TreeMap<>( (o1, o2) -> {
                String[] o1String = o1.split("_");
                String[] o2String = o2.split("_");
                int res =  o1String[0].compareTo(o2String[0]);
                if (res != 0){
                    return res;
                }
                int o1Int = Integer.parseInt(o1String[1]);
                int o2Int = Integer.parseInt(o2String[1]);
                return Integer.compare(o1Int, o2Int);
            });
            invertedSortedMap.putAll(inverseMap(abstractTokenMap));

            try {
                writeStringsToFile(
                        dataPoint.type().name().toLowerCase() + "/dict.jsonl", type.getRefresh(),
                        MAPPER.writeValueAsString(invertedSortedMap)
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            dataPoint.type().getRefresh().set(true);
        }
    }

    private <A,B> Map<B,A> inverseMap(Map<A,B> map){
        Map<B,A> inversedMap = new HashMap<>();
        for (Map.Entry<A,B> entry: map.entrySet()){
            if(inversedMap.containsKey(entry.getValue())){
                throw new IllegalStateException("Inverted Map makes problems!");
            }
            inversedMap.put(entry.getValue(), entry.getKey());
        }
        return inversedMap;
    }

    private Map<String, String> collectAbstractTokens(FineMethodData methodData) {
        MethodTokensDictionaryReader reader = new MethodTokensDictionaryReader();

        Map<String, String> abstractTokenMap = new HashMap<>();
        collectAbstractTokens(String.join(" ", methodData.testCase().toString()), reader, false, abstractTokenMap);
        collectAbstractTokens(String.join(" ", methodData.focalMethodTokens()), reader, false, abstractTokenMap);
        collectAbstractTokens(String.join(" ", methodData.testClassTokens()), reader, true, abstractTokenMap);
        collectAbstractTokens(String.join(" ", methodData.focalClassTokens()), reader, true, abstractTokenMap);
        return abstractTokenMap;
    }

    private void collectAbstractTokens(String code, MethodTokensDictionaryReader reader, boolean isClass, Map<String, String> abstractTokens) {
        AstNode node = isClass ?  parseClass(code) : parseMethod(code);
        node.accept(reader, abstractTokens);
    }

    private TypeDeclarator parseClass(String code) {
        return preprocessor.parseSingleClass(code).stream()
                .filter(TypeDeclarator.class::isInstance)
                .map(TypeDeclarator.class::cast).
                findFirst().orElseThrow(() -> new IllegalStateException("The TypeDeclarator should be available!"));
    }
    private MemberDeclarator<MethodDeclaration> parseMethod(String code) {
        return preprocessor.parseSingleMethod(code).stream()
                .filter(x->x instanceof MemberDeclarator)
                .map(x->(MemberDeclarator<MethodDeclaration>) x).
                findFirst().orElseThrow(() -> new IllegalStateException("The TypeDeclarator should be available!"));
    }


    private static class MethodTokensDictionaryReader implements AstVisitorWithDefaults<Void, Map<String, String>> {
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
        private static final String UNKNOWN_TYPE = "UNKNOWN_TYPE";

        private MethodTokensDictionaryReader() {
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
            counterMap.put(UNKNOWN_TYPE, -1);
        }

        @Override
        public Void defaultAction(AstNode node, Map<String, String> arg) {
            node.children().forEach(child -> child.accept(this, arg));
            return null;
        }

        @Override
        public Void visit(MethodDeclaration node, Map<String, String> map) {
            fillDict(METHOD, node.name().name(), map);
            node.children().stream().filter(Predicate.not(Predicate.isEqual(node.name()))).forEach(child -> child.accept(this, map));
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
                    fillDict(STRING, "\"" +node.value()+"\"", arg);
                    break;
                case "BigInteger":
                    fillDict(INTEGER, node.value(), arg);
                    break;
                case "Boolean":
                    fillDict(BOOLEAN, node.value(), arg);
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
                default:
                    fillDict(UNKNOWN_TYPE, node.value(), arg);
                    throw new RuntimeException("TEST");

            }
            return AstVisitorWithDefaults.super.visit(node, arg);
        }

        private <V> void fillDict(String identifier, V token, Map<String, String> map) {
            if (!map.containsKey(token.toString())) {
                Integer counterInt = counterMap.computeIfPresent(identifier, (key, value) -> value + 1);
                int counter = Optional.ofNullable(counterInt).orElse(0);
                map.put(token.toString(), identifier + "_" + counter);
            }
        }
    }

}
