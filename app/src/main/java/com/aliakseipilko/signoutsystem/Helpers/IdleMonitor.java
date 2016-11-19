/*
 * com.aliakseipilko.signoutsystem.Helpers.IdleMonitor was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2016.  All Rights Reserved.
 *
 * Last modified 19/11/16 10:41
 */

package com.aliakseipilko.signoutsystem.Helpers;

import android.os.Handler;
import android.util.Log;

public class IdleMonitor {

    private static final String TAG = "IdleMonitor";
    private static IdleMonitor ourInstance;
    private static IdleCallback callback;
    private Handler timer;
    private Runnable task;

    private IdleMonitor() {
        //Nothing needed here
    }

    public static IdleMonitor getInstance() {
        if (ourInstance == null) {
            ourInstance = new IdleMonitor();
        }

        return ourInstance;
    }

    private static void setDeviceStateIdle() {
        Log.d(TAG, "Device state idle");
        callback.onDeviceStateIdle();
    }

    public void setTimer() {
        nullify();
        setTask();
        timer = new Handler();
        //After 5 mins inactivity
        timer.postDelayed(task, 300000);
        Log.d(TAG, "Normal timer set");
    }

    public void setShortTimer() {
        nullify();
        setTask();
        timer = new Handler();
        //After 45 secs inactivity
        timer.postDelayed(task, 45000);
        Log.d(TAG, "Short timer set");
    }

    private void setTask() {
        task = new Runnable() {
            @Override
            public void run() {
                setDeviceStateIdle();
            }
        };
    }

    public void nullify() {
        if (task != null) {
            task = null;
        }
        if (timer != null) {
            //Remove all callbacks and messages
            timer.removeCallbacksAndMessages(null);
            timer = null;
            Log.d(TAG, "Timer nullified");
        }
    }

    public void registerIdleCallback(IdleCallback callback) {
        IdleMonitor.callback = callback;
    }

    public interface IdleCallback {
        void onDeviceStateIdle();
    }
}
