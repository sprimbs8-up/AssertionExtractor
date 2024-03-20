package de.uni_passau.fim.se2.assertion_exctractor.converters;

import de.uni_passau.fim.se2.assertion_exctractor.data.DataPoint;
import de.uni_passau.fim.se2.assertion_exctractor.data.DatasetType;
import de.uni_passau.fim.se2.assertion_exctractor.data.FineMethodData;
import de.uni_passau.fim.se2.assertion_exctractor.data.TestCase;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestCaseParser;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TokenParser;
import de.uni_passau.fim.se2.assertion_exctractor.utils.RandomUtil;
import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TrainSplitPreprocessingStepTest {
    private static final String EXAMPLE_TEST_CASE = """
            @Test
            public void testFoo() {
                String x = doFoo();
                assertEquals(x , "foo");
            }
            """;
    private static final String FOCAL_METHOD = """
            public String doFoo() {
                return "foo";
            }
            """;
    private FineMethodData methodData;
    @BeforeEach
    void setup() {
        TestCaseParser parser = new TestCaseParser();
        TokenParser tokenParser = new TokenParser();
        Optional<TestCase> testCase = parser.parseTestCase(EXAMPLE_TEST_CASE);
        List<String> focalMethodTokens = tokenParser.convertCodeToTokenStrings(FOCAL_METHOD).toList();
        List<String> testClassTokens = concat(Stream.of("class","Foo","{"), Arrays.stream(testCase.get().toString().split(" ")), Stream.of("}")).toList();
        List<String> focalClassTokens = concat(Stream.of("class","FooTest","{"),focalMethodTokens.stream(), Stream.of("}")).toList();
        String documentation = "This is an example JavaDoc comment.";

        methodData = new FineMethodData(testCase.get(),focalMethodTokens,documentation,focalClassTokens,testClassTokens);
        RandomUtil.getInstance().initializeRandom(0);

    }

    @SafeVarargs
    private static<T> Stream<T> concat(Stream<T>... streams) {
        return Arrays.stream(streams).reduce(Stream.empty(), Stream::concat);
    }
    @Test
    void testInvalidTrainSplit() {
        assertThrows(IllegalArgumentException.class, () -> new TrainSplitPreprocessingStep(1,1,1));
    }

    @Test
    void testTranSplitPreprocessingStep() {
        TrainSplitPreprocessingStep step = new TrainSplitPreprocessingStep(34,33,33);

        Pair<String, DataPoint> validationResult = step.process(Pair.of("path",methodData));
        assertThat(validationResult.a()).isEqualTo("path");
        assertThat(validationResult.b().type()).isEqualTo(DatasetType.VALIDATION);

        step.process(Pair.of("path",methodData));
        Pair<String, DataPoint> trainingResult = step.process(Pair.of("path",methodData));
        assertThat(trainingResult.a()).isEqualTo("path");
        assertThat(trainingResult.b().type()).isEqualTo(DatasetType.TRAINING);

        step.process(Pair.of("path",methodData));
        step.process(Pair.of("path",methodData));
        step.process(Pair.of("path",methodData));
        Pair<String, DataPoint> testingResult = step.process(Pair.of("path",methodData));
        assertThat(testingResult.a()).isEqualTo("path");
        assertThat(testingResult.b().type()).isEqualTo(DatasetType.TESTING);
    }
}