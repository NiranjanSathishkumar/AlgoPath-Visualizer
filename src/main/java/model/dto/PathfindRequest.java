package model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PathfindRequest {
    @JsonProperty("algorithm")
    private String algorithm;

    public PathfindRequest() {
    }

    public PathfindRequest(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
}
