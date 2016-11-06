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
