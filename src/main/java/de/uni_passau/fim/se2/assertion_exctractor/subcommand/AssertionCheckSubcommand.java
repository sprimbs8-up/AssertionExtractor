package de.uni_passau.fim.se2.assertion_exctractor.subcommand;

import de.uni_passau.fim.se2.assertion_exctractor.processors.ProcessorFactory;
import de.uni_passau.fim.se2.assertion_exctractor.utils.RandomUtil;
import picocli.CommandLine;

@CommandLine.Command(
        name="check",
        mixinStandardHelpOptions = true,
        showDefaultValues = true
)
public class AssertionCheckSubcommand implements Runnable{

    @CommandLine.Spec
    protected CommandLine.Model.CommandSpec spec;
    @CommandLine.Option(
            names = { "-c","--code" },
            description = "The code of the assertion",
            required = true
    )
    String code;


    @Override
    public void run() {
        System.out.println(code);
    }
}
