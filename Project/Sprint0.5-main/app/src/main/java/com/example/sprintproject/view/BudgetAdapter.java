package com.example.sprintproject.view;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.sprintproject.R;
import com.example.sprintproject.model.Budget;
import com.example.sprintproject.model.BudgetStatus;
import com.example.sprintproject.viewmodel.BudgetViewModel;
import com.example.sprintproject.viewmodel.ExpenseLogViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.ViewHolder> {

    private final ArrayList<Budget> budgetList;
    private final ExpenseLogViewModel expenseVM;
    private final BudgetViewModel budgetVM;

    public BudgetAdapter(ArrayList<Budget> dataSet, BudgetViewModel bvm, ExpenseLogViewModel evm) {
        budgetList = dataSet;
        budgetVM = bvm;
        expenseVM = evm;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.budget_box, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Budget budget = budgetList.get(position);
        viewHolder.bind(budget);

        viewHolder.itemView.setOnClickListener(v -> {
            View calc = v.findViewById(R.id.calculatorLayout);
            if (calc.getVisibility() == View.GONE) {
                calc.setVisibility(View.VISIBLE);
            } else {
                calc.setVisibility(View.GONE);
            }
        });

        Date[] window = budgetVM.getCurrentWindow(budget);
        Date windowStart = window[0];
        Date windowEnd = window[1];

        expenseVM.getCategoryTotalExpense(budget.getCategory(), windowStart, windowEnd)
                .observeForever(totalSpent -> {
                    if (totalSpent != null) {
                        double remaining = Math.max(0, budget.getAmount() - totalSpent);
                        TextView tvTotal = viewHolder.itemView.findViewById(R.id.tvTotal);
                        TextView tvSpent = viewHolder.itemView.findViewById(R.id.tvSpent);
                        TextView tvRemaining = viewHolder.itemView.findViewById(R.id.tvRemaining);

                        tvTotal.setText("Total: $" + String.format(Locale.US,
                                "%.2f", budget.getAmount()));
                        tvSpent.setText("Spent-to-Date: $" + String.format(Locale.US,
                                "%.2f", totalSpent));
                        tvRemaining.setText("Remaining: $" + String.format(Locale.US,
                                "%.2f", remaining));

                        BudgetStatus status = budgetVM.getBudgetStatus(budget, totalSpent);
                        // Update title color based on status
                        if (status == BudgetStatus.IN_PROGRESS) {
                            viewHolder.titleText.setTextColor(Color.YELLOW);
                        } else if (status == BudgetStatus.INCOMPLETE) {
                            viewHolder.titleText.setTextColor(Color.RED);
                        } else if (status == BudgetStatus.COMPLETE) {
                            viewHolder.titleText.setTextColor(Color.GREEN);
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    public void updateData(List<Budget> newData) {
        budgetList.clear();
        budgetList.addAll(newData);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final TextView amountText;
        private final TextView dateText;
        private final TextView categoryText;
        private final TextView frequencyText;

        public ViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.budgetTitle);
            amountText = itemView.findViewById(R.id.budgetAmount);
            dateText = itemView.findViewById(R.id.budgetDate);
            categoryText = itemView.findViewById(R.id.budgetCategory);
            frequencyText = itemView.findViewById(R.id.budgetFrequency);
        }

        public void bind(Budget budget) {
            titleText.setText(budget.getTitle());
            amountText.setText("$" + budget.getAmount());

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            String formattedDate = sdf.format(budget.getDate());
            dateText.setText(formattedDate);

            categoryText.setText(budget.getCategory());
            frequencyText.setText(budget.getFrequency().toString());
        }
    }
}
