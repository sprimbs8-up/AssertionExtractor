package de.uni_passau.fim.se2.assertion_exctractor;

import de.uni_passau.fim.se2.assertion_exctractor.parsing.*;
import de.uni_passau.fim.se2.assertion_exctractor.processors.ProcessorFactory;
import de.uni_passau.fim.se2.assertion_exctractor.utils.RandomUtil;
import picocli.CommandLine;

@CommandLine.Command(
    name = "Assertion Exctractor",
    version = "0.1",
    mixinStandardHelpOptions = true
)
public class Main implements Runnable {

    @CommandLine.Option(
        names = { "-m", "--max-assertions" },
        description = "Number of Maximal Assertions",
        defaultValue = "1"
    )
    int maxAssertions;

    @CommandLine.Option(
        names = { "-d", "--data-dir" },
        description = "The direcotry of the data",
        required = true
    )
    String dataDir;
    @CommandLine.Option(
        names = { "-s", "--save-dir" },
        description = "The direcotry to save the data",
        required = true
    )
    String saveDir;
    @CommandLine.Option(
        names = { "--model" },
        description = "The model the data should be parsed",
        required = true
    )
    String modelType;
    @CommandLine.Option(
        names = { "--seed" },
        description = "The seed for Randomness.",
        defaultValue = "1"
    )
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
