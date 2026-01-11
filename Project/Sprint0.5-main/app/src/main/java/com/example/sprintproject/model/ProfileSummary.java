package com.example.sprintproject.model;

public class ProfileSummary {
    private String email;
    private int totalExpenses;
    private int totalBudgets;
    private int totalCircles;

    public ProfileSummary(String email, int totalExpenses, int totalBudgets, int totalCircles) {
        this.email = email;
        this.totalExpenses = totalExpenses;
        this.totalBudgets = totalBudgets;
        this.totalCircles = totalCircles;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(int totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public int getTotalBudgets() {
        return totalBudgets;
    }

    public void setTotalBudgets(int totalBudgets) {
        this.totalBudgets = totalBudgets;
    }

    public int getTotalCircles() {
        return totalCircles;
    }

    public void setTotalCircles(int totalCircles) {
        this.totalCircles = totalCircles;
    }
}