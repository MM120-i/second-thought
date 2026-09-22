package com.mahim.secondthought;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.provider.Settings;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Helpers around the usage-access permission and per-app screen time. */
public final class UsageHelper {

    private UsageHelper() {}

    public static boolean hasUsageAccess(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        if (appOps == null) {
            return false;
        }
        int mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public static void openUsageAccessSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /** Foreground time per package since midnight, in milliseconds. */
    public static Map<String, Long> getTodayUsageMap(Context context) {
        Map<String, Long> map = new HashMap<>();
        UsageStatsManager usm =
                (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (usm == null) {
            return map;
        }
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();
        long end = System.currentTimeMillis();

        List<UsageStats> stats =
                usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end);
        if (stats == null) {
            return map;
        }
        for (UsageStats s : stats) {
            long t = s.getTotalTimeInForeground();
            if (t > 0) {
                map.put(s.getPackageName(), t);
            }
        }
        return map;
    }

    public static long getTodayUsageMs(Context context, String pkg) {
        Long v = getTodayUsageMap(context).get(pkg);
        return v == null ? 0 : v;
    }

    /** 90_000 ms -> "1m", 7_500_000 ms -> "2h 5m". */
    public static String formatDuration(long ms) {
        long totalMinutes = TimeUnit.MILLISECONDS.toMinutes(ms);
        long h = totalMinutes / 60;
        long m = totalMinutes % 60;
        if (h > 0) {
            return h + "h " + m + "m";
        }
        return m + "m";
    }
}
