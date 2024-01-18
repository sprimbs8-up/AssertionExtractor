package de.uni_passau.fim.se2.assertion_exctractor.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The RawMethodData record represents raw data associated with a method. It encapsulates the test method, focal method,
 * test file, and focal file.
 *
 * @param testMethod  The content of the test method.
 * @param focalMethod The content of the focal method.
 * @param testFile    The content of the test file.
 * @param focalFile   The content of the focal file.
 */
public record RawMethodData(
    @JsonProperty("test_method") String testMethod, @JsonProperty("focal_method") String focalMethod,
    @JsonProperty("test_file") String testFile, @JsonProperty("focal_file") String focalFile
) {
}
