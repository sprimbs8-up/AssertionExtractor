package de.uni_passau.fim.se2.assertion_exctractor.converters;

import java.util.List;
import java.util.Optional;

import de.uni_passau.fim.se2.assertion_exctractor.data.FineMethodData;
import de.uni_passau.fim.se2.assertion_exctractor.data.JavaDocMethod;
import de.uni_passau.fim.se2.assertion_exctractor.data.RawMethodData;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.FocalMethodParser;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestCase;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestCaseParser;
import de.uni_passau.fim.se2.assertion_exctractor.utils.ErrorChecker;
import de.uni_passau.fim.se2.assertion_exctractor.utils.StatisticsContainer;
import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;

public class Raw2FineDataPStep implements DataProcessingStep<RawMethodData, Optional<FineMethodData>> {

    private final static TestCaseParser TEST_CASE_PARSER = new TestCaseParser();
    private final static FocalMethodParser FOCAL_METHOD_PARSER = new FocalMethodParser();

    private final static int MAX_TESTCASE_LENGTH = 10_000;

    @Override
    public Pair<String, Optional<FineMethodData>> process(Pair<String, RawMethodData> rawMethodData) {
        return rawMethodData.map2(x->x, x->prepareRawMethodData(rawMethodData));
    }

    private static Optional<FineMethodData> prepareRawMethodData(Pair<String, RawMethodData> rawMethodDataPair) {
        RawMethodData rawMethodData = rawMethodDataPair.b();
        if(rawMethodData.testMethod().length() > MAX_TESTCASE_LENGTH) {
            StatisticsContainer.getInstance().notifyTooLongTestCase();
            ErrorChecker.getInstance().logCurrentInstanceTooLong(rawMethodDataPair.a());
            return Optional.empty();
        }
        Optional<TestCase> parsedTestCase = TEST_CASE_PARSER.parseTestCase(rawMethodData.testMethod());
        List<String> focalMethodTokens = FOCAL_METHOD_PARSER.parseMethodToMethodTokens(rawMethodData.focalMethod())
            .toList();
        if (parsedTestCase.isEmpty() || focalMethodTokens.isEmpty()) {
            return Optional.empty();
        }
        Optional<String> javaDocComment = FOCAL_METHOD_PARSER.parseClassToJavaDocMethods(rawMethodData.focalFile())
            .filter(method -> method.methodTokens().equals(focalMethodTokens))
            .map(JavaDocMethod::text)
            .findFirst();

        return Optional.of(new FineMethodData(parsedTestCase.get(), focalMethodTokens, javaDocComment.orElse(null), rawMethodData.focalFile(), rawMethodData.testFile()));
    }
}
