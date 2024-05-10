package de.uni_passau.fim.se2.assertion_exctractor;

import java.util.concurrent.Callable;

import de.uni_passau.fim.se2.assertion_exctractor.subcommand.AsserT5Subcommand;
import de.uni_passau.fim.se2.assertion_exctractor.subcommand.AssertionCheckSubcommand;
import de.uni_passau.fim.se2.assertion_exctractor.subcommand.PreprocessorSubcommand;
import picocli.CommandLine;

@CommandLine.Command(
    name = "Assertion Extractor",
    version = "0.1",
    mixinStandardHelpOptions = true,
    subcommands = { PreprocessorSubcommand.class, AssertionCheckSubcommand.class, AsserT5Subcommand.class }
)
public class Main implements Callable<Integer> {

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    /**
     * The main entry point for the Assertion Extractor tool. Parses command-line arguments, executes the specified
     * subcommand, and exits.
     *
     * @param args Command-line arguments provided by the user.
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        spec.commandLine().usage(System.out);
        return 0;
    }
}
