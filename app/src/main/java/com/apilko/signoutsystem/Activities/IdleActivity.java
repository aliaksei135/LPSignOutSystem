package com.apilko.signoutsystem.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Keep;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;

import com.apilko.signoutsystem.Fragments.calendarFragment;
import com.apilko.signoutsystem.Fragments.currentInfoFragment;
import com.apilko.signoutsystem.Fragments.forecastFragment;
import com.apilko.signoutsystem.Fragments.notifFragment;
import com.apilko.signoutsystem.R;

import SecuGen.FDxSDKPro.JSGFPLib;
import SecuGen.FDxSDKPro.SGAutoOnEventNotifier;
import SecuGen.FDxSDKPro.SGFingerPresentEvent;


@Keep
public class IdleActivity extends AppCompatActivity implements SGFingerPresentEvent{

    private static final String TAG = "IdleActivity";

    private SGAutoOnEventNotifier autoOn;
    private JSGFPLib bioLib;

    private Handler notifDisplayHandler;
    private Handler weatherUpdateHandler;
    private Handler calendarUpdateHandler;

    private notifFragment notifFrag;
    private final Runnable notifUpdateThread = new Runnable() {
        @Override
        public void run() {

            //Display the notifications
            //Index is handled internally by the fragment
            notifFrag.displayNotifsView();
            Log.i(TAG, "Notification Thread runs update");
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
            currentInfoFrag.updateWeather(IdleActivity.this);
            forecastFrag.updateWeather(IdleActivity.this);

            Log.i(TAG, "Weather Thread runs updates");

            //Refreshes weather data every 20 minutes
            weatherUpdateHandler.postDelayed(weatherUpdateThread, 1200000);
        }
    };
    private calendarFragment calendarFrag;
    private final Runnable calendarUpdateThread = new Runnable() {
        @Override
        public void run() {

            calendarFrag.populateCalendar(IdleActivity.this);

            Log.i(TAG, "Calendar Thread runs update");
            //Run next update in 4 Hours
            calendarUpdateHandler.postDelayed(calendarUpdateThread, 14400000);
        }
    };
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idle);

        showProgressDialog();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        notifFrag = (notifFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_notif);
        forecastFrag = new forecastFragment();
        currentInfoFrag = new currentInfoFragment();
        calendarFrag = new calendarFragment();

        notifDisplayHandler = new Handler();
        weatherUpdateHandler = new Handler();
        calendarUpdateHandler = new Handler();

        //Handle loading of content
        notifDisplayHandler.postDelayed(notifUpdateThread, 10000);
        weatherUpdateHandler.post(weatherUpdateThread);
        calendarUpdateHandler.post(calendarUpdateThread);

        hideProgressDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        notifDisplayHandler.removeCallbacks(notifUpdateThread);
        weatherUpdateHandler.removeCallbacks(weatherUpdateThread);
        calendarUpdateHandler.removeCallbacks(calendarUpdateThread);
        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //When user touches the Idle Activity (except for the calendar fragment)
        //it switches back to MainActivity awaiting further user input
        //eg manual signing
        startActivity(new Intent(this, MainActivity.class).putExtra("type", "touch"));

        return super.onTouchEvent(event);
    }

    @Override
    public void SGFingerPresentCallback() {
        autoOn.stop();
        startActivity(new Intent(this, MainActivity.class).putExtra("type", "fingerprint"));
    }

    private void showProgressDialog() {

        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {

        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }
}
