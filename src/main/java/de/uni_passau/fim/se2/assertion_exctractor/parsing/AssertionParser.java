package de.uni_passau.fim.se2.assertion_exctractor.parsing;

import de.uni_passau.fim.se2.assertion_exctractor.utils.ErrorChecker;
import org.antlr.runtime.tree.ParseTree;

import java.util.Optional;
import java.util.stream.Stream;

public class AssertionParser extends FocalMethodParser{

    public Optional<ParseTree> parseAssertionToParseTree(final String code) {
        ErrorChecker.getInstance().resetError();
        var codeFragment = codeParser.parseCodeFragment(code);
        if (ErrorChecker.getInstance().errorOccurred()) {
            return Optional.empty();
        }
        var x = methodTokenVisitor.visitStatement(codeFragment.statement());
        return Optional.empty();
    }
}
