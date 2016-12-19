/*
 * com.aliakseipilko.signoutsystem.Fragments.notifFragment was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2016.  All Rights Reserved.
 *
 * Last modified 19/12/16 18:16
 */

package com.aliakseipilko.signoutsystem.Fragments;

import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.MemoryDataStoreFactory;

import android.os.Bundle;
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
    private int index;
    private GoogleSheetsHandler sheetsHandler;
    private StoredCredential storedCredential;

    public notifFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        sheetsHandler = GoogleSheetsHandler.getInstance(getContext());
        try {
            DataStore<StoredCredential> credentialDataStore = MemoryDataStoreFactory.getDefaultInstance().getDataStore("credentialDataStore");
            storedCredential = credentialDataStore.get("default");
        } catch (IOException e) {
            e.printStackTrace();
        }


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
            notifList = getNotifList();
            notifLastUpdateTimeMillis = System.currentTimeMillis();
        }

        if (index >= notifList.size()) {
            index = 0;
        } else {
            notifSwitcher.setText(notifList.get(index));
            index++;
        }
    }

    private List<String> getNotifList() {

        try {
            GoogleCredential credential = new GoogleCredential.Builder()
                    .setTransport(AndroidHttp.newCompatibleTransport())
                    .setJsonFactory(JacksonFactory.getDefaultInstance())
                    .build();
            credential.setAccessToken(storedCredential.getAccessToken());
            credential.setRefreshToken(storedCredential.getRefreshToken());
            credential.setExpirationTimeMilliseconds(storedCredential.getExpirationTimeMilliseconds());
            return sheetsHandler.getLatestNotifs(credential);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

}
