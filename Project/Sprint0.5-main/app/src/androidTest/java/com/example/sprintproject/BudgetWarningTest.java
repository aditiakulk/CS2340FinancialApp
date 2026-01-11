package com.example.sprintproject;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.example.sprintproject.model.BudgetWarning;
import com.example.sprintproject.model.BudgetWarningQueue;

public class BudgetWarningTest {

    private BudgetWarningQueue warningQueue;
    private static final double WARNING_THRESHOLD = 80.0; // 80%

    @Before
    public void setUp() {
        warningQueue = new BudgetWarningQueue();
    }

    @Test
    public void testBudgetWarningPercentageAndRemainingCalculation() {
        // Test case: Budget of $1000, spent $850 (85%)
        BudgetWarning warning = new BudgetWarning(
                "Monthly Food Budget",
                "Groceries",
                1000.0,
                850.0
        );

        // Verify percentage calculation: (850 / 1000) * 100 = 85%
        assertEquals("Percentage should be 85%", 85.0, warning.getPercentage(), 0.01);
        
        // Verify remaining amount: 1000 - 850 = 150
        assertEquals("Remaining amount should be $150", 150.0, warning.getRemaining(), 0.01);
        
        // Verify other fields
        assertEquals("Budget title should match", "Monthly Food Budget", warning.getBudgetTitle());
        assertEquals("Category should match", "Groceries", warning.getCategory());
        assertEquals("Budget amount should match", 1000.0, warning.getBudgetAmount(), 0.01);
        assertEquals("Total spent should match", 850.0, warning.getTotalSpent(), 0.01);
    }

    @Test
    public void testBudgetWarningThresholdTriggering() {
        // Test case 1: Exactly at 80% threshold
        BudgetWarning warningAtThreshold = new BudgetWarning(
                "Transport Budget",
                "Gas",
                500.0,
                400.0 // Exactly 80%
        );
        assertEquals("Warning at exactly 80% threshold", 80.0, warningAtThreshold.getPercentage(), 0.01);
        assertTrue("Warning should be triggered at 80%", warningAtThreshold.getPercentage() >= WARNING_THRESHOLD);
        assertEquals("Remaining should be $100", 100.0, warningAtThreshold.getRemaining(), 0.01);
        
        // Verify warning can be enqueued
        assertTrue("Warning at threshold should be enqueued", warningQueue.enqueue(warningAtThreshold));
        
        // Test case 2: Above 80% threshold (85%)
        warningQueue.clear(); // Clear queue for new test
        BudgetWarning warningAboveThreshold = new BudgetWarning(
                "Shopping Budget",
                "Clothing",
                2000.0,
                1700.0 // 85%
        );
        assertEquals("Warning above threshold should be 85%", 85.0, warningAboveThreshold.getPercentage(), 0.01);
        assertTrue("Warning should be triggered above 80%", warningAboveThreshold.getPercentage() >= WARNING_THRESHOLD);
        assertEquals("Remaining should be $300", 300.0, warningAboveThreshold.getRemaining(), 0.01);
        assertTrue("Warning above threshold should be enqueued", warningQueue.enqueue(warningAboveThreshold));
        
        // Test case 3: At 100% (budget fully spent)
        warningQueue.clear();
        BudgetWarning warningAt100Percent = new BudgetWarning(
                "Entertainment Budget",
                "Movies",
                100.0,
                100.0 // 100%
        );
        assertEquals("Warning at 100% should calculate correctly", 100.0, warningAt100Percent.getPercentage(), 0.01);
        assertTrue("Warning should be triggered at 100%", warningAt100Percent.getPercentage() >= WARNING_THRESHOLD);
        assertEquals("Remaining should be $0", 0.0, warningAt100Percent.getRemaining(), 0.01);
        assertTrue("Warning at 100% should be enqueued", warningQueue.enqueue(warningAt100Percent));
        
        // Test case 4: Above 100% (budget exceeded)
        warningQueue.clear();
        BudgetWarning warningExceeded = new BudgetWarning(
                "Dining Budget",
                "Restaurants",
                500.0,
                600.0 // 120%
        );
        assertEquals("Warning should handle exceeded budget (120%)", 120.0, warningExceeded.getPercentage(), 0.01);
        assertTrue("Warning should be triggered when budget exceeded", warningExceeded.getPercentage() >= WARNING_THRESHOLD);
        assertEquals("Remaining should be $0 (cannot be negative)", 0.0, warningExceeded.getRemaining(), 0.01);
        assertTrue("Warning for exceeded budget should be enqueued", warningQueue.enqueue(warningExceeded));
    }
}

