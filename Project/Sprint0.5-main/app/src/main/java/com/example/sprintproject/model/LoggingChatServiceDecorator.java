package com.example.sprintproject.model;

import android.util.Log;

public class LoggingChatServiceDecorator implements ChatService {

    private static final String TAG = "LoggingChatService";
    private final ChatService delegate;

    public LoggingChatServiceDecorator(ChatService delegate) {
        this.delegate = delegate;
    }

    @Override
    public void chat(String userMessage, Network.Callback cb) {
        Log.d(TAG, "chat() userMessage=" + userMessage);
        delegate.chat(userMessage, cb);
    }

    @Override
    public void generateTitle(String firstUserMessage, Network.TitleCallback cb) {
        Log.d(TAG, "generateTitle() firstMessage=" + firstUserMessage);
        delegate.generateTitle(firstUserMessage, cb);
    }

    @Override
    public void summarizeConversation(String convoText, Network.SummaryCallback cb) {
        Log.d(TAG, "summarizeConversation() length=" + convoText.length());
        delegate.summarizeConversation(convoText, cb);
    }

    @Override
    public void generateSuggestions(String context, Network.SuggestionsCallback cb) {
        Log.d(TAG, "generateSuggestions() contextLength=" + context.length());
        delegate.generateSuggestions(context, cb);
    }
}