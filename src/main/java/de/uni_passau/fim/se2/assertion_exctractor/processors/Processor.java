package de.uni_passau.fim.se2.assertion_exctractor.processors;

import de.uni_passau.fim.se2.assertion_exctractor.data.Method2TestLoader;
import de.uni_passau.fim.se2.assertion_exctractor.data.MethodData;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestCaseParser;
import de.uni_passau.fim.se2.assertion_exctractor.utils.ProgressBarContainer;
import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;
import me.tongfei.progressbar.ProgressBar;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

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

    protected Stream<MethodData> loadMethodData(){
        try {
            return Method2TestLoader.loadDatasetAsJSON(dataDir)
                    .flatMap(MethodData::fromPreparation)
                    .peek(x-> ProgressBarContainer.getInstance().notifyStep());
        } catch (IOException e) {
            LOG.error("Error while loading json dataset",e);
            throw new RuntimeException(e);
        }

    }

    public void exportProcessedExamples(){
        ProgressBarContainer.getInstance().notifyStop();
    }
}
