package com.mahim.secondthought;

import java.util.HashSet;
import java.util.Set;

/**
 * In-memory session state for the "are you sure?" gate.
 *
 * <p>Once you tap "Yes, continue" for an app, it stays allowed until the session
 * ends. The session lives only in memory, so it naturally resets when the phone
 * reboots (or the process dies) — exactly the behaviour we want: the gate may
 * come back after a reboot, but never nags you twice in one session.
 */
public final class SessionManager {

    private SessionManager() {}

    private static final Set<String> allowedThisSession = new HashSet<>();
    private static long lastSystemUiInteractionMs = 0;

    /** How long after touching the notification shade we treat an app open as "from a notification". */
    private static final long NOTIFICATION_GRACE_MS = 10_000;

    public static boolean isAllowed(String pkg) {
        return allowedThisSession.contains(pkg);
    }

    public static void allow(String pkg) {
        allowedThisSession.add(pkg);
    }

    public static void onSystemUiInteraction() {
        lastSystemUiInteractionMs = System.currentTimeMillis();
    }

    public static boolean openedFromNotificationShade() {
        return System.currentTimeMillis() - lastSystemUiInteractionMs < NOTIFICATION_GRACE_MS;
    }

    public static void reset() {
        allowedThisSession.clear();
        lastSystemUiInteractionMs = 0;
    }
}
