package com.example.sprintproject.model;

public class ChatModel {
    public static final String USER_KEY = "user";
    public static final String BOT_KEY = "bot";

    private String message;
    private String sender;
    private String timestamp;
    private long createdAt;

    public ChatModel() {
    }

    public ChatModel(String message, String sender, String timestamp) {
        this.message = message;
        this.sender = sender;
        this.timestamp = timestamp;
        this.createdAt = System.currentTimeMillis();
    }

    public ChatModel(String message, String sender, String timestamp, long createdAt) {
        this.message = message;
        this.sender = sender;
        this.timestamp = timestamp;
        this.createdAt = createdAt;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public long getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
