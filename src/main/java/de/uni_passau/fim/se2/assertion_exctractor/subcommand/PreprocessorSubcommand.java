package de.uni_passau.fim.se2.assertion_exctractor.subcommand;

import de.uni_passau.fim.se2.assertion_exctractor.processors.AssertionPreprocessorFactory;
import de.uni_passau.fim.se2.assertion_exctractor.utils.RandomUtil;
import picocli.CommandLine;

@CommandLine.Command(
    name = "preprocess",
    mixinStandardHelpOptions = true,
    showDefaultValues = true
)
public class PreprocessorSubcommand implements Runnable {

    @CommandLine.Spec
    protected CommandLine.Model.CommandSpec spec;
    @CommandLine.Option(
        names = { "-m", "--max-assertions" },
        description = "Number of Maximal Assertions.",
        defaultValue = "1"
    )
    int maxAssertions;

    @CommandLine.Option(
        names = { "-d", "--data-file" },
        description = "The directory of the data.",
        required = true
    )
    String dataDir;
    @CommandLine.Option(
        names = { "-s", "--save-dir" },
        description = "The directory to save the data.",
        required = true
    )
    String saveDir;
    @CommandLine.Option(
        names = { "--model" },
        description = "The model the data should be parsed.",
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
        AssertionPreprocessorFactory.loadProcessors(modelType, dataDir, saveDir, maxAssertions)
            .exportProcessedExamples();
    }
}
