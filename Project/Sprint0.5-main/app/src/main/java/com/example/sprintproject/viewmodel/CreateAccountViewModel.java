package com.example.sprintproject.viewmodel;

import android.app.Application;
import android.util.Log;
import android.util.Patterns;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.sprintproject.model.Budget;
import com.example.sprintproject.model.Expense;
import com.example.sprintproject.model.FinancialFactory;
import com.example.sprintproject.model.FirebaseModel;
import com.example.sprintproject.model.FirestoreModel;
import com.example.sprintproject.model.Frequency;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateAccountViewModel extends AndroidViewModel {

    private final MutableLiveData<String> email = new MutableLiveData<>("");
    private final MutableLiveData<String> password = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> navigateBack = new MutableLiveData<>();

    private final FirebaseModel fbm;
    private final FirestoreModel fsm;

    public CreateAccountViewModel(Application application) {
        super(application);
        this.fbm = FirebaseModel.getInstance();
        this.fsm = FirestoreModel.getInstance();
    }


    public MutableLiveData<String> getPassword() {
        return password;
    }

    public MutableLiveData<Boolean> getLoading() {
        return loading;
    }

    public MutableLiveData<String> getToastMessage() {
        return toastMessage;
    }

    public MutableLiveData<Boolean> getNavigateBack() {
        return navigateBack;
    }

    public MutableLiveData<String> getEmail() {
        return email;
    }


    public void onRegisterClicked() {
        String e = email.getValue();
        String p = password.getValue();

        if (e == null || !Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
            toastMessage.setValue("Enter a valid email");
            return;
        }
        if (p == null || p.length() < 6) {
            toastMessage.setValue("Password must be at least 6 characters");
            return;
        }

        loading.setValue(true);

        Map<String, Object> profile = new HashMap<>();
        profile.put("email", e);

        fbm.registerAndCreateProfile(e, p, profile)
                .addOnSuccessListener(unused -> {
                    loading.postValue(false);
                    toastMessage.postValue("Account created successfully");
                    createSampleBudgets();
                    createSampleExpenses();
                    navigateBack.postValue(true);
                })
                .addOnFailureListener(err -> {
                    loading.postValue(false);
                    toastMessage.postValue("Error: " + err.getMessage());
                });

    }

    private void createSampleExpenses() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

            Date date1 = sdf.parse("2025-04-06");
            Expense sample1 = FinancialFactory.createExpense("Sample1", 25,
                    date1, "S1", null, null);

            Date date2 = sdf.parse("2025-04-01");
            Expense sample2 = FinancialFactory.createExpense("Sample2", 80,
                    date2, "S2", null, null);

            fsm.addExpense(sample1);
            fsm.addExpense(sample2);
        } catch (ParseException e) {
            Log.e("CreateAccountVM", "Error parsing default dates", e);
        }
    }

    private void createSampleBudgets() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

            Date date1 = sdf.parse("2025-04-06");
            Budget sample1 = FinancialFactory.createBudget("Sample1", 50,
                    "S1", date1, Frequency.WEEKLY);

            Date date2 = sdf.parse("2025-04-01");
            Budget sample2 = FinancialFactory.createBudget("Sample2", 100,
                    "S2", date2, Frequency.MONTHLY);
            fsm.addBudget(sample1);
            fsm.addBudget(sample2);
        } catch (ParseException e) {
            Log.e("CreateAccountVM", "Error parsing default dates", e);
        }
    }

}
