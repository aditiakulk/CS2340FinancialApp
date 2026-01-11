package com.example.sprintproject.viewmodel;

import android.app.Application;
import android.content.Intent;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.sprintproject.model.AllCategories;
import com.example.sprintproject.model.Budget;
import com.example.sprintproject.model.DashboardModel;
import com.example.sprintproject.model.Expense;
import com.example.sprintproject.model.NavModel;
import com.example.sprintproject.view.Login;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardViewModel extends AndroidViewModel {
    private NavModel navModel = new NavModel();
    private DashboardModel dashboardModel;

    private final ExpenseLogViewModel expenseVM;
    private final BudgetViewModel budgetVM;

    private final MutableLiveData<List<AllCategories>> allCategoryData = new MutableLiveData<>();
    private static final Map<String, Boolean> NOTIFICATION_SHOWN = new HashMap<>();

    public DashboardViewModel(Application application) {
        super(application);
        dashboardModel = new DashboardModel(application);
        expenseVM = new ExpenseLogViewModel();
        budgetVM = new BudgetViewModel();
    }

    public void logout(Application application) {
        dashboardModel.logout();

        Intent intent = new Intent(application, Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        application.startActivity(intent);
    }

    public void loadAllCategoryTotals(Date pivot) {
        final Date effectivePivot = (pivot != null) ? pivot : new Date();
        budgetVM.getAllBudgets().observeForever(budgets ->
            expenseVM.getAllExpenses().observeForever(expenses -> {
                List<AllCategories> list = new ArrayList<>();
                if (budgets != null) {
                    for (Budget b : budgets) {
                        if (b.getDate() != null && !b.getDate().after(effectivePivot)) {
                            Date[] window = getAnchoredWindow(b, effectivePivot);
                            double total = calculateTotalExpenseInWindow(
                                    expenses, b.getCategory(), window[0], window[1]);
                            list.add(new AllCategories(b.getCategory(), total, b.getAmount()));
                        }
                    }
                }
                allCategoryData.setValue(list);
            })
        );
    }

    private double calculateTotalExpenseInWindow(List<Expense> expenses, String category,
                                                 Date start, Date end) {
        if (expenses == null || expenses.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;
        for (Expense e : expenses) {
            if (e != null) {
                Date d = e.getDate();
                if (d != null && category.equals(e.getCategory())
                        && !d.before(start) && !d.after(end)) {
                    sum += e.getAmount();
                }
            }
        }
        return sum;
    }


    private static Date[] getAnchoredWindow(Budget b, Date pivot) {
        Date anchor = b.getDate();
        if (anchor == null) {
            anchor = pivot;
        }

        if (b.getFrequency() == com.example.sprintproject.model.Frequency.WEEKLY) {
            return weeklyWindow(pivot);
        } else {
            return monthlyWindow(anchor, pivot);
        }
    }

    private static Date[] weeklyWindow(Date pivot) {
        Calendar c = Calendar.getInstance();
        c.setTime(pivot);

        int currentDOW = c.get(Calendar.DAY_OF_WEEK);
        int daysToSubtract = (currentDOW - Calendar.SUNDAY + 7) % 7;
        c.add(Calendar.DAY_OF_MONTH, -daysToSubtract);

        setStartOfDay(c);
        Date start = c.getTime();

        c.add(Calendar.DAY_OF_MONTH, 6);
        setEndOfDay(c);
        Date end = c.getTime();

        Date[] startEnd = new Date[2];
        startEnd[0] = start;
        startEnd[1] = end;

        return startEnd;
    }

    private static Date[] monthlyWindow(Date anchor, Date pivot) {
        Calendar c = Calendar.getInstance();
        Calendar a = Calendar.getInstance();
        c.setTime(pivot);
        a.setTime(anchor);

        int anchorDOM = a.get(Calendar.DAY_OF_MONTH);

        Calendar startCal = (Calendar) c.clone();
        int thisMonthMax = startCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int targetDay = Math.min(anchorDOM, thisMonthMax);

        if (c.get(Calendar.DAY_OF_MONTH) >= targetDay) {
            startCal.set(Calendar.DAY_OF_MONTH, targetDay);
        } else {
            startCal.add(Calendar.MONTH, -1);
            int prevMax = startCal.getActualMaximum(Calendar.DAY_OF_MONTH);
            startCal.set(Calendar.DAY_OF_MONTH, Math.min(anchorDOM, prevMax));
        }
        setStartOfDay(startCal);
        Date start = startCal.getTime();

        Calendar endCal = (Calendar) startCal.clone();
        endCal.add(Calendar.MONTH, 1);
        endCal.add(Calendar.MILLISECOND, -1);
        setEndOfDay(endCal); // ensure it's the day's end
        Date end = endCal.getTime();

        Date[] startEnd = new Date[2];
        startEnd[0] = start;
        startEnd[1] = end;

        return startEnd;
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

    public NavModel getNavModel() {
        return navModel;
    }

    public MutableLiveData<List<AllCategories>> getAllCategoryData() {
        return allCategoryData;
    }

    public boolean isNotificationShown(String userId) {
        return NOTIFICATION_SHOWN.getOrDefault(userId, false);
    }

    public void setNotificationShown(String userId) {
        NOTIFICATION_SHOWN.put(userId, true);
    }

    public void resetNotificationForUser(String userId) {
        NOTIFICATION_SHOWN.remove(userId);
    }
}
