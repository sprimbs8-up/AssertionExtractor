package de.uni_passau.fim.se2.assertion_exctractor.converters;

import de.uni_passau.fim.se2.assertion_exctractor.data.DataPoint;
import de.uni_passau.fim.se2.assertion_exctractor.data.DatasetType;
import de.uni_passau.fim.se2.assertion_exctractor.data.FineMethodData;
import de.uni_passau.fim.se2.assertion_exctractor.utils.RandomUtil;
import de.uni_passau.fim.se2.deepcode.toolbox.util.functional.Pair;

/**
 * The TrainSplitPreprocessingStep class is an implementation of the {@link DataProcessingStep} interface. It performs
 * data preprocessing by assigning data points to different datasets (Training, Validation, Testing) based on the
 * specified split percentages.
 */
public class TrainSplitPreprocessingStep implements DataProcessingStep<FineMethodData, DataPoint> {

    private final int trainSplit;
    private final int valSplit;
    private final int testSplit;

    /**
     * Constructs a TrainSplitPreprocessingStep with the specified split percentages. The sum of trainSplit, valSplit,
     * and testSplit must be 100.
     *
     * @param trainSplit The percentage of data assigned to the training set.
     * @param valSplit   The percentage of data assigned to the validation set.
     * @param testSplit  The percentage of data assigned to the testing set.
     * @throws IllegalArgumentException If the sum of split percentages is not equal to 100.
     */
    public TrainSplitPreprocessingStep(int trainSplit, int valSplit, int testSplit) {
        if (trainSplit + valSplit + testSplit != 100) {
            throw new IllegalArgumentException("The splitting must sum up to 100");
        }
        this.trainSplit = trainSplit;
        this.valSplit = valSplit;
        this.testSplit = testSplit;
    }

    /**
     * Processes the input {@link FineMethodData} by assigning it to a DataPoint with a specified {@link DatasetType}
     * based on the split percentages provided during instantiation.
     *
     * @param methodData A {@link Pair} containing a String identifier and the input {@link FineMethodData}.
     * @return A {@link Pair} containing a String identifier and the processed {@link DataPoint}.
     */
    @Override
    public Pair<String, DataPoint> process(Pair<String, FineMethodData> methodData) {
        return methodData.mapB(b -> new DataPoint(b, getNext()));
    }

    /**
     * Generates the next DatasetType (Training, Validation, or Testing) based on random assignments..
     *
     * @return The next DatasetType for assignment.
     */
    private DatasetType getNext() {
        int counter = RandomUtil.getInstance().getRandom().nextInt(trainSplit + valSplit + testSplit);
        if (counter < trainSplit) {
            return DatasetType.TRAINING;
        }
        if (counter < trainSplit + valSplit) {
            return DatasetType.VALIDATION;
        }
        return DatasetType.TESTING;
    }
}
