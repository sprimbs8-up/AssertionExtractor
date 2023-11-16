package de.uni_passau.fim.se2.assertion_exctractor.parsing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.assertj.core.api.Assertions;
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
                assertEquals(value, 5);
                assertNotEquals(value, 3);
                assertThrows(Exception.class, () -> doFoo());
            }""";
        TestCase testCase = parser.parseTestCase(code);
        List<AssertionType> parsedTypes = testCase.testElements().stream()
            .filter(Assertion.class::isInstance)
            .map(Assertion.class::cast)
            .map(Assertion::type)
            .toList();
        Assertions.assertThat(parsedTypes).containsExactly(
            AssertionType.ASSERT_TRUE, AssertionType.ASSERT_FALSE, AssertionType.ASSERT_NULL,
            AssertionType.ASSERT_NOT_NULL, AssertionType.ASSERT_EQUALS, AssertionType.ASSERT_NOT_EQUALS,
            AssertionType.ASSERT_THROWS
        );

    }

    @Test
    public void testAllAssertionTypesButNotCorrectForm() {
        String code = """
            @Test
            public void testSum() {
                assertTrue();
                assertFalse(value, 12);
                assertNull(value, value2);
                assertNotNull(value, value2, value4);
                assertEquals(value);
                assertNotEquals(value);
                assertThrows(Exception.class, () -> doFoo(), () -> doBar());
            }""";
        TestCase testCase = parser.parseTestCase(code);
        List<AssertionType> parsedTypes = testCase.testElements().stream()
            .filter(Assertion.class::isInstance)
            .map(Assertion.class::cast)
            .map(Assertion::type)
            .toList();
        Assertions.assertThat(parsedTypes).isEmpty();

    }

    @Test
    public void testAssertForAssertion() {
        String code = """
            @Test
            public void test() {
                Assert.assertTrue(value);
            }""";
        TestCase testCase = parser.parseTestCase(code);
        assertThat(testCase.testElements()).hasSize(3);
        assertThat(testCase.testElements().get(0)).isInstanceOf(TestSequence.class);
        assertThat(testCase.testElements().get(0).tokens())
                .containsExactly("@ Test public void test ( ) {".split(" "));
        assertThat(testCase.testElements().get(1)).isInstanceOf(Assertion.class);
        assertThat(testCase.testElements().get(1).tokens())
                .containsExactly("assertTrue ( value )".split(" "));
        assertThat(testCase.testElements().get(2)).isInstanceOf(TestSequence.class);
        assertThat(testCase.testElements().get(2).tokens()).containsExactly(";", "}");

    }

    @Test
    public void testTryCatchAssertionParsing() {
        String code = """
            @Test
            public void testSum() {
                doThingsBeforeTryCatch();
                try {
                    doSomeThings();
                    doAnotherThing();
                    doSomeThingsWithException();
                    Assert.fail();
                 } catch (Exception e) {
                    verifyException(e);
                 }
                 doThingsAfterTryCatch();
            }""";
        TestCase testCase = parser.parseTestCase(code);
        testCase.print();
    }

}
