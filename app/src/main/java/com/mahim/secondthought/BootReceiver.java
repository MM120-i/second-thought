package com.mahim.secondthought;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * The gate's session state is in-memory, so a reboot already wipes it.
 * This receiver makes that explicit (and is a hook for anything we want
 * to do on boot in the future).
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SessionManager.reset();
        }
    }
}
