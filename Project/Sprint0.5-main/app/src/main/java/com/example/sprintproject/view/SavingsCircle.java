package com.example.sprintproject.view;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.sprintproject.model.DateModel;
import com.example.sprintproject.model.Frequency;
import com.example.sprintproject.model.SavingsCircleModel;
import com.example.sprintproject.viewmodel.SavingsCircleViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Map;

public class SavingsCircle extends AppCompatActivity {
    private Frequency selectedFrequency;
    private String email = FirebaseAuth.getInstance().getCurrentUser()
            .getEmail().toLowerCase(java.util.Locale.US);
    private SavingsCircleViewModel vm;

    private RecyclerView recyclerView;
    private BottomNavigationView bottomNavigationBar;
    private Button btnJoin;
    private Button btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_savingscircle);

        vm = new ViewModelProvider(this).get(SavingsCircleViewModel.class);

        initViews();
        SavingsCircleAdapter adapter = setupRecycler();
        setupBottomNav();
        applyWindowInsets();
        observeViewModel(adapter);
        setupButtonListeners();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.scRecyclerView);
        bottomNavigationBar = findViewById(R.id.bottomNavigationBar);
        btnJoin = findViewById(R.id.btnJoinGroup);
        btnCreate = findViewById(R.id.btnCreateGroup);
    }

    private SavingsCircleAdapter setupRecycler() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        SavingsCircleAdapter adapter = new SavingsCircleAdapter(
                new ArrayList<>(),
                email,
                new SavingsCircleAdapter.OnCircleActionListener() {
                    @Override
                    public void onDelete(SavingsCircleModel sc) {
                        vm.deleteSavingsCircle(sc);
                        Toast.makeText(SavingsCircle.this,
                                "Deleting " + sc.getGroupName(),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onInvite(SavingsCircleModel sc) {
                        showInviteDialog(sc);
                    }
                },
                vm,
                this
        );
        recyclerView.setAdapter(adapter);
        return adapter;
    }


    private void setupBottomNav() {
        vm.getNavModel().setUpNavigation(this, R.id.savings_Circle, bottomNavigationBar);
    }

    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.savingscircle), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void observeViewModel(SavingsCircleAdapter adapter) {
        vm.getAllSavingsCircles().observe(this, sc -> {
            if (sc != null) {
                adapter.updateData(sc);
            }
        });

        vm.getSavingsCircleError().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                showValidationErrors(msg);
            }
        });
    }

    private void setupButtonListeners() {
        btnJoin.setOnClickListener(v -> showJoinDialog());
        btnCreate.setOnClickListener(v -> showCreateGroupDialog());
    }


    private void showInviteDialog(SavingsCircleModel sc) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_invite, null);
        Dialog dialog = new Dialog(this);
        dialog.setContentView(dialogView);
        styleDialogWindow(dialog);

        EditText etInviteEmail = dialog.findViewById(R.id.etInviteEmail);
        Button btnSend = dialog.findViewById(R.id.btnSendInvite);
        Button btnCancel = dialog.findViewById(R.id.btnCancelInvite);

        btnCancel.setOnClickListener(x -> dialog.dismiss());
        btnSend.setOnClickListener(x -> {
            String invitedEmail = etInviteEmail.getText().toString().trim();
            if (invitedEmail.isEmpty()) {
                Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show();
                return;
            }
            vm.sendInvite(sc, invitedEmail);
            Toast.makeText(this, "Invite sent to " + invitedEmail, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showJoinDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.join_savings_circle_form, null);
        Dialog dialog = new Dialog(this);
        dialog.setContentView(dialogView);
        styleDialogWindow(dialog);

        RecyclerView rv = dialog.findViewById(R.id.joinRecylerView);
        rv.setLayoutManager(new LinearLayoutManager(this));

        IncomingInvitesAdapter invitesAdapter =
                new IncomingInvitesAdapter(new IncomingInvitesAdapter.InviteListener() {
                    @Override
                    public void onAccept(SavingsCircleModel sc) {
                        vm.acceptInvite(sc, email);
                    }

                    @Override
                    public void onDecline(SavingsCircleModel sc) {
                        vm.declineInvite(sc, email);
                    }
                });
        rv.setAdapter(invitesAdapter);

        vm.getIncomingInvites().observe(this, invitesAdapter::submit);

        dialog.show();
    }

    private void showCreateGroupDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.savings_circle_form, null);
        Dialog dialog = new Dialog(this);
        dialog.setContentView(dialogView);
        styleDialogWindow(dialog);

        EditText etGroupName = dialog.findViewById(R.id.etGroupName);
        EditText etChallengeTitle = dialog.findViewById(R.id.etChallengeTitle);
        EditText etGoalAmount = dialog.findViewById(R.id.etGoalAmount);
        EditText etNotes = dialog.findViewById(R.id.etNotes);

        Button btnWeekly = dialog.findViewById(R.id.btnWeekly);
        Button btnMonthly = dialog.findViewById(R.id.btnMonthly);
        Button btnSave = dialog.findViewById(R.id.btnSave);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        btnWeekly.setOnClickListener(x -> selectedFrequency = Frequency.WEEKLY);
        btnMonthly.setOnClickListener(x -> selectedFrequency = Frequency.MONTHLY);
        btnCancel.setOnClickListener(x -> dialog.dismiss());

        btnSave.setOnClickListener(x -> {
            String groupName = etGroupName.getText().toString().trim();
            String challengeTitle = etChallengeTitle.getText().toString().trim();
            String goalAmountStr = etGoalAmount.getText().toString().trim();
            String notes = etNotes.getText().toString().trim();

            double goalAmount = 0;
            boolean amountParsed = true;
            try {
                goalAmount = Double.parseDouble(goalAmountStr);
            } catch (NumberFormatException e) {
                amountParsed = false;
            }

            SavingsCircleModel newSC = new SavingsCircleModel(
                    groupName, challengeTitle, goalAmount, notes,
                    selectedFrequency, DateModel.getCurrentDate().getValue()
            );

            StringBuilder msg = new StringBuilder();
            if (!amountParsed && !goalAmountStr.isEmpty()) {
                msg.append("• Amount must be a number\n");
            }
            for (Map.Entry<String, String> kv : newSC.validate().entrySet()) {
                msg.append("• ").append(kv.getValue()).append("\n");
            }

            if (msg.length() > 0) {
                showValidationErrors(msg);
                return;
            }

            vm.addSavingsCircle(newSC);
            Toast.makeText(this, "Savings Circle created", Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void styleDialogWindow(Dialog dialog) {
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int w = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            window.setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void showValidationErrors(CharSequence fullMsg) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Errors:")
                .setMessage(fullMsg)
                .setPositiveButton("OK", null)
                .show();
    }
}
