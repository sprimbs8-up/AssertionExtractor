package de.uni_passau.fim.se2.assertion_exctractor.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public record PreparedMethodData(
        @JsonProperty("test_method") String testMethod, @JsonProperty("focal_method") String focalMethod,
        @JsonProperty("test_file") String testFile, @JsonProperty("focal_file") String focalFile
) {
}
