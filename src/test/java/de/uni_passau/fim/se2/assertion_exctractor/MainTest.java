package de.uni_passau.fim.se2.assertion_exctractor;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.assertion_exctractor.data.RawMethodData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    @TempDir
    Path tempDir;
    @Test
    void testMainRun() throws IOException {
        URL r = Thread.currentThread().getContextClassLoader().getResource("example_files/results-small.jsonl");
        assert r != null;
        Main.main(new String[] {"preprocess","--data-dir",r.getPath(),"--save-dir",tempDir.toString(),"--model","atlas:toga:code2seq:asserT5:gpt", "-m","1"});
    }

}