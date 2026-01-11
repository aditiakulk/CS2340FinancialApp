package com.example.sprintproject.view;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sprintproject.R;
import com.example.sprintproject.model.FirestoreModel;
import com.example.sprintproject.model.SavingsCircleModel;
import com.example.sprintproject.viewmodel.SavingsCircleViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SavingsCircleAdapter extends RecyclerView.Adapter<SavingsCircleAdapter.ViewHolder> {

    private final List<SavingsCircleModel> savingsCircleList;
    private final String currentUserEmail;
    private final OnCircleActionListener listener;
    private final SavingsCircleViewModel vm;
    private final Set<String> expandedCircleIds = new HashSet<>();
    private final LifecycleOwner lifecycleOwner;

    public SavingsCircleAdapter(List<SavingsCircleModel> dataSet, String currentUserEmail,
                                OnCircleActionListener listener, SavingsCircleViewModel vm,
                                LifecycleOwner lifecycleOwner) {
        this.savingsCircleList = dataSet;
        this.currentUserEmail = currentUserEmail;
        this.listener = listener;
        this.vm = vm;
        this.lifecycleOwner = lifecycleOwner;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.group_box, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SavingsCircleAdapter.ViewHolder viewHolder, int position) {
        SavingsCircleModel sc = savingsCircleList.get(position);
        viewHolder.bind(sc, currentUserEmail, listener);

        LinearLayout infosList = viewHolder.itemView.findViewById(R.id.infoList);

        boolean isExpanded = expandedCircleIds.contains(sc.getId());
        infosList.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        if (isExpanded) {
            populateMembers(infosList, sc);
        }

        viewHolder.itemView.setOnClickListener(v -> {
            if (infosList.getVisibility() == View.VISIBLE) {
                infosList.setVisibility(View.GONE);
                expandedCircleIds.remove(sc.getId());
            } else {
                infosList.setVisibility(View.VISIBLE);
                expandedCircleIds.add(sc.getId());
                populateMembers(infosList, sc);
            }
        });

        LiveData<Double> liveData = vm.getCircleTotal(sc.getCreatorUid(), sc.getId());

        if (viewHolder.circleObserver != null && viewHolder.lastLiveData != null) {
            viewHolder.lastLiveData.removeObserver(viewHolder.circleObserver);
        }

        vm.trackSavingsCircleTotal(sc.getCreatorUid(), sc.getId());
        viewHolder.lastLiveData = liveData;

        double currentAmount = sc.getCurrentAmount();
        if (currentAmount >= sc.getGoalAmount()) {
            viewHolder.groupName.setTextColor(Color.GREEN);
        } else {
            viewHolder.groupName.setTextColor(Color.BLACK);
        }

        viewHolder.circleObserver = total -> {
            if (total != null && total >= sc.getGoalAmount()) {
                viewHolder.groupName.setTextColor(Color.GREEN);
            } else {
                viewHolder.groupName.setTextColor(Color.BLACK);
            }
        };

        liveData.observe(lifecycleOwner, viewHolder.circleObserver);
    }

    private void populateMembers(LinearLayout infosList, SavingsCircleModel sc) {
        if (infosList.getChildCount() > 1) {
            infosList.removeViews(1, infosList.getChildCount() - 1);
        }

        List<String> emails = sc.getMemberEmails() != null
                ? sc.getMemberEmails() : new ArrayList<>();
        List<String> uids   = sc.getMemberUids()   != null
                ? sc.getMemberUids()   : new ArrayList<>();
        java.util.Map<String, Date> joinAt = sc.getMemberJoinAt() != null
                ? sc.getMemberJoinAt() : java.util.Collections.emptyMap();

        LayoutInflater inflater = LayoutInflater.from(infosList.getContext());
        FirestoreModel fsm = FirestoreModel.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

        View creatorRow = inflater.inflate(R.layout.member_row, infosList, false);
        TextView creatorName  = creatorRow.findViewById(R.id.memberNameText);
        TextView creatorMoney = creatorRow.findViewById(R.id.moneySpentText);
        TextView creatorDate  = creatorRow.findViewById(R.id.dateText);

        String creatorEmailLower = sc.getCreatorEmail() != null
                ? sc.getCreatorEmail().toLowerCase() : "";
        Date creatorStart = joinAt.getOrDefault(creatorEmailLower, sc.getStartDate());
        Date[] cWin = SavingsCircleViewModel.challengeWindow(creatorStart, sc.getFrequency());

        creatorName.setText(sc.getCreatorEmail());
        creatorMoney.setText("Loading...");
        creatorDate.setText(sdf.format(cWin[0]) + " - " + sdf.format(cWin[1]));
        infosList.addView(creatorRow);

        fsm.getExpensesForSavingsCircle(sc.getCreatorUid(), sc.getId(), cWin[0], cWin[1],
                (memberUid, total) -> creatorMoney.post(() ->
                        creatorMoney.setText(String.format("$%.2f / $%.2f",
                                total, sc.getGoalAmount()))));

        for (int i = 1; i < emails.size(); i++) {
            String email = emails.get(i);
            String uid   = (i < uids.size()) ? uids.get(i) : null;
            String emailLower = email != null ? email.toLowerCase() : "";

            View row = inflater.inflate(R.layout.member_row, infosList, false);
            TextView name  = row.findViewById(R.id.memberNameText);
            TextView money = row.findViewById(R.id.moneySpentText);
            TextView dates = row.findViewById(R.id.dateText);

            Date start = joinAt.getOrDefault(emailLower, sc.getStartDate()); // member-specific
            Date[] dateRange = SavingsCircleViewModel.challengeWindow(start, sc.getFrequency());

            name.setText(email != null ? email : "Unknown");
            money.setText("Loading...");
            dates.setText(sdf.format(dateRange[0]) + " - " + sdf.format(cWin[1]));
            infosList.addView(row);

            if (uid != null && !uid.isEmpty()) {
                fsm.getExpensesForSavingsCircle(uid, sc.getId(), dateRange[0], cWin[1],
                        (memberUid, total) -> money.post(() ->
                                money.setText(String.format("$%.2f / $%.2f",
                                        total, sc.getGoalAmount()))));
            } else {
                money.setText(String.format("$0.00 / $%.2f", sc.getGoalAmount()));
            }
        }
    }

    @Override
    public int getItemCount() {
        return savingsCircleList.size();
    }

    public void updateData(List<SavingsCircleModel> newData) {
        Set<String> newIds = new HashSet<>();
        for (SavingsCircleModel sc : newData) {
            newIds.add(sc.getId());
        }
        expandedCircleIds.removeIf(id -> !newIds.contains(id));

        savingsCircleList.clear();
        savingsCircleList.addAll(newData);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView groupName;
        private final Button deleteButton;
        private final Button inviteButton;
        private Observer<Double> circleObserver;
        private LiveData<Double> lastLiveData;

        public ViewHolder(View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.groupNameText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            inviteButton = itemView.findViewById(R.id.inviteButton);
        }

        public void bind(SavingsCircleModel sc, String currentUserEmail,
                         OnCircleActionListener listener) {
            groupName.setText(sc.getGroupName());

            if (sc.getCreatorEmail().equals(currentUserEmail)) {
                deleteButton.setVisibility(View.VISIBLE);
                inviteButton.setVisibility(View.VISIBLE);
            } else {
                deleteButton.setVisibility(View.GONE);
                inviteButton.setVisibility(View.GONE);
            }

            deleteButton.setOnClickListener(v -> listener.onDelete(sc));
            inviteButton.setOnClickListener(v -> listener.onInvite(sc));
        }
    }

    public interface OnCircleActionListener {
        void onDelete(SavingsCircleModel sc);
        void onInvite(SavingsCircleModel sc);
    }
}
