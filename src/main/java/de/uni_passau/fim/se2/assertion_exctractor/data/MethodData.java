package de.uni_passau.fim.se2.assertion_exctractor.data;

import de.uni_passau.fim.se2.assertion_exctractor.parsing.FocalMethodParser;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestCase;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestCaseParser;
import de.uni_passau.fim.se2.deepcode.toolbox.tokens.MethodTokenExtractor;
import de.uni_passau.fim.se2.deepcode.toolbox.tokens.MethodTokens;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.stringtemplate.v4.ST;

import java.util.Optional;
import java.util.stream.Collectors;

public record MethodData(TestCase testCase, String focalMethod, String documentation) {

    private final static TestCaseParser PARSER = new TestCaseParser();


    public static MethodData fromJSONObject(JSONObject jsonObject){
        String testCaseString = (String) ((JSONObject) jsonObject.get("test_case")).get("body");
        String focalMethodString = (String) ((JSONObject) jsonObject.get("focal_method")).get("body");
        FocalMethodParser focalMethodParser = new FocalMethodParser();
        return new MethodData(PARSER.parseTestCase(testCaseString),focalMethodParser.parseMethod(focalMethodString).collect(Collectors.joining(" ")), "");
    }
}
