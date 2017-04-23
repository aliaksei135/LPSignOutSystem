/*
 * com.aliakseipilko.signoutsystem.Activities.ManageSearchActivity was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2017.  All Rights Reserved.
 *
 * Last modified 23/04/17 20:43
 */

package com.aliakseipilko.signoutsystem.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;

import com.aliakseipilko.signoutsystem.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ManageSearchActivity extends AppCompatActivity {

    @BindView(R.id.searchfield)
    EditText searchField;
    @BindView(R.id.search_button)
    Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_manage_search);

        ButterKnife.bind(this);

    }

    @OnClick(R.id.search_button)
    private void validateSearchField() {
        if (searchField.getText().length() <= 0) {
            searchField.setError("Enter search text");
        } else {
            String searchText = searchField.getText().toString().trim();
            Intent i = new Intent(this, ManageResultsActivity.class).putExtra("query", searchText);
            startActivityForResult(i, 704);
        }
    }

}
