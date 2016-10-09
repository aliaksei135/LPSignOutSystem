package com.apilko.signoutsystem.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class OnBootService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            switch (intent.getAction()) {
                case "android.intent.action.BOOT_COMPLETED":
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
                    prefs.edit().putBoolean("scheduledTasksSet", false).commit();
                    break;
            }
        }
    }
}
