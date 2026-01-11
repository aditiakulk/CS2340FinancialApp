package com.example.sprintproject.model;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.HashSet;

public class BudgetWarningQueue {
    private final Queue<BudgetWarning> warningQueue;
    private final Set<String> shownWarnings; // Track shown warnings to avoid duplicates
    private boolean isShowingWarning;

    public BudgetWarningQueue() {
        this.warningQueue = new LinkedList<>();
        this.shownWarnings = new HashSet<>();
        this.isShowingWarning = false;
    }

    public boolean enqueue(BudgetWarning warning) {
        String warningKey = generateKey(warning);
        if (shownWarnings.contains(warningKey)) {
            return false; // Already shown this warning
        }
        
        // Check if this warning is already in the queue
        for (BudgetWarning queuedWarning : warningQueue) {
            if (generateKey(queuedWarning).equals(warningKey)) {
                return false; // Already in queue
            }
        }
        
        warningQueue.offer(warning);
        return true;
    }

    public BudgetWarning dequeue() {
        BudgetWarning warning = warningQueue.poll();
        if (warning != null) {
            String key = generateKey(warning);
            shownWarnings.add(key);
        }
        return warning;
    }

    public boolean hasWarnings() {
        return !warningQueue.isEmpty();
    }

    public BudgetWarning peek() {
        return warningQueue.peek();
    }

    public void setShowingWarning(boolean showing) {
        this.isShowingWarning = showing;
    }

    public boolean isShowingWarning() {
        return isShowingWarning;
    }

    public void clear() {
        warningQueue.clear();
        shownWarnings.clear();
        isShowingWarning = false;
    }

    private String generateKey(BudgetWarning warning) {
        // Round percentage to nearest 5% to group similar warnings
        int percentageRange = (int) (Math.floor(warning.getPercentage() / 5) * 5);
        // Include budget title to distinguish between different budgets with same category
        return warning.getBudgetTitle() + "_" + warning.getCategory() + "_" + percentageRange;
    }

    public void resetCategory(String category) {
        shownWarnings.removeIf(key -> key.startsWith(category + "_"));
    }
}

