package com.example.sprintproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.sprintproject.viewmodel.ChatbotViewModel;

import org.junit.Test;

public class ChatBotTests {

    private static class TestChatbotViewModel extends ChatbotViewModel {
        String lastVisibleMessage;
        String lastModelPrompt;
        int sendMessageInternalCalls = 0;
        int sendMessageCalls = 0;

        @Override
        public void sendMessageInternal(String userVisibleMessage, String modelPrompt) {
            this.lastVisibleMessage = userVisibleMessage;
            this.lastModelPrompt = modelPrompt;
            this.sendMessageInternalCalls++;
        }

        @Override
        public void sendMessage(String userMessage) {
            this.sendMessageCalls++;
            super.sendMessage(userMessage);
        }

    }

    @Test
    public void sendMessageIncludesFinanceContextInPrompt() {
        TestChatbotViewModel vm = new TestChatbotViewModel();
        String question = "How am I doing this month financially?";

        vm.sendMessage(question);

        assertEquals(question, vm.lastVisibleMessage);

        assertNotNull(vm.lastModelPrompt);

        assertTrue(
                "Prompt should mention financial situation",
                vm.lastModelPrompt.contains("Here is the user's financial situation:")
        );

        assertTrue(
                "Prompt should instruct to use budgets and expenses",
                vm.lastModelPrompt.contains("Using the above budgets and expenses, answer this question:")
        );

        assertTrue(
                "Prompt should contain the original question at the end",
                vm.lastModelPrompt.trim().endsWith(question)
        );
    }

    @Test
    public void sendMessageWithReferenceFallsBackToSendMessageWhenNoUidOrRef() {
        TestChatbotViewModel vm = new TestChatbotViewModel();
        String question = "Help me plan my groceries.";

        vm.sendMessageCalls = 0;
        vm.sendMessageInternalCalls = 0;

        vm.sendMessageWithReference(question, null);

        assertEquals("sendMessage should be called once as a fallback",
                1, vm.sendMessageCalls);

        assertEquals("sendMessageInternal should be invoked once from sendMessage",
                1, vm.sendMessageInternalCalls);

        assertNotNull("Model prompt should be captured",
                vm.lastModelPrompt);
        assertTrue("Prompt should include finance context even on fallback",
                vm.lastModelPrompt.contains("Here is the user's financial situation:"));
    }

    @Test
    public void sendMessageWithNoBudgetsOrExpensesShowsNoDataMessages() {
        TestChatbotViewModel vm = new TestChatbotViewModel();
        String question = "What does my budget look like?";

        vm.sendMessage(question);

        assertNotNull(vm.lastModelPrompt);
        String prompt = vm.lastModelPrompt;

        assertTrue(
                "Prompt should say no budgets created when there are none",
                prompt.contains("- No budgets created.")
        );

        assertTrue(
                "Prompt should say no expenses recorded when there are none",
                prompt.contains("- No expenses recorded.")
        );
    }

    //Test insures no repeated answers by chatbot
    @Test
    public void sendMessageQuestionAppearsOnceAtEndOfPrompt() {
        TestChatbotViewModel vm = new TestChatbotViewModel();
        String question = "Should I reduce my entertainment budget?";

        vm.sendMessage(question);

        String prompt = vm.lastModelPrompt;
        assertNotNull(prompt);

        int firstIndex = prompt.indexOf(question);
        int lastIndex  = prompt.lastIndexOf(question);

        assertTrue("Question should appear in the prompt", firstIndex >= 0);
        assertEquals("Question should only appear once in the prompt",
                firstIndex, lastIndex);

        assertTrue("Prompt should end with the question",
                prompt.trim().endsWith(question));
    }


}