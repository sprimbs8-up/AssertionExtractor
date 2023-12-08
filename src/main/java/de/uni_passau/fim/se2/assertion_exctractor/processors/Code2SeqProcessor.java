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
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.code2.transform.Code2Method;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.code2.transform.ToAstPathTransformer;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.code2.transform.path_transformers.Code2PathTransformer;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.code2.transform.path_transformers.Code2SeqPathTransformer;
import de.uni_passau.fim.se2.deepcode.toolbox.preprocessor.shared.MethodsExtractor;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Code2SeqProcessor extends Processor{
    private static final CommonPreprocessorOptions SINGLE_METHOD_OPTIONS = new CommonPreprocessorOptions(
            null, null, false, new TransformMode.None()
    );

    public Code2SeqProcessor(String dataDir, String saveDir, int maxAssertions) {
        super(dataDir, saveDir, maxAssertions);
    }

    @Override
    protected void exportTestCases(DataPoint dataPoint) {
        FineMethodData methodData = dataPoint.methodData();
        TestCase testCase = methodData.testCase();
        List<List<String>> assertions = testCase.testElements().stream()
                .filter(((Predicate<TestElement>) Assertion.class::isInstance).or(TryCatchAssertion.class::isInstance))
                .map(TestElement::tokens)
                .toList();
        Code2SeqPreprocessorAdapter p = new Code2SeqPreprocessorAdapter(SINGLE_METHOD_OPTIONS, true, 8, 12, 1, 1000, new Code2SeqPathTransformer());
        for(int idx = 0; idx < assertions.size(); idx++) {
            p.processSingleMethod(testCase.replaceAssertion(idx, null), assertions.get(idx)).ifPresent(x -> System.out.println(String.join("", x)));
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
                methodExtractor.process(el).stream().map(x->methodDeclToAstPaths(x, String.join("|",assertionToken))).flatMap(Optional::stream)
            ).findFirst();
        }

        private Optional<String> methodDeclToAstPaths(MethodDeclaration node, String newLabel) {
            ToAstPathTransformer extractor = new ToAstPathTransformer(this.maxPathWidth, this.maxPathLength, this.minCodeLength, this.maxCodeLength, this.transformer, !this.commonOptions.newLabels());
            Code2Method method = new Code2Method(newLabel, extractor.process(node));
            return !"".equals(method.methodName()) && !method.features().isEmpty() ? Optional.of(method.toString()) : Optional.empty();
        }
    }
}
