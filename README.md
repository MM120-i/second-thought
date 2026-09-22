# Second Thought

An Android app that puts a mindful **"are you sure?"** gate in front of the apps
you doomscroll. Open Instagram on autopilot → a calm screen asks if you really
want in, and shows how long you've already spent there today.

## How it works

- **Home screen** lists every launchable app **ranked by today's screen time**
  (via `UsageStatsManager`), with a per-app toggle to put the gate on it.
- When a gated app comes to the foreground, an `AccessibilityService`
  (`AppBlockerService`) notices and shows a translucent `InterceptActivity`:
  *"Hold on — Instagram? You've spent 1h 20m here today. Still want in?"*
- **"Yes, continue"** lets you in and the gate **won't nag you again this
  session**. **"Not now"** (or the back button) drops you to the home screen.
- **Sessions reset on reboot**: the allowed-set lives only in memory
  (`SessionManager`), so after a restart the gate may appear again — never
  twice in one session.
- **Notifications are never blocked.** Replying from the notification shade
  doesn't open the app at all, and tapping a notification to open a gated app
  is detected (system-UI interaction heuristic, 10s window) and let through
  without the gate — that one time only.
- **Dark mode**: System / Light / Dark toggle on the home screen
  (`AppCompatDelegate` + Material3 DayNight theme).

## First-run setup (2 steps, the app walks you through them)

1. **Usage access** — needed to rank apps by screen time.
   The app deep-links you to *Settings → Usage access*.
2. **Accessibility service** — needed to notice when a gated app opens.
   The app deep-links you to *Settings → Accessibility*.

Both are standard Android settings screens; the app never sees passwords or
message contents.

## Build & run

1. Open this folder in **Android Studio** (it will download Gradle automatically).
2. Let it sync, then **Run** on your physical device (USB debugging on).
3. Complete the two setup steps in the app, toggle Instagram/Discord/etc.,
   and try opening one.

- `minSdk 26` (Android 8.0), `targetSdk 34`, Java 17, AndroidX + Material3.
- No `SYSTEM_ALERT_WINDOW` needed: the gate is a normal translucent activity,
  which accessibility services are allowed to start from the background.

## Project layout

```
app/src/main/
  AndroidManifest.xml
  java/com/mahim/secondthought/
    MainActivity.java        # home: ranked app list, toggles, theme, search
    AppListAdapter.java      # RecyclerView rows
    InterceptActivity.java   # the "are you sure?" gate
    AppBlockerService.java   # AccessibilityService watching foreground apps
    SessionManager.java      # in-memory session (reset on reboot)
    PrefsManager.java        # gated apps + theme, in SharedPreferences
    UsageHelper.java         # usage-access permission + screen-time queries
    BootReceiver.java        # explicit session reset on BOOT_COMPLETED
    AppInfo.java             # list model
  res/
    layout/  values/  drawable/  xml/  mipmap-anydpi-v26/
```

## Known limitations / future ideas

- The notification-tap detection is a heuristic (system-UI interaction within
  10s). A `NotificationListenerService` could make it exact.
- Per-app daily limits with a hard block, weekly stats, and a "snooze for
  15 minutes" option would be natural next features.
