package com.example.sprintproject.view;

import static com.example.sprintproject.viewmodel.BudgetViewModel.resolveBudgetFields;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.example.sprintproject.model.BudgetWarningQueue;
import com.example.sprintproject.model.FinancialFactory;
import com.example.sprintproject.model.Frequency;
import com.example.sprintproject.viewmodel.BudgetViewModel;
import com.example.sprintproject.viewmodel.ExpenseLogViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Date;
import java.text.SimpleDateFormat;

public class Budgets extends AppCompatActivity {

    private Frequency selectedFrequency = null;
    private BudgetViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_budget);

        BottomNavigationView bottomNavigationBar = findViewById(R.id.bottomNavigationBar);
        vm = new ViewModelProvider(this).get(BudgetViewModel.class);
        vm.getNavModel().setUpNavigation(this, R.id.budget, bottomNavigationBar);
        ExpenseLogViewModel expenseVM = new ViewModelProvider(this).get(ExpenseLogViewModel.class);
        BudgetWarningQueue warningQueue = new BudgetWarningQueue();

        // Set up scrollable list
        RecyclerView recyclerView = findViewById(R.id.budgetRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        BudgetAdapter adapter = new BudgetAdapter(new ArrayList<>(), vm, expenseVM);
        recyclerView.setAdapter(adapter);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Observe budgets and expenses to check for warnings
        vm.getAllBudgets().observe(this, budgets -> {
            if (budgets != null) {
                adapter.updateData(budgets);
            }
        });

        vm.getBudgetError().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                showValidationErrors(msg);
            }
        });

        Button openCalc = findViewById(R.id.btn_calculate);
        openCalc.setOnClickListener(v -> openBudgetCalculatorDialog());

        ImageButton addBudgetButton = findViewById(R.id.add_budget_button);
        addBudgetButton.setOnClickListener(v -> showAddBudgetDialog());
    }

    private void openBudgetCalculatorDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_budget_calculator, null);
        Dialog dialog = new Dialog(this);
        dialog.setContentView(dialogView);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int w = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            window.setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setDimAmount(0.5f);
        }

        EditText etCategory  = dialogView.findViewById(R.id.etCategory);
        EditText etTotal     = dialogView.findViewById(R.id.etTotal);
        EditText etSpent     = dialogView.findViewById(R.id.etSpent);
        EditText etRemaining = dialogView.findViewById(R.id.etRemaining);
        Button btnApply      = dialogView.findViewById(R.id.btnApplyCalc);
        Button btnCancel     = dialogView.findViewById(R.id.btnCancelCalc);

        etCategory.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        btnApply.setOnClickListener(v -> {
            String category = etCategory.getText().toString().trim();
            if (category.isEmpty()) {
                return;
            }

            Double total     = parseNum(etTotal.getText().toString());
            Double spent     = parseNum(etSpent.getText().toString());
            Double remaining = parseNum(etRemaining.getText().toString());


            Double[] result = resolveBudgetFields(total, spent, remaining);
            total = result[0];
            spent = result[1];
            remaining = result[2];

            vm.applyCalculatorDeleteInsertUpdate(category, total, spent, remaining);

            dialog.dismiss();
        });


        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private Double parseNum(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();

        if (t.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(t);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void showValidationErrors(CharSequence fullMsg) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Errors:")
                .setMessage(fullMsg)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showAddBudgetDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.budget_form, null);
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

        etCategory.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        Button btnWeekly = dialogView.findViewById(R.id.btnWeekly);
        Button btnMonthly = dialogView.findViewById(R.id.btnMonthly);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        btnWeekly.setOnClickListener(x -> selectedFrequency = Frequency.WEEKLY);
        btnMonthly.setOnClickListener(x -> selectedFrequency = Frequency.MONTHLY);
        btnCancel.setOnClickListener(x -> dialog.dismiss());

        btnSave.setOnClickListener(x -> {
            String name         = etName.getText().toString().trim();
            String amountStr    = etAmount.getText().toString().trim();
            String startDateStr = etDate.getText().toString().trim();
            String category     = etCategory.getText().toString().trim();

            double amount = 0;
            boolean amountParsed = true;

            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                amountParsed = false;
            }

            Date startDate = null;
            boolean dateParsed = true;
            if (!startDateStr.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    sdf.setLenient(false);
                    startDate = sdf.parse(startDateStr);
                } catch (ParseException e) {
                    dateParsed = false;
                }
            }

            Budget newBudget = FinancialFactory.createBudget(
                    name, amount, category, startDate, selectedFrequency
            );

            Map<String, String> errors = newBudget.validate();

            StringBuilder msg = new StringBuilder();
            if (!amountParsed && !amountStr.isEmpty()) {
                msg.append("• Amount must be a number\n");
            }
            // small fix: check the *date* string for date errors
            if (!dateParsed && !startDateStr.isEmpty()) {
                msg.append("• Date must be in yyyy-MM-dd format\n");
            }
            for (String m : errors.values()) {
                msg.append("• ").append(m).append("\n");
            }

            if (msg.length() > 0) {
                showValidationErrors(msg);
                return;
            }

            vm.addBudget(newBudget);
            Toast.makeText(this, "Budget saved", Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });
    }
}