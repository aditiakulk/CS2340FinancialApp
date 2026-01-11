package com.example.sprintproject.viewmodel;

import static android.content.ContentValues.TAG;

import static com.example.sprintproject.model.DateModel.getCurrentDate;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sprintproject.model.Budget;
import com.example.sprintproject.model.BudgetStatus;
import com.example.sprintproject.model.FirestoreModel;
import com.example.sprintproject.model.Frequency;
import com.example.sprintproject.model.NavModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BudgetViewModel extends ViewModel {

    private final NavModel navModel = new NavModel();

    public NavModel getNavModel() {
        return navModel;
    }

    private final MutableLiveData<List<Budget>> budgets
            = new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<String> budgetError = new MutableLiveData<>(null);
    public LiveData<String> getBudgetError() {
        return budgetError;
    }

    private final FirestoreModel fsm = FirestoreModel.getInstance();

    public LiveData<List<Budget>> getBudgets() {
        return budgets;
    }

    public void addBudget(Budget budget) {
        fsm.categoryExistsForUser(budget.getCategory())
                .addOnSuccessListener(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        budgetError.setValue("• Category \"" + budget.getCategory()
                                + "\" already has a budget.");
                    } else {
                        fsm.addBudget(budget)
                                .addOnSuccessListener(documentReference ->
                                    Log.d(TAG, "Budget Added: " + documentReference.getId()))
                                .addOnFailureListener(e -> {
                                    Log.w(TAG, "Error adding budget", e);
                                    budgetError.setValue("• Failed to add budget: "
                                            + e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e ->
                        budgetError.setValue("• Could not check category: " + e.getMessage()));
    }

    public MutableLiveData<List<Budget>> getAllBudgets() {
        return fsm.getBudgets();
    }

    public BudgetStatus getBudgetStatus(Budget budget, double totalSpent) {
        double amount = budget.getAmount();

        if (amount <= 0) {
            return BudgetStatus.INCOMPLETE;
        }

        double ratio = totalSpent / amount;

        if (ratio >= 1.0) {
            return BudgetStatus.INCOMPLETE; // red
        } else if (ratio >= 0.60) {
            // spent 60%+ but not over
            return BudgetStatus.IN_PROGRESS; // yellow
        } else {
            return BudgetStatus.COMPLETE; // green
        }
    }

    public Date[] getCurrentWindow(Budget budget) {
        Calendar cal = Calendar.getInstance();
        Date today = getCurrentDate().getValue();

        if (today != null) {
            cal.setTime(today);
        }

        if (budget.getFrequency() == Frequency.MONTHLY) {
            cal.set(Calendar.DAY_OF_MONTH, 1);
            setStartOfDay(cal);
            Date start = cal.getTime();

            cal.add(Calendar.MONTH, 1);
            cal.add(Calendar.DAY_OF_YEAR, -1);
            setEndOfDay(cal);
            Date end = cal.getTime();

            return new Date[]{start, end};
        }

        cal.setFirstDayOfWeek(Calendar.SUNDAY);
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        int offset = (7 + (dow - Calendar.SUNDAY)) % 7;
        cal.add(Calendar.DAY_OF_MONTH, -offset);
        setStartOfDay(cal);
        Date start = cal.getTime();

        cal.add(Calendar.DAY_OF_MONTH, 6);
        setEndOfDay(cal);
        Date end = cal.getTime();

        return new Date[]{start, end};
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

    public LiveData<Budget> getBudgetByCategory(String category) {
        return fsm.getBudgetByCategory(category);
    }

    public void applyCalculatorDeleteInsertUpdate(String category, Double total,
                                                  Double spent, Double remaining) {
        if (category == null || category.trim().isEmpty()) {
            budgetError.setValue("• Enter a category to apply the calculator.");
            return;
        }

        String cat = category.trim();

        Double[] r = resolveBudgetFields(total, spent, remaining);
        Double resolvedTotal = r[0];
        Double resolvedSpent = r[1];

        if (resolvedTotal == null || resolvedSpent == null) {
            budgetError.setValue("• Provide at least two of Total / Spent / Remaining.");
            return;
        }
        double newTotal = resolvedTotal;
        double spentForCycle = resolvedSpent;

        fsm.getBudgetByCategoryOnce(category)
                .onSuccessTask(budget -> {
                    Date[] window = getCurrentWindow(budget);
                    Date start = window[0];
                    Date end = window[1];
                    Date now = getCurrentDate().getValue();

                    return fsm.deleteExpensesInRangeClientFiltered(category, start, end)
                            .onSuccessTask(v -> fsm.addCycleAdjustmentExpense(cat,
                                    spentForCycle, now))
                            .onSuccessTask(ref -> fsm.updateBudgetAmountByCategory(cat,
                                    newTotal));
                })
                .addOnFailureListener(e ->
                        budgetError.setValue("• Calculator apply failed: " + e.getMessage()));
    }

    public static Double[] resolveBudgetFields(Double total, Double spent, Double remaining) {
        int filled = (total != null ? 1 : 0) + (spent != null ? 1 : 0)
                + (remaining != null ? 1 : 0);
        if (filled < 2) {
            return new Double[]{total, spent, remaining };
        }

        if (total == null && spent != null && remaining != null) {
            total = spent + remaining;
        } else if (spent == null && total != null && remaining != null) {
            spent = total - remaining;
        } else if (remaining == null && total != null && spent != null) {
            remaining = total - spent;
        }
        return new Double[]{total, spent, remaining };
    }

}
