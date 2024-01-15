package de.uni_passau.fim.se2.assertion_exctractor.processors;

import java.util.List;

import de.uni_passau.fim.se2.assertion_exctractor.data.DataPoint;
import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;

public class CombinedProcessor extends Processor {

    private final List<? extends Processor> combinedProcessors;

    public CombinedProcessor(
        String dataDir, String saveDir, int maxAssertions, List<? extends Processor> combinedProcessors
    ) {
        super(dataDir, saveDir, maxAssertions);
        this.combinedProcessors = combinedProcessors;
    }

    @Override
    protected void setup() {
        combinedProcessors.forEach(Processor::setup);
    }

    @Override
    protected void shutDown() {
        combinedProcessors.forEach(Processor::shutDown);
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
