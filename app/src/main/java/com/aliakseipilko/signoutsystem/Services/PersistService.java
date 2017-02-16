/*
 * com.aliakseipilko.signoutsystem.Services.PersistService was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2017.  All Rights Reserved.
 *
 * Last modified 16/02/17 09:52
 */

package com.aliakseipilko.signoutsystem.Services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;


public class PersistService extends Service {

    private static final int INTERVAL = 10000; // poll every 10 secs
    private static final String PACKAGE_NAME = "com.aliakseipilko.signoutsystem";

    private static boolean stopTask;
    private PowerManager.WakeLock mWakeLock;

    @Override
    public void onCreate() {
        super.onCreate();

        stopTask = false;

        // Screen will never switch off this way
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "PersistService");
        mWakeLock.acquire();

        // Start your (polling) task
        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                // If you wish to stop the task/polling
                if (stopTask) {
                    this.cancel();
                }

                // The first in the list of RunningTasks is always the foreground task.
                ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                ActivityManager.RunningTaskInfo foregroundTaskInfo = activityManager.getRunningTasks(1).get(0);
                String foregroundTaskPackageName = foregroundTaskInfo.topActivity.getPackageName();

                // Check foreground app: If it is not in the foreground... bring it!
                if (!foregroundTaskPackageName.equals(PACKAGE_NAME)) {
                    Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(PACKAGE_NAME);
                    startActivity(LaunchIntent);
                }
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(task, 0, INTERVAL);
    }

    @Override
    public void onDestroy() {
        stopTask = true;
        if (mWakeLock != null)
            mWakeLock.release();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}