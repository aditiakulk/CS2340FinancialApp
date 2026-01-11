package com.example.sprintproject.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sprintproject.model.Budget;
import com.example.sprintproject.model.Expense;
import com.example.sprintproject.model.FirestoreModel;
import com.example.sprintproject.model.ProfileSummary;
import com.example.sprintproject.model.SavingsCircleModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ProfileViewModel extends ViewModel {

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirestoreModel db = FirestoreModel.getInstance();
    private MutableLiveData<String> profilePicture = new MutableLiveData<>();

    private static final String PROFILE_DEFAULT = "profile_default";

    private final MediatorLiveData<ProfileSummary> profileSummary = new MediatorLiveData<>();

    private List<Expense> latestExpenses = new ArrayList<>();
    private List<Budget> latestBudgets = new ArrayList<>();
    private List<SavingsCircleModel> latestCircles = new ArrayList<>();

    public ProfileViewModel() {
        loadProfileSummary();
        loadProfilePicture();
    }

    public LiveData<ProfileSummary> getProfileSummary() {
        return profileSummary;
    }

    private void loadProfileSummary() {
        if (user == null) {
            profileSummary.setValue(new ProfileSummary("", 0, 0, 0));
            return;
        }

        String email = user.getEmail();

        profileSummary.addSource(db.getExpenses(), expenses -> {
            if (expenses != null) {
                latestExpenses = expenses;
            }
            updateProfile(email);
        });

        profileSummary.addSource(db.getBudgets(), budgets -> {
            if (budgets != null) {
                latestBudgets = budgets;
            }
            updateProfile(email);
        });

        profileSummary.addSource(db.getSavingsCirclesByOwner(), circles -> {
            if (circles != null) {
                latestCircles = circles;
            }
            updateProfile(email);
        });
    }

    private void updateProfile(String email) {

        int totalExpenses = latestExpenses != null ? latestExpenses.size() : 0;
        int totalBudgets = latestBudgets != null ? latestBudgets.size() : 0;
        int totalCircles = latestCircles != null ? latestCircles.size() : 0;

        profileSummary.setValue(new ProfileSummary(email, totalExpenses,
                totalBudgets, totalCircles));
    }

    public LiveData<String> getProfilePicture() {
        return profilePicture;
    }

    private void loadProfilePicture() {
        if (user == null) {
            profilePicture.setValue(PROFILE_DEFAULT);
            return;
        }

        String userId = user.getUid();
        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId);

        docRef.addSnapshotListener((snapshot, e) -> {
            if (snapshot != null && snapshot.exists()) {
                String picture = snapshot.getString("profilePicture");
                profilePicture.setValue(picture != null ? picture : PROFILE_DEFAULT);
            } else {
                profilePicture.setValue(PROFILE_DEFAULT);
            }
        });
    }

    public void setProfilePicture(String pictureID) {
        profilePicture.setValue(pictureID);
        db.updateUserProfilePicture(pictureID);
    }
}
