package com.example.sprintproject.model;

public class AllCategories {
    private String category;
    private double totalSpent;
    private double budget;
    private double remaining;

    public AllCategories() {
        category = "";
        totalSpent = 0.0;
        budget = 0.0;
        remaining = 0.0;
    }

    public AllCategories(String category, double totalSpent, double budget) {
        this.category = category;
        this.totalSpent = totalSpent;
        this.budget = budget;
        this.remaining = (budget - totalSpent < 0) ? 0 : budget - totalSpent;
    }

    public String getCategory() {
        return category;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public double getBudget() {
        return budget;
    }

    public double getRemaining() {
        return remaining;
    }
}
