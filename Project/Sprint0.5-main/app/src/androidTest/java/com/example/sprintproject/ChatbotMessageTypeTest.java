package com.example.sprintproject;

import static org.junit.Assert.assertEquals;

import com.example.sprintproject.model.ChatModel;
import com.example.sprintproject.view.ChatbotAdapter;

import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;

public class ChatbotMessageTypeTest {

    private ArrayList<ChatModel> testList;

    @Before
    public void setup() {
        testList = new ArrayList<>();
    }

    @Test
    public void testGetItemViewTypeUser() {
        testList.add(new ChatModel("Hello", "user", "1:00 AM"));
        ChatbotAdapter adapter = new ChatbotAdapter(testList);

        assertEquals(0, adapter.getItemViewType(0));
    }

    @Test
    public void testGetItemViewTypeBot() {
        testList.add(new ChatModel("Hello, World", "bot", "1:00 AM"));
        ChatbotAdapter adapter = new ChatbotAdapter(testList);

        assertEquals(1, adapter.getItemViewType(0));
    }

    @Test
    public void testGetItemViewTypeInvalid() {
        testList.add(new ChatModel("something", "Unknown", "10:02 AM"));
        ChatbotAdapter adapter = new ChatbotAdapter(testList);

        assertEquals(-1, adapter.getItemViewType(0));
    }

    @Test
    public void testGetItemViewTypeMultipleMessages() {
        testList.add(new ChatModel("Hello", "user", "10:00 AM"));
        testList.add(new ChatModel("Hi there!", "bot", "10:01 AM"));
        testList.add(new ChatModel("How are you?", "user", "10:02 AM"));
        testList.add(new ChatModel("I'm good.", " bot", "10:03 AM"));

        ChatbotAdapter adapter = new ChatbotAdapter(testList);

        assertEquals(0, adapter.getItemViewType(0));
        assertEquals(1, adapter.getItemViewType(1));
        assertEquals(0, adapter.getItemViewType(2));
    }
}
