package com.example.sprintproject.view;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sprintproject.R;
import com.example.sprintproject.model.ChatModel;
import com.example.sprintproject.viewmodel.ChatbotViewModel;
import com.example.sprintproject.model.SuggestionModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class Chatbot extends AppCompatActivity {

    private EditText editText;
    private ChatbotViewModel vm;

    private final List<String> cachedChatIds = new ArrayList<>();

    private static final String DEFAULT_LABEL_1 = "Spending Summary";
    private static final String DEFAULT_LABEL_2 = "Save More";
    private static final String DEFAULT_LABEL_3 = "Unusual Activity";

    private static final String DEFAULT_PROMPT_1 = "Give me a summary of my recent spending.";
    private static final String DEFAULT_PROMPT_2 = "How can I save more money this month?";
    private static final String DEFAULT_PROMPT_3 = "Analyze my spending and show unusual activity.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chatbot);

        BottomNavigationView bottomNavigationBar = findViewById(R.id.bottomNavigationBar);
        vm = new ViewModelProvider(this).get(ChatbotViewModel.class);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            vm.initPersistence(uid);
        } else {
            Log.e("Chatbot", "No logged-in user; skipping initPersistence");
        }

        ArrayList<ChatModel> list = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.chatbot_recycler);
        ChatbotAdapter adapter = new ChatbotAdapter(list);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        vm.getNavModel().setUpNavigation(this, R.id.chatbot, bottomNavigationBar);
        vm.preloadFinancialData();

        vm.getChatHistory().observe(this, newChatList -> {
            if (newChatList != null) {
                int oldSize = list.size();
                int newSize = newChatList.size();
                list.clear();
                list.addAll(newChatList);
                if (newSize > oldSize) {
                    int newItemPosition = newSize - 1;
                    adapter.notifyItemInserted(newItemPosition);
                    recyclerView.scrollToPosition(newItemPosition);
                } else if (newSize == 0) {
                    adapter.notifyDataSetChanged();
                }
            }
        });

        vm.getChatIds().observe(this, ids -> {
            cachedChatIds.clear();
            if (ids != null) {
                cachedChatIds.addAll(ids);
            }
        });

        FloatingActionButton button = findViewById(R.id.sendButton);
        editText = findViewById(R.id.messageEditText);

        Button custom1 = findViewById(R.id.custom1);
        Button custom2 = findViewById(R.id.custom2);
        Button custom3 = findViewById(R.id.custom3);

        setupDefaults(custom1, custom2, custom3);

        vm.getSuggestions().observe(this, suggestions -> {
            if (suggestions == null || suggestions.isEmpty()) {
                return;
            }

            if (suggestions.size() > 0) {
                SuggestionModel s1 = suggestions.get(0);
                custom1.setText(s1.getLabel());
                custom1.setOnClickListener(v -> applySuggestion(s1.getPrompt()));
            }

            if (suggestions.size() > 1) {
                SuggestionModel s2 = suggestions.get(1);
                custom2.setText(s2.getLabel());
                custom2.setOnClickListener(v -> applySuggestion(s2.getPrompt()));
            }

            if (suggestions.size() > 2) {
                SuggestionModel s3 = suggestions.get(2);
                custom3.setText(s3.getLabel());
                custom3.setOnClickListener(v -> applySuggestion(s3.getPrompt()));
            }
        });



        button.setOnClickListener(v -> {
            String text = editText.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(Chatbot.this,
                        "Please enter your message.", Toast.LENGTH_SHORT).show();
                return;
            }
            showIncludePreviousDialogAndSend(text);
            editText.setText("");
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void showIncludePreviousDialogAndSend(String userMessage) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_chatbot); // using the unified layout

        Button btnNo = dialog.findViewById(R.id.btnIncludeNo);
        Button btnYes = dialog.findViewById(R.id.btnIncludeYes);
        Button btnSendWithContext = dialog.findViewById(R.id.btnSendWithContext);
        Button btnCancel = dialog.findViewById(R.id.btnCancelInclude);
        TextView tvExistingLabel = dialog.findViewById(R.id.tvExistingLabel);
        LinearLayout layoutSpinnerRow = dialog.findViewById(R.id.layoutSpinnerRow);
        Spinner spPreviousChats = dialog.findViewById(R.id.spPreviousChats);

        if (cachedChatIds.isEmpty()) {
            tvExistingLabel.setText("No previous chats available");
            btnSendWithContext.setEnabled(false);
        }

        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item,
                        cachedChatIds);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPreviousChats.setAdapter(arrayAdapter);

        btnNo.setOnClickListener(v -> {
            vm.sendMessage(userMessage);
            dialog.dismiss();
        });

        btnYes.setOnClickListener(v -> {
            if (cachedChatIds.isEmpty()) {
                Toast.makeText(this, "No previous chats to reference.", Toast.LENGTH_SHORT).show();
                return;
            }
            tvExistingLabel.setVisibility(View.VISIBLE);
            layoutSpinnerRow.setVisibility(View.VISIBLE);
        });

        btnSendWithContext.setOnClickListener(v -> {
            if (cachedChatIds.isEmpty()) {
                Toast.makeText(this, "No previous chats to reference.", Toast.LENGTH_SHORT).show();
                return;
            }
            int pos = spPreviousChats.getSelectedItemPosition();
            if (pos < 0 || pos >= cachedChatIds.size()) {
                Toast.makeText(this, "Please select a chat.", Toast.LENGTH_SHORT).show();
                return;
            }
            String refChatId = cachedChatIds.get(pos);
            vm.sendMessageWithReference(userMessage, refChatId);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.setCancelable(true);
        dialog.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("Chatbot", "onStop Called");
        vm.summarizeCurrentChat();
    }

    private void applySuggestion(String suggestion) {
        if (editText != null) {
            editText.setText(suggestion);
            editText.setSelection(suggestion.length());
            editText.requestFocus();
        }
    }

    private void setupDefaults(Button custom1, Button custom2, Button custom3) {
        custom1.setText(DEFAULT_LABEL_1);
        custom1.setOnClickListener(v -> applySuggestion(DEFAULT_PROMPT_1));

        custom2.setText(DEFAULT_LABEL_2);
        custom2.setOnClickListener(v -> applySuggestion(DEFAULT_PROMPT_2));

        custom3.setText(DEFAULT_LABEL_3);
        custom3.setOnClickListener(v -> applySuggestion(DEFAULT_PROMPT_3));
    }


}
