package de.uni_passau.fim.se2.assertion_exctractor.converters;

public interface DataProcessingStep<FROM, TO> {

    TO convert(FROM from);
}
