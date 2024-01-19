package de.uni_passau.fim.se2.assertion_exctractor.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AssertionNormalizerTest {

    @Test
    void testRegex() {
        String s = "org.junit.Assert.assertEquals(asfd, asdfs)";
        String cleanedCode = Utils.normalizeAssertions(s);
        assertThat(cleanedCode).isEqualTo("assertEquals(asfd, asdfs)");
    }

    @Test
    void testRegex2() {
        String s = "org .junit.Assert .assertEquals(asfd, asdfs)";
        String cleanedCode = Utils.normalizeAssertions(s);
        assertThat(cleanedCode).isEqualTo("assertEquals(asfd, asdfs)");
    }

    @Test
    void testRegex3() {
        String s = "org . junit . Assert . assertEquals(asfd, asdfs)";
        String cleanedCode = Utils.normalizeAssertions(s);
        assertThat(cleanedCode).isEqualTo("assertEquals(asfd, asdfs)");
    }

    @Test
    void testRegex4() {
        String s = "org . junit . Assert . noValidAssertion(asfd, asdfs)";
        String cleanedCode = Utils.normalizeAssertions(s);
        assertThat(cleanedCode).isEqualTo("org . junit . Assert . noValidAssertion(asfd, asdfs)");
    }

    @Test
    void testRegex5() {
        String s = """
            public void testMethod() {
                String s = "foo";
                assertEquals(s, "foo");
                org.junit.Assert.assertEquals("foo", foo2);
                TestUtils. assertEquals("foo", foo2");
                Test2Utils. assertNotNull(foo);
            }
            """;
        String expected = """
            public void testMethod() {
                String s = "foo";
                assertEquals(s, "foo");
                assertEquals("foo", foo2);
                assertEquals("foo", foo2");
                assertNotNull(foo);
            }
            """;
        String cleanedCode = Utils.normalizeAssertions(s);
        assertThat(cleanedCode).isEqualTo(expected);
    }
}
