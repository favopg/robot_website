package com.example.robotwebsite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KatagoAnalyzeRequest {

    @JsonProperty("sgf_path")
    private String sgfPath;

    @JsonProperty("sgf_content")
    private String sgfContent;

    @JsonProperty("turn_range")
    private String turnRange;

    @JsonProperty("max_visits")
    private Integer maxVisits;

    public KatagoAnalyzeRequest() {
    }

    public KatagoAnalyzeRequest(String sgfPath, String sgfContent, String turnRange, Integer maxVisits) {
        this.sgfPath = sgfPath;
        this.sgfContent = sgfContent;
        this.turnRange = turnRange;
        this.maxVisits = maxVisits;
    }

    public String getSgfPath() {
        return sgfPath;
    }

    public void setSgfPath(String sgfPath) {
        this.sgfPath = sgfPath;
    }

    public String getSgfContent() {
        return sgfContent;
    }

    public void setSgfContent(String sgfContent) {
        this.sgfContent = sgfContent;
    }

    public String getTurnRange() {
        return turnRange;
    }

    public void setTurnRange(String turnRange) {
        this.turnRange = turnRange;
    }

    public Integer getMaxVisits() {
        return maxVisits;
    }

    public void setMaxVisits(Integer maxVisits) {
        this.maxVisits = maxVisits;
    }
}
