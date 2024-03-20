package de.uni_passau.fim.se2.assertion_exctractor.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.assertion_exctractor.data.Assertion;
import de.uni_passau.fim.se2.assertion_exctractor.data.FineMethodData;
import de.uni_passau.fim.se2.assertion_exctractor.data.RawMethodData;
import de.uni_passau.fim.se2.assertion_exctractor.data.TestCase;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.statement.try_statement.Resources;
import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class Raw2FineDataPStepTest {

    @Test
    void testRaw2FineDataStep() throws IOException {
        URL r = Thread.currentThread().getContextClassLoader().getResource("example_files/one-example.jsonl");
        ObjectMapper m = new ObjectMapper();
        RawMethodData rawMethodData = m.readValue(r, RawMethodData.class);

        Raw2FineDataPStep step = new Raw2FineDataPStep();
        Pair<String, Optional<FineMethodData>> pair = step.process(Pair.of("path", rawMethodData));
        assertThat(pair.b()).isPresent();
        FineMethodData data = pair.b().get();
        assertThat(data.documentation()).isNullOrEmpty();
        TestCase tc = data.testCase();
        assertThat(tc.testElements()).hasSize(7);
        assertThat(tc.testElements().stream().filter(Assertion.class::isInstance)).hasSize(3);
        assertThat(data.focalMethodTokens()).hasSize(40);
        assertThat(data.testClassTokens()).hasSize(234);
        assertThat(data.focalClassTokens()).hasSize(1537);
    }

    @Test
    void testRaw2FineDataStepTooLongTestMethod() throws IOException {
        URL r = Thread.currentThread().getContextClassLoader().getResource("example_files/one-example.jsonl");
        ObjectMapper m = new ObjectMapper();
        RawMethodData rawMethodData = m.readValue(r, RawMethodData.class);
        String extraLongTestCase = rawMethodData.testMethod().repeat(300);
        RawMethodData newRawMethodData = new RawMethodData(extraLongTestCase,rawMethodData.focalMethod(),rawMethodData.testFile(), rawMethodData.focalFile());

        Raw2FineDataPStep step = new Raw2FineDataPStep();
        Pair<String, Optional<FineMethodData>> pair = step.process(Pair.of("path", newRawMethodData));
        assertThat(pair.b()).isEmpty();
    }

}