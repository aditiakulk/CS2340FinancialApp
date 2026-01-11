package com.example.sprintproject.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sprintproject.model.Expense;
import com.example.sprintproject.model.FirestoreModel;
import com.example.sprintproject.model.SavingsCircleModel;
import com.example.sprintproject.model.NavModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExpenseLogViewModel extends ViewModel {

    private final NavModel navModel = new NavModel();

    public NavModel getNavModel() {
        return navModel;
    }

    private final MutableLiveData<List<Expense>> expenses
            = new MutableLiveData<>(new ArrayList<>());

    private final FirestoreModel fsm = FirestoreModel.getInstance();

    private LiveData<List<SavingsCircleModel>> groupGoals;

    public LiveData<List<Expense>> getExpenses() {
        return expenses;
    }

    private final MutableLiveData<String> expenseError = new MutableLiveData<>(null);

    public LiveData<List<SavingsCircleModel>> getGroupGoals() {
        if (groupGoals == null) {
            groupGoals = fsm.getUserGroupGoals();
        }
        return groupGoals;
    }

    public void addExpense(Expense expense) {
        String category = expense.getCategory() != null ? expense.getCategory().trim() : "";
        String goalId   = expense.getGroupGoalId() != null ? expense.getGroupGoalId().trim() : "";
        if (!category.isEmpty() && goalId.isEmpty()) {
            fsm.categoryExistsForUser(category)
                    .addOnSuccessListener(exists -> {
                        if (Boolean.TRUE.equals(exists)) {
                            fsm.addExpense(expense);
                        } else {
                            expenseError.setValue("• Category \"" + category
                                    + "\" does not exist in your budgets.\n"
                                    + "Add a budget for this category first.");
                        }
                    })
                    .addOnFailureListener(e ->
                            expenseError.setValue("• Could not validate category: "
                                    + e.getMessage()));
            return;
        }

        if (!goalId.isEmpty()) {
            fsm.addExpense(expense)
                .addOnSuccessListener(ref ->
                    fsm.getSavingsCircleById(expense.getCircleCreatorUid(),
                                    expense.getGroupGoalId())
                            .addOnSuccessListener(document -> {
                                SavingsCircleModel circle
                                        = document.toObject(SavingsCircleModel.class);
                                if (circle != null) {
                                    Date expenseDate = expense.getDate();
                                    Date startDate = circle.getStartDate();
                                    Date[] dateRange = SavingsCircleViewModel.challengeWindow(
                                            startDate, circle.getFrequency());

                                    if (expenseDate != null && !expenseDate.before(dateRange[0])
                                            && !expenseDate.after(dateRange[1])) {
                                        fsm.updateSavingsCircleCurrentAmount(
                                                expense.getCircleCreatorUid(),
                                                expense.getGroupGoalId(),
                                                expense.getAmount()
                                        );
                                    }
                                }

                            })
                );
            return;
        }
        expenseError.setValue("• Please select a budget category or "
                + "link this expense to a group goal.");
    }

    public MutableLiveData<List<Expense>> getAllExpenses() {
        return fsm.getExpenses();
    }

    public MutableLiveData<Double> getCategoryTotalExpense(String category, Date start, Date end) {
        return fsm.getCategoryTotalExpense(category, start, end);
    }

    public void clearExpenses() {
        expenses.setValue(new ArrayList<>());
    }

    public LiveData<String> getExpenseError() {
        return expenseError;
    }
}
