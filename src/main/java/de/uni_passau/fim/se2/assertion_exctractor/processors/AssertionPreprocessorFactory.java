package de.uni_passau.fim.se2.assertion_exctractor.processors;

import java.util.Arrays;
import java.util.List;

/**
 * Factory class responsible for loading and creating instances different {@link AssertionPreprocessor} wrapped in
 * {@link CombinedAssertionPreprocessor} based on specified model types.
 */
public class AssertionPreprocessorFactory {

    /**
     * Loads processors based on the provided model types and creates a {@link CombinedAssertionPreprocessor}.
     *
     * @param modelsTypes   A colon-separated string specifying the model types to load as list.
     * @param dataDir       The directory containing data used by the processors.
     * @param saveDir       The directory where the processors can save their output.
     * @param maxAssertions The maximum number of assertions to be processed by the combined processor.
     * @return A {@link CombinedAssertionPreprocessor} instance with loaded processors.
     */
    public static CombinedAssertionPreprocessor loadProcessors(
        String modelsTypes, String dataDir, String saveDir, int maxAssertions
    ) {
        String[] types = modelsTypes.split(":");
        List<AssertionPreprocessor> processors = Arrays.stream(types)
            .distinct()
            .map(model -> loadProcessor(model, dataDir, saveDir, maxAssertions))
            .toList();
        return new CombinedAssertionPreprocessor(dataDir, saveDir, maxAssertions, processors);
    }

    /**
     * Loads a specific type of {@link AssertionPreprocessor} based on the provided model type.
     *
     * @param modelType     The type of model for which the processor should be loaded.
     * @param dataDir       The directory containing data used by the processor.
     * @param saveDir       The directory where the processor can save its output.
     * @param maxAssertions The maximum number of assertions to be processed by the processor.
     * @return An instance of {@link AssertionPreprocessor} corresponding to the specified model type.
     * @throws IllegalArgumentException if the specified model type is not recognized.
     */
    private static AssertionPreprocessor loadProcessor(
        String modelType, String dataDir, String saveDir, int maxAssertions
    ) {
        return switch (modelType) {
            case "atlas" -> new AtlasPreprocessor(dataDir, saveDir, maxAssertions);
            case "toga" -> new TogaPreprocessor(dataDir, saveDir, maxAssertions);
            case "code2seq" -> new Code2SeqProcessor(dataDir, saveDir, maxAssertions);
            case "asserT5" -> new JavaAsserT5Preprocessor(dataDir, saveDir, maxAssertions);
            default -> throw new IllegalArgumentException("The model \"" + modelType + "\" is not present.");
        };
    }

}
