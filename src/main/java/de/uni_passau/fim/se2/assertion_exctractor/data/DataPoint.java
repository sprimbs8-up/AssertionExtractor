package de.uni_passau.fim.se2.assertion_exctractor.data;

/**
 * The DataPoint record represents a data point resulting from the preprocessing steps. It encapsulates fine-grained
 * method data of type {@link FineMethodData} and the corresponding dataset type of type {@link DatasetType}.
 *
 * @param methodData The fine-grained method data associated with the data point.
 * @param type       The dataset type indicating whether the data point belongs to the {@link DatasetType#TRAINING},
 *                   {@link DatasetType#VALIDATION}, or {@link DatasetType#TESTING} set.
 */
public record DataPoint(FineMethodData methodData, DatasetType type) {
}
