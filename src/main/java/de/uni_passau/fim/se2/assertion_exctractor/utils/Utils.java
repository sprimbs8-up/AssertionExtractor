package de.uni_passau.fim.se2.assertion_exctractor.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import de.uni_passau.fim.se2.assertion_exctractor.data.AssertionType;
import de.uni_passau.fim.se2.assertion_exctractor.data.FineMethodData;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.code.CustomASTConverterPreprocessor;
import de.uni_passau.fim.se2.assertion_exctractor.visitors.MethodTokensDictionaryCollector;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.AstNode;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.declaration.MemberDeclarator;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.declaration.MethodDeclaration;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.declaration.TypeDeclarator;
import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;

public final class Utils {

    public static <A, B> Optional<Pair<A, B>> flatten(Pair<A, Optional<B>> pair) {
        Optional<B> optionalPart = pair.b();
        A firstPart = pair.a();
        return optionalPart.map(x -> Pair.of(firstPart, x));
    }

    public static <A, B> Map<B, A> inverseMap(Map<A, B> map) {
        Map<B, A> inversedMap = new HashMap<>();
        for (Map.Entry<A, B> entry : map.entrySet()) {
            if (inversedMap.containsKey(entry.getValue())) {
                throw new IllegalStateException("Inverted Map makes problems!");
            }
            inversedMap.put(entry.getValue(), entry.getKey());
        }
        return inversedMap;
    }

    public static Map<String, String> collectAbstractTokens(
        FineMethodData methodData, CustomASTConverterPreprocessor preprocessor
    ) {
        MethodTokensDictionaryCollector reader = new MethodTokensDictionaryCollector();

        Map<String, String> abstractTokenMap = new HashMap<>();
        Arrays.stream(AssertionType.values())
            .forEach(type -> abstractTokenMap.put(type.getIdentifier(), "ASSERT_" + type.ordinal()));
        collectAbstractTokens(
            String.join(" ", methodData.testCase().toString()), reader, false, abstractTokenMap, preprocessor
        );
        collectAbstractTokens(
            String.join(" ", methodData.focalMethodTokens()), reader, false, abstractTokenMap, preprocessor
        );
        collectAbstractTokens(
            String.join(" ", methodData.testClassTokens()), reader, true, abstractTokenMap, preprocessor
        );
        collectAbstractTokens(
            String.join(" ", methodData.focalClassTokens()), reader, true, abstractTokenMap, preprocessor
        );
        return abstractTokenMap;
    }

    private static void collectAbstractTokens(
        String code, MethodTokensDictionaryCollector reader, boolean isClass, Map<String, String> abstractTokens,
        CustomASTConverterPreprocessor preprocessor
    ) {
        AstNode node = isClass ? parseClass(code, preprocessor) : parseMethod(code, preprocessor);
        node.accept(reader, abstractTokens);
    }

    private static TypeDeclarator parseClass(String code, CustomASTConverterPreprocessor preprocessor) {
        return preprocessor.parseSingleClass(code).stream()
            .filter(TypeDeclarator.class::isInstance)
            .map(TypeDeclarator.class::cast).findFirst()
            .orElseThrow(() -> new IllegalStateException("The TypeDeclarator should be available!"));
    }

    private static MemberDeclarator<MethodDeclaration> parseMethod(
        String code, CustomASTConverterPreprocessor preprocessor
    ) {
        return preprocessor.parseSingleMethod(code).stream()
            .filter(x -> x instanceof MemberDeclarator)
            .map(x -> (MemberDeclarator<MethodDeclaration>) x).findFirst()
            .orElseThrow(() -> new IllegalStateException("The MethodDeclarator should be available!"));
    }
}
