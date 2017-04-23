/*
 * com.aliakseipilko.signoutsystem.Activities.ManageUserActivity was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2017.  All Rights Reserved.
 *
 * Last modified 23/04/17 20:43
 */

package com.aliakseipilko.signoutsystem.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.aliakseipilko.signoutsystem.DataHandlers.LocalRealmDBHandler;
import com.aliakseipilko.signoutsystem.DataHandlers.models.User;
import com.aliakseipilko.signoutsystem.R;

import butterknife.ButterKnife;


public class ManageUserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_manage_user);
        ButterKnife.bind(this);

        Intent i = getIntent();
        long id = i.getLongExtra("id", -1);

        LocalRealmDBHandler db = new LocalRealmDBHandler();
        User user = db.getRecordById(id);

    }

}
