/*
 * com.aliakseipilko.signoutsystem.Helpers.IdleMonitor was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2016.  All Rights Reserved.
 *
 * Last modified 18/11/16 21:07
 */

package com.aliakseipilko.signoutsystem.Helpers;

import android.os.Handler;

public class IdleMonitor {

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
        callback.onDeviceStateIdle();
    }

    public void setTimer() {
        nullify();
        setTask();
        timer = new Handler();
        //After 5 mins inactivity
        timer.postDelayed(task, 300000);
    }

    public void setShortTimer() {
        nullify();
        setTask();
        timer = new Handler();
        //After 45 secs inactivity
        timer.postDelayed(task, 45000);
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
        }
    }

    public void registerIdleCallback(IdleCallback callback) {
        IdleMonitor.callback = callback;
    }

    public interface IdleCallback {
        void onDeviceStateIdle();
    }
}
