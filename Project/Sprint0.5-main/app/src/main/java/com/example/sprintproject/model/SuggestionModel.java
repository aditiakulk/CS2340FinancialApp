package com.example.sprintproject.model;

public class SuggestionModel {
    private String label;
    private String prompt;

    public SuggestionModel() { }

    public SuggestionModel(String label, String prompt) {
        this.label = label;
        this.prompt = prompt;
    }

    public String getLabel() {
        return label;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
