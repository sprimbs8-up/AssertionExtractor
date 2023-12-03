package de.uni_passau.fim.se2.assertion_exctractor.data;

import java.util.List;

import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestCase;

public record FineMethodData(TestCase testCase, List<String> focalMethodTokens, String documentation) {

}
