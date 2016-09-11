package com.apilko.signoutsystem.Helpers;


import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.util.ArrayMap;

import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.client.okhttp.WeatherDefaultClient;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.exception.WeatherProviderInstantiationException;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.model.HourForecast;
import com.survivingwithandroid.weather.lib.model.WeatherHourForecast;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProviderType;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;

import java.util.Map;

public class WeatherRemoteFetch {

    ///Hardcoded values because paging GPS every time for the same location is pointless
    static final String CITY = "Reading";
    static final String OWM_CITY_ID = "2639577";
    private static final double CITY_LAT = 51.436623;
    private static final double CITY_LON = -0.945724;
    static Long weatherLastUpdateTimeMillis, forecastLastUpdateTimeMillis;
    static Map<String, Object> weather;
    static Map<String, Map<String, Object>> forecast;
    private static WeatherRemoteFetch ourInstance;
    private WeatherClient client;
    private WeatherCallback weatherCallback;
    private WeatherCallback forecastCallback;

    private WeatherRemoteFetch(Context context) {

        WeatherConfig config = new WeatherConfig();
        config.ApiKey = "0cf885aa8ebcb4149df4b8c615ee4778";
        config.lang = "en";
        config.unitSystem = WeatherConfig.UNIT_SYSTEM.M;

        client = new WeatherDefaultClient();
        WeatherClient.ClientBuilder builder = new WeatherClient.ClientBuilder();

        try {
            client = builder.attach(context)
                    .provider(new OpenweathermapProviderType())
                    .httpClient(com.survivingwithandroid.weather.lib.client.okhttp.WeatherDefaultClient.class)
                    .config(config)
                    .build();
        } catch (WeatherProviderInstantiationException e) {
            e.printStackTrace();
        }
    }

    public static WeatherRemoteFetch getInstance(Context context) {

        if (ourInstance == null) {
            ourInstance = new WeatherRemoteFetch(context);
            return ourInstance;
        } else {
            return ourInstance;
        }
    }

    public void setWeatherReadyCallback(WeatherCallback callback) {
        this.weatherCallback = callback;
    }

    public void setForecastCallback(WeatherCallback callback) {
        this.forecastCallback = callback;
    }

    public void getCurrentWeather() {

        if (weatherLastUpdateTimeMillis == null
                || (System.currentTimeMillis() - weatherLastUpdateTimeMillis) >= 1200000
                || weather == null) {
            new GetWeatherTask().execute();
        } else {
            weatherCallback.weatherReadyCallback(weather);
        }
    }

    public void getForecastWeather() {
        if (forecastLastUpdateTimeMillis == null
                || (System.currentTimeMillis() - forecastLastUpdateTimeMillis) >= 1200000
                || forecast == null) {
            new GetForecastTask().execute();
        } else {
            forecastCallback.weatherForecastReadyCallback(forecast);
        }
    }

    public interface WeatherCallback {
        void weatherReadyCallback(Map<String, Object> result);

        void weatherForecastReadyCallback(Map<String, Map<String, Object>> forecast);
    }

    private class GetForecastTask extends AsyncTask<Void, Void, Void> {

        public GetForecastTask() {
            super();
        }

        @Override
        protected Void doInBackground(Void... params) {
            fetchForecast();
            return null;
        }

        void fetchForecast() {
            final Map<String, Map<String, Object>> result = new ArrayMap<>();
            WeatherRequest request = new WeatherRequest(CITY_LON, CITY_LAT);
            client.getHourForecastWeather(request, new WeatherClient.HourForecastWeatherEventListener() {
                @Override
                public void onWeatherRetrieved(WeatherHourForecast forecast) {

                    HourForecast zeroHourForecast = forecast.getHourForecast().get(0);
                    Map<String, Object> hourZero = new ArrayMap<>();
                    hourZero.put("Timestamp", zeroHourForecast.timestamp);
                    hourZero.put("Temperature", String.valueOf(zeroHourForecast.weather.temperature.getTemp()));
                    hourZero.put("Wind Speed", String.valueOf(zeroHourForecast.weather.wind.getSpeed()));
                    hourZero.put("Wind Degrees", String.valueOf(zeroHourForecast.weather.wind.getDeg()));
                    hourZero.put("Icon", zeroHourForecast.weather.currentCondition.getIcon());
                    result.put("0", hourZero);

                    HourForecast threeHourForecast = forecast.getHourForecast().get(1);
                    Map<String, Object> hourThree = new ArrayMap<>();
                    hourThree.put("Timestamp", threeHourForecast.timestamp);
                    hourThree.put("Temperature", String.valueOf(threeHourForecast.weather.temperature.getTemp()));
                    hourThree.put("Wind Speed", String.valueOf(threeHourForecast.weather.wind.getSpeed()));
                    hourThree.put("Wind Degrees", String.valueOf(threeHourForecast.weather.wind.getDeg()));
                    hourThree.put("Icon", threeHourForecast.weather.currentCondition.getIcon());
                    result.put("3", hourThree);

                    HourForecast sixHourForecast = forecast.getHourForecast().get(2);
                    Map<String, Object> hourSix = new ArrayMap<>();
                    hourSix.put("Timestamp", sixHourForecast.timestamp);
                    hourSix.put("Temperature", String.valueOf(sixHourForecast.weather.temperature.getTemp()));
                    hourSix.put("Wind Speed", String.valueOf(sixHourForecast.weather.wind.getSpeed()));
                    hourSix.put("Wind Degrees", String.valueOf(sixHourForecast.weather.wind.getDeg()));
                    hourSix.put("Icon", sixHourForecast.weather.currentCondition.getIcon());
                    result.put("6", hourSix);

                    WeatherRemoteFetch.forecast = result;
                    WeatherRemoteFetch.forecastLastUpdateTimeMillis = System.currentTimeMillis();
                    forecastCallback.weatherForecastReadyCallback(result);
                }

                @Override
                public void onWeatherError(WeatherLibException wle) {
                    weatherCallback.weatherForecastReadyCallback(null);
                }

                @Override
                public void onConnectionError(Throwable t) {
                    weatherCallback.weatherForecastReadyCallback(null);
                }
            });
        }
    }

    private class GetWeatherTask extends AsyncTask<Void, Void, Void> {

        public GetWeatherTask() {
            super();
        }

        @Override
        protected Void doInBackground(Void... params) {
            fetchWeather();
            return null;
        }

        void fetchWeather() {
            final Map<String, Object> result = new ArrayMap<>();
            WeatherRequest request = new WeatherRequest(CITY_LON, CITY_LAT);
            client.getCurrentCondition(request, new WeatherClient.WeatherEventListener() {
                @Override
                public void onWeatherRetrieved(CurrentWeather weather) {
                    result.put("Temperature", String.valueOf(weather.weather.temperature.getTemp()));
                    result.put("Wind Speed", String.valueOf(weather.weather.wind.getSpeed()));
                    result.put("Wind Degrees", String.valueOf(weather.weather.wind.getDeg()));
                    result.put("Icon", weather.weather.currentCondition.getIcon());

                    WeatherRemoteFetch.weather = result;
                    WeatherRemoteFetch.weatherLastUpdateTimeMillis = System.currentTimeMillis();
                    weatherCallback.weatherReadyCallback(result);
                }

                @Override
                public void onWeatherError(WeatherLibException wle) {
                    weatherCallback.weatherReadyCallback(null);
                }

                @Override
                public void onConnectionError(Throwable t) {
                    weatherCallback.weatherReadyCallback(null);
                }
            });
        }
    }

}
