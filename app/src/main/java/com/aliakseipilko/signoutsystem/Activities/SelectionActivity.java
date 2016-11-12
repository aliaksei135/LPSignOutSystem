/*
 * com.aliakseipilko.signoutsystem.Activities.SelectionActivity was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2016.  All Rights Reserved.
 *
 * Last modified 12/11/16 15:12
 */

package com.aliakseipilko.signoutsystem.Activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aliakseipilko.signoutsystem.Fragments.stateAtGreenFragment;
import com.aliakseipilko.signoutsystem.Fragments.stateSignedInFragment;
import com.aliakseipilko.signoutsystem.Fragments.stateSignedOutFragment;
import com.aliakseipilko.signoutsystem.Fragments.stateStudyPeriodFragment;
import com.aliakseipilko.signoutsystem.Fragments.stateVisitHouseFragment;
import com.aliakseipilko.signoutsystem.R;

public class SelectionActivity extends AppCompatActivity implements
        stateSignedInFragment.OnFragmentInteractionListener,
        stateSignedOutFragment.OnFragmentInteractionListener,
        stateAtGreenFragment.OnFragmentInteractionListener,
        stateStudyPeriodFragment.OnFragmentInteractionListener,
        stateVisitHouseFragment.OnFragmentInteractionListener {

    ProgressDialog mProgressDialog;
    long id;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSysUI();
        setContentView(R.layout.activity_selection);
        Intent intent = getIntent();
        String state = intent.getStringExtra("state");
        name = intent.getStringExtra("name");
        int year = intent.getIntExtra("year", 13);
        id = intent.getLongExtra("id", -1);

        TextView currentStateTextView = (TextView) findViewById(R.id.currentStateTextView);
        if (currentStateTextView != null) {
            currentStateTextView.setText(state);
        }

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putInt("year", year);

            switch (state) {
                case "SIGN_IN":
                    stateSignedInFragment stateSignedInFrag = stateSignedInFragment.newInstance();
                    stateSignedInFrag.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateSignedInFrag).commit();
                    break;
                case "Signed In":
                    stateSignedInFrag = stateSignedInFragment.newInstance();
                    stateSignedInFrag.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateSignedInFrag).commit();
                    break;
                case "SIGN_OUT":
                    stateSignedOutFragment stateSignedOutFrag = stateSignedOutFragment.newInstance();
                    stateSignedOutFrag.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateSignedOutFrag).commit();
                    break;
                case "Signed Out":
                    stateSignedOutFrag = stateSignedOutFragment.newInstance();
                    stateSignedOutFrag.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateSignedOutFrag).commit();
                    break;
                case "AT_GREEN":
                    stateAtGreenFragment stateAtGreenFrag = stateAtGreenFragment.newInstance();
                    stateAtGreenFrag.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateAtGreenFrag).commit();
                    break;
                case "Gone to Green":
                    stateAtGreenFrag = stateAtGreenFragment.newInstance();
                    stateAtGreenFrag.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateAtGreenFrag).commit();
                    break;
                case "STUDY_PERIOD":
                    stateStudyPeriodFragment stateStudyPeriodFrag = stateStudyPeriodFragment.newInstance();
                    stateStudyPeriodFrag.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateStudyPeriodFrag).commit();
                    break;
                case "Study Period":
                    stateStudyPeriodFrag = stateStudyPeriodFragment.newInstance();
                    stateStudyPeriodFrag.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateStudyPeriodFrag).commit();
                    break;
                case "VISIT_HOUSE_FIELD":
                    stateVisitHouseFragment stateVisitFieldHouseFrag = stateVisitHouseFragment.newInstance("FIELD");
                    stateVisitFieldHouseFrag.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateVisitFieldHouseFrag).commit();
                    break;
                case "Visiting Field":
                    stateVisitFieldHouseFrag = stateVisitHouseFragment.newInstance("FIELD");
                    stateVisitFieldHouseFrag.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateVisitFieldHouseFrag).commit();
                    break;
                case "VISIT_HOUSE_GROVE":
                    stateVisitHouseFragment stateVisitGroveHouseFrag = stateVisitHouseFragment.newInstance("GROVE");
                    stateVisitGroveHouseFrag.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateVisitGroveHouseFrag).commit();
                    break;
                case "Visiting Grove":
                    stateVisitGroveHouseFrag = stateVisitHouseFragment.newInstance("GROVE");
                    stateVisitGroveHouseFrag.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateVisitGroveHouseFrag).commit();
                    break;
                case "VISIT_HOUSE_RECKITT":
                    stateVisitHouseFragment stateVisitReckittHouseFrag = stateVisitHouseFragment.newInstance("RECKITT");
                    stateVisitReckittHouseFrag.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateVisitReckittHouseFrag).commit();
                    break;
                case "Visiting Reckitt":
                    stateVisitReckittHouseFrag = stateVisitHouseFragment.newInstance("RECKITT");
                    stateVisitReckittHouseFrag.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateVisitReckittHouseFrag).commit();
                    break;
                case "VISIT_HOUSE_FRYER":
                    stateVisitHouseFragment stateVisitFryerHouseFrag = stateVisitHouseFragment.newInstance("FRYER");
                    stateVisitFryerHouseFrag.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateVisitFryerHouseFrag).commit();
                    break;
                case "Visiting Fryer":
                    stateVisitFryerHouseFrag = stateVisitHouseFragment.newInstance("FRYER");
                    stateVisitFryerHouseFrag.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, stateVisitFryerHouseFrag).commit();
                    break;
                default:
                    //Display all possible states fragment
                    //TODO Handle this
                    break;
            }
        }
    }

    private void showSysUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void hideSysUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void userActionConfirm(final Intent resultIntent) {

        String type = resultIntent.getStringExtra("location");

        //Gotta have *some* easter eggs :)
        switch (type) {
            case "Visiting Grove":
                type = "Why would you want to do that? Are you sure you want to visit Grove";
                break;
            case "Visiting Field":
                type = "Last chance to turn back... Are you sure you want to visit Field";
                break;
            case "Visiting Reckitt":
                type = "It's a long trek to a land far away, do you really want to visit Reckitt";
                break;
            default:
                break;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");

        TextView textView = new TextView(this);
        textView.setText(type + "?");
        textView.setGravity(Gravity.CENTER);
        textView.setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Large);
        textView.setPadding(5, 5, 5, 5);
        builder.setView(textView);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showProgressDialog();
                sendResultIntent(resultIntent);
            }
        });

        //No need for OCL as result defaults to false
        builder.setNegativeButton("No", null);

        AlertDialog dialog = builder.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Large);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Large);

    }

    private void sendResultIntent(Intent result) {
        setResult(RESULT_OK, result);
        finish();
    }

    private void showProgressDialog() {

        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {

        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @Override
    //Fragment Listener interface implementation
    public void onFragmentInteraction(String type) {
        Intent result = new Intent();
        result.putExtra("id", id);
        switch (type) {
            case "CANCEL":
                setResult(RESULT_CANCELED);
                finish();
                break;
            case "SIGN_OUT":
                result.putExtra("name", name);
                result.putExtra("location", "Signed Out");
                //Not functional for now, added for compatibility with other methods in future eg NFC
                result.putExtra("type", "Fingerprint");
                break;
            case "GREEN":
                result.putExtra("name", name);
                result.putExtra("location", "Gone to Green");
                //Not functional for now, added for compatibility with other methods in future eg NFC
                result.putExtra("type", "Fingerprint");
                break;
            case "STUDY_PERIOD":
                result.putExtra("name", name);
                result.putExtra("location", "Study Period");
                //Not functional for now, added for compatibility with other methods in future eg NFC
                result.putExtra("type", "Fingerprint");
                break;
            case "VISIT_HOUSE_FIELD":
                result.putExtra("name", name);
                result.putExtra("location", "Visiting Field");
                //Not functional for now, added for compatibility with other methods in future eg NFC
                result.putExtra("type", "Fingerprint");
                break;
            case "VISIT_HOUSE_GROVE":
                result.putExtra("name", name);
                result.putExtra("location", "Visiting Grove");
                //Not functional for now, added for compatibility with other methods in future eg NFC
                result.putExtra("type", "Fingerprint");
                break;
            case "VISIT_HOUSE_RECKITT":
                result.putExtra("name", name);
                result.putExtra("location", "Visiting Reckitt");
                //Not functional for now, added for compatibility with other methods in future eg NFC
                result.putExtra("type", "Fingerprint");
                break;
            case "VISIT_HOUSE_FRYER":
                result.putExtra("name", name);
                result.putExtra("location", "Visiting Fryer");
                //Not functional for now, added for compatibility with other methods in future eg NFC
                result.putExtra("type", "Fingerprint");
                break;
            case "SIGN_IN":
                result.putExtra("name", name);
                result.putExtra("location", "Signed In");
                //Not functional for now, added for compatibility with other methods in future eg NFC
                result.putExtra("type", "Fingerprint");
                break;
            default:
                Toast.makeText(this, "That didn't work!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
        if (!type.equals("CANCEL")) {
            userActionConfirm(result);
        }
    }

}
