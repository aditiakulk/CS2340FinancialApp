package com.example.sprintproject.viewmodel;

import static com.example.sprintproject.model.ChatModel.BOT_KEY;
import static com.example.sprintproject.model.ChatModel.USER_KEY;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sprintproject.model.Budget;
import com.example.sprintproject.model.ChatModel;
import com.example.sprintproject.model.ChatService;
import com.example.sprintproject.model.Expense;
import com.example.sprintproject.model.FinanceContextVisitor;
import com.example.sprintproject.model.FirestoreModel;
import com.example.sprintproject.model.LoggingChatServiceDecorator;
import com.example.sprintproject.model.NavModel;
import com.example.sprintproject.model.Network;
import com.example.sprintproject.model.SuggestionModel;
import com.example.sprintproject.model.ChatRepo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class ChatbotViewModel extends ViewModel {
    private NavModel navModel = new NavModel();

    public NavModel getNavModel() {
        return navModel;
    }

    private final MutableLiveData<List<ChatModel>> chatHistoryData
            = new MutableLiveData<>(new ArrayList<>());
    private LiveData<List<ChatModel>> chatHistory = chatHistoryData;

    public LiveData<List<ChatModel>> getChatHistory() {
        return chatHistory;
    }

    public LiveData<List<String>> getChatIds() {
        return chatIds;
    }

    public LiveData<List<SuggestionModel>> getSuggestions() {
        return suggestions;
    }

    private final MutableLiveData<List<String>> chatIdsData =
            new MutableLiveData<>(new ArrayList<>());
    private LiveData<List<String>> chatIds = chatIdsData;

    private final MutableLiveData<List<SuggestionModel>> suggestionsData =
            new MutableLiveData<>(new ArrayList<>());
    private LiveData<List<SuggestionModel>> suggestions = suggestionsData;


    private final ChatService nw = new LoggingChatServiceDecorator(new Network());
    private final SimpleDateFormat sdf
            = new SimpleDateFormat("hh:mm:ss a  MM/dd/yyyy", Locale.getDefault());


    private final FirestoreModel fsm = FirestoreModel.getInstance();
    private List<Expense> cachedExpenses = new ArrayList<>();
    private List<Budget> cachedBudgets = new ArrayList<>();
    private boolean budgetsLoaded = false;
    private boolean expensesLoaded = false;
    private static final String TAG = "ChatbotVM";

    private final ChatRepo repo = new ChatRepo();
    private String uid;
    private String chatId;

    private String getCurrentTime() {
        return sdf.format(new Date());
    }

    public void initPersistence(String uid) {
        this.uid = uid;
        loadChatIds();
    }

    private void loadChatIds() {
        if (uid == null) {
            return;
        }

        repo.getChatIds(uid, new ChatRepo.ChatIdsCallback() {
            @Override
            public void onSuccess(List<String> ids) {
                chatIdsData.postValue(ids);
            }

            @Override
            public void onError(Exception e) {
                chatIdsData.postValue(new ArrayList<>());
            }
        });
    }

    public void startNewChat() {
        this.chatId = null;
        chatHistoryData.postValue(new ArrayList<>());
    }

    public void openExistingChat(String selectedChatId) {
        if (uid == null) {
            return;
        }

        this.chatId = selectedChatId;
        repo.fetchMessages(uid, selectedChatId, new ChatRepo.MessagesCallback() {
            @Override
            public void onSuccess(List<ChatModel> messages) {
                chatHistoryData.postValue(messages);
            }

            @Override
            public void onError(Exception e) {
                chatHistoryData.postValue(new ArrayList<>());
            }
        });
    }

    public void sendMessage(String userMessage) {
        String financeContext = buildFinanceContext();

        String finalPrompt = financeContext
                        + "\n\nUsing the above budgets and expenses, answer this question:\n"
                        + userMessage;

        sendMessageInternal(userMessage, finalPrompt);
    }

    public void sendMessageInternal(String userVisibleMessage, String modelPrompt) {
        boolean isNewChat = (chatId == null);
        addMessage(userVisibleMessage, USER_KEY);

        if (isNewChat && uid != null) {
            nw.generateTitle(userVisibleMessage, new Network.TitleCallback() {
                @Override
                public void onSuccess(String title) {
                    repo.createChatWithTitle(uid, title, new ChatRepo.ChatIdCallback() {
                        @Override
                        public void onSuccess(String newChatId) {
                            chatId = newChatId;
                            List<ChatModel> history = chatHistoryData.getValue();
                            if (history != null) {
                                for (ChatModel msg : history) {
                                    repo.addMessage(uid, chatId, msg);
                                }
                            }
                            loadChatIds();
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e(TAG, "Failed to create chat or save initial messages", e);
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Failed to generate chat title: " + error);
                }
            });
        }

        nw.chat(modelPrompt, new Network.Callback() {
            @Override
            public void onSuccess(String reply) {
                addMessage(reply, BOT_KEY);
            }

            @Override
            public void onError(String error) {
                addMessage("Error: " + error, BOT_KEY);
            }
        });
    }

    private void addMessage(String message, String key) {
        List<ChatModel> currentList = chatHistoryData.getValue() != null
                ? new ArrayList<>(chatHistoryData.getValue())
                : new ArrayList<>();
        ChatModel newMsg = new ChatModel(message, key, getCurrentTime());
        currentList.add(newMsg);
        chatHistoryData.postValue(currentList);

        if (uid != null && chatId != null) {
            repo.addMessage(uid, chatId, newMsg);
        }

        if (BOT_KEY.equals(key)) {
            maybeRefreshSuggestions();
        }
    }

    public void sendMessageWithReference(String userMessage, String refChatId) {
        if (uid == null || refChatId == null || refChatId.isEmpty()) {
            sendMessage(userMessage);
            return;
        }

        repo.getSummary(uid, refChatId, new ChatRepo.SummaryCallback() {
            @Override
            public void onSuccess(String summary) {
                String financeContext = buildFinanceContext();

                StringBuilder prompt = new StringBuilder();
                prompt.append(financeContext).append("\n\n");

                if (summary != null && !summary.isEmpty()) {
                    prompt.append("Here is a summary of a previous budgeting conversation:\n")
                            .append(summary)
                            .append("\n\n");
                }

                prompt.append("Using the budgeting data and the previous conversation summary, ")
                        .append("answer this new question:\n")
                        .append(userMessage);

                sendMessageInternal(userMessage, prompt.toString());
            }

            @Override
            public void onError(Exception e) {
                sendMessage(userMessage);
            }
        });
    }

    private String buildConversationText(List<ChatModel> history) {
        StringBuilder sb = new StringBuilder();
        for (ChatModel msg : history) {
            String role = USER_KEY.equals(msg.getSender()) ? "User" : "Assistant";
            sb.append(role).append(": ")
                    .append(msg.getMessage())
                    .append("\n");
        }
        return sb.toString();
    }

    public void summarizeCurrentChat() {
        if (uid == null || chatId == null) {
            return;
        }

        List<ChatModel> history = chatHistoryData.getValue();
        if (history == null || history.isEmpty()) {
            return;
        }

        String convoText = buildConversationText(history);

        nw.summarizeConversation(convoText, new Network.SummaryCallback() {
            @Override
            public void onSuccess(String summary) {
                repo.saveSummary(uid, chatId, summary);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Summary failed: " + error);
            }
        });
    }

    public void preloadFinancialData() {
        fsm.getBudgets().observeForever(budgets -> {
            if (budgets != null) {
                cachedBudgets = budgets;
            } else {
                cachedBudgets = new ArrayList<>();
            }
            budgetsLoaded = true;
            maybeRefreshSuggestions();
        });

        fsm.getExpenses().observeForever(expenses -> {
            if (expenses != null) {
                cachedExpenses = expenses;
            } else {
                cachedExpenses = new ArrayList<>();
            }
            expensesLoaded = true;
            maybeRefreshSuggestions();
        });
    }

    private String buildFinanceContext() {
        FinanceContextVisitor visitor = new FinanceContextVisitor();

        for (Budget b : cachedBudgets) {
            if (b != null) {
                b.accept(visitor);
            }
        }

        int limit = Math.min(10, cachedExpenses.size());
        for (int i = 0; i < limit; i++) {
            Expense e = cachedExpenses.get(i);
            if (e != null) {
                e.accept(visitor);
            }
        }

        return visitor.buildResult();

    }

    private void maybeRefreshSuggestions() {
        if (!budgetsLoaded || !expensesLoaded) {
            return;
        }

        String context = buildFinanceContext();

        nw.generateSuggestions(context, new Network.SuggestionsCallback() {
            @Override
            public void onSuccess(List<SuggestionModel> list) {
                suggestionsData.postValue(list);
            }

            @Override
            public void onError(String error) {
                suggestionsData.postValue(new ArrayList<>());
            }
        });
    }
}