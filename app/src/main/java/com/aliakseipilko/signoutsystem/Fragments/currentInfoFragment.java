/*
 * com.aliakseipilko.signoutsystem.Fragments.currentInfoFragment was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2017.  All Rights Reserved.
 *
 * Last modified 22/01/17 11:44
 */

package com.aliakseipilko.signoutsystem.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Keep;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

import com.aliakseipilko.signoutsystem.Helpers.WeatherRemoteFetch;
import com.aliakseipilko.signoutsystem.R;
import com.squareup.picasso.Picasso;

import java.util.Map;

@Keep
public class currentInfoFragment extends Fragment implements WeatherRemoteFetch.WeatherCallback {

    private static final String ICON_BASE_URL = "http://openweathermap.org/img/w/";
    //    private static Context context;
    //UI Elements
    private static TextView currentTemp;
    private static ImageView weatherIcon;
    private Context ctx;

    public currentInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_current_info, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        weatherIcon = (ImageView) getView().findViewById(R.id.weather_icon);
        currentTemp = (TextView) getView().findViewById(R.id.current_temp);
        TextClock timeClock = (TextClock) getView().findViewById(R.id.timeTextClock);
        TextClock dateClock = (TextClock) getView().findViewById(R.id.dateTextClock);

        timeClock.setFormat24Hour("HH:mm:ss");
        dateClock.setFormat24Hour("EEEE dd\nMMMM");

    }

    public void updateWeather(WeatherRemoteFetch weatherFetch, Context ctx) {
        this.ctx = ctx;
        weatherFetch.getCurrentWeather();
        weatherFetch.setWeatherReadyCallback(this);
    }

    @Override
    public void weatherReadyCallback(final Map<String, Object> result) {

        String temp = (String) result.get("Temperature");
        temp = String.valueOf(Math.round(Double.parseDouble(temp)));
        Handler handler = new Handler(Looper.getMainLooper());
        final String finalTemp = temp;
        handler.post(new Runnable() {
            @Override
            public void run() {
                currentTemp.setText(finalTemp + "℃");

                Picasso.with(ctx)
                        .load(ICON_BASE_URL + result.get("Icon") + ".png")
                        .resize(90, 90)
                        .into(weatherIcon);
            }
        });

    }

    @Override
    public void weatherForecastReadyCallback(Map<String, Map<String, Object>> forecast) {
        //Not needed here
    }

}
