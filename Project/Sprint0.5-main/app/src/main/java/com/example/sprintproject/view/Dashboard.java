package com.example.sprintproject.view;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sprintproject.R;
import com.example.sprintproject.model.AllCategories;
import com.example.sprintproject.model.DateModel;
import com.example.sprintproject.viewmodel.ProfileViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.sprintproject.viewmodel.DashboardViewModel;
import com.example.sprintproject.viewmodel.ExpenseLogViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Dashboard extends AppCompatActivity {
    private ExpenseLogViewModel expenseVM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        BottomNavigationView bottomNavigationBar = findViewById(R.id.bottomNavigationBar);
        DashboardViewModel vm = new ViewModelProvider(this).get(DashboardViewModel.class);
        vm.getNavModel().setUpNavigation(this, R.id.dashboard, bottomNavigationBar);
        expenseVM = new ViewModelProvider(this).get(ExpenseLogViewModel.class);

        ImageButton calendarButton = findViewById(R.id.calendar_button);
        calendarButton.setOnClickListener(v -> dateChooserDialog());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        Button clearCacheButton = findViewById(R.id.logout_button);
        clearCacheButton.setOnClickListener(v -> {
            vm.logout(getApplication());
            vm.resetNotificationForUser(user.getUid());
        });

        ImageButton profileButton = findViewById(R.id.profile_button);
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, Profile.class);
            startActivity(intent);
        });

        ProfileViewModel profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        profileViewModel.getProfilePicture().observe(this, pictureID -> {
            int resId = getResources().getIdentifier(pictureID, "drawable", getPackageName());

            if (resId != 0) {
                profileButton.setImageResource(resId);
            } else {
                profileButton.setImageResource(R.drawable.profile_default);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView recycler = findViewById(R.id.DashboardRecyclerView);
        DashboardAdapter adapter = new DashboardAdapter();
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        vm.getAllCategoryData().observe(this, adapter::updateData);
        drawPieChart(vm, DateModel.getCurrentDate().getValue());
        drawBarGraph(vm, DateModel.getCurrentDate().getValue());

        DateModel.getCurrentDate().observe(this, date -> {
            Date pivot = (date != null) ? date : new Date();
            vm.loadAllCategoryTotals(pivot);
        });

        if (user != null) {
            String userKey = user.getUid();

            expenseVM.getAllExpenses().observe(this, expenses -> {
                if (expenses != null && !expenses.isEmpty() && !vm.isNotificationShown(userKey)) {
                    Date lastExpenseDate = expenses.get(0).getDate();
                    checkLastExpense(lastExpenseDate);
                    vm.setNotificationShown(userKey);
                }
            });
        }
    }

    //Gives pop up if the difference between today and last expense is more than a day.
    private void checkLastExpense(Date lastExpenseDate) {
        if (lastExpenseDate == null) {
            return;
        }
        Date currentDate = new Date();
        long diffInMillis = currentDate.getTime() - lastExpenseDate.getTime();

        long daysSinceLastLog = TimeUnit.MILLISECONDS.toDays(diffInMillis);

        if (daysSinceLastLog > 1) {
            showMissedExpenseDialog(daysSinceLastLog);
        }
    }
    //Shows the pop up to the user.
    private void showMissedExpenseDialog(long days) {
        String message = String.format(Locale.getDefault(),
                "It's been %d days since your last expense!", days);
        new AlertDialog.Builder(this)
                .setTitle("Reminder")
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton("Log Expense", (dialog, which) -> {
                    Intent intent = new Intent(Dashboard.this, ExpenseLog.class);
                    startActivity(intent);
                    dialog.dismiss();
                })
                .setNegativeButton("Dismiss", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void dateChooserDialog() {
        Calendar cal = Calendar.getInstance();
        Date curr = DateModel.getCurrentDate().getValue();
        if (curr != null) {
            cal.setTime(curr);
        }

        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH);
        int d = cal.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar c = Calendar.getInstance();
            c.set(year, month, day, 0, 0, 0);
            c.set(Calendar.MILLISECOND, 0);
            DateModel.setCurrentDate(c.getTime());
            Toast.makeText(this, "Selected: " + (month + 1) + "/" + day
                    + "/" + year, Toast.LENGTH_SHORT).show();
        }, y, m, d).show();
    }

    public void drawPieChart(DashboardViewModel dvm, Date pivot) {
        PieChart pieChart = findViewById(R.id.pieChart);

        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(45f);
        pieChart.setTransparentCircleRadius(52f);
        pieChart.setEntryLabelTextSize(11f);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setNoDataText("No spending in this cycle.");

        dvm.loadAllCategoryTotals(pivot);

        dvm.getAllCategoryData().observe(this, data -> {
            ArrayList<PieEntry> entries = new ArrayList<>();
            double grandTotal = 0.0;

            if (data != null) {
                for (AllCategories ac : data) {
                    grandTotal += ac.getTotalSpent();
                }
                for (AllCategories ac : data) {
                    float spent = (float) ac.getTotalSpent();
                    if (spent > 0f) {
                        entries.add(new PieEntry(spent, ac.getCategory()));
                    }
                }
            }

            if (data == null || data.isEmpty() || grandTotal <= 0.0) {
                entries.clear();
                entries.add(new PieEntry(1f, "No Data"));
            }

            PieDataSet set = new PieDataSet(entries,
                    (data == null || data.isEmpty() || grandTotal <= 0.0)
                            ? "Example Categories" : "Spending by Category");
            set.setSliceSpace(2f);
            set.setValueTextSize(12f);
            boolean noData = (data == null || data.isEmpty() || grandTotal <= 0.0);
            if (noData) {
                set.setColors(Color.LTGRAY);
                set.setDrawValues(false);
            } else {
                set.setColors(ColorTemplate.MATERIAL_COLORS);
                set.setDrawValues(true);
            }

            PieData pieData = new PieData(set);
            pieData.setValueFormatter(new PercentFormatter(pieChart));
            pieData.setValueTextColor(Color.WHITE);

            pieChart.setData(pieData);
            pieChart.getLegend().setEnabled(true);
            pieChart.highlightValues(null);
            pieChart.invalidate();
        });
    }

    public void drawBarGraph(DashboardViewModel dvm, Date endDate) {
        BarChart barChart = findViewById(R.id.barChart);

        dvm.loadAllCategoryTotals(endDate);
        dvm.getAllCategoryData().observe(this, data -> {
            List<BarEntry> entries = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            for (int i = 0; i < data.size(); i++) {
                AllCategories category = data.get(i);
                labels.add(category.getCategory());
                float spent = (float) category.getTotalSpent();
                float budget = (float) category.getBudget();

                if (spent > budget) {
                    spent = budget;
                }
                entries.add(new BarEntry(i, new float[]{spent, budget - spent}));
            }

            BarDataSet dataSet = new BarDataSet(entries, "Budget Usage");
            dataSet.setColors(Color.YELLOW, Color.GREEN);
            dataSet.setStackLabels(new String[]{"Spending", "Remaining"});
            dataSet.setDrawValues(false);
            BarData datas = new BarData(dataSet);
            barChart.setData(datas);

            datas.setBarWidth(0.9f);
            barChart.setFitBars(true);
            XAxis xAxis = barChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setGranularity(1f);
            xAxis.setGranularityEnabled(true);
            xAxis.setLabelRotationAngle(315);
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override public String getAxisLabel(float value, AxisBase axis) {
                    final int i = Math.round(value);
                    return (i >= 0 && i < labels.size()) ? labels.get(i) : "";
                }
            });

            barChart.setDragEnabled(true);
            barChart.setScaleEnabled(false);
            if (entries.size() > 5) {
                barChart.setVisibleXRangeMaximum(5f);
                barChart.moveViewToX(0f);
            }

            barChart.setExtraBottomOffset(12f);

            // Legend Entry for colors
            List<LegendEntry> legendInfo = new ArrayList<>();
            LegendEntry legendA = new LegendEntry();
            legendA.label = "Remaining";
            legendA.formColor = Color.GREEN;
            LegendEntry legendB = new LegendEntry();
            legendB.label = "Spending";
            legendB.formColor = Color.YELLOW;
            legendInfo.add(legendA);
            legendInfo.add(legendB);
            barChart.getLegend().setCustom(legendInfo);

            barChart.getAxisRight().setEnabled(false);
            barChart.getDescription().setEnabled(false);
            barChart.invalidate();
        });
    }
}