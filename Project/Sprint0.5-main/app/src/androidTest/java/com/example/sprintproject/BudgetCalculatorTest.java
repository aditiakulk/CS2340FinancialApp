package com.example.sprintproject;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import com.example.sprintproject.viewmodel.BudgetViewModel;

public class BudgetCalculatorTest {

    @Test
    public void computeTotalFromSpentAndRemaining() {
        Double[] r = BudgetViewModel.resolveBudgetFields(null, 300.0, 200.0);
        assertEquals(500.0, r[0], 0.0001);
        assertEquals(300.0, r[1], 0.0001);
        assertEquals(200.0, r[2], 0.0001);
    }

    @Test
    public void computeSpentFromTotalAndRemaining() {
        Double[] r = BudgetViewModel.resolveBudgetFields(500.0, null, 200.0);
        assertEquals(500.0, r[0], 0.0001);
        assertEquals(300.0, r[1], 0.0001);
        assertEquals(200.0, r[2], 0.0001);
    }

    @Test
    public void computeRemainingFromTotalAndSpent() {
        Double[] r = BudgetViewModel.resolveBudgetFields(500.0, 300.0, null);
        assertEquals(500.0, r[0], 0.0001);
        assertEquals(300.0, r[1], 0.0001);
        assertEquals(200.0, r[2], 0.0001);
    }
}