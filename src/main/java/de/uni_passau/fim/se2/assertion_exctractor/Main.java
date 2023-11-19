package de.uni_passau.fim.se2.assertion_exctractor;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.LogManager;

import de.uni_passau.fim.se2.assertion_exctractor.parsing.*;
import de.uni_passau.fim.se2.assertion_exctractor.processors.ProcessorFactory;
import de.uni_passau.fim.se2.assertion_exctractor.utils.RandomUtil;
import org.json.simple.JSONObject;

import de.uni_passau.fim.se2.assertion_exctractor.data.Method2TestLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;
import org.slf4j.spi.LoggingEventBuilder;
import picocli.CommandLine;

@CommandLine.Command(name = "Assertion Exctractor", version = "0.1", mixinStandardHelpOptions = true)
public class Main implements Runnable {
    @CommandLine.Option(names = {"-m", "--max-assertions"}, description = "Number of Maximal Assertions", defaultValue = "1")
    int maxAssertions;

    @CommandLine.Option(names = {"-d", "--data-dir"}, description = "The direcotry of the data", required = true)
    String dataDir;
    @CommandLine.Option(names = {"-s", "--save-dir"}, description = "The direcotry to save the data", required = true)
    String saveDir;
    @CommandLine.Option(names = {"--model"}, description = "The model the data should be parsed", required = true)
    String modelType;
    @CommandLine.Option(names = {"--seed"}, description = "The seed for Randomness.", defaultValue = "1")
    int seed;


    @Override
    public void run() {
        RandomUtil.getInstance().initializeRandom(seed);
        ProcessorFactory.loadProcessor(modelType, dataDir, saveDir, maxAssertions).exportProcessedExamples();
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }




}
