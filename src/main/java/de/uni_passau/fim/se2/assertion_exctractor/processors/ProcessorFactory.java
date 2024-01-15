package de.uni_passau.fim.se2.assertion_exctractor.processors;

import java.util.Arrays;
import java.util.List;

public class ProcessorFactory {

    private static Processor loadProcessor(String modelType, String dataDir, String saveDir, int maxAssertions) {
        return switch (modelType) {
            case "atlas" -> new AtlasProcessor(dataDir, saveDir, maxAssertions);
            case "toga" -> new TogaProcessor(dataDir, saveDir, maxAssertions);
            case "code2seq" -> new Code2SeqProcessor(dataDir, saveDir, maxAssertions);
            case "ata" -> new ATAClassPreprocessor(dataDir, saveDir, maxAssertions);
            default -> throw new IllegalArgumentException("The model \"" + modelType + "\" is not present.");
        };
    }

    public static CombinedProcessor loadProcessors(
        String modelsTypes, String dataDir, String saveDir, int maxAssertions
    ) {
        List<Processor> processors = Arrays.stream(modelsTypes.split(":")).distinct()
            .map(model -> loadProcessor(model, dataDir, saveDir, maxAssertions)).toList();
        return new CombinedProcessor(dataDir, saveDir, maxAssertions, processors);
    }
}
