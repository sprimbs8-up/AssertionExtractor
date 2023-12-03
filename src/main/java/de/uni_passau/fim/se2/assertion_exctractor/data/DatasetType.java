package de.uni_passau.fim.se2.assertion_exctractor.data;

import java.util.concurrent.atomic.AtomicBoolean;

public enum DatasetType {

    TRAINING(new AtomicBoolean(false)), VALIDATION(new AtomicBoolean(false)), TESTING(new AtomicBoolean(false));

    final AtomicBoolean refresh;

    DatasetType(AtomicBoolean refresh) {
        this.refresh = refresh;
    }

    public AtomicBoolean getRefresh() {
        return refresh;
    }
}
