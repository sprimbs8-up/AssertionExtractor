package de.uni_passau.fim.se2.assertion_exctractor.parsing;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import de.uni_passau.fim.se2.assertion_exctractor.utils.ErrorChecker;
import de.uni_passau.fim.se2.assertion_exctractor.visitors.ValidAssertionVisitor;

/**
 * The {@link AssertionParser} class extends {@link TokenParser} and provides methods for parsing and validating
 * syntactic correctness of assertions in a list of code fragments.
 */
public class AssertionParser extends TokenParser {
    // Logger for logging messages

    private static final Logger LOG = LoggerFactory.getLogger(AssertionParser.class);
    // Visitor for checking the syntactic correctness of assertions

    private final ValidAssertionVisitor validAssertionVisitor = new ValidAssertionVisitor();
    // ObjectMapper for parsing JSON representation of assertion codes

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Checks the syntactic correctness of a list of assertion codes.
     *
     * @param codes JSON representation of a list of assertion codes.
     * @return A list of Boolean values indicating the syntactic correctness of each assertion.
     */
    public List<Boolean> areSyntacticCorrectAssertions(String codes) {
        try {
            List<String> assertionList = mapper
                .readValue(codes, TypeFactory.defaultInstance().constructCollectionType(List.class, String.class));
            return assertionList.stream().map(this::isSyntacticCorrectAssertion).toList();
        }
        catch (JsonProcessingException e) {
            LOG.warn("Codes could not have been parsed!", e);
            return Collections.emptyList();
        }
    }

    /**
     * Checks the syntactic correctness of a single assertion code.
     *
     * @param code The assertion code to be validated.
     * @return True if the assertion is syntactically correct, false otherwise.
     */
    private boolean isSyntacticCorrectAssertion(final String code) {
        ErrorChecker.getInstance().resetError();
        var codeFragment = codeParser.parseCodeFragment(code);
        if (ErrorChecker.getInstance().errorOccurred()) {
            return false;
        }
        return validAssertionVisitor.visitExpression(codeFragment.expression());
    }
}
