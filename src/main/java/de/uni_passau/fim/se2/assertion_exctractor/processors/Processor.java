package de.uni_passau.fim.se2.assertion_exctractor.processors;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_passau.fim.se2.assertion_exctractor.converters.MoveToDatapointPStep;
import de.uni_passau.fim.se2.assertion_exctractor.converters.Raw2FineDataPStep;
import de.uni_passau.fim.se2.assertion_exctractor.data.DataPoint;
import de.uni_passau.fim.se2.assertion_exctractor.data.FineMethodData;
import de.uni_passau.fim.se2.assertion_exctractor.data.Method2TestLoader;
import de.uni_passau.fim.se2.assertion_exctractor.utils.ProgressBarContainer;
import de.uni_passau.fim.se2.assertion_exctractor.utils.StatisticsContainer;

public abstract class Processor {

    private static final Logger LOG = LoggerFactory.getLogger(Processor.class);
    private final Raw2FineDataPStep raw2fineConverter = new Raw2FineDataPStep();
    private final MoveToDatapointPStep orderDataset = new MoveToDatapointPStep(80, 10, 10);
    protected final String dataDir;
    protected final String saveDir;
    protected final int maxAssertions;

    public Processor(final String dataDir, final String saveDir, final int maxAssertions) {
        this.dataDir = dataDir;
        this.saveDir = saveDir;
        this.maxAssertions = maxAssertions;
    }

    protected Stream<FineMethodData> loadMethodData() {
        try {
            return Method2TestLoader.loadDatasetAsJSON(dataDir)
                .map(raw2fineConverter::process)
                .flatMap(Optional::stream)
                .peek(el -> ProgressBarContainer.getInstance().notifyStep());
        }
        catch (IOException e) {
            LOG.error("Error while loading json dataset", e);
            throw new RuntimeException(e);
        }

    }

    public void exportProcessedExamples() {
        setup();
        loadMethodData()
            .filter(data -> data.testCase().getNumberAssertions() <= maxAssertions)
            .filter(data -> data.testCase().getNumberAssertions() >= 0)
            .peek(x -> StatisticsContainer.getInstance().notifyTestCase())
            .map(orderDataset::process)
            .forEach(this::exportTestCases);
        ProgressBarContainer.getInstance().notifyStop();
        int usedTestCases = StatisticsContainer.getInstance().getUsedTestCases();
        int totalTestCases = ProgressBarContainer.getInstance().getTotalCount();
        float percentage = (float) usedTestCases / totalTestCases * 100;
        LOG.info("Collected " + usedTestCases + "/" + totalTestCases + " complete test data instances.");
        LOG.info(String.format("This are %.2f", percentage) + "%.");
        shutDown();
    }

    protected void setup() {
        Path savePath = Path.of(saveDir);
        if (!savePath.toFile().exists()) {
            boolean successfullyCreated = savePath.toFile().mkdirs();
            if (successfullyCreated) {
                LOG.info(String.format("Created %s path successfully.", savePath));
            }
            else {
                LOG.warn(String.format("Creating %s path did not work successfully.", savePath));

            }
        }
    }

    protected void shutDown() {
    }

    protected abstract void exportTestCases(DataPoint dataPoint);

    protected void writeStringsToFile(String file, AtomicBoolean append, String tokens) {
        File savePath = Path.of(saveDir, file).toFile();
        if (!savePath.exists()) {

            savePath.getParentFile().mkdirs();

        }
        ;
        try (FileWriter writer = new FileWriter(saveDir + "/" + file, append.get())) {
            writer.write(tokens + System.getProperty("line.separator"));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
