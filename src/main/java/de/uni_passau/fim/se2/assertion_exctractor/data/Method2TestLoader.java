package de.uni_passau.fim.se2.assertion_exctractor.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import de.uni_passau.fim.se2.assertion_exctractor.utils.ProgressBarContainer;
import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import me.tongfei.progressbar.ProgressBar;

public final class Method2TestLoader {

    public static Stream<JSONObject> loadDatasetAsJSON(String baseDir) throws IOException {
        JSONParser parser = new JSONParser();
        List<Path> files = listFiles(Path.of(baseDir));
        ProgressBarContainer.getInstance().setProgressBar("Preparing dataset", files.size());
        ProgressBarContainer.getInstance().notifyStart();
        return files.stream()
                .map(Path::toFile)
                .map(Method2TestLoader::createFileReader)
                .map(reader -> parse(parser, reader))
                .filter(JSONObject.class::isInstance)
                .map(JSONObject.class::cast);

    }

    public static List<Path> listFiles(Path path) throws IOException {

        List<Path> result;
        try (Stream<Path> walk = Files.walk(path)) {
            result = walk.filter(Files::isRegularFile).toList();
        }
        return result;

    }

    private static FileReader createFileReader(File file) {
        try {
            return new FileReader(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object parse(JSONParser parser, FileReader fileReader) {
        try {
            return parser.parse(fileReader);
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

}
