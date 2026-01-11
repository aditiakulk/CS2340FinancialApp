package com.example.sprintproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.example.sprintproject.model.SuggestionModel;

import org.junit.Test;

public class SuggestionModelTest {
    @Test
    public void testParameterizedConstructorAndGetters() {
        final String TEST_LABEL = "Check Spending";
        final String TEST_PROMPT = "Analyze my spending for the last 30 days and flag any unusually high categories.";

        SuggestionModel model = new SuggestionModel(TEST_LABEL, TEST_PROMPT);
        assertEquals("The label should match the constructor argument.", TEST_LABEL, model.getLabel());
        assertEquals("The prompt should match the constructor argument.", TEST_PROMPT, model.getPrompt());
    }

    @Test
    public void testEmptyConstructorAndSetters() {
        final String TEST_LABEL = "Set Goal";
        final String TEST_PROMPT = "Create a new savings goal for a down payment on a house.";
        SuggestionModel model = new SuggestionModel();

        assertNull("Initial label should be null.", model.getLabel());
        assertNull("Initial prompt should be null.", model.getPrompt());

        model.setLabel(TEST_LABEL);
        model.setPrompt(TEST_PROMPT);

        assertEquals("The label should match the value set by the setter.", TEST_LABEL, model.getLabel());
        assertEquals("The prompt should match the value set by the setter.", TEST_PROMPT, model.getPrompt());
    }

}