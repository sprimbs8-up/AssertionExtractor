package de.uni_passau.fim.se2.assertion_exctractor.processors;

import de.uni_passau.fim.se2.assertion_exctractor.data.MethodData;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.Assertion;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestCase;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestElement;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TryCatchAssertion;
import de.uni_passau.fim.se2.assertion_exctractor.utils.RandomUtil;
import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class AtlasProcessor extends Processor {



    public AtlasProcessor(String dataDir, String saveDir, int maxAssertions) {
        super(dataDir, saveDir, maxAssertions);
    }

    @Override
    public void exportProcessedExamples() {

        List<MethodData> methodDataStream = super.loadMethodData()
                .filter(data -> data.testCase().getNumberAssertions() <= maxAssertions)
                .filter(data -> data.testCase().getNumberAssertions() >= 1).toList();
        zip(methodDataStream, createDataTypeList(methodDataStream.size())).forEach(x -> exportTestCases(x.a().testCase(), x.b()));
    }
    public static <A, B> Stream<Pair<A, B>> zip(List<A> as, List<B> bs) {
        return IntStream.range(0, Math.min(as.size(), bs.size()))
                .mapToObj(i -> new Pair<>(as.get(i), bs.get(i)));
    }

    private List<DataType> createDataTypeList(int length){
        int[] splitting = {80,10,10};
        return IntStream.range(0,length).mapToObj(n -> {
            if (n<splitting[0]*length * 0.01) {
                return DataType.TRAINING;
            } else if ( n<(splitting[0]+splitting[1]) * length * 0.01){
                return DataType.VALIDATION;
            }
            return DataType.TESTING;
        }).toList();
    }

    private  enum DataType {
        TRAINING(new AtomicBoolean(false)), VALIDATION(new AtomicBoolean(false)), TESTING(new AtomicBoolean(false));

        private final AtomicBoolean refresh;

        DataType(AtomicBoolean refresh){
            this.refresh = refresh;
        }
    }

    private void exportTestCases(TestCase x, DataType type) {
        List<List<String>> tokens = x.testElements().stream()
                .filter(v -> v instanceof Assertion || v instanceof TryCatchAssertion)
                .map(TestElement::tokens).toList();
        for (int i = 0; i < tokens.size(); i++) {
            writeStringsToFile(type.name().toLowerCase()+"/assertLines.txt", type.refresh, String.join(" ", tokens.get(i)));
            writeStringsToFile(type.name().toLowerCase()+"/testMethods.txt", type.refresh, x.replaceAssertion(i));
            type.refresh.set(true);
        }
    }

    private void writeStringsToFile(String file, AtomicBoolean append, String tokens) {
        File savePath = Path.of(saveDir, file).toFile();
        if(!savePath.exists()){

                savePath.getParentFile().mkdirs();

        };
        try (FileWriter writer = new FileWriter(saveDir + "/" + file, append.get())) {
            writer.write(tokens + System.getProperty("line.separator"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
