package com.example.sprintproject.model;

import java.util.Date;

public class FinancialFactory {

    private FinancialFactory() {

    }
    public static Expense createExpense(String name,
        double amount, Date date,
        String category, String groupGoalId, String circleCreatorUid) {
        return new Expense(name, amount, date, category, groupGoalId, circleCreatorUid);
    }

    public static Expense createExpense() {
        return new Expense();
    }

    public static Budget createBudget(String title, double amount,
        String category, Date date, Frequency frequency) {
        return new Budget(title, amount, category, date, frequency);
    }

    public static Budget createBudget() {
        return new Budget();
    }
}
