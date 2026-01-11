package com.example.sprintproject.model;

public interface ChatService {
    void chat(String userMessage, Network.Callback cb);
    void generateTitle(String firstUserMessage, Network.TitleCallback cb);
    void summarizeConversation(String convoText, Network.SummaryCallback cb);
    void generateSuggestions(String context, Network.SuggestionsCallback cb);
}
