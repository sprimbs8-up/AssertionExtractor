package de.uni_passau.fim.se2.assertion_exctractor.processors;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_passau.fim.se2.assertion_exctractor.data.DataPoint;
import de.uni_passau.fim.se2.assertion_exctractor.data.FineMethodData;
import de.uni_passau.fim.se2.assertion_exctractor.data.TestCase;
import de.uni_passau.fim.se2.assertion_exctractor.data.TestElement;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.code.CustomAstCodeParser;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.AstNode;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.declaration.MethodDeclaration;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.CommonPreprocessorOptions;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.ProcessingException;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.code2.Code2Preprocessor;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.code2.transform.AstPath;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.code2.transform.ToAstPathTransformer;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.code2.transform.path_transformers.Code2PathTransformer;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.code2.transform.path_transformers.Code2SeqPathTransformer;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.shared.MethodsExtractor;
import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;

public class Code2SeqProcessor extends AssertionPreprocessor {

    private static final Logger LOG = LoggerFactory.getLogger(Code2SeqProcessor.class);

    public Code2SeqProcessor(String dataDir, String saveDir, int maxAssertions) {
        super(dataDir, saveDir, maxAssertions);
    }

    @Override
    protected String getModelName() {
        return "code2seq";
    }

    @Override
    protected void exportTestCases(Pair<String, DataPoint> dataPointPair) {
        DataPoint dataPoint = dataPointPair.b();
        FineMethodData methodData = dataPoint.methodData();
        TestCase testCase = methodData.testCase();
        List<List<String>> assertions = testCase.testElements().stream()
            .filter(TestElement::isAssertion)
            .map(TestElement::tokens)
            .toList();
        Code2SeqPreprocessorAdapter p = new Code2SeqPreprocessorAdapter(
            SINGLE_METHOD_OPTIONS, true, 8, 12, 1, 1000, new Code2SeqPathTransformer()
        );

        for (int idx = 0; idx < assertions.size(); idx++) {
            Optional<String> result = p.processSingleMethod(testCase.replaceAssertion(idx, null), assertions.get(idx));
            if (result.isPresent()) {
                writeStringsToFile(
                    dataPoint.type().name().toLowerCase() + ".c2s", dataPoint.type().getRefresh(), result.get()
                );
                dataPoint.type().getRefresh().set(true);
            }
        }
    }

    private static class Code2SeqPreprocessorAdapter extends Code2Preprocessor {

        private final int maxPathWidth;
        private final int maxPathLength;
        private final int maxCodeLength;
        private final int minCodeLength;
        private final Code2PathTransformer transformer;

        public Code2SeqPreprocessorAdapter(
            CommonPreprocessorOptions commonOptions, boolean singleMethod, int maxPathWidth, int maxPathLength,
            int minCodeLength, int maxCodeLength, Code2PathTransformer transformer
        ) {
            super(commonOptions, singleMethod, maxPathWidth, maxPathLength, minCodeLength, maxCodeLength, transformer);
            this.maxPathWidth = maxPathWidth;
            this.maxPathLength = maxPathLength;
            this.maxCodeLength = maxCodeLength;
            this.minCodeLength = minCodeLength;
            this.transformer = transformer;
        }

        public Optional<String> processSingleMethod(String code, List<String> assertionToken) {
            MethodsExtractor methodExtractor = new MethodsExtractor(false);
            return this.processSingleElement(code, true)
                .flatMap(
                    (el) -> methodExtractor.process(el).stream().map(x -> methodDeclToAstPaths(x, assertionToken))
                        .flatMap(Optional::stream)
                ).findFirst();
        }

        @Override
        protected Stream<AstNode> processSingleElement(String code, boolean singleMethod) throws ProcessingException {
            CustomAstCodeParser codeParser = new CustomAstCodeParser();
            return codeParser.parseMethodSkipErrors(code).stream().filter(Objects::nonNull).map(AstNode.class::cast);
        }

        private Optional<String> methodDeclToAstPaths(MethodDeclaration node, List<String> assertionTokens) {
            ToAstPathTransformer extractor = new ToAstPathTransformer(
                this.maxPathWidth, this.maxPathLength, this.minCodeLength, this.maxCodeLength, this.transformer,
                !this.commonOptions.newLabels()
            );
            Code2Assertion method = new Code2Assertion(assertionTokens, extractor.process(node));
            return !method.assertionTokens.isEmpty() && !method.features().isEmpty() ? Optional.of(method.toString())
                : Optional.empty();
        }
    }

    public record Code2Assertion(List<String> assertionTokens, List<AstPath> features) {

        public String toString() {
            return assertionTokens.stream().map(y -> y.replaceAll(" ", "###")).collect(Collectors.joining("|")) + " "
                + features.stream().map(AstPath::toString).collect(Collectors.joining(" "));
        }
    }
}
