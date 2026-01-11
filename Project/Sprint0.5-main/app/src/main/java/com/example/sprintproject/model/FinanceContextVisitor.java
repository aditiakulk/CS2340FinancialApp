package com.example.sprintproject.model;

import java.util.Locale;

public class FinanceContextVisitor implements FinancialVisitor {

    private final StringBuilder sb = new StringBuilder();
    private boolean budgetsStarted = false;
    private boolean expensesStarted = false;
    private boolean anyBudgets = false;
    private boolean anyExpenses = false;

    public FinanceContextVisitor() {
        // Header will be added lazily
    }

    private void ensureHeader() {
        if (sb.length() == 0) {
            sb.append("Here is the user's financial situation:\n\n");
        }
    }

    @Override
    public void visit(Budget b) {
        ensureHeader();
        if (!budgetsStarted) {
            sb.append("Budgets:\n");
            budgetsStarted = true;
        }
        anyBudgets = true;
        sb.append("- ")
                .append(b.getCategory())
                .append(": $").append(String.format(Locale.US, "%.2f", b.getAmount()))
                .append(" (").append(b.getFrequency()).append(")\n");
    }

    @Override
    public void visit(Expense e) {
        ensureHeader();
        if (!expensesStarted) {
            sb.append("\nRecent Expenses:\n");
            expensesStarted = true;
        }
        anyExpenses = true;
        sb.append("- $").append(String.format(Locale.US, "%.2f", e.getAmount()))
                .append(" on ").append(e.getCategory())
                .append(" (").append(e.getName()).append(")\n");
    }

    public String buildResult() {
        ensureHeader();
        if (!anyBudgets) {
            sb.append("Budgets:\n- No budgets created.\n");
        }
        if (!anyExpenses) {
            sb.append("\nRecent Expenses:\n- No expenses recorded.\n");
        }
        return sb.toString();
    }
}