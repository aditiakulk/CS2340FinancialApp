package com.example.sprintproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import java.util.Date;
import java.util.Calendar;
import com.example.sprintproject.model.Budget;
import com.example.sprintproject.model.Expense;
import com.example.sprintproject.model.FinancialFactory;
import com.example.sprintproject.model.Frequency;

public class FinancialFactoryTest {

    private static final String DEFAULT = "Default";

    private Date createTestDate(int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, month - 1, day, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    @Test
    public void expenseAllArgs() {
        String name = "Lunch at Cafe";
        double amount = 67.00;
        Date date = createTestDate(10, 27);
        String category = "Food";
        String groupGoalId = "goal123";
        String circleCreatorUid = "userA456";

        Expense expense = FinancialFactory.createExpense(
                name, amount, date,
                category, groupGoalId, circleCreatorUid);

        assertNotNull(expense);
        assertEquals(name, expense.getName());
        assertEquals(amount, expense.getAmount(), 0.0001);
        assertEquals(date, expense.getDate());
        assertEquals(category, expense.getCategory());
        assertEquals(groupGoalId, expense.getGroupGoalId());
        assertEquals(circleCreatorUid, expense.getCircleCreatorUid());
    }

    @Test
    public void expenseNoArgs() {
        Expense expense = FinancialFactory.createExpense();
        assertNotNull(expense);
        assertEquals(DEFAULT, expense.getName());
        assertEquals(0.00, expense.getAmount(), 0.0001);
        assertEquals(DEFAULT, expense.getCategory());
        assertNull(expense.getGroupGoalId());
        assertNotNull(expense.getDate());
    }

    @Test
    public void budgetAllArgs() {
        // Arrange
        String title = "Monthly Groceries";
        double amount = 676.76;
        String category = "Groceries";
        Date date = createTestDate(11, 1);
        Frequency frequency = Frequency.MONTHLY;
        Budget budget = FinancialFactory.createBudget(
                title, amount, category, date, frequency);
        assertNotNull(budget);
        assertEquals(title, budget.getTitle());
        assertEquals(amount, budget.getAmount(), 0.0001);
        assertEquals(category, budget.getCategory());
        assertEquals(date, budget.getDate());
        assertEquals(frequency, budget.getFrequency());
    }

    @Test
    public void budgetNoArgs() {
        Budget budget = FinancialFactory.createBudget();
        assertNotNull(budget);
        assertEquals(DEFAULT, budget.getTitle());
        assertEquals(0.00, budget.getAmount(), 0.0001);
        assertEquals(DEFAULT, budget.getCategory());
        assertEquals(Frequency.WEEKLY, budget.getFrequency());
        assertNotNull(budget.getDate());
    }
}