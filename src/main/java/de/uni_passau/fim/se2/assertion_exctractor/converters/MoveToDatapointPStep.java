package de.uni_passau.fim.se2.assertion_exctractor.converters;

import de.uni_passau.fim.se2.assertion_exctractor.data.DataPoint;
import de.uni_passau.fim.se2.assertion_exctractor.data.DatasetType;
import de.uni_passau.fim.se2.assertion_exctractor.data.FineMethodData;
import de.uni_passau.fim.se2.deepcode.toolbox.util.Randomness;

public class MoveToDatapointPStep implements DataProcessingStep<FineMethodData, DataPoint> {

    private final int trainSplit;
    private final int valSplit;
    private final int testSplit;

    public MoveToDatapointPStep(int trainSplit, int valSplit, int testSplit){
        if(trainSplit + valSplit + testSplit != 100){
            throw new IllegalArgumentException("The splitting must sum up to 100");
        }
        this.trainSplit = trainSplit;
        this.valSplit = valSplit;
        this.testSplit = testSplit;
    }
    @Override
    public DataPoint process(FineMethodData methodData) {
        return new DataPoint(methodData, getNext());
    }

    private DatasetType getNext(){
        int counter = Randomness.nextInt(100);
        if(counter < trainSplit){
            return DatasetType.TRAINING;
        }
        if(counter < trainSplit + valSplit){
            return DatasetType.VALIDATION;
        }
        return DatasetType.TESTING;
    }
}
