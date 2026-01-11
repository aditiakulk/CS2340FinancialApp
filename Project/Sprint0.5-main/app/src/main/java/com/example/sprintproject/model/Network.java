package com.example.sprintproject.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Network implements ChatService {

    private static final String MODEL = "model";
    private static final String LLAMA32 = "llama3.2";
    private static final String STREAM = "stream";
    private static final String CONTENT = "content";
    private static final String MESSAGES = "messages";
    private static final String HTTP = "HTTP ";
    private static final String MESSAGE = "message";
    private static final String ERROR = "Error: ";

    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    // Emulator → Laptop mapping
    private static final String BASEURL = "http://10.0.2.2:11434/api/chat";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    public void chat(String userMessage, Callback cb) {
        new Thread(() -> {
            try {
                // Build JSON payload
                JSONObject json = new JSONObject();
                json.put(MODEL, LLAMA32);
                json.put(STREAM, false);

                JSONArray messages = new JSONArray();
                JSONObject msg = new JSONObject();
                msg.put("role", "user");
                msg.put(CONTENT, userMessage);
                messages.put(msg);
                json.put(MESSAGES, messages);

                RequestBody body = RequestBody.create(json.toString(), JSON);

                Request request = new Request.Builder()
                        .url(BASEURL)
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();

                if (!response.isSuccessful()) {
                    cb.onError(HTTP + response.code());
                    return;
                }

                String responseText = response.body().string();

                // Extract reply
                JSONObject root = new JSONObject(responseText);
                JSONObject messageObj = root
                        .getJSONObject(MESSAGE);

                String reply = messageObj.getString(CONTENT);

                cb.onSuccess(reply);

            } catch (Exception e) {
                cb.onError(ERROR + e.getMessage());
            }
        }).start();
    }

    public void generateTitle(String firstUserMessage, TitleCallback cb) {
        new Thread(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put(MODEL, LLAMA32);
                json.put(STREAM, false);

                JSONArray messages = new JSONArray();

                // System message: tell the model what we want
                JSONObject sys = new JSONObject();
                sys.put("role", "system");
                sys.put(CONTENT,
                        "You generate concise titles for budgeting conversations. "
                                + "Return ONLY a short title, max 6 words, no quotes.");
                messages.put(sys);

                // User message: give it the first user prompt
                JSONObject user = new JSONObject();
                user.put("role", "user");
                user.put(CONTENT,
                        "Create a title for this budgeting chat based on this message: "
                                + firstUserMessage);
                messages.put(user);

                json.put(MESSAGES, messages);

                RequestBody body = RequestBody.create(json.toString(), JSON);

                Request request = new Request.Builder()
                        .url(BASEURL)
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();

                if (!response.isSuccessful()) {
                    cb.onError(HTTP + response.code());
                    return;
                }

                String responseText = response.body().string();

                JSONObject root = new JSONObject(responseText);
                JSONObject messageObj = root.getJSONObject(MESSAGE);
                String title = messageObj.getString(CONTENT).trim();

                title = title.replace("\n", " ").replace("\"", "").trim();

                cb.onSuccess(title);

            } catch (Exception e) {
                cb.onError(ERROR + e.getMessage());
            }
        }).start();
    }

    public void summarizeConversation(String convoText, SummaryCallback cb) {
        new Thread(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put(MODEL, LLAMA32);
                json.put(STREAM, false);

                JSONArray messages = new JSONArray();

                JSONObject sys = new JSONObject();
                sys.put("role", "system");
                sys.put(CONTENT,
                        "You summarize budgeting conversations. "
                                + "Return a concise summary (3-6 sentences).");
                messages.put(sys);

                JSONObject user = new JSONObject();
                user.put("role", "user");
                user.put(CONTENT, convoText);
                messages.put(user);

                json.put(MESSAGES, messages);

                RequestBody body = RequestBody.create(json.toString(), JSON);

                Request request = new Request.Builder()
                        .url(BASEURL)
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    cb.onError(HTTP + response.code());
                    return;
                }

                String responseText = response.body().string();
                JSONObject root = new JSONObject(responseText);
                JSONObject messageObj = root.getJSONObject(MESSAGE);
                String summary = messageObj.getString(CONTENT).trim();
                cb.onSuccess(summary);
            } catch (Exception e) {
                cb.onError(e.getMessage());
            }
        }).start();
    }

    public void generateSuggestions(String context, SuggestionsCallback cb) {
        new Thread(() -> {
            try {
                String instruction =
                        "You are a financial assistant in a budgeting app. "
                            + "You will receive a summary of the user’s recent financial activity"
                            + " and chat history. Based on that, generate exactly 3 helpful, "
                            + "budget-focused suggestions.\n\n"
                            + "Each suggestion must have:\n"
                            + "- 'label': a very short human-friendly"
                            + "button label, 3 words or fewer.\n"
                            + "- 'prompt': a longer natural language prompt that the app can send "
                            + "back to you to actually perform that analysis.\n\n"
                            + "Return ONLY valid JSON in this format, with no extra text:\n"
                            + "{ \"suggestions\": [ "
                            + "{\"label\": \"...\", \"prompt\": \"...\"},"
                            + "{\"label\": \"...\", \"prompt\": \"...\"},"
                            + "{\"label\": \"...\", \"prompt\": \"...\"}"
                            + "] }\n\n"
                            + "User financial context:\n" + context;

                JSONObject json = new JSONObject();
                json.put(MODEL, LLAMA32);
                json.put(STREAM, false);

                JSONArray messages = new JSONArray();
                JSONObject msg = new JSONObject();
                msg.put("role", "user");
                msg.put(CONTENT, instruction);
                messages.put(msg);
                json.put(MESSAGES, messages);

                RequestBody body = RequestBody.create(json.toString(), JSON);

                Request request = new Request.Builder()
                        .url(BASEURL)
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();

                if (!response.isSuccessful()) {
                    cb.onError(HTTP + response.code());
                    return;
                }

                String responseText = response.body().string();

                JSONObject root = new JSONObject(responseText);
                JSONObject messageObj = root.getJSONObject(MESSAGE);
                String content = messageObj.getString(CONTENT);

                JSONObject contentJson = new JSONObject(content);
                JSONArray suggestionsArray = contentJson.getJSONArray("suggestions");

                java.util.List<com.example.sprintproject.model.SuggestionModel> result =
                        new java.util.ArrayList<>();

                for (int i = 0; i < suggestionsArray.length(); i++) {
                    JSONObject sObj = suggestionsArray.getJSONObject(i);
                    String label = sObj.optString("label", "");
                    String prompt = sObj.optString("prompt", "");
                    if (!label.isEmpty() && !prompt.isEmpty()) {
                        result.add(new com.example.sprintproject.model.SuggestionModel(label,
                                prompt));
                    }
                }

                if (result.isEmpty()) {
                    cb.onError("No valid suggestions returned");
                } else {
                    cb.onSuccess(result);
                }

            } catch (Exception e) {
                cb.onError(ERROR + e.getMessage());
            }
        }).start();
    }

    public interface Callback {
        void onSuccess(String reply);
        void onError(String error);
    }

    public interface TitleCallback {
        void onSuccess(String title);
        void onError(String error);
    }

    public interface SummaryCallback {
        void onSuccess(String summary);
        void onError(String error);
    }

    public interface SuggestionsCallback {
        void onSuccess(java.util.List<com.example.sprintproject.model.SuggestionModel> suggestions);
        void onError(String error);
    }
}

