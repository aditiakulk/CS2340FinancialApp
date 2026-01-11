package com.example.sprintproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.sprintproject.model.Frequency;
import com.example.sprintproject.model.SavingsCircleModel;
import com.example.sprintproject.viewmodel.SavingsCircleViewModel;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

public class SavingsCircleTest {
    @Test
    public void validateMissingFields() {
        SavingsCircleModel m = new SavingsCircleModel();
        m.setGroupName(" ");
        m.setChallengeTitle(null);
        m.setGoalAmount(0);
        m.setNotes("");

        Map<String,String> errs = m.validate();
        assertTrue(errs.containsKey("groupName"));
        assertTrue(errs.containsKey("challengeTitle"));
        assertTrue(errs.containsKey("goalAmount"));
        assertTrue(errs.containsKey("notes"));
        assertFalse(errs.containsKey("frequency")); // default is set in ctor
    }

    @Test
    public void validateNoErrors() {
        SavingsCircleModel m = new SavingsCircleModel(
                "Trip Fund","Save for trip",100.0,
                "Let’s go!", Frequency.WEEKLY,new Date()
        );
        Map<String,String> errs = m.validate();
        assertTrue(errs.isEmpty());
    }

    @Test
    public void challengeWindowWeekly() {
        Calendar c = Calendar.getInstance();
        c.set(2025, Calendar.NOVEMBER, 2, 15, 0, 0);  // arbitrary midday
        c.set(Calendar.MILLISECOND, 0);

        Date start = c.getTime();
        Date[] w = SavingsCircleViewModel.challengeWindow(start, Frequency.WEEKLY);

        Calendar a = Calendar.getInstance();
        a.setTime(w[0]);
        assertEquals(0, a.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, a.get(Calendar.MINUTE));
        assertEquals(0, a.get(Calendar.SECOND));
        assertEquals(0, a.get(Calendar.MILLISECOND));

        Calendar b = Calendar.getInstance();
        b.setTime(w[1]);
        assertEquals(23, b.get(Calendar.HOUR_OF_DAY));
        assertEquals(59, b.get(Calendar.MINUTE));
        assertEquals(59, b.get(Calendar.SECOND));
        assertEquals(999, b.get(Calendar.MILLISECOND));

        long days = (w[1].getTime() - w[0].getTime()) / (24L * 60 * 60 * 1000);
        assertEquals(7L, days);
    }

    @Test
    public void challengeWindowMonthly() {
        Calendar c = Calendar.getInstance();
        c.set(2025, Calendar.SEPTEMBER, 10, 12, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        Date start = c.getTime();

        Date[] w = SavingsCircleViewModel.challengeWindow(start, Frequency.MONTHLY);

        Calendar s = Calendar.getInstance();
        s.setTime(w[0]);
        Calendar e = Calendar.getInstance();
        e.setTime(w[1]);


        assertEquals(10, s.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, s.get(Calendar.HOUR_OF_DAY));


        assertEquals(9, e.get(Calendar.DAY_OF_MONTH));
        assertEquals(23, e.get(Calendar.HOUR_OF_DAY));
        assertEquals(59, e.get(Calendar.MINUTE));
        assertEquals(59, e.get(Calendar.SECOND));
        assertEquals(999, e.get(Calendar.MILLISECOND));
    }
}
