package com.example.sprintproject;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import com.example.sprintproject.model.Budget;
import com.example.sprintproject.model.BudgetStatus;
import com.example.sprintproject.model.FinancialFactory;
import com.example.sprintproject.model.Frequency;
import com.example.sprintproject.viewmodel.BudgetViewModel;
import com.example.sprintproject.model.DateModel;

import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.Calendar;
import java.util.TimeZone;


public class BudgetColorIndicatorTest {
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private BudgetViewModel budgetVM = new BudgetViewModel(); // whatever yours is

    @Before
    public void setup() {
        sdf.setLenient(false);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Test
    public void testWeeklyBudgetUnderLimit() throws Exception {
        Date start = sdf.parse("2025-10-19");
        Budget budget = FinancialFactory.createBudget("Groceries", 100, "Food", start, Frequency.WEEKLY);

        double totalSpent = 50; // 0.50 -> < 0.60
        BudgetStatus status = budgetVM.getBudgetStatus(budget, totalSpent);
        assertEquals(BudgetStatus.COMPLETE, status);
    }

    @Test
    public void testWeeklyBudgetOverLimit() throws Exception {
        Date start = sdf.parse("2025-10-19");
        Budget budget = FinancialFactory.createBudget("Groceries", 100, "Food", start, Frequency.WEEKLY);

        double totalSpent = 120; // 1.20 -> >= 1.0
        BudgetStatus status = budgetVM.getBudgetStatus(budget, totalSpent);
        assertEquals(BudgetStatus.INCOMPLETE, status);
    }

    @Test
    public void testMonthlyBudgetComplete() throws Exception {
        Date start = sdf.parse("2025-09-01");
        Budget budget = FinancialFactory.createBudget("Rent", 1000, "Housing", start, Frequency.MONTHLY);

        double totalSpent = 900;
        BudgetStatus status = budgetVM.getBudgetStatus(budget, totalSpent);
        assertEquals(BudgetStatus.IN_PROGRESS, status); // was COMPLETE
    }

    @Test
    public void testMonthlyBudgetIncomplete() throws Exception {
        Date start = sdf.parse("2025-09-01");
        Budget budget = FinancialFactory.createBudget("Rent", 1000, "Housing", start, Frequency.MONTHLY);

        double totalSpent = 1200;
        BudgetStatus status = budgetVM.getBudgetStatus(budget, totalSpent);
        assertEquals(BudgetStatus.INCOMPLETE, status);
    }

    @Test
    public void testBudgetNotStarted() throws Exception {
        Date start = sdf.parse("2100-01-01");
        Budget budget = FinancialFactory.createBudget("Future Budget", 100, "Misc", start, Frequency.WEEKLY);

        double totalSpent = 0;
        BudgetStatus status = budgetVM.getBudgetStatus(budget, totalSpent);
        assertEquals(BudgetStatus.COMPLETE, status);
    }
}