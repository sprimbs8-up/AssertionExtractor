package de.uni_passau.fim.se2.assertion_exctractor.data;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_passau.fim.se2.assertion_exctractor.utils.ProgressBarContainer;
import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;

public final class Method2TestLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(Method2TestLoader.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static Stream<Pair<String, RawMethodData>> loadDatasetAsJSON(String preparedFile) throws IOException {
        LOGGER.info("Load data from json");
        int numberOfLines = readNumberLines(preparedFile);
        LOGGER.info("Line numbers loaded.");
        ProgressBarContainer.getInstance().setProgressBar("Preparing dataset", numberOfLines);
        ProgressBarContainer.getInstance().notifyStart();
        return listFiles(Path.of(preparedFile))
            .map(Method2TestLoader::parseMethodData)
            .filter(Objects::nonNull);
    }

    private static Pair<String, RawMethodData> parseMethodData(String line) {
        try {
            return Pair.of(line, OBJECT_MAPPER.readValue(line, RawMethodData.class));
        }
        catch (JsonProcessingException e) {
            LOGGER.debug("Processing of " + line.substring(0, 50) + "... was not possible", e);
            return null;
        }
    }

    public static Stream<String> listFiles(Path path) throws IOException {
        return Files.lines(path);
    }

    public static int numberLinesOf(String file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            int lines = 0;
            while (reader.readLine() != null) {
                lines++;
                if (lines % 1000 == 0) {
                    System.out.print("\rCurrently read lines:" + lines);
                }
            }
            System.out.print("\r");
            return lines;
        }
        catch (IOException e) {
            return 0;
        }

    }

    public static int readNumberLines(String file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file + ".lines"))) {
            return Integer.parseInt(reader.readLine());
        }
        catch (IOException e) {
            int numberLinesOfFile = numberLinesOf(file);
            try (FileOutputStream outputStream = new FileOutputStream(file + ".lines")) {
                outputStream.write(String.valueOf(numberLinesOfFile).getBytes());
            }
            catch (FileNotFoundException ex) {
                return numberLinesOfFile;
            }
            catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            return numberLinesOfFile;

        }
    }
}
