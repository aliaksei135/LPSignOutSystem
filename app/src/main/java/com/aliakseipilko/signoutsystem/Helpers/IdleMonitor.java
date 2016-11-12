/*
 * com.aliakseipilko.signoutsystem.Helpers.IdleMonitor was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2016.  All Rights Reserved.
 *
 * Last modified 12/11/16 19:21
 */

package com.aliakseipilko.signoutsystem.Helpers;

import java.util.Timer;
import java.util.TimerTask;

public class IdleMonitor {

    private static IdleMonitor ourInstance;

    private static IdleCallback callback;
    private Timer timer;

    private TimerTask task;

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
        timer = new Timer("Idle Monitor");
        //After 5 mins inactivity
        timer.schedule(task, 300000);
    }

    public void setShortTimer() {
        nullify();
        setTask();
        timer = new Timer("Short Idle Monitor");
        //After 45 secs inactivity
        timer.schedule(task, 45000);
    }

    private void setTask() {
        task = new TimerTask() {
            @Override
            public void run() {
                setDeviceStateIdle();
            }
        };
    }

    public void nullify() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (timer != null) {
            timer.cancel();
            timer.purge();
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
