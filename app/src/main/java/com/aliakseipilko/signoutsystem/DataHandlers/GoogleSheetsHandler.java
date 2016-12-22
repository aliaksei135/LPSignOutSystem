/*
 * com.aliakseipilko.signoutsystem.DataHandlers.GoogleSheetsHandler was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2016.  All Rights Reserved.
 *
 * Last modified 22/12/16 15:35
 */

package com.aliakseipilko.signoutsystem.DataHandlers;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.annotation.Keep;

import com.aliakseipilko.signoutsystem.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Keep
public class GoogleSheetsHandler {

    private static GoogleSheetsHandler ourInstance;
    private final String SPREADSHEET_ID;
    private GoogleCredential credential;
    private ConnectivityManager cm;
    private SharedPreferences sp;
    private int cachedRequestCount = 0;

    private GoogleSheetsHandler(Context context) {
        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        sp = context.getSharedPreferences("CachedGSheetsRequests", Context.MODE_PRIVATE);

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

        return new GetNotifsTask().execute().get();
    }

    public List<String> getLatestNotifs(GoogleCredential credential) throws ExecutionException, InterruptedException {
        this.credential = credential;
        return new GetNotifsTask().execute().get();
    }

    public boolean makeNewLogEntryAsync(String[] singleRowData, GoogleCredential credential) {

        this.credential = credential;

        if (cm.getActiveNetworkInfo().isConnected() && credential != null) {
            boolean result = false;
            try {
                result = new MakeUpdateTask().execute(singleRowData).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            return result;
        } else {
            sp.edit().putStringSet(Integer.toString(cachedRequestCount), new HashSet<>(Arrays.asList(singleRowData))).apply();
            cachedRequestCount++;
            return true;
        }
    }

    public boolean makeNewLogEntrySync(String[] singleRowData, GoogleCredential credential) {

        this.credential = credential;

        if (cm.getActiveNetworkInfo().isConnected() && credential != null) {
            Sheets mService = getSheetsService();

            ValueRange range = new ValueRange();
            List<List<Object>> valuesList = new ArrayList<>();
            valuesList.add(Arrays.<Object>asList(singleRowData));
            range.setValues(valuesList);

            try {
                mService.spreadsheets().values().append(SPREADSHEET_ID, "Log!A1:D", range).setValueInputOption("RAW").execute();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

        } else {
            sp.edit().putStringSet(Integer.toString(cachedRequestCount), new HashSet<>(Arrays.asList(singleRowData))).apply();
            cachedRequestCount++;
            return true;
        }
    }

    public void dispatchCachedRequests() {
        if (cachedRequestCount < 1) {
            return;
        }

        for (int i = 0; i <= cachedRequestCount; i++) {
            Set<String> s = sp.getStringSet(Integer.toString(i), null);
            if (s != null) {
                String[] sa = new String[5];
                s.toArray(sa);
                if (sa.length < 2) {
                    new MakeUpdateTask().execute(sa);
                }
            }
        }
        cachedRequestCount = 0;
    }

    private Sheets getSheetsService() {

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        return new Sheets.Builder(transport, JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("Sign Out System")
                .build();
    }

    private class MakeUpdateTask extends AsyncTask<String, Void, Boolean> {
        private com.google.api.services.sheets.v4.Sheets mService = null;

        MakeUpdateTask() {
            mService = getSheetsService();
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
