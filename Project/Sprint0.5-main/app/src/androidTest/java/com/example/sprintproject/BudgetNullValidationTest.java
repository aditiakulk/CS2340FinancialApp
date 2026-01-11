package com.example.sprintproject;

import static org.junit.Assert.assertNull;

import com.example.sprintproject.model.Budget;
import com.example.sprintproject.model.FinancialFactory;
import com.example.sprintproject.model.Frequency;

import org.junit.Test;

import java.util.Date;

public class BudgetNullValidationTest {
    @Test
    public void budgetCreation() {
        Budget budget = FinancialFactory.createBudget("Test Title", 100.0, "FOOD", new Date(), Frequency.MONTHLY);

        budget.setTitle(null);
        budget.setCategory(null);
        budget.setDate(null);
        budget.setFrequency(null);

        assertNull("Title should be null after setting it to null.", budget.getTitle());
        assertNull("Category should be null after setting it to null.", budget.getCategory());
        assertNull("Date should be null after setting it to null.", budget.getDate());
        assertNull("Frequency should be null after setting it to null.", budget.getFrequency());
    }
}
