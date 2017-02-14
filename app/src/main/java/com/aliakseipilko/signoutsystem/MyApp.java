/*
 * com.aliakseipilko.signoutsystem.MyApp was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2017.  All Rights Reserved.
 *
 * Last modified 14/02/17 13:50
 */

package com.aliakseipilko.signoutsystem;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

import io.realm.Realm;


public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        Realm.init(getApplicationContext());
    }
}
