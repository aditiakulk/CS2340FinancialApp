package com.example.sprintproject.viewmodel;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sprintproject.model.Budget;
import com.example.sprintproject.model.DateModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.sprintproject.model.FirestoreModel;
import com.example.sprintproject.model.Frequency;
import com.example.sprintproject.model.NavModel;
import com.example.sprintproject.model.SavingsCircleModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SavingsCircleViewModel extends ViewModel {
    private final NavModel navModel = new NavModel();
    public NavModel getNavModel() {
        return navModel;
    }

    private final MutableLiveData<List<SavingsCircleModel>> savingsCircles
            = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<SavingsCircleModel>> getSavingsCircles() {
        return savingsCircles;
    }

    private final MutableLiveData<String> savingsCircleError = new MutableLiveData<>(null);
    public LiveData<String> getSavingsCircleError() {
        return savingsCircleError;
    }

    public MutableLiveData<List<SavingsCircleModel>> getIncomingInvites() {
        return fsm.getIncomingInvitesForCurrentUser();
    }

    private final FirestoreModel fsm = FirestoreModel.getInstance();

    public void addSavingsCircle(SavingsCircleModel newSC) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String creatorUid   = user.getUid();
        String creatorEmail = user.getEmail().toLowerCase();

        newSC.setCreatorUid(creatorUid);
        newSC.setCreatorEmail(creatorEmail);
        newSC.setMemberUids(new ArrayList<>(List.of(creatorUid)));
        newSC.setMemberEmails(new ArrayList<>(List.of(creatorEmail)));
        newSC.setPendingInvites(new ArrayList<>());

        Date now = DateModel.getCurrentDate().getValue();

        newSC.setStartDate(now);

        Map<String, Date> joinMap = new HashMap<>();
        joinMap.put(creatorEmail, now);
        newSC.setMemberJoinAt(joinMap);

        fsm.addSavingsCircle(newSC)
                .addOnSuccessListener(docRef -> {
                    newSC.setId(docRef.getId());

                    Budget b = new Budget();
                    b.setTitle(newSC.getChallengeTitle());
                    b.setCategory("Circle: " + newSC.getGroupName());
                    b.setFrequency(newSC.getFrequency());
                    b.setAmount(newSC.getGoalAmount());
                    b.setDate(now);
                    b.setCircleId(newSC.getId());

                    fsm.upsertBudgetForCircle(newSC.getId(), b);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding savings circle", e);
                    savingsCircleError.setValue("• Failed to add savings circle: "
                            + e.getMessage());
                });
    }

    public void deleteSavingsCircle(SavingsCircleModel sc) {
        if (sc == null || sc.getId() == null) {
            savingsCircleError.setValue("• Cannot delete: invalid savings circle.");
            return;
        }
        String creatorUid = sc.getCreatorUid();
        if (creatorUid == null || creatorUid.isEmpty()) {
            savingsCircleError.setValue("• Cannot delete: circle missing creator UID.");
            return;
        }

        fsm.deleteSavingsCircleCascade(creatorUid, sc.getId())
                .addOnSuccessListener(v ->
                        Log.d(TAG, "Cascade-deleted circle, budgets, and expenses: "
                                + sc.getGroupName()))
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Cascade delete failed", e);
                    savingsCircleError.setValue("• Delete failed: " + e.getMessage());
                });
    }

    public MutableLiveData<List<SavingsCircleModel>> getAllSavingsCircles() {
        return fsm.getSavingsCirclesForCurrentUser();
    }

    public void sendInvite(SavingsCircleModel sc, String inviteEmail) {
        fsm.sendCircleInvite(sc.getCreatorUid(), sc.getId(), inviteEmail.toLowerCase());
    }

    public void acceptInvite(SavingsCircleModel sc, String myEmail) {
        fsm.acceptCircleInvite(sc.getCreatorUid(), sc.getId(), myEmail)
                .addOnSuccessListener(v -> {
                    Budget b = new Budget();
                    b.setTitle(sc.getChallengeTitle());
                    b.setCategory("Circle: " + sc.getGroupName());
                    b.setFrequency(sc.getFrequency());
                    b.setAmount(sc.getGoalAmount());
                    b.setCircleId(sc.getId());
                    b.setDate(DateModel.getCurrentDate().getValue());
                    fsm.upsertBudgetForCircle(sc.getId(), b);
                })
                .addOnFailureListener(e ->
                        savingsCircleError.setValue("• Accept failed: " + e.getMessage()));
    }

    public void declineInvite(SavingsCircleModel sc, String myEmail) {
        fsm.declineCircleInvite(sc.getCreatorUid(), sc.getId(), myEmail)
                .addOnFailureListener(e -> savingsCircleError.setValue("• Decline failed: "
                        + e.getMessage()));
    }

    public static Date[] challengeWindow(Date acceptanceDate, Frequency frequency) {
        Calendar cal = Calendar.getInstance();
        if (acceptanceDate != null) {
            cal.setTime(acceptanceDate);
        }

        setStartOfDay(cal);
        Date start = cal.getTime();

        if (frequency == Frequency.MONTHLY) {
            cal.add(Calendar.MONTH, 1);
            cal.add(Calendar.DAY_OF_YEAR, -1);
            setEndOfDay(cal);
            Date end = cal.getTime();

            return new Date[] {start, end};
        }
        cal.add(Calendar.DAY_OF_MONTH, 6);
        setEndOfDay(cal);
        Date end = cal.getTime();

        return new Date[]{start, end};
    }

    private static void setStartOfDay(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    private static void setEndOfDay(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
    }

    public LiveData<Double> getCircleTotal(String creatorUid, String circleId) {
        return fsm.getCircleTotalLiveData(creatorUid, circleId);
    }

    public void trackSavingsCircleTotal(String creatorUid, String circleId) {
        fsm.trackSavingsCircleTotal(creatorUid, circleId);
    }
}
