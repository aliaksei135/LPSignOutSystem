/*
 * com.aliakseipilko.signoutsystem.DataHandlers.GoogleSheetsHandler was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2016.  All Rights Reserved.
 *
 * Last modified 11/11/16 20:11
 */

package com.aliakseipilko.signoutsystem.DataHandlers;

import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Keep;

import com.aliakseipilko.signoutsystem.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Keep
public class GoogleSheetsHandler {

    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static GoogleSheetsHandler ourInstance;
    private static String authCode;
    private final String SPREADSHEET_ID;
    private final Context context;
    private GoogleCredential credential;

    private GoogleSheetsHandler(Context context) {
        this.context = context;

        SPREADSHEET_ID = context.getString(R.string.SPREADSHEET_ID);
    }

    public static GoogleSheetsHandler getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new GoogleSheetsHandler(context);
            return ourInstance;
        } else {
            return ourInstance;
        }
    }

    public List<String> getLatestNotifs(String authCode) throws ExecutionException, InterruptedException {

        GoogleSheetsHandler.authCode = authCode;
        return new GetNotifsTask().execute().get();
    }

    public boolean makeNewLogEntry(String[] singleRowData, String authCode) {

        boolean result = false;
        GoogleSheetsHandler.authCode = authCode;
        try {
            result = new MakeUpdateTask().execute(singleRowData).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return result;
    }


    private Sheets getSheetsService() {

        if (credential == null) {
            try {
                credential = new GetAuthorisedCredentialTask().execute().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return null;
            }
        } else if (credential.getExpiresInSeconds() != null) {
            if (credential.getExpiresInSeconds() < 10) {
                try {
                    if (!credential.refreshToken()) {
                        credential = null;
                        try {
                            credential = new GetAuthorisedCredentialTask().execute().get();
                        } catch (InterruptedException | ExecutionException ex) {
                            ex.printStackTrace();
                            return null;
                        }
                    }
                } catch (TokenResponseException e) {
                    e.printStackTrace();
                    credential = null;
                    try {
                        credential = new GetAuthorisedCredentialTask().execute().get();
                    } catch (InterruptedException | ExecutionException ex) {
                        ex.printStackTrace();
                        return null;
                    }
                } catch (IOException exc) {
                    exc.printStackTrace();
                }
            }
        }
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        return new Sheets.Builder(transport, JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("Sign Out System")
                .build();
    }

    private class GetAuthorisedCredentialTask extends AsyncTask<Void, Void, GoogleCredential> {


        @Override
        protected GoogleCredential doInBackground(Void... params) {

            try {
                return authorise();
            } catch (IOException e) {
                e.printStackTrace();
                cancel(true);
                return null;
            }
        }

        GoogleCredential authorise() throws IOException {
            if (authCode == null) {
                throw new IOException("No Server Auth Code!");
            }
            InputStream is = context.getResources().openRawResource(R.raw.client_secret);
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(), new BufferedReader(new InputStreamReader(is)));

            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    transport,
                    JacksonFactory.getDefaultInstance(),
                    clientSecrets.getDetails().getClientId(),
                    clientSecrets.getDetails().getClientSecret(),
                    authCode,
                    "")
                    .setScopes(SCOPES)
                    .execute();
            tokenResponse.setExpiresInSeconds((long) 3600);
            String accessToken = tokenResponse.getAccessToken();

            GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
            System.out.println(credential.toString());
            return credential;
        }
    }

    private class MakeUpdateTask extends AsyncTask<String, Void, Boolean> {
        private com.google.api.services.sheets.v4.Sheets mService = null;

        MakeUpdateTask() {
            mService = getSheetsService();
            if (mService == null) {
                cancel(true);
            }
        }

        @Override
        protected Boolean doInBackground(String... params) {
            return updateDatafromApi(params);
        }

        private Boolean updateDatafromApi(String[] params) {

            ValueRange range = new ValueRange();
            List<List<Object>> valuesList = new ArrayList<>();
            valuesList.add(Arrays.<Object>asList(params));
            range.setValues(valuesList);

            try {
                mService.spreadsheets().values().append(SPREADSHEET_ID, "Log!A1:D", range).setValueInputOption("RAW").execute();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private class GetNotifsTask extends AsyncTask<Void, Void, List<String>> {

        private com.google.api.services.sheets.v4.Sheets mService = null;

        GetNotifsTask() {
            mService = getSheetsService();
            if (mService == null) {
                cancel(true);
            }
        }

        @Override
        protected List<String> doInBackground(Void... params) {

            List<String> result;
            try {
                result = getNotifs();
            } catch (Exception e) {
                e.printStackTrace();
                cancel(true);
                return null;
            }
            return result;
        }

        private List<String> getNotifs() throws IOException {

            List<String> results = new ArrayList<>();
            ValueRange response = this.mService.spreadsheets().values()
                    .get(SPREADSHEET_ID, "Notifications!A2:A")
                    .execute();
            List<List<Object>> values = response.getValues();
            if (values != null) {
                for (List row : values) {
                    //Get first element, only first column is needed
                    results.add((String) row.get(0));
                }
            }
            return results;
        }
    }
}
