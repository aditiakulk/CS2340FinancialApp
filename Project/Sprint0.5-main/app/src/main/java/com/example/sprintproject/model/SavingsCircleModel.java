package com.example.sprintproject.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class SavingsCircleModel implements Validatable {
    private static final String DEFAULT = "Default";
    
    private String id;
    private String groupName;
    private String creatorEmail;
    private String challengeTitle;
    private double goalAmount;
    private String notes;
    private Frequency frequency;
    private Date startDate;
    private String creatorUid;
    private List<String> memberEmails = new ArrayList<>();
    private List<String> pendingInvites = new ArrayList<>();
    private double currentAmount;
    private Date lastContributed;
    private List<String> memberUids;
    private Map<String, Date> memberJoinAt;


    public SavingsCircleModel() {
        groupName = DEFAULT;
        creatorEmail = DEFAULT;
        challengeTitle = DEFAULT;
        goalAmount = 0.0;
        notes = DEFAULT;
        frequency = Frequency.WEEKLY;
        startDate = new Date();
        currentAmount = 0.0;
        lastContributed = new Date();
        memberUids = new ArrayList<>();
    }

    public SavingsCircleModel(String groupName, String challengeTitle, double goalAmount,
                              String notes, Frequency frequency, Date startDate) {
        this.groupName = groupName;
        this.challengeTitle = challengeTitle;
        this.goalAmount = goalAmount;
        this.notes = notes;
        this.frequency = frequency;
        this.startDate = startDate;
        this.currentAmount = 0.0;
        this.lastContributed = new Date();
    }

    public String getGroupName() {
        return groupName;
    }

    public String getCreatorEmail() {
        return creatorEmail;
    }

    public String getChallengeTitle() {
        return challengeTitle;
    }

    public double getGoalAmount() {
        return goalAmount;
    }

    public String getNotes() {
        return notes;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Map<String, Date> getMemberJoinAt() {
        return memberJoinAt;
    }

    public void setMemberJoinAt(Map<String, Date> memberJoinAt) {
        this.memberJoinAt = memberJoinAt;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
    }

    public Date getLastContributed() {
        return lastContributed;
    }

    public void setLastContributed(Date lastContributed) {
        this.lastContributed = lastContributed;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setCreatorEmail(String creatorEmail) {
        this.creatorEmail = creatorEmail;
    }

    public void setChallengeTitle(String challengeTitle) {
        this.challengeTitle = challengeTitle;
    }

    public void setGoalAmount(double goalAmount) {
        this.goalAmount = goalAmount;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatorUid() {
        return creatorUid;
    }
    public void setCreatorUid(String creatorUid) {
        this.creatorUid = creatorUid;
    }

    public List<String> getMemberEmails() {
        return memberEmails;
    }
    public void setMemberEmails(List<String> memberEmails) {
        this.memberEmails = memberEmails;
    }

    public List<String> getPendingInvites() {
        return pendingInvites;
    }
    public void setPendingInvites(List<String> pendingInvites) {
        this.pendingInvites = pendingInvites;
    }

    public List<String> getMemberUids() {
        return memberUids;
    }

    public void setMemberUids(List<String> memberUids) {
        this.memberUids = memberUids;
    }

    public Map<String, String> validate() {
        Map<String, String> errors = new HashMap<>();

        if (groupName == null || groupName.trim().isEmpty()) {
            errors.put("groupName", "Group Name is required");
        }
        if (challengeTitle == null || challengeTitle.trim().isEmpty()) {
            errors.put("challengeTitle", "Challenge Title is required");
        }
        if (goalAmount <= 0) {
            errors.put("goalAmount", "Amount must be greater than 0");
        }
        if (notes == null || notes.trim().isEmpty()) {
            errors.put("notes", "Notes must not be empty");
        }
        if (frequency == null) {
            errors.put("frequency", "Frequency is required");
        }

        return errors;
    }
}
