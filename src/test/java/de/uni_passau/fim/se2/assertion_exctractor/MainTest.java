package de.uni_passau.fim.se2.assertion_exctractor;

import ch.qos.logback.core.util.FileUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.assertion_exctractor.data.RawMethodData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.rules.TemporaryFolder;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    @TempDir
    Path tempDir;
    @Test
    void testMainRun() throws URISyntaxException, IOException {
        URL r = Thread.currentThread().getContextClassLoader().getResource("example_files/results-small.jsonl");
        assertThat(r).isNotNull();
        Path copiedFile = Path.of(tempDir.toString(), "result.jsonl");
        Files.copy(Paths.get(r.toURI()), copiedFile);
        String[] args = new String[] {"preprocess","--data-dir", copiedFile.toString(),"--save-dir",tempDir.toString(),"--model","atlas:toga:code2seq:asserT5:gpt", "-m","1"};
        new CommandLine(new Main()).execute(args);
    }


}