package com.mahim.secondthought;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/** Persisted user choices: which apps are gated, and the theme mode. */
public class PrefsManager {

    private static final String PREFS = "second_thought_prefs";
    private static final String KEY_MONITORED = "monitored_packages";
    private static final String KEY_DARK_MODE = "dark_mode"; // "system" | "dark" | "light"

    private final SharedPreferences prefs;

    public PrefsManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean isMonitored(String pkg) {
        Set<String> set = prefs.getStringSet(KEY_MONITORED, null);
        return set != null && set.contains(pkg);
    }

    public void setMonitored(String pkg, boolean monitored) {
        Set<String> current = prefs.getStringSet(KEY_MONITORED, null);
        Set<String> updated = current == null ? new HashSet<>() : new HashSet<>(current);
        if (monitored) {
            updated.add(pkg);
        } else {
            updated.remove(pkg);
        }
        prefs.edit().putStringSet(KEY_MONITORED, updated).apply();
    }

    public String getDarkMode() {
        return prefs.getString(KEY_DARK_MODE, "system");
    }

    public void setDarkMode(String mode) {
        prefs.edit().putString(KEY_DARK_MODE, mode).apply();
    }
}
