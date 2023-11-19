package de.uni_passau.fim.se2.assertion_exctractor.parsing;

import de.uni_passau.fim.se2.deepcode.toolbox.ast.generated.JavaParser;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.generated.JavaParserBaseVisitor;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.parser.CodeParser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class FocalMethodParser {

    private static class CustomCodeParser extends CodeParser {

        @Override
        public JavaParser parseCodeFragment(String code) {
            JavaParser parser = super.parseCodeFragment(code);
            parser.getErrorListeners().clear();
            return parser;
        }
    }

    public Stream<String> parseMethod(final String code) {
        final CodeParser codeParser = new CustomCodeParser();
        final MethodTokenVisitor visitor = new MethodTokenVisitor();
        visitor.visitClassBodyDeclaration(codeParser.parseCodeFragment(code).classBodyDeclaration());
        return visitor.codeStream;
    }

    private static class MethodTokenVisitor extends JavaParserBaseVisitor<Void> {

        private Stream<String> codeStream = Stream.empty();

        @Override
        public Void visitClassBodyDeclaration(JavaParser.ClassBodyDeclarationContext ctx) {
            traverseTestCase(ctx);
            return null;
        }

        private void traverseTestCase(ParseTree parseTree) {
            if (parseTree.getChildCount() == 0) {
                codeStream = Stream.concat(codeStream, Stream.of(parseTree.getText()));
                return;
            }
            for (int i = 0; i < parseTree.getChildCount(); i++) {
                ParseTree child = parseTree.getChild(i);
                traverseTestCase(child);
            }
        }
    }
}
