package de.uni_passau.fim.se2.assertion_exctractor.parsing;

import de.uni_passau.fim.se2.assertion_exctractor.data.JavaDocMethod;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TokenParserTest {
    private static final String FOCAL_METHOD_WITH_JAVA_DOC = """
            /**
            * This is an arbitrary JavaDoc comment.
            */
            public String foo() {
                return "foo";
            }
            """;


    @Test
    void testParseMethodToJavaDocMethods(){
        TokenParser parser = new TokenParser();
        List<JavaDocMethod> javaDocMethods = parser.parseClassToJavaDocMethods(FOCAL_METHOD_WITH_JAVA_DOC).toList();
        assertThat(javaDocMethods).hasSize(1);
        JavaDocMethod method = javaDocMethods.get(0);
        assertThat(method.methodTokens()).containsExactly("public", "String", "foo", "(",")","{","return","\"foo\"",";","}");
        assertThat(method.text()).isEqualTo("This is an arbitrary JavaDoc comment.");
    }
    @Test
    void testParseClassToJavaDocMethods(){
        TokenParser parser = new TokenParser();
        String focalClass = "class Foo {\n"+FOCAL_METHOD_WITH_JAVA_DOC  +"\n"+FOCAL_METHOD_WITH_JAVA_DOC  +"\n"+"}";
        List<JavaDocMethod> javaDocMethods = parser.parseClassToJavaDocMethods(focalClass).toList();
        assertThat(javaDocMethods).hasSize(2);
    }

    @Test
    void testParseCodeToCodeTokens(){
        TokenParser parser = new TokenParser();
        List<String> javaDocMethods = parser.convertCodeToTokenStrings(FOCAL_METHOD_WITH_JAVA_DOC).toList();
        assertThat(javaDocMethods).containsExactly("/**\n* This is an arbitrary JavaDoc comment.\n*/","public", "String", "foo", "(",")","{","return","\"foo\"",";","}");
    }
}