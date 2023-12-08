package de.uni_passau.fim.se2.assertion_exctractor.processors;

import de.uni_passau.fim.se2.assertion_exctractor.data.DataPoint;
import de.uni_passau.fim.se2.assertion_exctractor.data.FineMethodData;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.Assertion;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestCase;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TestElement;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.TryCatchAssertion;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.declaration.MethodDeclaration;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.transformer.util.TransformMode;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.CommonPreprocessorOptions;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.code2.Code2Preprocessor;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.code2.transform.AstPath;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.code2.transform.Code2Method;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.code2.transform.ToAstPathTransformer;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.code2.transform.path_transformers.Code2PathTransformer;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.code2.transform.path_transformers.Code2SeqPathTransformer;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.shared.MethodsExtractor;
import de.uni_passau.fim.se2.deepcode.toolbox.util.Tokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Code2SeqProcessor extends Processor {
    private static final Logger LOG = LoggerFactory.getLogger(Code2SeqProcessor.class);

    private static final CommonPreprocessorOptions SINGLE_METHOD_OPTIONS = new CommonPreprocessorOptions(
            null, null, false, new TransformMode.None()
    );

    public Code2SeqProcessor(String dataDir, String saveDir, int maxAssertions) {
        super(dataDir, saveDir, maxAssertions);
    }

    @Override
    protected String getModelName() {
        return "code2seq";
    }

    @Override
    protected void exportTestCases(DataPoint dataPoint) {
        FineMethodData methodData = dataPoint.methodData();
        TestCase testCase = methodData.testCase();
        List<List<String>> assertions = testCase.testElements().stream()
                .filter(TestElement::isAssertion)
                .map(TestElement::tokens)
                .toList();
        Code2SeqPreprocessorAdapter p = new Code2SeqPreprocessorAdapter(SINGLE_METHOD_OPTIONS, true, 8, 12, 1, 1000, new Code2SeqPathTransformer());

        for (int idx = 0; idx < assertions.size(); idx++) {
            Optional<String> result = p.processSingleMethod(testCase.replaceAssertion(idx, null), assertions.get(idx));
            if (result.isPresent()) {
                writeStringsToFile(
                        dataPoint.type().name().toLowerCase()+".c2s",dataPoint. type().getRefresh(),result.get()
                );
            } else{
                LOG.warn("");
            }
        }
    }

    private static class Code2SeqPreprocessorAdapter extends Code2Preprocessor {
        private final int maxPathWidth;
        private final int maxPathLength;
        private final int maxCodeLength;
        private final int minCodeLength;
        private final Code2PathTransformer transformer;

        public Code2SeqPreprocessorAdapter(CommonPreprocessorOptions commonOptions, boolean singleMethod, int maxPathWidth, int maxPathLength, int minCodeLength, int maxCodeLength, Code2PathTransformer transformer) {
            super(commonOptions, singleMethod, maxPathWidth, maxPathLength, minCodeLength, maxCodeLength, transformer);
            this.maxPathWidth = maxPathWidth;
            this.maxPathLength = maxPathLength;
            this.maxCodeLength = maxCodeLength;
            this.minCodeLength = minCodeLength;
            this.transformer = transformer;
        }

        public Optional<String> processSingleMethod(String code, List<String> assertionToken) {
            MethodsExtractor methodExtractor = new MethodsExtractor(false);
            return this.processSingleElement(code, true).flatMap((el) ->
                    methodExtractor.process(el).stream().map(x -> methodDeclToAstPaths(x, assertionToken)).flatMap(Optional::stream)
            ).findFirst();
        }

        private Optional<String> methodDeclToAstPaths(MethodDeclaration node, List<String> assertionTokens) {
            ToAstPathTransformer extractor = new ToAstPathTransformer(this.maxPathWidth, this.maxPathLength, this.minCodeLength, this.maxCodeLength, this.transformer, !this.commonOptions.newLabels());
            Code2Assertion method = new Code2Assertion(assertionTokens, extractor.process(node));
            return !method.assertionTokens.isEmpty() && !method.features().isEmpty() ? Optional.of(method.toString()) : Optional.empty();
        }
    }

    public record Code2Assertion(List<String> assertionTokens, List<AstPath> features) {
        public String toString() {
            return String.join("|", assertionTokens) + " " + features.stream().map(AstPath::toString).collect(Collectors.joining(" "));
        }
    }
}
