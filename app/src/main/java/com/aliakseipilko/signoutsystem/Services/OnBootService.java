/*
 * com.aliakseipilko.signoutsystem.Services.OnBootService was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2016.  All Rights Reserved.
 *
 * Last modified 23/12/16 13:12
 */

package com.aliakseipilko.signoutsystem.Services;

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
