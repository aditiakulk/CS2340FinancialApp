package com.example.sprintproject;

import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.*;

import com.example.sprintproject.model.Budget;
import com.example.sprintproject.model.Frequency;

public class BudgetModelTest {

    private Date sampleDate;

    @Before
    public void setUp() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sampleDate = sdf.parse("2025-01-01");
    }

    @Test
    public void testDefaultConstructorInitializesFields() {
        Budget budget = new Budget();

        assertEquals("Default", budget.getTitle());
        assertEquals(0.00, budget.getAmount(), 0.001);
        assertEquals("Default", budget.getCategory());
        assertEquals(Frequency.WEEKLY, budget.getFrequency());
        assertNotNull("Date should be initialized", budget.getDate());
    }

    @Test
    public void testParameterizedConstructorSetsFieldsCorrectly() {
        Budget budget = new Budget("Food", 150.0, "Groceries", sampleDate, Frequency.MONTHLY);

        assertEquals("Food", budget.getTitle());
        assertEquals(150.0, budget.getAmount(), 0.001);
        assertEquals("Groceries", budget.getCategory());
        assertEquals(Frequency.MONTHLY, budget.getFrequency());
        assertEquals(sampleDate, budget.getDate());
    }

    @Test
    public void testSettersAndGettersWork() {
        Budget budget = new Budget();

        budget.setTitle("Rent");
        budget.setAmount(1200.50);
        budget.setCategory("Housing");
        budget.setFrequency(Frequency.MONTHLY);
        budget.setDate(sampleDate);
        budget.setCircleId("circle123");
        budget.setId("id456");

        assertEquals("Rent", budget.getTitle());
        assertEquals(1200.50, budget.getAmount(), 0.001);
        assertEquals("Housing", budget.getCategory());
        assertEquals(Frequency.MONTHLY, budget.getFrequency());
        assertEquals(sampleDate, budget.getDate());
        assertEquals("circle123", budget.getCircleId());
        assertEquals("id456", budget.getId());
    }
}

