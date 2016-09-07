package com.apilko.signoutsystem.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

import com.apilko.signoutsystem.Helpers.WeatherRemoteFetch;
import com.apilko.signoutsystem.R;
import com.squareup.picasso.Picasso;

import java.util.Map;

@Keep
public class currentInfoFragment extends Fragment implements WeatherRemoteFetch.WeatherCallback {

    private static final String ICON_BASE_URL = "http://openweathermap.org/img/w/";
    //UI Elements
    private static TextView currentTemp;
    private static ImageView weatherIcon;

    private static Context context;

    WeatherRemoteFetch weatherFetch;

    public currentInfoFragment() {
        // Required empty public constructor
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_current_info, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        weatherFetch = WeatherRemoteFetch.getInstance(getContext());

        currentTemp = (TextView) getView().findViewById(R.id.current_temp);
        weatherIcon = (ImageView) getView().findViewById(R.id.weather_icon);
        TextClock timeClock = (TextClock) getView().findViewById(R.id.timeTextClock);
        TextClock dateClock = (TextClock) getView().findViewById(R.id.dateTextClock);

        timeClock.setFormat24Hour("HH:mm:ss");
        dateClock.setFormat24Hour("EEEE dd\nMMMM yyyy");

    }

    public void updateWeather(Context context) {

        currentInfoFragment.context = context;

        if (weatherFetch == null) {
            weatherFetch = WeatherRemoteFetch.getInstance(context);
        }
        weatherFetch.getCurrentWeather();
        weatherFetch.setWeatherReadyCallback(this);
    }


    @Override
    public void weatherReadyCallback(Map<String, Object> result) {

        String temp = (String) result.get("Temperature");
        temp = String.valueOf(Math.round(Double.parseDouble(temp)));
        currentTemp.setText(temp + "℃");

        Picasso.with(currentInfoFragment.context)
                .load(ICON_BASE_URL + result.get("Icon") + ".png")
                .resize(90, 90)
                .into(weatherIcon);
    }

    @Override
    public void weatherForecastReadyCallback(Map<String, Map<String, Object>> forecast) {
        //Not needed here
    }

}
