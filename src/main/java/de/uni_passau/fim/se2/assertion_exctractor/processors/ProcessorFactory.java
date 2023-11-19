package de.uni_passau.fim.se2.assertion_exctractor.processors;

import org.apache.commons.lang3.NotImplementedException;

public class ProcessorFactory {

    public static Processor loadProcessor(String modelType, String dataDir, String saveDir, int maxAssertions) {
        return switch (modelType) {
            case "atlas" -> new AtlasProcessor(dataDir, saveDir, maxAssertions);
            case "toga" -> throw new NotImplementedException("TOGA is not implemented.");
            default -> throw new IllegalArgumentException("The model \"modelType\" is not present.");
        };
    }
}
