package com.mahim.secondthought;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * The translucent "are you sure?" gate shown over a gated app.
 *
 * <p>"Yes, continue" allows the app for the rest of the session (until reboot).
 * "Not now" — or the system back button — drops you back to the home screen.
 */
public class InterceptActivity extends AppCompatActivity {

    public static final String EXTRA_PACKAGE = "extra_package";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intercept);

        String pkg = getIntent().getStringExtra(EXTRA_PACKAGE);
        if (pkg == null) {
            finish();
            return;
        }

        PackageManager pm = getPackageManager();
        String appName = pkg;
        Drawable icon = null;
        try {
            ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
            appName = pm.getApplicationLabel(ai).toString();
            icon = pm.getApplicationIcon(ai);
        } catch (PackageManager.NameNotFoundException ignored) {
            // Fall back to the raw package name.
        }

        TextView title = findViewById(R.id.intercept_title);
        TextView subtitle = findViewById(R.id.intercept_subtitle);
        ImageView iconView = findViewById(R.id.intercept_icon);

        title.setText(getString(R.string.intercept_title, appName));
        long todayMs = UsageHelper.getTodayUsageMs(this, pkg);
        subtitle.setText(getString(R.string.intercept_subtitle,
                UsageHelper.formatDuration(todayMs)));
        if (icon != null) {
            iconView.setImageDrawable(icon);
        }

        Button continueBtn = findViewById(R.id.btn_continue);
        Button backBtn = findViewById(R.id.btn_back);

        continueBtn.setOnClickListener(v -> {
            // Allowed for the rest of this session — no more prompts until reboot.
            SessionManager.allow(pkg);
            Intent launch = pm.getLaunchIntentForPackage(pkg);
            if (launch != null) {
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(launch);
            }
            finish();
        });

        backBtn.setOnClickListener(v -> goHome());
    }

    @Override
    public void onBackPressed() {
        // Back means "not now": go home instead of slipping through into the app.
        goHome();
    }

    private void goHome() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(home);
        finish();
    }
}
