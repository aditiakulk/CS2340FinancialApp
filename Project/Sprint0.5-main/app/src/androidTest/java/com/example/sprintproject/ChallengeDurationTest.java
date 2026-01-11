package com.example.sprintproject;

import static org.junit.Assert.*;

import com.example.sprintproject.viewmodel.SavingsCircleViewModel;
import com.example.sprintproject.model.Frequency;

import org.junit.Test;
import org.junit.Before;

import java.util.Date;
import java.util.Calendar;

public class ChallengeDurationTest {
    private Calendar baseCalendar;

    @Before
    public void setUp() {
        // Create a fixed date to ensure tests are deterministic
        baseCalendar = Calendar.getInstance();
        baseCalendar.set(2025, Calendar.OCTOBER, 15, 10, 30, 45);
        baseCalendar.set(Calendar.MILLISECOND, 123);
    }

    /**
     * Test Case 1: ChallengeWindow for a MONTHLY frequency.
     */
    @Test
    public void testChallengeWindowMonthly() {
        Date acceptanceDate = baseCalendar.getTime();

        // Expected Start: October 15, 2025
        Calendar expectedStartCal = (Calendar) baseCalendar.clone();
        setStartOfDay(expectedStartCal);
        Date expectedStartDate = expectedStartCal.getTime();

        // Expected End: November 14, 2025, 23:59:59.999
        Calendar expectedEndCal = (Calendar) baseCalendar.clone();
        expectedEndCal.add(Calendar.MONTH, 1);
        expectedEndCal.add(Calendar.DAY_OF_YEAR, -1);
        setEndOfDay(expectedEndCal);
        Date expectedEndDate = expectedEndCal.getTime();

        Date[] result = SavingsCircleViewModel.challengeWindow(acceptanceDate, Frequency.MONTHLY);

        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals(expectedStartDate, result[0]);
        assertEquals(expectedEndDate, result[1]);
    }

    /**
     * Test Case 2: ChallengeWindow for a WEEKLY frequency.
     */
    @Test
    public void testChallengeWindowWeekly() {
        Date acceptanceDate = baseCalendar.getTime();

        // Expected Start: October 15, 2025
        Calendar expectedStartCal = (Calendar) baseCalendar.clone();
        setStartOfDay(expectedStartCal);
        Date expectedStartDate = expectedStartCal.getTime();

        // Expected End: October 21, 2025
        Calendar expectedEndCal = (Calendar) baseCalendar.clone();
        expectedEndCal.add(Calendar.DAY_OF_MONTH, 6);
        setEndOfDay(expectedEndCal);
        Date expectedEndDate = expectedEndCal.getTime();

        Date[] result = SavingsCircleViewModel.challengeWindow(acceptanceDate, Frequency.WEEKLY);

        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals(expectedStartDate, result[0]);
        assertEquals(expectedEndDate, result[1]);
    }
    private static void setStartOfDay(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    private static void setEndOfDay(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
    }
}
