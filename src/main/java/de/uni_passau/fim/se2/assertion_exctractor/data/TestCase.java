package de.uni_passau.fim.se2.assertion_exctractor.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;

/**
 * The TestCase record represents a test case, consisting of a list of test elements.
 *
 * @param testElements The list of test elements in the test case.
 */
public record TestCase(List<TestElement> testElements) {

    /**
     * The string used as a mask to represent assertions in the test case.
     */
    public static final String ASSERTION_MASK = "<ASSERTION>";

    /**
     * Replaces the element at the specified position with the assertion mask token.
     *
     * @param pos The position of the element to be replaced with the assertion mask.
     * @return A string representation of the modified test case.
     */
    public String replaceAssertion(int pos) {
        return replaceAssertion(pos, ASSERTION_MASK);
    }

    /**
     * Replaces the element at the specified position with the specified mask token.
     *
     * @param pos       The position of the element to be replaced.
     * @param maskToken The token used as a mask in place of the assertion.
     * @return A string representation of the modified test case.
     */
    public String replaceAssertion(int pos, String maskToken) {
        return replaceAssertionStream(pos, maskToken).collect(Collectors.joining(" "));
    }

    /**
     * Returns a stream of tokens with the element at the specified position replaced by the assertion mask token.
     *
     * @param pos The position of the element to be replaced with the assertion mask.
     * @return A stream of tokens representing the modified test case.
     */
    public Stream<String> replaceAssertionStream(int pos) {
        return replaceAssertionStream(pos, ASSERTION_MASK);
    }

    /**
     * Returns a stream of tokens with the element at the specified position replaced by the specified mask token.
     *
     * @param pos       The position of the element to be replaced.
     * @param maskToken The token used as a mask in place of the assertion.
     * @return A stream of tokens representing the modified test case.
     */
    public Stream<String> replaceAssertionStream(int pos, String maskToken) {
        List<Pair<TestElement, Integer>> assertPosPairs = generateAssertionPositionPair();
        return assertPosPairs.stream().flatMap(x -> {
            if (x.b() == pos) {
                if (x.a() instanceof Assertion) {
                    return Optional.ofNullable(maskToken).stream();
                }
                else if (x.a() instanceof TryCatchAssertion tryCatchAssertion) {
                    List<String> tryCatTokens = tryCatchAssertion.tryCatchTokens();
                    return Stream.concat(
                        Optional.ofNullable(maskToken).stream(),
                        tryCatTokens.subList(1, tryCatTokens.size() - 1).stream()
                    );
                }
            }
            return x.a().tokens().stream();
        });
    }

    /**
     * Generates a list of pairs containing test elements and their positions within the test case.
     *
     * @return A list of pairs representing test elements and their positions.
     */
    private List<Pair<TestElement, Integer>> generateAssertionPositionPair() {
        List<Pair<TestElement, Integer>> assertionsWithPositions = new ArrayList<>();
        int curPos = 0;
        for (TestElement el : testElements()) {
            if (!(el instanceof TestSequence)) {
                assertionsWithPositions.add(Pair.of(el, curPos++));
            }
            else {
                assertionsWithPositions.add(Pair.of(el, curPos));
            }
        }
        return assertionsWithPositions;
    }

    /**
     * Gets the total number of assertions in the test case.
     *
     * @return The number of assertions.
     */
    public int getNumberAssertions() {
        return (int) testElements.stream().filter(TestElement::isAssertion).count();
    }

    @Override
    public String toString() {
        return testElements.stream().map(TestElement::onlyTokens).flatMap(Collection::stream)
            .collect(Collectors.joining(" "));
    }
}
