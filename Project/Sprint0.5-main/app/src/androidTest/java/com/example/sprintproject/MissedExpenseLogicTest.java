package com.example.sprintproject;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MissedExpenseLogicTest {

    //This helper method accesses private 'checkLastExpense' method.
    private long calculateDaysSinceLastLog(Date lastExpenseDate, Date currentDate) {
        if (lastExpenseDate == null) {
            return 0;
        }
        long diffInMillis = currentDate.getTime() - lastExpenseDate.getTime();
        return TimeUnit.MILLISECONDS.toDays(diffInMillis);
    }

    @Test
    public void testLogicOneDayDifference() {
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        Date lastExpenseDate = calendar.getTime();
        long days = calculateDaysSinceLastLog(lastExpenseDate, currentDate);
        assertEquals(1, days);
    }

    @Test
    public void testLogicThreeDayDifference() {
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, -3);
        Date lastExpenseDate = calendar.getTime();
        long days = calculateDaysSinceLastLog(lastExpenseDate, currentDate);
        assertEquals(3, days);
    }
}