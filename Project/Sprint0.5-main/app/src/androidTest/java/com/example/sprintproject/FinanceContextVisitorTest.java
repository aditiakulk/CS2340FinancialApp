package com.example.sprintproject;


import org.junit.Test;
import java.util.Date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.sprintproject.model.Budget;
import com.example.sprintproject.model.Expense;
import com.example.sprintproject.model.FinanceContextVisitor;
import com.example.sprintproject.model.Frequency;

public class FinanceContextVisitorTest {

    private Budget createTestBudget(String category, double amount, Frequency frequency) {
        return new Budget(category + " Budget", amount, category, new Date(), frequency);
    }

    private Expense createTestExpense(String name, double amount, String category) {
        return new Expense(name, amount, new Date(), category, "groupid", "userid");
    }

    private static final String SUBSCRIPTIONS = "Subscriptions";
    private static final String TRANSPORTATION = "Transportation";
    private static final String CHIPOTLE = "Chipotle";
    private static final String SHELL = "Shell";
    private static final String TICKET = "Movie Tickets";
    private static final String UTILITIES = "Utilities";
    private static final String AMAZON = "Amazon";
    private static final String GYM = "Gym";

    @Test
    public void testEmptyVisitorBuildResult() {
        FinanceContextVisitor visitor = new FinanceContextVisitor();
        String result = visitor.buildResult();
        String expected = "Here is the user's financial situation:\n\n" +
                "Budgets:\n" +
                "- No budgets created.\n" +
                "\nRecent Expenses:\n" +
                "- No expenses recorded.\n";

        assertEquals(expected, result);
    }

    @Test
    public void testVisitOnlyBudget() {
        FinanceContextVisitor visitor = new FinanceContextVisitor();

        Budget rent = createTestBudget("Housing", 1250.00, Frequency.MONTHLY);
        visitor.visit(rent);

        String result = visitor.buildResult();

        String expected = "Here is the user's financial situation:\n\n" +
                "Budgets:\n" +
                "- Housing: $1250.00 (MONTHLY)\n" +
                "\nRecent Expenses:\n" +
                "- No expenses recorded.\n";

        assertEquals(expected, result);
    }

    @Test
    public void testVisitOnlyExpense() {
        FinanceContextVisitor visitor = new FinanceContextVisitor();

        Expense coffee = createTestExpense("Starbucks", 5.95, "Coffee");
        visitor.visit(coffee);

        String result = visitor.buildResult();

        assertTrue(result.startsWith("Here is the user's financial situation"));

        assertTrue(result.contains("Budgets:"));

        assertTrue(result.contains("No budgets"));

        assertTrue(result.contains("Recent Expenses:"));

        assertTrue(result.contains("$5.95"));
        assertTrue(result.contains("Coffee"));
        assertTrue(result.contains("Starbucks"));
    }

    @Test
    public void testVisitMultipleBudgetsAndExpenses() {
        FinanceContextVisitor visitor = new FinanceContextVisitor();

        visitor.visit(createTestBudget(SUBSCRIPTIONS, 29.99, Frequency.MONTHLY));
        visitor.visit(createTestBudget(TRANSPORTATION, 300.50, Frequency.WEEKLY));
        visitor.visit(createTestExpense(CHIPOTLE, 12.00, "Food"));
        visitor.visit(createTestExpense(SHELL, 45.75, "Gas"));

        String result = visitor.buildResult();

        // Instead of strict equality, verify key content and ordering.
        assertTrue(result.startsWith("Here is the user's financial situation"));
        assertTrue(result.contains(SUBSCRIPTIONS));
        assertTrue(result.contains(TRANSPORTATION));
        assertTrue(result.contains(CHIPOTLE));
        assertTrue(result.contains(SHELL));

        // Ensure ordering matches visit sequence
        assertTrue(result.indexOf(SUBSCRIPTIONS) < result.indexOf(TRANSPORTATION));
        assertTrue(result.indexOf(TRANSPORTATION) < result.indexOf(CHIPOTLE));
        assertTrue(result.indexOf(CHIPOTLE) < result.indexOf(SHELL));
    }

    @Test
    public void testVisitMixedOrder() {
        FinanceContextVisitor visitor = new FinanceContextVisitor();

        visitor.visit(createTestExpense(TICKET, 22.00, "Entertainment"));
        visitor.visit(createTestBudget(UTILITIES, 80.00, Frequency.MONTHLY));
        visitor.visit(createTestExpense(AMAZON, 50.00, "Shopping"));
        visitor.visit(createTestBudget(GYM, 50.00, Frequency.MONTHLY));

        String result = visitor.buildResult();

        // Still expect all elements present
        assertTrue(result.contains(TICKET));
        assertTrue(result.contains(UTILITIES));
        assertTrue(result.contains(AMAZON));
        assertTrue(result.contains(GYM));

        // Ordering must match visitation
        assertTrue(result.indexOf(TICKET) < result.indexOf(UTILITIES));
        assertTrue(result.indexOf(UTILITIES) < result.indexOf(AMAZON));
        assertTrue(result.indexOf(AMAZON) < result.indexOf(GYM));
    }
}