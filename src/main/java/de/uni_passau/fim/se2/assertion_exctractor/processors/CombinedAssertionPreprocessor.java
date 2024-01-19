package de.uni_passau.fim.se2.assertion_exctractor.processors;

import java.util.List;

import de.uni_passau.fim.se2.assertion_exctractor.data.DataPoint;
import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;

/**
 * The {@link CombinedAssertionPreprocessor} class is an extension of the {@link AssertionPreprocessor} that allows
 * combining multiple AssertionPreprocessors. It delegates setup, shutdown, and test case export operations to each
 * individual preprocessor in the list of combined processors.
 */
public class CombinedAssertionPreprocessor extends AssertionPreprocessor {

    private final List<? extends AssertionPreprocessor> combinedProcessors;

    public CombinedAssertionPreprocessor(
        String dataDir, String saveDir, int maxAssertions, List<? extends AssertionPreprocessor> combinedProcessors
    ) {
        super(dataDir, saveDir, maxAssertions);
        this.combinedProcessors = combinedProcessors;
    }

    @Override
    protected void setup() {
        combinedProcessors.forEach(AssertionPreprocessor::setup);
    }

    @Override
    protected void shutDown() {
        combinedProcessors.forEach(AssertionPreprocessor::shutDown);
    }

    @Override
    protected String getModelName() {
        return null;
    }

    @Override
    protected void exportTestCases(Pair<String, DataPoint> dataPoint) {
        combinedProcessors.forEach(x -> x.exportTestCases(dataPoint));
    }
}
