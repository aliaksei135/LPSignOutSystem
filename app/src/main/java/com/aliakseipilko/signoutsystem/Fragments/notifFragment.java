/*
 * com.aliakseipilko.signoutsystem.Fragments.notifFragment was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2017.  All Rights Reserved.
 *
 * Last modified 18/03/17 21:08
 */

package com.aliakseipilko.signoutsystem.Fragments;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.aliakseipilko.signoutsystem.DataHandlers.GoogleSheetsHandler;
import com.aliakseipilko.signoutsystem.R;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class notifFragment extends Fragment {

    private Long notifLastUpdateTimeMillis;
    private TextSwitcher notifSwitcher;
    private List<String> notifList;
    private int index = 0;
    private GoogleSheetsHandler sheetsHandler;


    public notifFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        sheetsHandler = GoogleSheetsHandler.getInstance(getContext());

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notif, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        notifSwitcher = (TextSwitcher) getView().findViewById(R.id.notifTextSwitcher);
        notifSwitcher.setInAnimation(getContext(), android.R.anim.slide_in_left);
        notifSwitcher.setOutAnimation(getContext(), android.R.anim.slide_out_right);

        ViewSwitcher.ViewFactory viewFactory = new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {

                TextView t = new TextView(getContext());
                t.setGravity(Gravity.CENTER);
                t.setTextAppearance(getContext(), android.R.style.TextAppearance_Large);
                return t;
            }
        };
        notifSwitcher.setFactory(viewFactory);
        displayNotifsView();
    }

    public void displayNotifsView() {

        //Update Notifs if old
        if (notifList == null ||
                notifLastUpdateTimeMillis == null
                || (System.currentTimeMillis() - notifLastUpdateTimeMillis) >= 900000) { //Update if older than 15mins
            Log.d("NotifFragment", "Notifications List is Updated");
            List<String> temp = getNotifList();
            if (temp != null && temp.size() >= 1) {
                notifList = temp;
                notifLastUpdateTimeMillis = System.currentTimeMillis();
            }
        }

        if (index >= notifList.size()) {
            index = 0;
        }

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                notifSwitcher.setText(notifList.get(index));
                index++;
            }
        });
    }

    private GoogleCredential getCredential() {

        AccountManager am = AccountManager.get(getContext());
        Account[] accounts = am.getAccounts();
        AccountManagerFuture<Bundle> amf = am.getAuthToken(accounts[0], "oauth2:https://www.googleapis.com/auth/spreadsheets", null, true, null, null);
        try {
            return new GetCredentialTask().execute(amf).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<String> getNotifList() {

        try {
            return sheetsHandler.getLatestNotifs(getCredential());
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return notifList;
        }
    }

    private class GetCredentialTask extends AsyncTask<AccountManagerFuture<Bundle>, Void, GoogleCredential> {

        @SafeVarargs
        @Override
        protected final GoogleCredential doInBackground(AccountManagerFuture<Bundle>... params) {
            try {
                Bundle result = params[0].getResult();
                GoogleCredential credential = new GoogleCredential();
                credential.setAccessToken(result.getString(AccountManager.KEY_AUTHTOKEN));
                return credential;
            } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

}
