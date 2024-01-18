package de.uni_passau.fim.se2.assertion_exctractor.loading;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_passau.fim.se2.assertion_exctractor.data.RawMethodData;
import de.uni_passau.fim.se2.assertion_exctractor.utils.ProgressBarContainer;
import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;

/**
 * The Method2TestLoader class is responsible for loading method-to-test data from JSON files and providing a stream of
 * pairs where each pair contains a String identifier and the corresponding raw method data.
 */
public final class Method2TestLoader {

    private Method2TestLoader() {
        throw new IllegalCallerException("Private constructor should not be called!");
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Method2TestLoader.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static final String LINES_SUFFIX = ".lines";

    /**
     * Loads the dataset from the specified JSON file and returns a stream of pairs, where each pair contains a String
     * identifier and the corresponding raw method data.
     *
     * @param preparedFile The path to the prepared JSON file.
     * @return A stream of pairs representing the loaded data.
     * @throws IOException If an I/O error occurs while reading the file.
     */
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

    /**
     * Parses a line of JSON data into a pair containing a String identifier and the corresponding raw method data.
     *
     * @param line A line of JSON data.
     * @return A pair representing the parsed data, or null if parsing is not successful.
     */
    private static Pair<String, RawMethodData> parseMethodData(String line) {
        try {
            return Pair.of(line, OBJECT_MAPPER.readValue(line, RawMethodData.class));
        }
        catch (JsonProcessingException e) {
            LOGGER.debug("Processing of " + line.substring(0, 50) + "... was not possible", e);
            return null;
        }
    }

    /**
     * Lists all lines from the specified file and returns them as a stream of strings (lazy loading).
     *
     * @param path The path to the file.
     * @return A stream of lines from the file.
     * @throws IOException If an I/O error occurs while reading the file.
     */
    private static Stream<String> listFiles(Path path) throws IOException {
        return Files.lines(path);
    }

    /**
     * Reads the number of lines from the specified file or counts them if the file does not exist.
     *
     * @param file The path to the file.
     * @return The number of lines in the file.
     */
    private static int readNumberLines(String file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file + LINES_SUFFIX))) {
            return Integer.parseInt(reader.readLine());
        }
        catch (IOException e) {
            // If file does not exist.
            int numberLinesOfFile = countLines(file);
            try (FileOutputStream outputStream = new FileOutputStream(file + LINES_SUFFIX)) {
                outputStream.write(String.valueOf(numberLinesOfFile).getBytes());
            }
            catch (IOException ex) {
                LOGGER.warn("File for the line numbers was not exported correctly.");
            }
            return numberLinesOfFile;

        }
    }

    /**
     * Counts the number of lines in the specified file.
     *
     * @param file The path to the file.
     * @return The number of lines in the file.
     */
    private static int countLines(String file) {
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
}
