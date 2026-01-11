package com.example.sprintproject.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.sprintproject.R;
import com.example.sprintproject.model.Expense;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    private final ArrayList<Expense> expenseList;

    public CustomAdapter(ArrayList<Expense> dataSet) {
        expenseList = dataSet;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.expense_box, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Expense expense = expenseList.get(position);
        viewHolder.bind(expense);
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public void updateData(List<Expense> newData) {
        expenseList.clear();
        expenseList.addAll(newData);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView amountText;
        private final TextView dateText;
        private final TextView categoryText;

        public ViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.expenseName);
            amountText = itemView.findViewById(R.id.expenseAmount);
            dateText = itemView.findViewById(R.id.expenseDate);
            categoryText = itemView.findViewById(R.id.expenseCategory);
        }

        public void bind(Expense expense) {
            nameText.setText(expense.getName());
            amountText.setText("$" + expense.getAmount());

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            String formattedDate = sdf.format(expense.getDate());
            dateText.setText(formattedDate);

            categoryText.setText(expense.getCategory());
        }
    }
}
