package com.example.sprintproject.model;

/**
 * Represents a budget warning when spending approaches a threshold.
 */
public class BudgetWarning {
    private final String budgetTitle;
    private final String category;
    private final double budgetAmount;
    private final double totalSpent;
    private final double percentage;

    public BudgetWarning(String budgetTitle,
                         String category, double budgetAmount, double totalSpent) {
        this.budgetTitle = budgetTitle;
        this.category = category;
        this.budgetAmount = budgetAmount;
        this.totalSpent = totalSpent;
        this.percentage = budgetAmount > 0 ? (totalSpent / budgetAmount) * 100 : 0;
    }

    public String getBudgetTitle() {
        return budgetTitle;
    }

    public String getCategory() {
        return category;
    }

    public double getBudgetAmount() {
        return budgetAmount;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public double getPercentage() {
        return percentage;
    }

    public double getRemaining() {
        return Math.max(0, budgetAmount - totalSpent);
    }
}

