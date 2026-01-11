package com.example.sprintproject.view;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sprintproject.R;
import com.example.sprintproject.model.Budget;
import com.example.sprintproject.model.BudgetWarning;
import com.example.sprintproject.model.BudgetWarningQueue;
import com.example.sprintproject.model.Expense;
import com.example.sprintproject.model.FinancialFactory;
import com.example.sprintproject.model.SavingsCircleModel;
import com.example.sprintproject.viewmodel.BudgetViewModel;
import com.example.sprintproject.viewmodel.ExpenseLogViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExpenseLog extends AppCompatActivity {

    private static final double WARNING_THRESHOLD_MIN = 0.80; // 80%

    private ExpenseLogViewModel vm;
    private BudgetViewModel budgetVM;
    private BudgetWarningQueue warningQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_expense_log);

        // Set up scrollable list
        RecyclerView recyclerView = findViewById(R.id.expenseRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        CustomAdapter adapter = new CustomAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Set up bottom nav bar
        BottomNavigationView bottomNavigationBar = findViewById(R.id.bottomNavigationBar);
        vm = new ViewModelProvider(this).get(ExpenseLogViewModel.class);
        budgetVM = new ViewModelProvider(this).get(BudgetViewModel.class);
        warningQueue = new BudgetWarningQueue();

        vm.getNavModel().setUpNavigation(this, R.id.expense_Log, bottomNavigationBar);

        vm.getExpenseError().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                showValidationErrors(msg);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        vm.getAllExpenses().observe(this, expenses -> {
            if (expenses != null) {
                adapter.updateData(expenses);
                List<Budget> budgets = budgetVM.getAllBudgets().getValue();
                if (budgets != null) {
                    checkBudgetWarnings(budgets);
                }
            }
        });

        ImageButton addExpenseButton = findViewById(R.id.add_expense_button);
        addExpenseButton.setOnClickListener(v -> {
            View dialogView = getLayoutInflater().inflate(R.layout.expenselog_form, null);
            Dialog dialog = new Dialog(this);
            dialog.setContentView(dialogView);
            dialog.show();

            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                int w = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
                window.setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT);
            }

            EditText etName     = dialogView.findViewById(R.id.etName);
            EditText etAmount   = dialogView.findViewById(R.id.etAmount);
            EditText etDate     = dialogView.findViewById(R.id.etDate);
            EditText etCategory = dialogView.findViewById(R.id.etCategory);
            Button btnSave      = dialogView.findViewById(R.id.btnSave);
            Button btnCancel    = dialogView.findViewById(R.id.btnCancel);
            Spinner spinnerGroupGoal = dialogView.findViewById(R.id.spinnerGroupGoal);

            etCategory.setFilters(new InputFilter[] {new InputFilter.AllCaps()});

            final List<SavingsCircleModel> goalList = new ArrayList<>();
            final ArrayAdapter<String> goalAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item);
            goalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            goalAdapter.setNotifyOnChange(false);
            spinnerGroupGoal.setAdapter(goalAdapter);


            final androidx.lifecycle.Observer<List<SavingsCircleModel>> goalsObserver = goals -> {
                goalList.clear();
                goalAdapter.clear();


                goalAdapter.add("None (Personal Budget Expense)");

                java.util.LinkedHashSet<String> seen = new java.util.LinkedHashSet<>();
                if (goals != null) {
                    for (SavingsCircleModel g : goals) {
                        if (g == null || g.getId() == null) {
                            continue;
                        }
                        if (seen.add(g.getId())) {
                            goalList.add(g);
                            goalAdapter.add("GOAL: " + g.getGroupName());
                        }
                    }
                }
                goalAdapter.notifyDataSetChanged();
            };

            vm.getGroupGoals().observe(ExpenseLog.this, goalsObserver);

            dialog.setOnDismissListener(d -> vm.getGroupGoals().removeObserver(goalsObserver));

            btnCancel.setOnClickListener(x -> dialog.dismiss());

            ArrayList<EditText> packager = new ArrayList<>();
            packager.add(etName);
            packager.add(etAmount);
            packager.add(etDate);
            packager.add(etCategory);
            btnSave.setOnClickListener(x -> save(packager, spinnerGroupGoal, goalList, vm, dialog));
        });
    }
  
    private void showValidationErrors(CharSequence fullMsg) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Errors:")
                .setMessage(fullMsg)
                .setPositiveButton("OK", null)
                .show();
    }

    private void save(ArrayList<EditText> packager, Spinner spinnerGroupGoal,
                      List<SavingsCircleModel> goalList,
                      ExpenseLogViewModel vm, Dialog dialog) {
        String name = packager.get(0).getText().toString().trim();
        String amountStr = packager.get(1).getText().toString().trim();
        String dateStr = packager.get(2).getText().toString().trim();
        String category = packager.get(3).getText().toString().trim();

        double amount = 0;
        Date date = null;

        boolean amountParsed = true;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            amountParsed = false;
        }

        boolean dateParsed = true;
        if (!dateStr.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                sdf.setLenient(false);
                date = sdf.parse(dateStr);
            } catch (ParseException e) {
                dateParsed = false;
            }
        }

        String selectedGoalId = null;
        String creatorUserUid = null;
        int selectedPosition = spinnerGroupGoal.getSelectedItemPosition();

        if (selectedPosition > 0) {
            SavingsCircleModel selectedCircle = goalList.get(selectedPosition - 1);
            selectedGoalId = selectedCircle.getId();
            creatorUserUid = selectedCircle.getCreatorUid();
            category = "Circle: " + selectedCircle.getGroupName();
            packager.get(3).setText(category);
        }

        Expense expense = FinancialFactory.createExpense(name, amount, date,
                category, selectedGoalId, creatorUserUid);
        Map<String, String> errors = expense.validate();

        StringBuilder msg = new StringBuilder();
        if (!amountParsed) {
            msg.append("• Amount must be a number\n");
        }
        if (!dateParsed) {
            msg.append("• Date format must be YYYY-MM-DD\n");
        }
        if ((selectedGoalId == null || selectedGoalId.isEmpty())
                && (category == null || category.trim().isEmpty())) {
            msg.append("• Must be linked to a budget category OR a group goal.\n");
        }
        for (String m : errors.values()) {
            msg.append("• ").append(m).append("\n");
        }

        if (msg.length() > 0) {
            showValidationErrors(msg);
            return;
        }

        vm.addExpense(expense);
        Toast.makeText(this, "Expense added", Toast.LENGTH_LONG).show();
        dialog.dismiss();
    }

    private void checkBudgetWarnings(List<Budget> budgets) {
        if (budgets == null || budgets.isEmpty()) {
            return;
        }

        for (Budget budget : budgets) {
            Date[] window = budgetVM.getCurrentWindow(budget);
            Date windowStart = window[0];
            Date windowEnd = window[1];

            vm.getCategoryTotalExpense(budget.getCategory(), windowStart, windowEnd)
                    .observe(this, totalSpent -> {
                        if (totalSpent != null && budget.getAmount() > 0) {
                            double percentage = (totalSpent / budget.getAmount()) * 100;

                            // Check if spending approaches or reaches 80% (80% and above)
                            // Triggers pop-up when user spending reaches 80% or more of the set
                            // budget
                            if (percentage >= WARNING_THRESHOLD_MIN * 100) {
                                BudgetWarning warning = new BudgetWarning(
                                        budget.getTitle(),
                                        budget.getCategory(),
                                        budget.getAmount(),
                                        totalSpent
                                );

                                // Add to queue if not already shown
                                // Each budget gets its own warning based on budget title + category
                                if (warningQueue.enqueue(warning)) {
                                    // Try to show next warning if none is currently showing
                                    // This will be called for each budget that exceeds threshold
                                    showNextWarningIfAvailable();
                                }
                            }
                        }
                    });
        }
    }

    private void showNextWarningIfAvailable() {
        if (warningQueue.isShowingWarning()) {
            return; // Already showing a warning
        }

        if (!warningQueue.hasWarnings()) {
            return; // No warnings in queue
        }

        BudgetWarning warning = warningQueue.dequeue();
        if (warning != null) {
            showBudgetWarningDialog(warning);
        }
    }

    private void showBudgetWarningDialog(BudgetWarning warning) {
        warningQueue.setShowingWarning(true);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_budget_warning, null);
        Dialog dialog = new Dialog(this);
        dialog.setContentView(dialogView);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int w = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            window.setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setDimAmount(0.5f);
        }

        // Set warning message
        android.widget.TextView tvWarningMessage = dialogView.findViewById(R.id.tvWarningMessage);
        String message = String.format(Locale.US,
                "You've reached %.0f%% of your %s budget",
                warning.getPercentage(),
                warning.getCategory());
        tvWarningMessage.setText(message);

        // Set budget details
        android.widget.TextView tvBudgetCategory = dialogView.findViewById(R.id.tvBudgetCategory);
        tvBudgetCategory.setText("Category: " + warning.getCategory());

        android.widget.TextView tvBudgetTotal = dialogView.findViewById(R.id.tvBudgetTotal);
        tvBudgetTotal.setText(String.format(Locale.US,
                "Budget Total: $%.2f", warning.getBudgetAmount()));

        android.widget.TextView tvSpentAmount = dialogView.findViewById(R.id.tvSpentAmount);
        tvSpentAmount.setText(String.format(Locale.US,
                "Spent: $%.2f", warning.getTotalSpent()));

        android.widget.TextView tvRemainingAmount = dialogView.findViewById(R.id.tvRemainingAmount);
        tvRemainingAmount.setText(String.format(Locale.US,
                "Remaining: $%.2f", warning.getRemaining()));

        android.widget.TextView tvPercentage = dialogView.findViewById(R.id.tvPercentage);
        tvPercentage.setText(String.format(Locale.US,
                "%.0f%% of budget used", warning.getPercentage()));

        // Set progress bar
        android.widget.ProgressBar progressBar = dialogView.findViewById(R.id.progressBarBudget);
        int progressValue = (int) Math.min(100, Math.max(0, warning.getPercentage()));
        progressBar.setProgress(progressValue);

        Button btnOK = dialogView.findViewById(R.id.btnWarningOK);
        btnOK.setOnClickListener(v -> {
            dialog.dismiss();
            warningQueue.setShowingWarning(false);
            // Show next warning if available
            showNextWarningIfAvailable();
        });

        // Handle dialog dismissal
        dialog.setOnDismissListener(d -> {
            warningQueue.setShowingWarning(false);
            showNextWarningIfAvailable();
        });

        dialog.show();
    }
}