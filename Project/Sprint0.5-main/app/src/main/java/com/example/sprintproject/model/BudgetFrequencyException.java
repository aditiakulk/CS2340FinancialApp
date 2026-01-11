package com.example.sprintproject.model;

public class BudgetFrequencyException extends RuntimeException {
    public BudgetFrequencyException(String message) {
        super(message);
    }

    public BudgetFrequencyException(String message, Throwable cause) {
        super(message, cause);
    }
}