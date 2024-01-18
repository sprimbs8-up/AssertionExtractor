package de.uni_passau.fim.se2.assertion_exctractor.data;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The {@link DatasetType} enum represents the different types of datasets that a data point can belong to, namely
 * Training, Validation, and Testing. Each dataset type is associated with an {@link AtomicBoolean} flag indicating
 * whether a refresh is required for the respective dataset.
 */
public enum DatasetType {

    /**
     * Represents the Training dataset type.
     */
    TRAINING(new AtomicBoolean(false)),
    /**
     * Represents the Validation dataset type.
     */
    VALIDATION(new AtomicBoolean(false)),
    /**
     * Represents the testing dataset type.
     */
    TESTING(new AtomicBoolean(false));

    private final AtomicBoolean refresh;

    /**
     * Constructs a DatasetType with the specified refresh flag for exporting data types.
     *
     * @param refresh The AtomicBoolean flag indicating whether a refresh is required for the dataset when exporting.
     */
    DatasetType(AtomicBoolean refresh) {
        this.refresh = refresh;
    }

    /**
     * Getter for the current refresh parameter.
     *
     * @return The refresh parameter.
     */
    public AtomicBoolean getRefresh() {
        return refresh;
    }
}
