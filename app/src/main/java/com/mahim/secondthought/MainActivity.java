package com.mahim.secondthought;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Home screen: every launchable app ranked by today's screen time, a switch per
 * app to put the "are you sure?" gate on it, setup checklist, and theme toggle.
 */
public class MainActivity extends AppCompatActivity {

    private AppListAdapter adapter;
    private final List<AppInfo> allApps = new ArrayList<>();
    private PrefsManager prefs;
    private MaterialCardView setupCard;
    private TextView setupHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = new PrefsManager(this);
        // Apply before super.onCreate so the theme is right on first draw.
        applyDarkMode(prefs.getDarkMode());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recycler = findViewById(R.id.app_list);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppListAdapter(app -> {
            app.monitored = !app.monitored;
            prefs.setMonitored(app.packageName, app.monitored);
            adapter.notifyDataSetChanged();
        });
        recycler.setAdapter(adapter);

        setupCard = findViewById(R.id.setup_card);
        setupHint = findViewById(R.id.setup_hint);
        Button setupBtn = findViewById(R.id.btn_setup);
        setupBtn.setOnClickListener(v -> openNextMissingPermission());

        EditText search = findViewById(R.id.search);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        MaterialButtonToggleGroup themeGroup = findViewById(R.id.theme_toggle);
        String mode = prefs.getDarkMode();
        if ("dark".equals(mode)) {
            themeGroup.check(R.id.theme_dark);
        } else if ("light".equals(mode)) {
            themeGroup.check(R.id.theme_light);
        } else {
            themeGroup.check(R.id.theme_system);
        }
        themeGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            String newMode = "system";
            if (checkedId == R.id.theme_dark) {
                newMode = "dark";
            } else if (checkedId == R.id.theme_light) {
                newMode = "light";
            }
            prefs.setDarkMode(newMode);
            applyDarkMode(newMode);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSetupCard();
        loadApps();
    }

    private void applyDarkMode(String mode) {
        int nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        if ("dark".equals(mode)) {
            nightMode = AppCompatDelegate.MODE_NIGHT_YES;
        } else if ("light".equals(mode)) {
            nightMode = AppCompatDelegate.MODE_NIGHT_NO;
        }
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }

    /** Shows the setup card until both required permissions are granted. */
    private void refreshSetupCard() {
        boolean usageOk = UsageHelper.hasUsageAccess(this);
        boolean a11yOk = isAccessibilityEnabled();
        if (usageOk && a11yOk) {
            setupCard.setVisibility(View.GONE);
            return;
        }
        setupCard.setVisibility(View.VISIBLE);
        if (!usageOk) {
            setupHint.setText(R.string.setup_usage_hint);
        } else {
            setupHint.setText(R.string.setup_a11y_hint);
        }
    }

    private void openNextMissingPermission() {
        if (!UsageHelper.hasUsageAccess(this)) {
            UsageHelper.openUsageAccessSettings(this);
        } else {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        }
    }

    private boolean isAccessibilityEnabled() {
        AccessibilityManager am =
                (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am == null) {
            return false;
        }
        List<AccessibilityServiceInfo> enabled =
                am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for (AccessibilityServiceInfo info : enabled) {
            if (getPackageName().equals(info.getResolveInfo().serviceInfo.packageName)) {
                return true;
            }
        }
        return false;
    }

    private void loadApps() {
        allApps.clear();
        PackageManager pm = getPackageManager();
        Map<String, Long> usage = UsageHelper.hasUsageAccess(this)
                ? UsageHelper.getTodayUsageMap(this)
                : Collections.emptyMap();

        Intent launcher = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> infos = pm.queryIntentActivities(launcher, 0);
        for (ResolveInfo ri : infos) {
            String pkg = ri.activityInfo.packageName;
            if (pkg.equals(getPackageName())) {
                continue;
            }
            if ("com.android.systemui".equals(pkg)) {
                continue;
            }
            String name = ri.loadLabel(pm).toString();
            Drawable icon;
            try {
                icon = pm.getApplicationIcon(pkg);
            } catch (PackageManager.NameNotFoundException e) {
                continue;
            }
            long ms = usage.containsKey(pkg) ? usage.get(pkg) : 0L;
            allApps.add(new AppInfo(pkg, name, icon, ms, prefs.isMonitored(pkg)));
        }

        // Most screen time first; ties broken alphabetically.
        Collections.sort(allApps, (a, b) -> {
            int c = Long.compare(b.todayUsageMs, a.todayUsageMs);
            return c != 0 ? c : a.name.compareToIgnoreCase(b.name);
        });

        long total = 0;
        for (AppInfo a : allApps) {
            total += a.todayUsageMs;
        }
        TextView stat = findViewById(R.id.stat_total);
        stat.setText(getString(R.string.stat_total_today, UsageHelper.formatDuration(total)));

        EditText search = findViewById(R.id.search);
        filter(search.getText().toString());
    }

    private void filter(String query) {
        String q = query.trim().toLowerCase();
        List<AppInfo> shown = new ArrayList<>();
        for (AppInfo a : allApps) {
            if (q.isEmpty() || a.name.toLowerCase().contains(q)) {
                shown.add(a);
            }
        }
        adapter.submit(shown);
    }
}
