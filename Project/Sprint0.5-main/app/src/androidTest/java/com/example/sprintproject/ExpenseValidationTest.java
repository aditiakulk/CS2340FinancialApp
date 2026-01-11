package com.example.sprintproject;

import com.example.sprintproject.model.Expense;
import com.example.sprintproject.model.FinancialFactory;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Locale;

public class ExpenseValidationTest {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String SAMPLE_DATE = "2025-10-01";
    private static final String GROCERIES = "Groceries";

    @Test
    public void validExpenseEmptyErrors() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        Date date = sdf.parse(SAMPLE_DATE);

        Expense expense = FinancialFactory.createExpense(GROCERIES, 50.0, date, "Food", null, null);
        Map<String, String> errors = expense.validate();

        assertTrue(errors.isEmpty());
    }

    @Test
    public void emptyNameNameErrors() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        Date date = sdf.parse(SAMPLE_DATE);

        Expense expense = FinancialFactory.createExpense("", 50.0, date, "Food", null, null);
        Map<String, String> errors = expense.validate();

        assertTrue(errors.containsKey("name"));
        assertEquals("Name is required", errors.get("name"));
    }

    @Test
    public void negativeAmountAmountError() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        Date date = sdf.parse(SAMPLE_DATE);

        Expense expense = FinancialFactory.createExpense(GROCERIES, -10.0, date, "Food", null, null);
        Map<String, String> errors = expense.validate();

        assertTrue(errors.containsKey("amount"));
        assertEquals("Amount must be greater than 0", errors.get("amount"));
    }

    @Test
    public void dateErrors() {
        Expense expense1 = FinancialFactory.createExpense(GROCERIES, 50.0, null, "Food", null, null);
        Map<String, String> errors = expense1.validate();
        assertTrue(errors.containsKey("date"));
        assertEquals("Date is required", errors.get("date"));

        Date futureDate = new Date(System.currentTimeMillis() + 100000000);
        Expense expense = FinancialFactory.createExpense(GROCERIES, 50.0, futureDate, "Food", null, null);
        Map<String, String> errors1 = expense.validate();
        assertTrue(errors1.containsKey("date"));
        assertEquals("Date cannot be in the future.", errors1.get("date"));
    }

    @Test
    public void nullCategoryCategoryError() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        Date date = sdf.parse(SAMPLE_DATE);

        Expense expense = FinancialFactory.createExpense(GROCERIES, 50.0, date, null, null, null);
        Map<String, String> errors = expense.validate();

        assertTrue(errors.containsKey("category"));
        assertEquals("Category is required", errors.get("category"));
    }
}
