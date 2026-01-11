package com.example.sprintproject.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WeeklyBudgetValidationStrategy implements BudgetValidationStrategy {
    @Override
    public Map<String, String> validateDate(Date date) {
        Date start = BudgetValidationStrategy.atStartOfDay(date);
        Date expected = BudgetValidationStrategy.weeklyWindowStart(date, WEEK_START);
        Map<String, String> errors = new HashMap<>();

        if (!BudgetValidationStrategy.sameDay(start, expected)) {
            errors.put("date_week",
                    "For weekly budgets, the start date must be the first day of the week ("
                            + BudgetValidationStrategy.weekStartName(WEEK_START)
                            + "). For that week it should be "
                            + YMD.format(expected) + ".");
        }
        return errors;
    }
}
