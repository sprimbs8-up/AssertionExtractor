package de.uni_passau.fim.se2.assertion_exctractor.converters;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.uni_passau.fim.se2.assertion_exctractor.data.FineMethodData;
import de.uni_passau.fim.se2.assertion_exctractor.data.JavaDocMethod;
import de.uni_passau.fim.se2.assertion_exctractor.data.RawMethodData;
import de.uni_passau.fim.se2.assertion_exctractor.data.TestCase;
import de.uni_passau.fim.se2.assertion_exctractor.data.TestElement;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestCaseParser;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TokenParser;
import de.uni_passau.fim.se2.assertion_exctractor.utils.ErrorChecker;
import de.uni_passau.fim.se2.assertion_exctractor.utils.StatisticsContainer;
import de.uni_passau.fim.se2.assertion_exctractor.utils.Utils;
import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;

/**
 * The Raw2FineDataPStep class is an implementation of the {@link DataProcessingStep} interface. It performs data
 * preprocessing by converting raw method data of type {@link RawMethodData} to fine-grained method data of type
 * {@link FineMethodData} and encapsulating it in an {@link Optional}. This class includes methods for parsing test
 * cases, extracting tokens from code snippets, and handling various conditions such as too long test cases or unusable
 * test cases without assertions.
 *
 */
public class Raw2FineDataPStep implements DataProcessingStep<RawMethodData, Optional<FineMethodData>> {

    private final static TestCaseParser TEST_CASE_PARSER = new TestCaseParser();
    private final static TokenParser FOCAL_METHOD_PARSER = new TokenParser();

    private final static int MAX_TESTCASE_LENGTH = 10_000;

    /**
     * Processes the input raw method data and returns the result encapsulated in a {@link Pair}. The result is an
     * {@link Optional} containing fine-grained method data or an empty {@link Optional} if certain conditions are not
     * met during preprocessing.
     *
     * @param rawMethodData A Pair containing a String identifier and the input raw method data.
     * @return A Pair containing a String identifier and an Optional containing the processed fine-grained method data.
     */
    @Override
    public Pair<String, Optional<FineMethodData>> process(Pair<String, RawMethodData> rawMethodData) {
        return rawMethodData.map2(x -> x, x -> prepareRawMethodData(rawMethodData));
    }

    /**
     * Prepares the raw method data for processing by converting it to fine-grained method data. Handles conditions such
     * as too long test cases, unusable test cases without assertions, etc.
     *
     * @param rawMethodDataPair A Pair containing a String identifier and the input raw method data.
     * @return An Optional containing the processed fine-grained method data or an empty Optional if conditions are not
     *         met.
     */
    private Optional<FineMethodData> prepareRawMethodData(Pair<String, RawMethodData> rawMethodDataPair) {
        RawMethodData rawMethodData = rawMethodDataPair.b();

        // Check if the test case length exceeds the specified maximum length.
        if (rawMethodData.testMethod().length() > MAX_TESTCASE_LENGTH) {
            StatisticsContainer.getInstance().notifyTooLongTestCase();
            ErrorChecker.getInstance().logCurrentInstanceTooLong(rawMethodDataPair.a());
            return Optional.empty();
        }

        // Parse the test case and extract tokens from the focal and test methods.
        Optional<TestCase> parsedTestCase = TEST_CASE_PARSER.parseTestCase(rawMethodData.testMethod());
        List<String> focalMethodTokens = FOCAL_METHOD_PARSER.convertCodeToTokenStrings(rawMethodData.focalMethod())
            .toList();
        List<String> focalClassTokens = FOCAL_METHOD_PARSER
            .convertCodeToTokenStrings(Utils.removeJavaDocs(rawMethodData.focalFile())).toList();
        List<String> testClassTokens = FOCAL_METHOD_PARSER
            .convertCodeToTokenStrings(Utils.removeJavaDocs(rawMethodData.testFile())).toList();

        // Check if parsing results in a valid test case and focal method tokens.
        if (parsedTestCase.isEmpty() || focalMethodTokens.isEmpty()) {
            return Optional.empty();
        }

        // Filter out non-assertion elements from the test case and check for a usable test case.
        String codeWithoutAssertions = parsedTestCase.get().testElements().stream()
            .filter(Predicate.not(TestElement::isAssertion)).map(TestElement::tokenString)
            .collect(Collectors.joining(" "));

        if (FOCAL_METHOD_PARSER.convertCodeToTokenStrings(codeWithoutAssertions).findAny().isEmpty()) {
            StatisticsContainer.getInstance().notifiedUnusableTestCaseWithoutAssertions();
            return Optional.empty();
        }

        // Extract JavaDoc comment associated with the focal method if available.
        Optional<String> javaDocComment = FOCAL_METHOD_PARSER.parseClassToJavaDocMethods(rawMethodData.focalFile())
            .filter(method -> method.methodTokens().equals(focalMethodTokens))
            .map(JavaDocMethod::text)
            .findFirst();

        return Optional.of(
            new FineMethodData(
                parsedTestCase.get(), focalMethodTokens, javaDocComment.orElse(null), focalClassTokens, testClassTokens
            )
        );
    }
}
