package de.uni_passau.fim.se2.assertion_exctractor.data;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import de.uni_passau.fim.se2.assertion_exctractor.parsing.FocalMethodParser;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestCase;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestCaseParser;


public record FineMethodData(TestCase testCase, List<String> focalMethodTokens, String documentation) {

}
