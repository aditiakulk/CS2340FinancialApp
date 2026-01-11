package com.example.sprintproject.model;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import android.util.Log;

import java.text.ParseException;
import java.util.Locale;

public class Budget implements Validatable {
    private String title;
    private double amount;
    private String category;
    private Date date;
    private Frequency frequency;
    private String circleId;
    private String id;


    public Budget() {
        this.title = "Default";
        this.amount = 0.00;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            this.date = sdf.parse("0001-01-01");
        } catch (ParseException e) {
            Log.e("CreateAccountVM", "Error parsing default dates", e);
            this.date = null;
        }
        this.category = "Default";
        this.frequency = Frequency.WEEKLY;
    }

    public Budget(String title, double amount, String category, Date date, Frequency frequency) {
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.frequency = frequency;
    }

    @Override
    public Map<String, String> validate() {
        Map<String, String> errors = new HashMap<>();

        if (title == null || title.trim().isEmpty()) {
            errors.put("title", "Title is required");
        }
        if (amount <= 0) {
            errors.put("amount", "Amount must be positive");
        }
        if (category == null || category.trim().isEmpty()) {
            errors.put("category", "Category is required");
        }
        if (frequency == null) {
            errors.put("frequency", "Frequency is required");
        }
        if (date == null) {
            errors.put("date", "Start date is required");
            return errors;
        }

        BudgetValidationStrategy dateStrategy;
        if (frequency == Frequency.WEEKLY) {
            dateStrategy = new WeeklyBudgetValidationStrategy();
        } else if (frequency == Frequency.MONTHLY) {
            dateStrategy = new MonthlyBudgetValidationStrategy();
        } else {
            throw new BudgetFrequencyException("A validation strategy is "
                    + "not defined for frequency: " + frequency);
        }

        errors.putAll(dateStrategy.validateDate(date));
        return errors;
    }

    public void accept(FinancialVisitor v) {
        v.visit(this);
    }

    public String getCircleId() {
        return circleId;
    }

    public void setCircleId(String circleId) {
        this.circleId = circleId;
    }

    public String getTitle() {
        return title;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public Date getDate() {
        return date;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


}