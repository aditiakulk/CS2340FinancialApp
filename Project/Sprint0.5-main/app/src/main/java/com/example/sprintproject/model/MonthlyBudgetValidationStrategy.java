package com.example.sprintproject.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MonthlyBudgetValidationStrategy implements BudgetValidationStrategy {
    public Map<String, String> validateDate(Date date) {
        Date expected = BudgetValidationStrategy.monthStart(date);
        Date start = BudgetValidationStrategy.atStartOfDay(date);

        Map<String, String> errors = new HashMap<>();
        if (!BudgetValidationStrategy.sameDay(start, expected)) {
            errors.put("date_month",
                    "For monthly budgets, the start date must be the 1st of the month."
                            + "For that month it should be "
                            + YMD.format(expected) + ".");
        }
        return errors;
    }
}
