package de.uni_passau.fim.se2.assertion_exctractor.data;

import java.util.List;

import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestCase;

/**
 * The FineMethodData record represents fine-grained information about a method after preprocessing. It encapsulates
 * details such as the test case, focal method tokens, documentation, focal class tokens, and test class tokens.
 *
 * @param testCase          The parsed test case associated with the method.
 * @param focalMethodTokens The list of tokens extracted from the focal method's code.
 * @param documentation     The documentation (JavaDoc) associated with the focal method, or null if not available.
 * @param focalClassTokens  The list of tokens extracted from the focal class's code.
 * @param testClassTokens   The list of tokens extracted from the test class's code.
 */
public record FineMethodData(
    TestCase testCase, List<String> focalMethodTokens, String documentation, List<String> focalClassTokens,
    List<String> testClassTokens
) {

}
