package de.uni_passau.fim.se2.assertion_exctractor.subcommand;

import de.uni_passau.fim.se2.assertion_exctractor.parsing.AssertionParser;
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
            names = { "-c","--codes" },
            description = "The codes of the assertions",
            required = true
    )
    private String codes;


    @Override
    public void run() {
        AssertionParser a = new AssertionParser();
        System.out.println(a.areSyntacticCorrectAssertions(codes));
    }
}
