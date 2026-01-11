package com.example.sprintproject;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.sprintproject.model.FirebaseModel;
import com.google.android.gms.tasks.Task;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class FirebaseModelTest {

    @Test
    public void testAppContext() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.sprintproject", appContext.getPackageName());
    }

    @Test
    public void testSingletonInstance() {
        FirebaseModel instance1 = FirebaseModel.getInstance();
        FirebaseModel instance2 = FirebaseModel.getInstance();
        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    @Test
    public void testRegisterAndCreateProfile() throws Exception {
        FirebaseModel model = FirebaseModel.getInstance();
        // generate user for this
        String email = "testuser" + System.currentTimeMillis() + "@example.com";
        String password = "password123";
        Map<String, Object> profile = new HashMap<>();
        profile.put("name", "Test User");
        profile.put("score", 0);
        Task<Void> task = model.registerAndCreateProfile(email, password, profile);
        task.addOnFailureListener(e -> fail("Failed with exception: " + e.getMessage()));
        task.addOnSuccessListener(result -> assertTrue(true));
        while (!task.isComplete()) {
            Thread.sleep(100); // fine for UT
        }
        assertTrue(task.isSuccessful());
        assertNotNull(model.getUID());
    }
}
