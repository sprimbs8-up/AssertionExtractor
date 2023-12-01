package de.uni_passau.fim.se2.assertion_exctractor.data;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import jline.internal.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_passau.fim.se2.assertion_exctractor.utils.ProgressBarContainer;
import de.uni_passau.fim.se2.assertion_exctractor.utils.RandomUtil;

public final class Method2TestLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(Method2TestLoader.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static Stream<PreparedMethodData> loadDatasetAsJSON(String preparedFile) throws IOException {
        LOGGER.info("Load data from json");
        int numberOfLines = readNumberLines(preparedFile);
        LOGGER.info("Line numbers loaded.");
        ProgressBarContainer.getInstance().setProgressBar("Preparing dataset",numberOfLines);
        ProgressBarContainer.getInstance().notifyStart();
        Stream<PreparedMethodData> files = listFiles(Path.of(preparedFile))
                    .map(Method2TestLoader::parseMethodData)
                    .filter(Objects::nonNull);
        return files.sorted((x, y) -> x == y ? 0 : RandomUtil.getInstance().getRandom().nextBoolean() ? -1 : 1);
    }

    private static PreparedMethodData parseMethodData(String line) {
        try {
            return OBJECT_MAPPER.readValue(line, PreparedMethodData.class);
        }
        catch (JsonProcessingException e) {
            LOGGER.debug("Processing of " + line.substring(0, 50) + "... was not possible", e);
            return null;
        }
    }

    public static Stream<String> listFiles(Path path) throws IOException {
        try (FileReader fr = new FileReader(String.valueOf(path))) {
            BufferedReader br = new BufferedReader(fr);  // creates a buffering character input stream
            Stream.Builder<String> b = Stream.builder();
            String line;
            while ((line = br.readLine()) != null) {
                b.add(line);
            }
            return b.build();
        }
    }

    public static int numberLinesOf(String file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))){
            int lines = 0;
            while (reader.readLine() != null) {
                lines++;
                System.out.print("\r"+lines);
            }
            return lines;
        } catch (IOException e) {
            return 0;
        }


    }

    public static int readNumberLines(String file){
        try (BufferedReader reader = new BufferedReader(new FileReader(file+".lines"))){
            return Integer.parseInt(reader.readLine());
        } catch (IOException e) {
            return numberLinesOf(file);
        }
    }
}
