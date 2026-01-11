package com.example.sprintproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import com.example.sprintproject.model.BudgetValidationStrategy;
import com.example.sprintproject.model.WeeklyBudgetValidationStrategy;
import com.example.sprintproject.model.MonthlyBudgetValidationStrategy;

public class ValidationStrategyTest {
    private Date createTestDate(int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, month - 1, day, 12, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    @Test
    public void monthStartReturnsFirstDayOfMonth() {
        Date middleOfMonth = createTestDate(11, 15);
        Date expectedStart = createTestDate(11, 1);
        Date actualStart = BudgetValidationStrategy.monthStart(middleOfMonth);
        assertTrue(BudgetValidationStrategy.sameDay(expectedStart, actualStart));
        Calendar cal = Calendar.getInstance();
        cal.setTime(actualStart);
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
    }

    @Test
    public void weeklyWindowStartReturnsSundayStart() {
        Date wednesdayDate = createTestDate(10, 30); // Wednesday, Oct 30
        Date expectedSunday = createTestDate(10, 27); // Sunday, Oct 27
        Date actualStart = BudgetValidationStrategy.weeklyWindowStart(
                wednesdayDate, BudgetValidationStrategy.WEEK_START);
        assertTrue(BudgetValidationStrategy.sameDay(expectedSunday, actualStart));
    }

    @Test
    public void weeklyStrategyValidSundayStart() {
        Date validDate = createTestDate(10, 27);
        WeeklyBudgetValidationStrategy strategy = new WeeklyBudgetValidationStrategy();
        Map<String, String> errors = strategy.validateDate(validDate);
        assertTrue("No errors expected for a valid weekly start date.", errors.isEmpty());
    }

    @Test
    public void weeklyStrategyInvalidMondayStart() {
        Date validDate = createTestDate(10, 28);
        WeeklyBudgetValidationStrategy strategy = new WeeklyBudgetValidationStrategy();
        Map<String, String> errors = strategy.validateDate(validDate);
        assertFalse("Invalid weekly start date (Monday).", errors.isEmpty());
    }

    @Test
    public void weeklyStrategyInvalidMidWeekStart() {
        Date invalidDate = createTestDate(10, 30);
        WeeklyBudgetValidationStrategy strategy = new WeeklyBudgetValidationStrategy();
        Map<String, String> errors = strategy.validateDate(invalidDate);
        assertFalse("Invalid date should throw errors.", errors.isEmpty());
    }

    @Test
    public void monthlyStrategyValidFirstOfMonthStart() {
        Date validDate = createTestDate(11, 1);
        MonthlyBudgetValidationStrategy strategy = new MonthlyBudgetValidationStrategy();
        Map<String, String> errors = strategy.validateDate(validDate);
        assertTrue("No errors expected for a valid monthly start date.", errors.isEmpty());
    }

    @Test
    public void monthlyStrategyInvalidMidMonthStart() {
        Date invalidDate = createTestDate(11, 15);
        MonthlyBudgetValidationStrategy strategy = new MonthlyBudgetValidationStrategy();
        Map<String, String> errors = strategy.validateDate(invalidDate);
        assertFalse("Error map should not be empty for invalid date.", errors.isEmpty());
    }
}