package com.example.robotwebsite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KatagoAnalyzeRequest {

    @JsonProperty("date")
    private String date;

    public KatagoAnalyzeRequest() {
    }

    public KatagoAnalyzeRequest(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
