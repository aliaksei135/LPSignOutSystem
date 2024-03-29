/*
 * com.aliakseipilko.signoutsystem.Activities.IdleActivity was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2017.  All Rights Reserved.
 *
 * Last modified 15/02/17 12:43
 */

package com.aliakseipilko.signoutsystem.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Keep;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.aliakseipilko.signoutsystem.DataHandlers.BiometricDataHandler;
import com.aliakseipilko.signoutsystem.Fragments.calendarFragment;
import com.aliakseipilko.signoutsystem.Fragments.currentInfoFragment;
import com.aliakseipilko.signoutsystem.Fragments.forecastFragment;
import com.aliakseipilko.signoutsystem.Fragments.notifFragment;
import com.aliakseipilko.signoutsystem.Helpers.WeatherRemoteFetch;
import com.aliakseipilko.signoutsystem.R;

import SecuGen.FDxSDKPro.SGAutoOnEventNotifier;
import SecuGen.FDxSDKPro.SGFingerPresentEvent;


@Keep
public class IdleActivity extends AppCompatActivity implements SGFingerPresentEvent {

    private static final String TAG = "IdleActivity";

    private SGAutoOnEventNotifier autoOn;

    private HandlerThread handlerThread = null;

    private Handler notifDisplayHandler;
    private Handler weatherUpdateHandler;
    private Handler calendarUpdateHandler;

    private WeatherRemoteFetch wFetch;

    private notifFragment notifFrag;
    private final Runnable notifUpdateThread = new Runnable() {
        @Override
        public void run() {

            //Display the notifications
            //Index is handled internally by the fragment
            notifFrag.displayNotifsView();
            Log.d(TAG, "Notification Thread runs update");
            //Display each notif for 10 seconds
            notifDisplayHandler.postDelayed(notifUpdateThread, 10000);
        }
    };
    private currentInfoFragment currentInfoFrag;
    private forecastFragment forecastFrag;
    private final Runnable weatherUpdateThread = new Runnable() {
        @Override
        public void run() {

            //The callbacks for these are implemented in the respective fragments
            // so calling this updates the fragments as well
            currentInfoFrag.updateWeather(wFetch, IdleActivity.this);
            forecastFrag.updateWeather(wFetch, IdleActivity.this);

            Log.d(TAG, "Weather Thread runs updates");

            //Refreshes weather data every 20 minutes
            weatherUpdateHandler.postDelayed(weatherUpdateThread, 1200000);
        }
    };
    private calendarFragment calendarFrag;
    private final Runnable calendarUpdateThread = new Runnable() {
        @Override
        public void run() {

            calendarFrag.populateCalendar();

            Log.d(TAG, "Calendar Thread runs update");
            //Run next update in 4 Hours
            calendarUpdateHandler.postDelayed(calendarUpdateThread, 14400000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        hideSysUI();
        setContentView(R.layout.activity_idle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        handlerThread = new HandlerThread("IdleHandlerThread");
        handlerThread.start();

        autoOn = new SGAutoOnEventNotifier(BiometricDataHandler.bioLib, null);
        autoOn.start();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        notifFrag = (notifFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_notif);
        forecastFrag = (forecastFragment) getSupportFragmentManager().findFragmentById(R.id.forecastFragment);
        currentInfoFrag = (currentInfoFragment) getSupportFragmentManager().findFragmentById(R.id.currentInfoFragment);
        calendarFrag = (calendarFragment) getSupportFragmentManager().findFragmentById(R.id.calendarFragment);

        notifDisplayHandler = new Handler(handlerThread.getLooper());
        weatherUpdateHandler = new Handler(handlerThread.getLooper());
        calendarUpdateHandler = new Handler(handlerThread.getLooper());

        wFetch = WeatherRemoteFetch.getInstance(this);

        notifDisplayHandler.postDelayed(notifUpdateThread, 10000);
        weatherUpdateHandler.postDelayed(weatherUpdateThread, 1);
        calendarUpdateHandler.postDelayed(calendarUpdateThread, 1);

        hideSysUI();
    }

    private void showSysUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void hideSysUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onStop() {
        super.onStop();

        notifDisplayHandler.removeCallbacksAndMessages(null);
        weatherUpdateHandler.removeCallbacksAndMessages(null);
        calendarUpdateHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handlerThread.quitSafely();
        handlerThread = null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //When user touches the Idle Activity (except for the calendar fragment)
        //it switches back to MainActivity awaiting further user input
        //eg manual signing
        startActivity(new Intent(this, MainActivity.class)/*.putExtra("type", "touch")*/);

        return super.onTouchEvent(event);
    }

    @Override
    public void SGFingerPresentCallback() {
        autoOn.stop();
        startActivity(new Intent(this, MainActivity.class).putExtra("type", "fingerprint"));
        finish();
    }
}
