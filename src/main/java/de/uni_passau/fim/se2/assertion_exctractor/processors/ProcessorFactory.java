package de.uni_passau.fim.se2.assertion_exctractor.processors;

public class ProcessorFactory {

    public static Processor loadProcessor(String modelType, String dataDir, String saveDir, int maxAssertions) {
        return switch (modelType) {
            case "atlas" -> new AtlasProcessor(dataDir, saveDir, maxAssertions);
            case "toga" -> new TogaProcessor(dataDir, saveDir, maxAssertions);
            case "code2seq" -> new Code2SeqProcessor(dataDir, saveDir, maxAssertions);
            default -> throw new IllegalArgumentException("The model \"" + modelType + "\" is not present.");
        };
    }
}
