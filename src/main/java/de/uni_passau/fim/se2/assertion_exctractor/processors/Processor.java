package de.uni_passau.fim.se2.assertion_exctractor.processors;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_passau.fim.se2.assertion_exctractor.data.Method2TestLoader;
import de.uni_passau.fim.se2.assertion_exctractor.data.MethodData;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.*;
import de.uni_passau.fim.se2.assertion_exctractor.utils.ProgressBarContainer;
import de.uni_passau.fim.se2.assertion_exctractor.utils.StatisticsContainer;
import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;

public abstract class Processor {

    private static final Logger LOG = LoggerFactory.getLogger(Processor.class);
    protected final String dataDir;
    protected final String saveDir;
    protected final int maxAssertions;

    public Processor(final String dataDir, final String saveDir, final int maxAssertions) {
        this.dataDir = dataDir;
        this.saveDir = saveDir;
        this.maxAssertions = maxAssertions;
    }

    protected Stream<MethodData> loadMethodData() {
        try {
            return Method2TestLoader.loadDatasetAsJSON(dataDir)
                .flatMap(MethodData::fromPreparation)
                .peek(x -> ProgressBarContainer.getInstance().notifyStep());
        }
        catch (IOException e) {
            LOG.error("Error while loading json dataset", e);
            throw new RuntimeException(e);
        }

    }

    public void exportProcessedExamples() {
        List<MethodData> methodDataStream = loadMethodData()
            .filter(data -> data.testCase().getNumberAssertions() <= maxAssertions)
            .filter(data -> data.testCase().getNumberAssertions() >= 1)
            .peek(x -> StatisticsContainer.getInstance().notifyTestCase())
            .toList();
        zip(methodDataStream, createDataTypeList(methodDataStream.size()))
            .forEach(x -> exportTestCases(x.a(), x.b()));
        ProgressBarContainer.getInstance().notifyStop();
        int usedTestCases = StatisticsContainer.getInstance().getUsedTestCases();
        int totalTestCases = ProgressBarContainer.getInstance().getTotalCount();
        float percentage = (float) usedTestCases / totalTestCases * 100;
        LOG.info("Collected " + usedTestCases + "/" + totalTestCases + " complete test data instances.");
        LOG.info(String.format("This are %.2f", percentage) + "%.");
    }

    protected abstract void exportTestCases(MethodData x, DataType type);

    public static <A, B> Stream<Pair<A, B>> zip(List<A> as, List<B> bs) {
        return IntStream.range(0, Math.min(as.size(), bs.size()))
            .mapToObj(i -> new Pair<>(as.get(i), bs.get(i)));
    }

    private List<DataType> createDataTypeList(int length) {
        int[] splitting = { 80, 10, 10 };
        return IntStream.range(0, length).mapToObj(n -> {
            if (n < splitting[0] * length * 0.01) {
                return DataType.TRAINING;
            }
            else if (n < (splitting[0] + splitting[1]) * length * 0.01) {
                return DataType.VALIDATION;
            }
            return DataType.TESTING;
        }).toList();
    }

    protected enum DataType {

        TRAINING(new AtomicBoolean(false)), VALIDATION(new AtomicBoolean(false)), TESTING(new AtomicBoolean(false));

        final AtomicBoolean refresh;

        DataType(AtomicBoolean refresh) {
            this.refresh = refresh;
        }

        public AtomicBoolean getRefresh() {
            return refresh;
        }
    }
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
