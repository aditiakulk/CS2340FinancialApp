package com.example.sprintproject.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public interface BudgetValidationStrategy {
    Map<String, String> validateDate(Date date);
    SimpleDateFormat YMD = new SimpleDateFormat("yyyy-MM-dd");
    static final int WEEK_START = Calendar.SUNDAY;


    static Date atStartOfDay(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    static Date monthStart(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }
    static Date weeklyWindowStart(Date d, int weekStartDay) {
        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(weekStartDay);
        c.setTime(d);
        int dow = c.get(Calendar.DAY_OF_WEEK);
        int offset = (7 + (dow - weekStartDay)) % 7;
        c.add(Calendar.DAY_OF_MONTH, -offset);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    static boolean sameDay(Date a, Date b) {
        Calendar ca = Calendar.getInstance();
        Calendar cb = Calendar.getInstance();
        ca.setTime(a);
        cb.setTime(b);
        return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR)
                && ca.get(Calendar.MONTH) == cb.get(Calendar.MONTH)
                && ca.get(Calendar.DAY_OF_MONTH) == cb.get(Calendar.DAY_OF_MONTH);
    }
    static String weekStartName(int weekStart) {
        return (weekStart == Calendar.MONDAY) ? "Monday" : "Sunday";
    }

}
