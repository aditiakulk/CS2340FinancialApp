package com.example.sprintproject.model;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatRepo {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "ChatRepo";
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_CHARTS = "chats";

    public void addMessage(String uid, String chatId, ChatModel message) {
        Log.d(TAG, "addMessage: uid=" + uid + ", chatId=" + chatId
                + ", msg=" + message.getMessage());

        db.collection(COLLECTION_USERS).document(uid)
                .collection(COLLECTION_CHARTS).document(chatId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(ref ->
                        Log.d(TAG, "Message stored at: " + ref.getPath()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to store message", e));
    }

    public void getChatIds(String uid, ChatIdsCallback cb) {
        db.collection(COLLECTION_USERS).document(uid)
                .collection(COLLECTION_CHARTS)
                .get()
                .addOnSuccessListener(snap -> {
                    List<String> ids = new ArrayList<>();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        ids.add(doc.getId());
                    }
                    cb.onSuccess(ids);
                })
                .addOnFailureListener(cb::onError);
    }

    public void fetchMessages(String uid, String chatId, MessagesCallback cb) {
        db.collection(COLLECTION_USERS).document(uid)
                .collection(COLLECTION_CHARTS).document(chatId)
                .collection("messages")
                .orderBy("createdAt")
                .get()
                .addOnSuccessListener(snap -> {
                    List<ChatModel> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        ChatModel msg = doc.toObject(ChatModel.class);
                        if (msg != null) {
                            list.add(msg);
                        }
                    }
                    cb.onSuccess(list);
                })
                .addOnFailureListener(cb::onError);
    }

    public void createChatWithTitle(String uid, String title, ChatIdCallback cb) {
        String chatId = title.trim();
        if (chatId.isEmpty()) {
            cb.onError(new IllegalArgumentException("Chat title cannot be empty"));
            return;
        }

        long now = System.currentTimeMillis();
        HashMap<String, Object> data = new HashMap<>();
        data.put("createdAt", now);

        db.collection(COLLECTION_USERS).document(uid)
                .collection(COLLECTION_CHARTS).document(chatId)   // 👈 doc ID = title
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Created chat doc with title: " + chatId);
                    cb.onSuccess(chatId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create chat doc", e);
                    cb.onError(e);
                });
    }

    public void saveSummary(String uid, String chatId, String summary) {
        db.collection(COLLECTION_USERS).document(uid)
                .collection(COLLECTION_CHARTS).document(chatId)
                .update("summary", summary)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Summary saved for chat " + chatId))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to save summary", e));
    }

    public void getSummary(String uid, String chatId, SummaryCallback cb) {
        db.collection(COLLECTION_USERS).document(uid)
                .collection(COLLECTION_CHARTS).document(chatId)
                .get()
                .addOnSuccessListener(doc -> {
                    String summary = doc.getString("summary");
                    cb.onSuccess(summary == null ? "" : summary);
                })
                .addOnFailureListener(cb::onError);
    }

    public interface ChatIdsCallback {
        void onSuccess(List<String> chatIds);
        void onError(Exception e);
    }

    public interface MessagesCallback {
        void onSuccess(List<ChatModel> messages);
        void onError(Exception e);
    }

    public interface ChatIdCallback {
        void onSuccess(String chatId);
        void onError(Exception e);
    }

    public interface SummaryCallback {
        void onSuccess(String summary);
        void onError(Exception e);
    }
}
