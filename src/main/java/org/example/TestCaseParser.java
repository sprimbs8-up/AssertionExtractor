package org.example;

public class TestCaseParser {
    public TestCase parseTestCase(final String testCode) {
        MethodTokenExtractorAssertions extractor = new MethodTokenExtractorAssertions();
        return extractor.extractAssertions(testCode);
    }
}
