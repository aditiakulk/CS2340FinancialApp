package com.example.sprintproject.model;

import java.io.File;

import android.app.Application;
import android.util.Log;

import java.nio.file.Files;
import java.io.IOException;

public class DashboardModel {
    private final Application application;
    private static final String TAG = "CacheClear";

    public DashboardModel(Application application) {
        this.application = application;
    }

    public void logout() {
        clearAppCache();
    }

    private void clearAppCache() {
        clearAppCacheHelper(application.getCacheDir());
        clearAppCacheHelper(application.getExternalCacheDir());
    }

    private void clearAppCacheHelper(File cacheDir) {
        if (cacheDir != null) {
            if (deleteDirRecursive(cacheDir)) {
                Log.i(TAG, "Cache cleared successfully.");
            } else {
                Log.w(TAG, "Some cache files could not be deleted.");
            }
        }
    }

    private boolean deleteDirRecursive(File dir) {
        boolean success = true;
        if (dir == null) {
            return false;
        }
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (!deleteDirRecursive(child)) {
                        success = false;
                    }
                }
            }
        }

        try {
            Files.delete(dir.toPath());
            Log.i(TAG, "Cleared: " + dir.getAbsolutePath());
        } catch (IOException e) {
            success = false;
            Log.e(TAG, "Failed to delete: " + dir.getAbsolutePath(), e);
        }

        return success;
    }

}