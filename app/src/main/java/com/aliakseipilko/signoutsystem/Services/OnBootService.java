/*
 * com.aliakseipilko.signoutsystem.Services.OnBootService was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2017.  All Rights Reserved.
 *
 * Last modified 22/01/17 12:42
 */

package com.aliakseipilko.signoutsystem.Services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.aliakseipilko.signoutsystem.Activities.MainActivity;

import java.util.Calendar;

public class OnBootService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            switch (intent.getAction()) {
                case "android.intent.action.BOOT_COMPLETED":
//                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
//                    prefs.edit().putBoolean("scheduledTasksSet", false).commit();
                    scheduleResetSignedIn(context);
                    break;
            }
        }
    }

    private void scheduleResetSignedIn(Context ctx) {

        //Schedule reset to registered state task
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent resetIntent = new Intent(ctx, MainActivity.class);
        resetIntent.putExtra("type", "ResetRegistered");
        PendingIntent taskIntent = PendingIntent.getBroadcast(ctx, 0, resetIntent, 0);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, 1);
        cal.set(Calendar.MINUTE, 30);

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, taskIntent);
    }
}
