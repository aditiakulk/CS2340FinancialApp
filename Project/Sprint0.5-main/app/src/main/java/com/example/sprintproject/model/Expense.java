package com.example.sprintproject.model;

import static com.example.sprintproject.model.DateModel.getCurrentDate;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Expense implements Validatable {
    private String name;
    private double amount;
    private Date date;
    private String category;
    private String groupGoalId;
    private String circleCreatorUid;


    public Expense() {
        name = "Default";
        amount = 0.00;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            date = sdf.parse("0001-01-01");
        } catch (ParseException e) {
            Log.e("CreateAccountVM", "Error parsing default dates", e);
        }
        this.date = new Date();
        category = "Default";
        groupGoalId = null;
    }

    public Expense(String name, double amount, Date date, String category,
                   String groupGoalId, String circleCreatorUid) {
        this.name = name;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.groupGoalId = groupGoalId;
        this.circleCreatorUid = circleCreatorUid;
    }

    public void accept(FinancialVisitor v) {
        v.visit(this);
    }

    public String getName() {
        return name;
    }

    public double getAmount() {
        return amount;
    }

    public Date getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public String getGroupGoalId() {
        return groupGoalId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setGroupGoalId(String groupGoalId) {
        this.groupGoalId = groupGoalId;
    }

    public String getCircleCreatorUid() {
        return circleCreatorUid;
    }

    public void setCircleCreatorUid(String circleCreatorUid) {
        this.circleCreatorUid = circleCreatorUid;
    }

    @Override
    public Map<String, String> validate() {
        Map<String, String> errors = new HashMap<>();

        if (name == null || name.trim().isEmpty()) {
            errors.put("name", "Name is required");
        }

        if (amount <= 0) {
            errors.put("amount", "Amount must be greater than 0");
        }

        if (category == null || category.trim().isEmpty()
                && (groupGoalId == null))  {
            errors.put("category", "Category is required");
        }

        if (date == null) {
            errors.put("date", "Date is required");
        } else if (date.after(getCurrentDate().getValue())) {
            errors.put("date", "Date cannot be in the future.");
        }

        return errors;
    }
}