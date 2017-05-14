/*
 * com.aliakseipilko.signoutsystem.MyApp was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2017.  All Rights Reserved.
 *
 * Last modified 08/05/17 22:10
 */

package com.aliakseipilko.signoutsystem;

import com.google.common.base.Charsets;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

import de.adorsys.android.securestoragelibrary.SecurePreferences;

import java.math.BigInteger;
import java.security.SecureRandom;

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

        String strKey = SecurePreferences.getStringValue("REALM_KEY", this, null);

        if (strKey == null) {
            strKey = new BigInteger(212, new SecureRandom()).toString(64);
            try {
                SecurePreferences.setValue("REALM_KEY", strKey, this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        byte[] key = strKey.getBytes(Charsets.UTF_8);

        RealmConfiguration config = new RealmConfiguration.Builder()
                .encryptionKey(key)
                .build();
        Realm.setDefaultConfiguration(config);

    }
}
