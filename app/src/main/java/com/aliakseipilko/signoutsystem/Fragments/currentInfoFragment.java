/*
 * com.aliakseipilko.signoutsystem.Fragments.currentInfoFragment was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2017.  All Rights Reserved.
 *
 * Last modified 18/03/17 21:33
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

@Keep
public class currentInfoFragment extends Fragment implements WeatherRemoteFetch.WeatherCallback {

    private static final String ICON_BASE_URL = "http://openweathermap.org/img/w/";
    //    private static Context context;
    //UI Elements
    @BindView(R.id.weather_icon)
    ImageView weatherIcon;
    @BindView(R.id.current_temp)
    TextView currentTemp;
    @BindView(R.id.timeTextClock)
    TextClock timeClock;
    @BindView(R.id.dateTextClock)
    TextClock dateClock;
    Unbinder unbinder;
    private Context ctx;

    public currentInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_current_info, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
