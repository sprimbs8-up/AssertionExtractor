package de.uni_passau.fim.se2.assertion_exctractor.parsing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class AssertionParserTest {

    @Test
    void testAssertionParsingList() throws JsonProcessingException {
        List<String> assertions = List.of(
            "assertEquals(\"2\", result", "assertTrue ( helloWorld )", "TRY_CATCH", "assertEquals",
            "assertTrue(\"true\")"
        );
        String inputAssertions = new ObjectMapper().writeValueAsString(assertions);
        AssertionParser parser = new AssertionParser();
        assertThat(parser.areSyntacticCorrectAssertions(inputAssertions))
            .containsExactly(true, true, true, false, true);
    }

}
