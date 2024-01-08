package de.uni_passau.fim.se2.assertion_exctractor.parsing;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AssertionParserTest {

    @Test
    void testAssertionParsingList(){
        String inputAssertions = "['assertEquals ( \\\"2\\\" , result )','assertTrue ( helloWorld )', 'TRY_CATCH', 'assertEquals', 'assertTrue(\\\"true\\\")']";
        AssertionParser parser = new AssertionParser();
        assertThat(parser.areSyntacticCorrectAssertions(inputAssertions)).containsExactly(true, true, true, false, true);
    }

}