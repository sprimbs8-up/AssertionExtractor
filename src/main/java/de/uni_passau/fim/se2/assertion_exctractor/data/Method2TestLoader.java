package de.uni_passau.fim.se2.assertion_exctractor.data;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.assertion_exctractor.utils.ProgressBarContainer;
import de.uni_passau.fim.se2.assertion_exctractor.utils.RandomUtil;
import de.uni_passau.fim.se2.assertion_exctractor.utils.StatisticsContainer;
import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import me.tongfei.progressbar.ProgressBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Method2TestLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(Method2TestLoader.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static Stream<PreparedMethodData> loadDatasetAsJSON(String preparedFile) throws IOException {
        List<PreparedMethodData> files = listFiles(Path.of(preparedFile))
                .flatMap(Method2TestLoader::parseMethodData)
                .toList();
        files = new ArrayList<>(files);
        Collections.shuffle(files, RandomUtil.getInstance().getRandom());
        ProgressBarContainer.getInstance().setProgressBar("Preparing dataset", files.size());
        ProgressBarContainer.getInstance().notifyStart();
        return files.stream();
    }

    private static Stream<PreparedMethodData> parseMethodData(String line) {
        try {
            return Stream.of(OBJECT_MAPPER.readValue(line, PreparedMethodData.class));
        } catch (JsonProcessingException e) {
            LOGGER.debug("Processing of " + line.substring(0,50) + "... was not possible", e);
            return Stream.empty();
        }
    }

    public static Stream<String> listFiles(Path path) throws IOException {
        try (FileReader fr = new FileReader(String.valueOf(path))) {
            BufferedReader br = new BufferedReader(fr);  //creates a buffering character input stream
            Stream.Builder<String> b = Stream.builder();
            String line;
            while ((line = br.readLine()) != null) {
                b.add(line);
            }
            return b.build();
        }
    }
}

