package com.mahim.secondthought;

import android.graphics.drawable.Drawable;

/** One row in the home-screen app list. */
public class AppInfo {
    public final String packageName;
    public final String name;
    public final Drawable icon;
    public final long todayUsageMs;
    public boolean monitored;

    public AppInfo(String packageName, String name, Drawable icon,
                   long todayUsageMs, boolean monitored) {
        this.packageName = packageName;
        this.name = name;
        this.icon = icon;
        this.todayUsageMs = todayUsageMs;
        this.monitored = monitored;
    }
}
