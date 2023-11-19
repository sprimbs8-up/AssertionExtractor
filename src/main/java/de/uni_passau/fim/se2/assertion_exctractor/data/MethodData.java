package de.uni_passau.fim.se2.assertion_exctractor.data;

import de.uni_passau.fim.se2.assertion_exctractor.parsing.FocalMethodParser;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestCase;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestCaseParser;
import de.uni_passau.fim.se2.deepcode.toolbox.tokens.MethodTokenExtractor;
import de.uni_passau.fim.se2.deepcode.toolbox.tokens.MethodTokens;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.stringtemplate.v4.ST;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record MethodData(TestCase testCase, String focalMethod, String documentation) {

    private final static TestCaseParser TEST_CASE_PARSER = new TestCaseParser();
    private final static FocalMethodParser FOCAL_METHOD_PARSER = new FocalMethodParser();


    public static Stream<MethodData> fromPreparation(PreparedMethodData preparedMethodData) {
        Optional<TestCase> parsedTestCase = TEST_CASE_PARSER.parseTestCase(preparedMethodData.testMethod());
        List<String> focalMethodStream = FOCAL_METHOD_PARSER.parseMethod(preparedMethodData.focalMethod()).toList();
        if (parsedTestCase.isEmpty() || focalMethodStream.isEmpty()) {
            return Stream.empty();
        }
        return Stream.of(new MethodData(parsedTestCase.get(), String.join(" ", focalMethodStream), null));
    }
}
