package de.uni_passau.fim.se2.assertion_exctractor.subcommand;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_passau.fim.se2.assertion_exctractor.data.FineMethodData;
import de.uni_passau.fim.se2.assertion_exctractor.data.TestCase;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestCaseParser;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TokenParser;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.code.CustomASTConverterPreprocessor;
import de.uni_passau.fim.se2.assertion_exctractor.utils.Utils;
import picocli.CommandLine;

@CommandLine.Command(
    name = "asserT5",
    mixinStandardHelpOptions = true,
    showDefaultValues = true
)
public class AsserT5Subcommand implements Runnable {

    @CommandLine.Spec
    protected CommandLine.Model.CommandSpec spec;

    @CommandLine.Option(
        names = { "--model" },
        description = "The model type. Raw and Abstract are available.",
        required = true
    )
    String modelType;
    @CommandLine.Option(
        names = { "-f", "--focal-method" },
        description = "The focal method.",
        required = true
    )
    String inputFocalMethod;
    @CommandLine.Option(
        names = { "-t", "--test-method" },
        description = "The focal method.",
        required = true
    )
    String inputTestMethod;
    @CommandLine.Option(
        names = { "--focal-class" },
        description = "The focal class.",
        required = true
    )
    String inputFocalClass;
    @CommandLine.Option(
        names = { "--test-class" },
        description = "The test class.",
        required = true
    )
    String inputTestClass;

    protected final CustomASTConverterPreprocessor preprocessor = new CustomASTConverterPreprocessor(
        Utils.SINGLE_METHOD_OPTIONS, true, false
    );

    @Override
    public void run() {
        try {
            ObjectMapper mapper = new ObjectMapper();

            String dummyAssertion = "assertTrue(true);";
            inputTestMethod = mapper.readValue(inputTestMethod, String.class);
            inputFocalMethod = mapper.readValue(inputFocalMethod, String.class);
            inputFocalClass = mapper.readValue(inputFocalClass, String.class);
            inputTestClass = mapper.readValue(inputTestClass, String.class);
            modelType = mapper.readValue(modelType, String.class);
            String newInputTestMethod = replaceLast(inputTestMethod, "}", dummyAssertion + "}");
            TestCase tc = new TestCaseParser().parseTestCase(newInputTestMethod).get();
            List<String> methodTokens = new TokenParser().convertCodeToTokenStrings(inputFocalMethod)
                .filter(token -> !token.equals("")).toList();
            List<String> focalClassTokens = new TokenParser().convertCodeToTokenStrings(inputFocalClass).toList();
            List<String> testClassTokens = new TokenParser().convertCodeToTokenStrings(inputTestClass).toList();
            FineMethodData methodData = new FineMethodData(tc, methodTokens, "", focalClassTokens, testClassTokens);
            Map<String, String> map = modelType.equals("abstract")
                ? Utils.collectAbstractTokens(methodData, preprocessor)
                : new HashMap<>();
            Stream<String> assertionStream = tc.replaceAssertionStream(tc.getNumberAssertions() - 1)
                .map(v -> map.getOrDefault(v, v));
            Stream<String> methodStream = methodTokens.stream().map(v -> map.getOrDefault(v, v));
            Stream<String> combined = concat(
                Stream.of("TEST_METHOD:"), assertionStream, Stream.of("FOCAL_METHOD:"), methodStream
            );
            ExportData data = new ExportData(combined.collect(Collectors.joining(" ")), map);

            System.out.println(mapper.writeValueAsString(data));
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String reverse(String s) {
        return new StringBuilder(s).reverse().toString();
    }

    private static String replaceLast(String text, String oldString, String newString) {
        return reverse(reverse(text).replaceFirst(reverse(oldString), reverse(newString)));
    }

    private static <T> Stream<T> concat(Stream<T>... streams) {
        return Arrays.stream(streams).reduce(Stream.empty(), Stream::concat);
    }

    private record ExportData(String preprocessedText, Map<String, String> translationDict) {
    }
}
