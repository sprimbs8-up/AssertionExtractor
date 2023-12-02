package de.uni_passau.fim.se2.assertion_exctractor.converters;

import de.uni_passau.fim.se2.assertion_exctractor.data.FineMethodData;
import de.uni_passau.fim.se2.assertion_exctractor.data.JavaDocMethod;
import de.uni_passau.fim.se2.assertion_exctractor.data.RawMethodData;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.FocalMethodParser;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestCase;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestCaseParser;

import java.util.List;
import java.util.Optional;

public class Raw2FineDataPStep implements DataProcessingStep<RawMethodData, Optional<FineMethodData>> {
    private final static TestCaseParser TEST_CASE_PARSER = new TestCaseParser();
    private final static FocalMethodParser FOCAL_METHOD_PARSER = new FocalMethodParser();

    @Override
    public Optional<FineMethodData> process(RawMethodData rawMethodData) {
        Optional<TestCase> parsedTestCase = TEST_CASE_PARSER.parseTestCase(rawMethodData.testMethod());
        List<String> focalMethodTokens = FOCAL_METHOD_PARSER.parseMethodToMethodTokens(rawMethodData.focalMethod()).toList();
        if (parsedTestCase.isEmpty() || focalMethodTokens.isEmpty()) {
            return Optional.empty();
        }
        Optional<String> javaDocComment = FOCAL_METHOD_PARSER.parseClassToJavaDocMethods(rawMethodData.focalFile())
                .filter(method-> method.methodTokens().equals(focalMethodTokens))
                .map(JavaDocMethod::text)
                .findFirst();


        return Optional.of(new FineMethodData(parsedTestCase.get(), focalMethodTokens, javaDocComment.orElse(null)));
    }
}
