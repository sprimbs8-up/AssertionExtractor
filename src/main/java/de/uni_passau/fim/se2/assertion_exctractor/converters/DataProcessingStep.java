package de.uni_passau.fim.se2.assertion_exctractor.converters;

import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;

/**
 * The {@link DataProcessingStep} interface defines a contract for a generic data processing step. Implementing classes
 * are expected to process data from one type (FROM) to another type (TO), encapsulated in a {@link Pair} structure.
 *
 * @param <FROM> The type of data to be processed.
 * @param <TO>   The type of data after processing.
 */
public interface DataProcessingStep<FROM, TO> {

    /**
     * Processes the input data and returns the result encapsulated in a {@link Pair}.
     *
     * @param from A Pair containing a String identifier (expected as the source-file name) and the input data of type
     *             FROM.
     * @return A {@link Pair} containing a String identifier and the processed data of type TO.
     */
    Pair<String, TO> process(Pair<String, FROM> from);
}
