package com.mahim.secondthought;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

/**
 * Watches for window changes and shows the "are you sure?" gate when a
 * user-selected app comes to the foreground.
 *
 * <p>Skipped when:
 * <ul>
 *   <li>the app was already allowed this session (tapped "Yes, continue"),</li>
 *   <li>the app was opened within seconds of touching the notification shade
 *       (treated as opening from a notification, e.g. tapping a DM),</li>
 *   <li>the event is from our own UI or the system UI itself.</li>
 * </ul>
 */
public class AppBlockerService extends AccessibilityService {

    private static final String SYSTEM_UI = "com.android.systemui";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return;
        }
        CharSequence pkgSeq = event.getPackageName();
        if (pkgSeq == null) {
            return;
        }
        String pkg = pkgSeq.toString();

        // Taps in the notification shade / status bar area. If a gated app opens
        // right after this, it was almost certainly opened from a notification.
        if (SYSTEM_UI.equals(pkg)) {
            SessionManager.onSystemUiInteraction();
            return;
        }

        // Never gate ourselves.
        if (pkg.equals(getPackageName())) {
            return;
        }

        PrefsManager prefs = new PrefsManager(this);
        if (!prefs.isMonitored(pkg)) {
            return;
        }
        if (SessionManager.isAllowed(pkg)) {
            return;
        }
        if (SessionManager.openedFromNotificationShade()) {
            // Notification open: let it through this once, without consuming
            // the session allowance.
            return;
        }

        Intent intent = new Intent(this, InterceptActivity.class);
        intent.putExtra(InterceptActivity.EXTRA_PACKAGE, pkg);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onInterrupt() {
        // Nothing to clean up.
    }
}
