/*
 * com.aliakseipilko.signoutsystem.Activities.ManageResultsActivity was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2017.  All Rights Reserved.
 *
 * Last modified 23/04/17 20:43
 */

package com.aliakseipilko.signoutsystem.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.aliakseipilko.signoutsystem.DataHandlers.LocalRealmDBHandler;
import com.aliakseipilko.signoutsystem.DataHandlers.models.User;
import com.aliakseipilko.signoutsystem.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ManageResultsActivity extends AppCompatActivity {

    @BindView(R.id.results_listview)
    ListView resultsLv;

    List<User> results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_manage_results);
        ButterKnife.bind(this);

        Intent i = getIntent();
        String query = i.getStringExtra("query");
        LocalRealmDBHandler db = new LocalRealmDBHandler();
        results = db.searchRecords(query);

        ListAdapter adapter = new ArrayAdapter<User>(this, R.layout.list_holder, results);

        resultsLv.setAdapter(adapter);

        resultsLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                long recordId = ((User) parent.getAdapter().getItem(position)).getId();
                Intent i = new Intent(ManageResultsActivity.this, ManageUserActivity.class);
                i.putExtra("id", recordId);
                startActivityForResult(i, 705);
            }
        });
    }

}
