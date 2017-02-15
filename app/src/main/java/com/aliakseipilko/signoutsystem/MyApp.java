/*
 * com.aliakseipilko.signoutsystem.MyApp was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2017.  All Rights Reserved.
 *
 * Last modified 15/02/17 14:46
 */

package com.aliakseipilko.signoutsystem;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

import io.realm.Realm;
import io.realm.RealmConfiguration;


public class MyApp extends Application {

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

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
        //This is obfuscated by Proguard so is inaccessible and unreadable
        byte[] key = hexStringToByteArray("5b9fd2a46d1e47007893ff93fd75d5a161f80e671ae10466fd205201441b7ecd024be0f6dda2b290917d172c3bfe417042d64db61cd73c7e8fe8ed1370a3187c");
        RealmConfiguration config = new RealmConfiguration.Builder()
                .encryptionKey(key)
                .build();
        Realm.setDefaultConfiguration(config);
    }
}
