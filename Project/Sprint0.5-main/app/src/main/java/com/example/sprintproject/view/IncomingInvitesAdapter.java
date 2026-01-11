package com.example.sprintproject.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sprintproject.R;
import com.example.sprintproject.model.SavingsCircleModel;
import java.util.ArrayList;
import java.util.List;

public class IncomingInvitesAdapter extends RecyclerView.Adapter<IncomingInvitesAdapter.VH> {

    private final ArrayList<SavingsCircleModel> items = new ArrayList<>();
    private final InviteListener listener;

    public IncomingInvitesAdapter(InviteListener listener) {
        this.listener = listener;
    }

    public void submit(List<SavingsCircleModel> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @Override public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.incoming_savings_circle_box, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(VH h, int pos) {
        SavingsCircleModel sc = items.get(pos);
        h.name.setText(sc.getGroupName());
        h.from.setText("From: " + sc.getCreatorEmail());
        h.accept.setOnClickListener(v -> listener.onAccept(sc));
        h.decline.setOnClickListener(v -> listener.onDecline(sc));
    }

    @Override public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView from;
        private final Button accept;
        private final Button decline;
        VH(View item) {
            super(item);
            name = item.findViewById(R.id.tvGroupName);
            from = item.findViewById(R.id.tvFrom);
            accept = item.findViewById(R.id.btnAccept);
            decline = item.findViewById(R.id.btnDecline);
        }
    }

    public interface InviteListener {
        void onAccept(SavingsCircleModel sc);
        void onDecline(SavingsCircleModel sc);
    }
}