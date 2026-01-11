package com.example.sprintproject;

import com.example.sprintproject.model.SavingsCircleModel;
import com.example.sprintproject.model.Frequency;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.Map;

public class SavingsCircleValidationTest {

    private static final String GROUP_NAME = "Brainrot";
    private static final String TITLE = "Vacation";
    private static final double AMOUNT = 500.00;
    private static final String NOTES = "Idk but there's notes";

    private static final String GROUP_NAME_TEXT = "groupName";
    private static final String CHALLENGE_TITLE_TEXT = "challengeTitle";
    private static final String GOAL_AMOUNT_TEXT = "goalAmount";
    private static final String NOTES_TEXT = "notes";
    private static final String FREQUENCY_TEXT = "frequency";


    @Test
    public void validSavingsCircle() {
        SavingsCircleModel sc = new SavingsCircleModel(
                GROUP_NAME,
                TITLE,
                AMOUNT,
                NOTES,
                Frequency.MONTHLY,
                new Date()
        );

        Map<String, String> errors = sc.validate();
        assertTrue(errors.isEmpty());
    }

    @Test
    public void emptyGroupName() {
        SavingsCircleModel sc = new SavingsCircleModel(
                "",
                TITLE,
                AMOUNT,
                NOTES,
                Frequency.WEEKLY,
                new Date()
        );

        Map<String, String> errors = sc.validate();
        assertTrue(errors.containsKey(GROUP_NAME_TEXT));
        assertEquals("Group Name is required", errors.get(GROUP_NAME_TEXT));
    }

    @Test
    public void emptyChallengeTitle() {
        SavingsCircleModel sc = new SavingsCircleModel(
                GROUP_NAME,
                "",
                AMOUNT,
                NOTES,
                Frequency.WEEKLY,
                new Date()
        );

        Map<String, String> errors = sc.validate();
        assertTrue(errors.containsKey(CHALLENGE_TITLE_TEXT));
        assertEquals("Challenge Title is required", errors.get(CHALLENGE_TITLE_TEXT));
    }

    @Test
    public void zeroGoalAmount() {
        SavingsCircleModel sc = new SavingsCircleModel(
                GROUP_NAME,
                TITLE,
                0.00,
                NOTES,
                Frequency.MONTHLY,
                new Date()
        );

        Map<String, String> errors = sc.validate();
        assertTrue(errors.containsKey(GOAL_AMOUNT_TEXT));
        assertEquals("Amount must be greater than 0", errors.get(GOAL_AMOUNT_TEXT));
    }

    @Test
    public void negativeGoalAmount() {
        SavingsCircleModel sc = new SavingsCircleModel(
                GROUP_NAME,
                TITLE,
                -10.00,
                NOTES,
                Frequency.MONTHLY,
                new Date()
        );

        Map<String, String> errors = sc.validate();
        assertTrue(errors.containsKey(GOAL_AMOUNT_TEXT));
        assertEquals("Amount must be greater than 0", errors.get(GOAL_AMOUNT_TEXT));
    }

    @Test
    public void emptyNotes() {
        SavingsCircleModel sc = new SavingsCircleModel(
                GROUP_NAME,
                TITLE,
                AMOUNT,
                "",
                Frequency.WEEKLY,
                new Date()
        );

        Map<String, String> errors = sc.validate();
        assertTrue(errors.containsKey(NOTES_TEXT));
        assertEquals("Notes must not be empty", errors.get(NOTES_TEXT));
    }

    @Test
    public void nullFrequency() {
        SavingsCircleModel sc = new SavingsCircleModel(
                GROUP_NAME,
                TITLE,
                AMOUNT,
                NOTES,
                null,
                new Date()
        );

        Map<String, String> errors = sc.validate();
        assertTrue(errors.containsKey(FREQUENCY_TEXT));
        assertEquals("Frequency is required", errors.get(FREQUENCY_TEXT));
    }

    @Test
    public void multipleErrors() {
        SavingsCircleModel sc = new SavingsCircleModel(
                "",
                "",
                -10.0,
                "",
                null,
                new Date()
        );

        Map<String, String> errors = sc.validate();

        assertEquals(5, errors.size());
        assertTrue(errors.containsKey(GROUP_NAME_TEXT));
        assertTrue(errors.containsKey(CHALLENGE_TITLE_TEXT));
        assertTrue(errors.containsKey(GOAL_AMOUNT_TEXT));
        assertTrue(errors.containsKey(NOTES_TEXT));
        assertTrue(errors.containsKey(FREQUENCY_TEXT));
    }
}