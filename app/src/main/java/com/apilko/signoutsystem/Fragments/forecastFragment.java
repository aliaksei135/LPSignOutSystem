package com.apilko.signoutsystem.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.apilko.signoutsystem.Helpers.WeatherRemoteFetch;
import com.apilko.signoutsystem.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class forecastFragment extends Fragment implements WeatherRemoteFetch.WeatherCallback {

    private static final String ICON_BASE_URL = "http://openweathermap.org/img/w/";
    private static Context context;
    //UI elements
    private ImageView firstIconView;
    private ImageView secondIconView;
    private ImageView thirdIconView;
    private TextView firstTempView;
    private TextView secondTempView;
    private TextView thirdTempView;
    private TextView firstTimeView;
    private TextView secondTimeView;
    private TextView thirdTimeView;
    private WeatherRemoteFetch weatherFetch;

    public forecastFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        weatherFetch = WeatherRemoteFetch.getInstance(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forecast, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        firstTimeView = (TextView) getView().findViewById(R.id.firstHourTimeTextView);
        secondTimeView = (TextView) getView().findViewById(R.id.secondHourTimeTextView);
        thirdTimeView = (TextView) getView().findViewById(R.id.thirdHourTimeTextView);

        firstIconView = (ImageView) getView().findViewById(R.id.firstHourForecastImageView);
        secondIconView = (ImageView) getView().findViewById(R.id.secondHourForecastImageView);
        thirdIconView = (ImageView) getView().findViewById(R.id.thirdHourForecastImageView);

        firstTempView = (TextView) getView().findViewById(R.id.firstHourForecastTextView);
        secondTempView = (TextView) getView().findViewById(R.id.secondHourForecastTextView);
        thirdTempView = (TextView) getView().findViewById(R.id.thirdHourForecastTextView);

    }

    public void updateWeather(Context context) {

        forecastFragment.context = context;

        if (weatherFetch == null) {
            weatherFetch = WeatherRemoteFetch.getInstance(context);
        }
        weatherFetch.getForecastWeather();
        weatherFetch.setForecastCallback(this);
    }

    @Override
    public void weatherReadyCallback(Map<String, Object> result) {
        //Not needed here
    }

    @Override
    public void weatherForecastReadyCallback(Map<String, Map<String, Object>> forecast) {

        String firstTemp = (String) forecast.get("0").get("Temperature");
        String secondTemp = (String) forecast.get("3").get("Temperature");
        String thirdTemp = (String) forecast.get("6").get("Temperature");

        firstTemp = String.valueOf(Math.round(Double.parseDouble(firstTemp)));
        secondTemp = String.valueOf(Math.round(Double.parseDouble(secondTemp)));
        thirdTemp = String.valueOf(Math.round(Double.parseDouble(thirdTemp)));

        firstTempView.setText(firstTemp + "℃");
        secondTempView.setText(secondTemp + "℃");
        thirdTempView.setText(thirdTemp + "℃");

        SimpleDateFormat format = new SimpleDateFormat("HH:mm");

        Date firstTime = new Date((long) (forecast.get("0").get("Timestamp")) * 1000);
        firstTimeView.setText(format.format(firstTime));

        Date secondTime = new Date((long) (forecast.get("3").get("Timestamp")) * 1000);
        secondTimeView.setText(format.format(secondTime));

        Date thirdTime = new Date((long) (forecast.get("6").get("Timestamp")) * 1000);
        thirdTimeView.setText(format.format(thirdTime));

        Picasso.with(forecastFragment.context)
                .load(ICON_BASE_URL + forecast.get("0").get("Icon") + ".png")
                .resize(80, 80)
                .into(firstIconView);
        Picasso.with(forecastFragment.context)
                .load(ICON_BASE_URL + forecast.get("3").get("Icon") + ".png")
                .resize(80, 80)
                .into(secondIconView);
        Picasso.with(forecastFragment.context)
                .load(ICON_BASE_URL + forecast.get("3").get("Icon") + ".png")
                .resize(80, 80)
                .into(thirdIconView);
    }

}
