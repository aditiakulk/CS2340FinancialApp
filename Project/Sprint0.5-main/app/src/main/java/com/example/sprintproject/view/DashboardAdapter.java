package com.example.sprintproject.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.sprintproject.R;
import com.example.sprintproject.model.AllCategories;

import java.util.ArrayList;
import java.util.List;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ViewHolder> {
    private List<AllCategories> data = new ArrayList<>();

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dashboard_box, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void updateData(List<AllCategories> newData) {
        data.clear();
        data.addAll(newData);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView categoryText;
        private TextView totalText;
        private TextView budgetText;
        private TextView remainingText;

        public ViewHolder(View itemView) {
            super(itemView);
            categoryText = itemView.findViewById(R.id.text_category);
            totalText = itemView.findViewById(R.id.text_expenses);
            budgetText = itemView.findViewById(R.id.text_budget);
            remainingText = itemView.findViewById(R.id.text_remaining);
        }

        public void bind(AllCategories c) {
            categoryText.setText(c.getCategory());
            totalText.setText(String.format("Spent: $%.2f", c.getTotalSpent()));
            budgetText.setText(String.format("Budget: $%.2f", c.getBudget()));
            remainingText.setText(String.format("Remaining: $%.2f", c.getRemaining()));
        }
    }
}
