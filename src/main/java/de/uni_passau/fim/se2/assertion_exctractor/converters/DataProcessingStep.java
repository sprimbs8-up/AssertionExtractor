package de.uni_passau.fim.se2.assertion_exctractor.converters;

import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;

public interface DataProcessingStep<FROM, TO> {

    Pair<String, TO> process(Pair<String, FROM> from);
}
