package de.uni_passau.fim.se2.assertion_exctractor.data;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import de.uni_passau.fim.se2.assertion_exctractor.parsing.FocalMethodParser;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestCase;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestCaseParser;


public record MethodData(TestCase testCase, String focalMethod, String documentation) {

    private final static TestCaseParser TEST_CASE_PARSER = new TestCaseParser();
    private final static FocalMethodParser FOCAL_METHOD_PARSER = new FocalMethodParser();

    public static Stream<MethodData> fromPreparation(PreparedMethodData preparedMethodData) {
        Optional<TestCase> parsedTestCase = TEST_CASE_PARSER.parseTestCase(preparedMethodData.testMethod());
        List<String> focalMethodStream = FOCAL_METHOD_PARSER.parseMethod(preparedMethodData.focalMethod()).toList();
        if (parsedTestCase.isEmpty() || focalMethodStream.isEmpty()) {
            return Stream.empty();
        }
       var x =  FOCAL_METHOD_PARSER.parseCompleteCode(preparedMethodData.focalFile()).toList();
        x.forEach(System.out::println);


        return Stream.of(new MethodData(parsedTestCase.get(), String.join(" ", focalMethodStream), null));
    }




    private static String cleanCode(String string){
        return string.replaceAll("\n"," ").replaceAll("\t"," ").replaceAll("\r"," ").replace(" ","");
    }
    private static String cleanJDoc(String string){
        return string.replaceAll("\n"," ").replaceAll("\t"," ").replaceAll("\r"," ");
    }

}
