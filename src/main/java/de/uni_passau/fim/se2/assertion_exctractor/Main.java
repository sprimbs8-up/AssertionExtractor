package de.uni_passau.fim.se2.assertion_exctractor;




import de.uni_passau.fim.se2.assertion_exctractor.parsing.Assertion;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestCaseParser;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TryCatchAssertion;
import me.tongfei.progressbar.ProgressBar;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        List<Path> files = listFiles(Path.of("/root/master/methods2test-crawler/dataset/eval/"));
        ProgressBar pb = new ProgressBar("Parsing dataset.",files.size());
        pb.start();
        AtomicBoolean append = new AtomicBoolean(false);
        files.stream().map(file ->{
            JSONObject object = null;
            try {
                object = (JSONObject) parser.parse(new FileReader(file.toFile()));
            } catch (IOException | ParseException e) {
                throw new RuntimeException(e);
            }
            String s = (String) ((JSONObject) object.get("test_case")).get("body");
            TestCaseParser testCaseParser = new TestCaseParser();
            pb.step();
            return testCaseParser.parseTestCase(s);
        }).filter(tc -> tc.getNumberAssertions() == 1).forEachOrdered(x->{
            List<String> tokens = x.testElements().stream().filter(v -> v instanceof Assertion || v instanceof TryCatchAssertion).reduce((f,s)->f).map(v->v.tokens()).orElse(Collections.emptyList());
            try (FileWriter writer = new FileWriter("assertLines.txt", append.get())){
                writer.write(String.join(" ", tokens)+ System.getProperty("line.separator"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try (FileWriter writer = new FileWriter("testMethods.txt", append.get())){
                writer.write(x.replaceAssertions()+ System.getProperty("line.separator"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            append.set(true);
        });
        pb.stop();

    }

    public static List<Path> listFiles(Path path) throws IOException {

        List<Path> result;
        try (Stream<Path> walk = Files.walk(path)) {
            result = walk.filter(Files::isRegularFile).toList();
        }
        return result;

    }
}
