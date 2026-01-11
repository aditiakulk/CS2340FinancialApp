package com.example.sprintproject.model;
import android.app.Activity;
import android.content.Intent;

import com.example.sprintproject.R;
import com.example.sprintproject.view.Budgets;
import com.example.sprintproject.view.Chatbot;
import com.example.sprintproject.view.Dashboard;
import com.example.sprintproject.view.ExpenseLog;
import com.example.sprintproject.view.SavingsCircle;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavModel {

    public NavModel() { }

    public void setUpNavigation(Activity host, int selectedId, BottomNavigationView bottomNav) {
        bottomNav.setSelectedItemId(selectedId);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedId) {
                return true;
            }

            boolean handled = navigate(host, id);

            if (handled) {
                host.overridePendingTransition(0, 0);
            }
            return handled;
        });
    }

    public boolean navigate(Activity host, int targetId) {
        if (targetId == R.id.dashboard) {
            host.startActivity(new Intent(host, Dashboard.class));
            return true;
        } else if (targetId == R.id.expense_Log) {
            host.startActivity(new Intent(host, ExpenseLog.class));
            return true;
        } else if (targetId == R.id.budget) {
            host.startActivity(new Intent(host, Budgets.class));
            return true;
        } else if (targetId == R.id.chatbot) {
            host.startActivity(new Intent(host, Chatbot.class));
            return true;
        } else if (targetId == R.id.savings_Circle) {
            host.startActivity(new Intent(host, SavingsCircle.class));
            return true;
        }
        return false;
    }
}
