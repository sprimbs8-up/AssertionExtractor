package de.uni_passau.fim.se2.assertion_exctractor;

import de.uni_passau.fim.se2.assertion_exctractor.parsing.*;
import de.uni_passau.fim.se2.assertion_exctractor.processors.ProcessorFactory;
import de.uni_passau.fim.se2.assertion_exctractor.subcommand.AssertionCheckSubcommand;
import de.uni_passau.fim.se2.assertion_exctractor.subcommand.PreprocessorSubcommand;
import de.uni_passau.fim.se2.assertion_exctractor.utils.RandomUtil;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
    name = "Assertion Exctractor",
    version = "0.1",
    mixinStandardHelpOptions = true,
        subcommands = {PreprocessorSubcommand.class, AssertionCheckSubcommand.class}
)
public class Main implements Callable<Integer> {


    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call()  {
        spec.commandLine().usage(System.out);

        return 0;
    }
}
