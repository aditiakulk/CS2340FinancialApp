package com.example.sprintproject.model;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


public class FirebaseModel {
    private static final AtomicReference<FirebaseModel> INSTANCE = new AtomicReference<>();
    private final FirebaseAuth auth;
    private final DatabaseReference db;

    private FirebaseModel() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();
    }

    public static FirebaseModel getInstance() {
        FirebaseModel result = INSTANCE.get();
        if (result == null) {
            result = new FirebaseModel();
            if (!INSTANCE.compareAndSet(null, result)) {
                result = INSTANCE.get();
            }
        }
        return result;
    }

    public Task<AuthResult> login(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> register(String email, String password) {
        return auth.createUserWithEmailAndPassword(email, password);
    }

    public Task<Void> createProfile(String uid, Map<String, Object> profile) {
        return db.child("users").child(uid).setValue(profile);
    }

    public Task<Void> registerAndCreateProfile(String email, String password,
                                               Map<String, Object> profile) {
        return register(email, password)
                .onSuccessTask(result -> {
                    if (result == null || result.getUser() == null) {
                        return Tasks.forException(new IllegalStateException("User is null"));
                    }
                    return createProfile(result.getUser().getUid(), profile);
                });
    }

    public String getUID() {
        FirebaseUser currentUser = auth.getCurrentUser();
        String uid = null;
        if (currentUser != null) {
            uid = currentUser.getUid();
        }
        return uid;
    }
}