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

public class AssertionParser extends TokenParser {

    private static final Logger LOG = LoggerFactory.getLogger(AssertionParser.class);

    private final ValidAssertionVisitor validAssertionVisitor = new ValidAssertionVisitor();
    private final ObjectMapper mapper = new ObjectMapper();

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

    private boolean isSyntacticCorrectAssertion(final String code) {
        ErrorChecker.getInstance().resetError();
        var codeFragment = codeParser.parseCodeFragment(code);
        if (ErrorChecker.getInstance().errorOccurred()) {
            return false;
        }
        return validAssertionVisitor.visitExpression(codeFragment.expression());
    }
}
