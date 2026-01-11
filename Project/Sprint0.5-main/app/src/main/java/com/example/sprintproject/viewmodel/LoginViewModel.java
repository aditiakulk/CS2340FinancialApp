package com.example.sprintproject.viewmodel;

import android.app.Application;
import android.content.Intent;
import android.util.Patterns;
import android.widget.Toast;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.sprintproject.model.FirebaseModel;
import com.example.sprintproject.view.CreateAccount;
import com.example.sprintproject.view.Dashboard;

public class LoginViewModel extends AndroidViewModel {

    private MutableLiveData<String> email = new MutableLiveData<>("");
    private MutableLiveData<String> password = new MutableLiveData<>("");

    private final FirebaseModel fbm;
    private final Application app;

    public LoginViewModel(Application application) {
        super(application);
        this.app = application;
        this.fbm = FirebaseModel.getInstance();
    }

    public MutableLiveData<String> getEmail() {
        return email;
    }

    public MutableLiveData<String> getPassword() {
        return password;
    }

    public void onLoginClicked() {
        String emailVal = email.getValue();
        String passwordVal = password.getValue();

        if (emailVal == null || !Patterns.EMAIL_ADDRESS.matcher(emailVal).matches()) {
            Toast.makeText(app, "Enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (passwordVal == null || passwordVal.isEmpty()) {
            Toast.makeText(app, "Password required", Toast.LENGTH_SHORT).show();
            return;
        }

        fbm.login(emailVal, passwordVal)
                .addOnSuccessListener(authResult -> {
                    Intent intent = new Intent(app, Dashboard.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    app.startActivity(intent);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(app, "Login failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    public void onCreateAccountClicked() {
        Intent intent = new Intent(app, CreateAccount.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        app.startActivity(intent);
    }
}
