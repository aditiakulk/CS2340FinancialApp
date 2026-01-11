package com.example.sprintproject;

import static org.junit.Assert.*;

import android.app.Application;
import android.content.Context;

import androidx.annotation.Nullable;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.sprintproject.model.DashboardModel;

import org.jetbrains.annotations.Contract;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class DashboardModelTest {
    private Application app;
    private DashboardModel dashboardModel;

    @Before
    public void setup() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        app = (Application) context.getApplicationContext();
        dashboardModel = new DashboardModel(app);
    }

    @Test
    public void testLogoutClearsCache() throws IOException {
        // Put dummy files in cache
        File cacheDir = app.getCacheDir();
        File externalCacheDir = app.getExternalCacheDir();
        File cacheFile = new File(cacheDir, "temp_cache_file.txt");
        File externalCacheFile = new File(externalCacheDir, "temp_external_cache_file.txt");

        writeDummyFile(cacheFile);
        writeDummyFile(externalCacheFile);

        assertTrue(cacheFile.exists());
        assertTrue(externalCacheFile.exists());

        dashboardModel.logout();

        assertFalse(cacheFile.exists());
        assertFalse(externalCacheFile.exists());
    }

    private void writeDummyFile(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("dummy blah blah blah blah!");
        }
    }

    //ensures logout page resets
    @Test
    public void testLogoutDeletesCacheFiles() throws IOException {
        File cacheFile = new File(app.getCacheDir(), "temp.txt");
        try (FileWriter writer = new FileWriter(cacheFile)) {
            writer.write("test");
        }
        assertTrue(cacheFile.exists());
        dashboardModel.logout();
        assertFalse("Cache file should be deleted after logout", cacheFile.exists());
    }

    @Test
    public void testLogoutWithEmptyCasheDirs() {
        Application fakeApp = new Application() {
            public File getCacheDir() {
                return null;
            }
            public File getExternalCacheDir() {
                return null;
            }
        };
        DashboardModel model = new DashboardModel(fakeApp);
        try {
            model.logout();
        } catch (Exception e) {
            fail("Logout should handle the null cache directories.");
        }
    }

    @Test
    public void testLogoutWithEmptyCacheDirs() {
        Application fakeApp = new Application() {
            @Nullable
            @Contract(pure = true)
            @Override
            public File getCacheDir() {
                return null;
            }

            @Nullable
            @Contract(pure = true)
            @Override
            public File getExternalCacheDir() {
                return null;
            }
        };

        DashboardModel model = new DashboardModel(fakeApp);

        try {
            model.logout();
        } catch (Exception e) {
            fail("Logout should handle null cache directories without throwing: " + e.getMessage());
        }
    }

}
