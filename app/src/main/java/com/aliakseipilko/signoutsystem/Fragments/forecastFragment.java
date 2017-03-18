/*
 * com.aliakseipilko.signoutsystem.Fragments.forecastFragment was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2017.  All Rights Reserved.
 *
 * Last modified 18/03/17 21:33
 */

package com.aliakseipilko.signoutsystem.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliakseipilko.signoutsystem.Helpers.WeatherRemoteFetch;
import com.aliakseipilko.signoutsystem.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class forecastFragment extends Fragment implements WeatherRemoteFetch.WeatherCallback {

    private static final String ICON_BASE_URL = "http://openweathermap.org/img/w/";
    //UI elements
    @BindView(R.id.firstHourTimeTextView)
    TextView firstTimeView;
    @BindView(R.id.secondHourTimeTextView)
    TextView secondTimeView;
    @BindView(R.id.thirdHourTimeTextView)
    TextView thirdTimeView;
    @BindView(R.id.firstHourForecastImageView)
    ImageView firstIconView;
    @BindView(R.id.secondHourForecastImageView)
    ImageView secondIconView;
    @BindView(R.id.thirdHourForecastImageView)
    ImageView thirdIconView;
    @BindView(R.id.firstHourForecastTextView)
    TextView firstTempView;
    @BindView(R.id.secondHourForecastTextView)
    TextView secondTempView;
    @BindView(R.id.thirdHourForecastTextView)
    TextView thirdTempView;
    Unbinder unbinder;
    private Context ctx;

    public forecastFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_forecast, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    public void updateWeather(WeatherRemoteFetch weatherFetch, Context ctx) {
        this.ctx = ctx;
        weatherFetch.getForecastWeather();
        weatherFetch.setForecastCallback(this);
    }

    @Override
    public void weatherReadyCallback(Map<String, Object> result) {
        //Not needed here
    }

    @Override
    public void weatherForecastReadyCallback(final Map<String, Map<String, Object>> forecast) {

        String firstTemp = (String) forecast.get("0").get("Temperature");
        String secondTemp = (String) forecast.get("3").get("Temperature");
        String thirdTemp = (String) forecast.get("6").get("Temperature");

        firstTemp = String.valueOf(Math.round(Double.parseDouble(firstTemp)));
        secondTemp = String.valueOf(Math.round(Double.parseDouble(secondTemp)));
        thirdTemp = String.valueOf(Math.round(Double.parseDouble(thirdTemp)));

        final SimpleDateFormat format = new SimpleDateFormat("HH:mm");

        final Date firstTime = new Date((long) (forecast.get("0").get("Timestamp")) * 1000);
        final Date secondTime = new Date((long) (forecast.get("3").get("Timestamp")) * 1000);
        final Date thirdTime = new Date((long) (forecast.get("6").get("Timestamp")) * 1000);

        Handler handler = new Handler(Looper.getMainLooper());
        final String finalFirstTemp = firstTemp;
        final String finalSecondTemp = secondTemp;
        final String finalThirdTemp = thirdTemp;
        handler.post(new Runnable() {
            @Override
            public void run() {
                firstTempView.setText(finalFirstTemp + "℃");
                secondTempView.setText(finalSecondTemp + "℃");
                thirdTempView.setText(finalThirdTemp + "℃");

                firstTimeView.setText(format.format(firstTime));
                secondTimeView.setText(format.format(secondTime));
                thirdTimeView.setText(format.format(thirdTime));

                Picasso.with(ctx)
                        .load(ICON_BASE_URL + forecast.get("0").get("Icon") + ".png")
                        .resize(80, 80)
                        .into(firstIconView);
                Picasso.with(ctx)
                        .load(ICON_BASE_URL + forecast.get("3").get("Icon") + ".png")
                        .resize(80, 80)
                        .into(secondIconView);
                Picasso.with(ctx)
                        .load(ICON_BASE_URL + forecast.get("3").get("Icon") + ".png")
                        .resize(80, 80)
                        .into(thirdIconView);
            }
        });


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}
