package com.apilko.signoutsystem.Services;


import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import SecuGen.FDxSDKPro.JSGFPLib;
import SecuGen.FDxSDKPro.SGAutoOnEventNotifier;
import SecuGen.FDxSDKPro.SGFingerPresentEvent;

public class FingerprintMonitorService extends IntentService implements SGFingerPresentEvent {

    static final String TAG = "FingerprintMonitor";

    JSGFPLib bioLibrary;
    private SGAutoOnEventNotifier autoOn;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public FingerprintMonitorService(String name, JSGFPLib bioLib) {
        super(name);
        bioLibrary = bioLib;
        autoOn = new SGAutoOnEventNotifier(bioLib, this);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        autoOn.start();
        Log.d(TAG, "Service starts");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    @Override
    public void SGFingerPresentCallback() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        autoOn.stop();
        Log.d(TAG, "Service destroyed");
    }
}
