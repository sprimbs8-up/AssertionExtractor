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
import de.uni_passau.fim.se2.deepcode.toolbox.ast.transformer.util.TransformMode;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.CommonPreprocessorOptions;
import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;

/**
 * The Utils class provides utility methods for various common operations, such as flattening pairs, inverting maps,
 * normalizing assertions, removing JavaDocs, and collecting abstract tokens from code. It also includes methods for
 * parsing classes and methods using a custom AST converter preprocessor.
 */
public final class Utils {

    /**
     * Common preprocessor options for handling single methods.
     */
    public static final CommonPreprocessorOptions SINGLE_METHOD_OPTIONS = new CommonPreprocessorOptions(
        null, null, false, new TransformMode.None()
    );

    /**
     * Flattens a pair containing an element and an optional element into a new pair.
     *
     * @param pair The input pair.
     * @param <A>  The type of the first element in the pair.
     * @param <B>  The type of the optional second element in the pair.
     * @return An Optional Pair containing the first and second elements if the optional is present.
     */
    public static <A, B> Optional<Pair<A, B>> flatten(Pair<A, Optional<B>> pair) {
        Optional<B> optionalPart = pair.b();
        A firstPart = pair.a();
        return optionalPart.map(x -> Pair.of(firstPart, x));
    }

    /**
     * Inverts the keys and values of a map, ensuring unique values in the inverted map.
     *
     * @param map The input map to be inverted.
     * @param <A> The type of keys in the input map.
     * @param <B> The type of values in the input map.
     * @return An inverted map with values as keys and keys as values.
     * @throws IllegalStateException if the inverted map would contain duplicate values.
     */
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

    /**
     * Normalizes assertions in the given code by replacing variations with the assertion type identifier.
     *
     * @param code The input code containing assertions.
     * @return The code with normalized assertions.
     */
    public static String normalizeAssertions(String code) {
        for (AssertionType type : AssertionType.values()) {
            code = code.replaceAll("(([a-zA-Z]+)( )*.( )*)*" + type.getIdentifier(), type.getIdentifier());
        }
        return code;
    }

    /**
     * Removes JavaDoc comments from the given code.
     *
     * @param code The input code containing JavaDoc comments.
     * @return The code with JavaDoc comments removed.
     */
    public static String removeJavaDocs(String code) {
        return code.replaceAll("/\\*\\*(?s:(?!\\*/).)*\\*/", "");
    }

    /**
     * Collects abstract tokens from the given method data using a custom AST converter preprocessor.
     *
     * @param methodData   The fine-grained method data to collect tokens from.
     * @param preprocessor The custom AST converter preprocessor.
     * @return A map of abstract tokens and their corresponding representations.
     */
    public static Map<String, String> collectAbstractTokens(
        FineMethodData methodData, CustomASTConverterPreprocessor preprocessor
    ) {

        Pair<MethodTokensDictionaryCollector, Map<String, String>> abstractTokenMapPair = collectAbstractMethodTokensFromInput(
            methodData, preprocessor
        );
        MethodTokensDictionaryCollector reader = abstractTokenMapPair.a();
        Map<String, String> abstractTokenMap = abstractTokenMapPair.b();
        Arrays.stream(AssertionType.values())
            .forEach(type -> abstractTokenMapPair.b().put(type.getIdentifier(), "ASSERT_" + type.ordinal()));
        collectAbstractTokens(
            String.join(" ", methodData.testClassTokens()), reader, true, abstractTokenMap, preprocessor
        );
        collectAbstractTokens(
            String.join(" ", methodData.focalClassTokens()), reader, true, abstractTokenMap, preprocessor
        );
        return abstractTokenMap;
    }

    /**
     * Collects abstract tokens from the given method data using a custom AST converter preprocessor.
     *
     * @param methodData   The fine-grained method data to collect tokens from.
     * @param preprocessor The custom AST converter preprocessor.
     * @return A map of abstract tokens and their corresponding representations.
     */
    public static Map<String, String> collectAbstractMethodTokens(
        FineMethodData methodData, CustomASTConverterPreprocessor preprocessor
    ) {
        return collectAbstractMethodTokensFromInput(methodData, preprocessor).b();
    }

    private static Pair<MethodTokensDictionaryCollector, Map<String, String>> collectAbstractMethodTokensFromInput(
        FineMethodData methodData, CustomASTConverterPreprocessor preprocessor
    ) {
        MethodTokensDictionaryCollector reader = new MethodTokensDictionaryCollector();

        Map<String, String> abstractTokenMap = new HashMap<>();
        Arrays.stream(AssertionType.values())
            .forEach(type -> abstractTokenMap.put(type.getIdentifier(), "ASSERT_" + type.ordinal()));
        collectAbstractTokens(
            String.join(" ", methodData.testCase().toString()), reader, false, abstractTokenMap, preprocessor
        );
        if (!methodData.focalMethodTokens().isEmpty()) {
            collectAbstractTokens(
                String.join(" ", methodData.focalMethodTokens()), reader, false, abstractTokenMap, preprocessor
            );
        }
        return Pair.of(reader, abstractTokenMap);
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
