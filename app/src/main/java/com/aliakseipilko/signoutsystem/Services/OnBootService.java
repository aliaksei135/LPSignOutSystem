/*
 * com.aliakseipilko.signoutsystem.Services.OnBootService was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2016.  All Rights Reserved.
 *
 * Last modified 11/11/16 20:11
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
