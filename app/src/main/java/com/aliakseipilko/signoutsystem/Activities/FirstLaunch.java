/*
 * com.aliakseipilko.signoutsystem.Activities.FirstLaunch was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2017.  All Rights Reserved.
 *
 * Last modified 08/05/17 22:10
 */

package com.aliakseipilko.signoutsystem.Activities;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.sheets.v4.SheetsScopes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.aliakseipilko.signoutsystem.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FirstLaunch extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "FirstLaunch";
    @BindView(R.id.google_sign_in_button)
    SignInButton gSignInButton;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_launch);
        ButterKnife.bind(this);

        gSignInButton.setSize(SignInButton.SIZE_WIDE);
        gSignInButton.setColorScheme(SignInButton.COLOR_DARK);
        gSignInButton.setEnabled(true);
        gSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(SheetsScopes.SPREADSHEETS))
                .requestServerAuthCode(FirstLaunch.this.getResources().getString(R.string.server_client_id))
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addScope(new Scope(SheetsScopes.SPREADSHEETS))
                .build();
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.d(TAG, "Sign in result: " + result.isSuccess());
            if (result.isSuccess()) {
                if (result.getSignInAccount().getServerAuthCode() != null) {
                    prefs.edit().putBoolean("isFirstRun", false).commit();
                    setResult(RESULT_OK, new Intent(this, MainActivity.class).putExtra("result", "Success!").putExtra("authCode", result.getSignInAccount().getServerAuthCode()));
                    finish();
                } else {
                    Toast.makeText(this, "Server didn't return auth code!\nApp terminated", Toast.LENGTH_SHORT).show();
                    System.exit(1);
                }
            } else {
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        setResult(RESULT_CANCELED, new Intent(this, MainActivity.class).putExtra("result", "Connection Failed"));
        finish();
    }
}
