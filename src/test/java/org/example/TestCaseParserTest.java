package org.example;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class TestCaseParserTest {

    private final TestCaseParser parser = new TestCaseParser();

    @Test
    public void testAssertEquals() {
        String code = """
            @Test
            public void test() {
                int x = 3;
                assertMul(x, 35);
                assertEquals(result.parseInt() , 35);
            }""";
        TestCase testCase = parser.parseTestCase(code);
        assertThat(testCase.testElements()).hasSize(3);
        assertThat(testCase.testElements().get(0)).isInstanceOf(TestSequence.class);
        assertThat(testCase.testElements().get(0).tokens())
            .containsExactly("@ Test public void test ( ) { int x = 3 ; assertMul ( x , 35 ) ;".split(" "));

        assertThat(testCase.testElements().get(1)).isInstanceOf(Assertion.class);
        assertThat(testCase.testElements().get(1).tokens())
            .containsExactly("assertEquals ( result . parseInt ( ) , 35 )".split(" "));
        assertThat(testCase.testElements().get(2)).isInstanceOf(TestSequence.class);
        assertThat(testCase.testElements().get(2).tokens()).containsExactly(";", "}");

    }

    @Test
    public void testAllAssertionTypes() {
        String code = """
            @Test
            public void testSum() {
                assertTrue(value);
                assertFalse(value);
                assertNull(value);
                assertNotNull(value);
                assertEquals(value);
                assertNotEquals(value);
                assertThrows(Exception.class, () -> doFoo());
            }""";
        TestCase testCase = parser.parseTestCase(code);
        List<AssertionType> parsedTypes = testCase.testElements().stream()
            .filter(Assertion.class::isInstance)
            .map(Assertion.class::cast)
            .map(Assertion::type)
            .toList();
        assertThat(parsedTypes).containsExactly(
            AssertionType.ASSERT_TRUE, AssertionType.ASSERT_FALSE, AssertionType.ASSERT_NULL,
            AssertionType.ASSERT_NOT_NULL, AssertionType.ASSERT_EQUALS, AssertionType.ASSERT_NOT_EQUALS,
            AssertionType.ASSERT_THROWS
        );

    }

}
