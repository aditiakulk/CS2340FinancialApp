package com.example.sprintproject.model;

public interface FinancialVisitor {
    void visit(Budget b);
    void visit(Expense e);
}
