package org.example;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TestCaseParserTest {
    private final TestCaseParser parser = new TestCaseParser();

    @Test
    public void testAssertEquals() {
        String code = """
                @Test
                public void testSum() {
                    int x = 3;
                    assertMul(x, 35);
                    assertEquals(result.parseInt() , 35);
                }""";
        TestCase testCase = parser.parseTestCase(code);
        assertThat(testCase.testElements()).hasSize(3);
        assertThat(testCase.testElements().get(0)).isInstanceOf(TestSequence.class);
        assertThat(testCase.testElements().get(0).tokens()).containsExactly(
                "@", "Test", "public", "void", "testSum", "(", ")", "{",
                "int", "x", "=", "3", ";", "assertMul", "(", "x", ",", "35", ")", ";"
        );
        assertThat(testCase.testElements().get(1)).isInstanceOf(Assertion.class);
        assertThat(testCase.testElements().get(1).tokens()).containsExactly("assertEquals","(","result",".","parseInt","(",")",",","35",")");
        assertThat(testCase.testElements().get(2)).isInstanceOf(TestSequence.class);
        assertThat(testCase.testElements().get(2).tokens()).containsExactly(";","}");

    }

}