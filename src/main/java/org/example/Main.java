package org.example;

import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.AstNode;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.declaration.MemberDeclarator;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.declaration.MethodDeclaration;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.expression.MethodInvocation;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.identifier.SimpleIdentifier;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.statement.ExpressionStmt;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.statement.Statement;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.transformer.util.TransformMode;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.visitor.AstVisitorWithDefaults;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.CommonPreprocessorOptions;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.ast_conversion.AstConverterPreprocessor;
import de.uni_passau.fim.se2.deepcode.toolbox.tokens.MethodTokenExtractor;
import de.uni_passau.fim.se2.deepcode.toolbox.tokens.MethodTokens;

import java.sql.SQLOutput;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        String code = """
                @Test
                public void testSum() {
                    int x = 3;
                    assertMul(x, 35);
                    for (var x : childs) {
                        assertEquals(result.parseInt() , 35);
                    }
                    int y = 4;
                    assertNotNull(y);
                }""";
        MethodTokenExtractorAssertions extractor = new MethodTokenExtractorAssertions();
        TestCase testCase = extractor.extractAssertions(code);
        testCase.printAndReplaceAssertions();


    }
}